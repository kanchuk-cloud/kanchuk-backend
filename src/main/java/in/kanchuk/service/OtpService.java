package in.kanchuk.service;

import in.kanchuk.entity.OtpPurpose;
import in.kanchuk.entity.OtpVerification;
import in.kanchuk.repository.OtpVerificationRepository;
import in.kanchuk.sms.OtpProperties;
import in.kanchuk.sms.SmsUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private final OtpVerificationRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;
    private final OtpProperties otpProperties;

    @Transactional
    public void sendOtp(String mobileNumber, OtpPurpose purpose) {
        validateMobileNumber(mobileNumber);

        // Resend cooldown: check the most-recently created record regardless of its state
        otpRepo.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(mobileNumber, purpose)
                .ifPresent(latest -> {
                    if (latest.getResendAfter() != null && latest.getResendAfter().isAfter(OffsetDateTime.now())) {
                        long secondsLeft = Duration.between(OffsetDateTime.now(), latest.getResendAfter()).getSeconds();
                        throw new IllegalArgumentException(
                                "Please wait " + secondsLeft + " second(s) before requesting a new OTP.");
                    }
                });

        // Invalidate all previous un-verified OTPs so only one active OTP exists per mobile+purpose
        otpRepo.invalidateActive(mobileNumber, purpose, OffsetDateTime.now());

        String rawOtp = generateOtp();

        OtpVerification record = new OtpVerification();
        record.setMobileNumber(mobileNumber);
        record.setOtpHash(passwordEncoder.encode(rawOtp));
        record.setPurpose(purpose);
        record.setExpiresAt(OffsetDateTime.now().plusMinutes(otpProperties.getExpiryMinutes()));
        record.setResendAfter(OffsetDateTime.now().plusSeconds(otpProperties.getResendCooldownSeconds()));
        record.setAttemptCount(0);
        otpRepo.save(record);

        smsService.sendOtp(mobileNumber, rawOtp, otpProperties.getExpiryMinutes());
        log.info("OTP dispatched for {} (purpose={})", SmsUtil.mask(mobileNumber), purpose);
    }

    /**
     * noRollbackFor ensures the incremented attempt_count is persisted even when an
     * IllegalArgumentException is thrown for a wrong OTP guess.
     */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void verifyOtp(String mobileNumber, String rawOtp, OtpPurpose purpose) {
        validateMobileNumber(mobileNumber);

        OtpVerification record = otpRepo
                .findActiveOtp(mobileNumber, purpose, OffsetDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP."));

        if (record.getAttemptCount() >= otpProperties.getMaxAttempts()) {
            throw new IllegalArgumentException("Too many failed attempts. Please request a new OTP.");
        }

        record.setAttemptCount(record.getAttemptCount() + 1);

        if (!passwordEncoder.matches(rawOtp, record.getOtpHash())) {
            otpRepo.save(record);
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        record.setVerifiedAt(OffsetDateTime.now());
        otpRepo.save(record);
        log.info("OTP verified for {} (purpose={})", SmsUtil.mask(mobileNumber), purpose);
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, otpProperties.getLength());
        int otp = new SecureRandom().nextInt(bound);
        return String.format("%0" + otpProperties.getLength() + "d", otp);
    }

    private void validateMobileNumber(String mobile) {
        if (mobile == null || !mobile.matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid mobile number. Must be exactly 10 digits.");
        }
    }
}

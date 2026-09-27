package in.kanchuk.service;

import in.kanchuk.entity.OtpPurpose;
import in.kanchuk.entity.OtpVerification;
import in.kanchuk.repository.OtpVerificationRepository;
import in.kanchuk.sms.OtpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtpServiceTest {

    @Mock OtpVerificationRepository otpRepo;
    @Mock PasswordEncoder passwordEncoder;
    @Mock SmsService smsService;
    @Mock OtpProperties otpProperties;
    @InjectMocks OtpService otpService;

    @BeforeEach
    void setUp() {
        when(otpProperties.getLength()).thenReturn(6);
        when(otpProperties.getExpiryMinutes()).thenReturn(5);
        when(otpProperties.getMaxAttempts()).thenReturn(5);
        when(otpProperties.getResendCooldownSeconds()).thenReturn(60);
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-hashed-otp");
        when(otpRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── sendOtp ────────────────────────────────────────────────────────────────

    @Test
    void sendOtp_savesRecordWithHashedOtp() {
        when(otpRepo.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.empty());

        otpService.sendOtp("9876543210", OtpPurpose.LOGIN);

        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepo).save(captor.capture());
        OtpVerification saved = captor.getValue();

        assertThat(saved.getMobileNumber()).isEqualTo("9876543210");
        assertThat(saved.getPurpose()).isEqualTo(OtpPurpose.LOGIN);
        assertThat(saved.getOtpHash()).isEqualTo("bcrypt-hashed-otp");
        assertThat(saved.getExpiresAt()).isAfter(OffsetDateTime.now());
        assertThat(saved.getAttemptCount()).isZero();
    }

    @Test
    void sendOtp_callsSmsService() {
        when(otpRepo.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.empty());

        otpService.sendOtp("9876543210", OtpPurpose.LOGIN);

        verify(smsService).sendOtp(eq("9876543210"), anyString(), eq(5));
    }

    @Test
    void sendOtp_invalidatesPreviousOtps() {
        when(otpRepo.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.empty());

        otpService.sendOtp("9876543210", OtpPurpose.LOGIN);

        verify(otpRepo).invalidateActive(eq("9876543210"), eq(OtpPurpose.LOGIN), any());
    }

    @Test
    void sendOtp_hashesOtp_neverStoresPlaintext() {
        when(otpRepo.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.empty());

        otpService.sendOtp("9876543210", OtpPurpose.SIGNUP);

        verify(passwordEncoder).encode(anyString());
        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepo).save(captor.capture());
        // The stored hash is NOT the raw 6-digit OTP
        assertThat(captor.getValue().getOtpHash()).isEqualTo("bcrypt-hashed-otp");
        assertThat(captor.getValue().getOtpHash()).doesNotMatch("\\d{6}");
    }

    @Test
    void sendOtp_withinCooldown_throws() {
        OtpVerification recent = new OtpVerification();
        recent.setResendAfter(OffsetDateTime.now().plusSeconds(30));
        when(otpRepo.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(recent));

        assertThrows(IllegalArgumentException.class,
                () -> otpService.sendOtp("9876543210", OtpPurpose.LOGIN));
        verifyNoInteractions(smsService);
    }

    @Test
    void sendOtp_afterCooldown_succeeds() {
        OtpVerification old = new OtpVerification();
        old.setResendAfter(OffsetDateTime.now().minusSeconds(10)); // cooldown elapsed
        when(otpRepo.findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(any(), any()))
                .thenReturn(Optional.of(old));

        assertDoesNotThrow(() -> otpService.sendOtp("9876543210", OtpPurpose.LOGIN));
    }

    @Test
    void sendOtp_invalidMobile_9digits_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> otpService.sendOtp("987654321", OtpPurpose.LOGIN));
    }

    @Test
    void sendOtp_invalidMobile_hasLetters_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> otpService.sendOtp("9876ABCD10", OtpPurpose.LOGIN));
    }

    @Test
    void sendOtp_nullMobile_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> otpService.sendOtp(null, OtpPurpose.LOGIN));
    }

    // ── verifyOtp ─────────────────────────────────────────────────────────────

    @Test
    void verifyOtp_correctOtp_succeeds() {
        OtpVerification record = activeRecord();
        when(otpRepo.findActiveOtp(any(), any(), any())).thenReturn(Optional.of(record));
        when(passwordEncoder.matches("123456", "bcrypt-hashed-otp")).thenReturn(true);

        assertDoesNotThrow(() -> otpService.verifyOtp("9876543210", "123456", OtpPurpose.LOGIN));
        assertThat(record.getVerifiedAt()).isNotNull();
    }

    @Test
    void verifyOtp_correctOtp_persistsVerifiedAt() {
        OtpVerification record = activeRecord();
        when(otpRepo.findActiveOtp(any(), any(), any())).thenReturn(Optional.of(record));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        otpService.verifyOtp("9876543210", "123456", OtpPurpose.LOGIN);

        ArgumentCaptor<OtpVerification> captor = ArgumentCaptor.forClass(OtpVerification.class);
        verify(otpRepo).save(captor.capture());
        assertThat(captor.getValue().getVerifiedAt()).isNotNull();
    }

    @Test
    void verifyOtp_noActiveOtp_throws() {
        when(otpRepo.findActiveOtp(any(), any(), any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp("9876543210", "123456", OtpPurpose.LOGIN));
    }

    @Test
    void verifyOtp_wrongOtp_throws() {
        OtpVerification record = activeRecord();
        when(otpRepo.findActiveOtp(any(), any(), any())).thenReturn(Optional.of(record));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp("9876543210", "999999", OtpPurpose.LOGIN));
    }

    @Test
    void verifyOtp_wrongOtp_incrementsAttemptCount() {
        OtpVerification record = activeRecord();
        record.setAttemptCount(2);
        when(otpRepo.findActiveOtp(any(), any(), any())).thenReturn(Optional.of(record));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp("9876543210", "wrong", OtpPurpose.LOGIN));

        assertThat(record.getAttemptCount()).isEqualTo(3);
        verify(otpRepo).save(record); // attempt count persisted
    }

    @Test
    void verifyOtp_maxAttemptsReached_throws() {
        OtpVerification record = activeRecord();
        record.setAttemptCount(5); // equals maxAttempts
        when(otpRepo.findActiveOtp(any(), any(), any())).thenReturn(Optional.of(record));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp("9876543210", "123456", OtpPurpose.LOGIN));

        assertThat(ex.getMessage()).containsIgnoringCase("too many");
        verify(otpRepo, never()).save(any()); // nothing saved, no match check
    }

    @Test
    void verifyOtp_alreadyUsed_noActiveOtpReturned() {
        // findActiveOtp only returns un-verified OTPs; already-used ones are filtered by the query
        when(otpRepo.findActiveOtp(any(), any(), any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> otpService.verifyOtp("9876543210", "123456", OtpPurpose.LOGIN));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private OtpVerification activeRecord() {
        OtpVerification r = new OtpVerification();
        r.setMobileNumber("9876543210");
        r.setOtpHash("bcrypt-hashed-otp");
        r.setPurpose(OtpPurpose.LOGIN);
        r.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        r.setAttemptCount(0);
        return r;
    }
}

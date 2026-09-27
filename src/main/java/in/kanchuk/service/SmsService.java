package in.kanchuk.service;

import in.kanchuk.sms.SmsProperties;
import in.kanchuk.sms.SmsProvider;
import in.kanchuk.sms.SmsResponse;
import in.kanchuk.sms.SmsUtil;
import in.kanchuk.sms.template.OrderNotificationType;
import in.kanchuk.sms.template.SmsTemplates;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final SmsProvider smsProvider;
    private final SmsProperties smsProperties;

    /**
     * Sends an OTP SMS synchronously. Failures are logged and returned as SmsResponse(false),
     * never propagated — the caller decides whether to fail the request.
     */
    public SmsResponse sendOtp(String mobileNumber, String rawOtp, int expiryMinutes) {
        if (!smsProperties.isEnabled()) {
            log.debug("SMS disabled — OTP suppressed for {}", SmsUtil.mask(mobileNumber));
            return new SmsResponse(false, "SMS disabled");
        }
        String message = SmsTemplates.formatOtp(rawOtp, expiryMinutes);
        try {
            return smsProvider.send(mobileNumber, message);
        } catch (Exception e) {
            log.warn("OTP SMS failed for {}: {}", SmsUtil.mask(mobileNumber), e.getMessage());
            return new SmsResponse(false, e.getMessage());
        }
    }

    /**
     * Sends an order status SMS asynchronously. Runs in a separate thread so a slow or failed
     * SMS never delays the order API response. All errors are caught and logged.
     */
    @Async("smsExecutor")
    public void sendOrderNotification(String mobileNumber, String orderNumber, OrderNotificationType type) {
        if (!smsProperties.isEnabled()) {
            log.debug("SMS disabled — {} notification skipped for order {}", type, orderNumber);
            return;
        }
        if (mobileNumber == null || mobileNumber.isBlank()) {
            log.debug("No phone number — {} notification skipped for order {}", type, orderNumber);
            return;
        }
        log.info("Sending {} SMS for order {} to {}", type, orderNumber, SmsUtil.mask(mobileNumber));
        String message = SmsTemplates.format(type, orderNumber);
        try {
            smsProvider.send(mobileNumber, message);
        } catch (Exception e) {
            log.warn("SMS notification failed for order {}: {}", orderNumber, e.getMessage());
        }
    }
}

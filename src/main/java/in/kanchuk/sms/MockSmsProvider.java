package in.kanchuk.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Free development SMS provider — logs every message instead of sending a real SMS.
 * Activated when sms.provider=mock (the default).
 *
 * To switch to a real provider: set sms.provider=msg91 (or twilio) and implement
 * the matching SmsProvider bean. No other code changes required.
 */
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(MockSmsProvider.class);

    @Override
    public SmsResponse send(String mobileNumber, String message) {
        log.info("[MOCK SMS] To: {} | Message: {}", SmsUtil.mask(mobileNumber), message);
        return new SmsResponse(true, "Mock SMS logged");
    }
}

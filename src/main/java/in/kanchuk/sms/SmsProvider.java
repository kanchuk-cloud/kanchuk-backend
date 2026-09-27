package in.kanchuk.sms;

/**
 * Strategy interface for SMS delivery. Swap provider via sms.provider config:
 *   mock (default) → MockSmsProvider
 *   msg91          → Msg91SmsProvider (future)
 *   twilio         → TwilioSmsProvider (future)
 */
public interface SmsProvider {
    SmsResponse send(String mobileNumber, String message);
}

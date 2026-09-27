package in.kanchuk.sms;

public final class SmsUtil {

    private SmsUtil() {}

    /** Masks all but the last 4 digits: 9876543210 → ******3210 */
    public static String mask(String mobile) {
        if (mobile == null || mobile.length() < 4) return "****";
        return "******" + mobile.substring(mobile.length() - 4);
    }
}

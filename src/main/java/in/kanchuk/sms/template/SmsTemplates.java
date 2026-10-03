package in.kanchuk.sms.template;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralised SMS message templates.
 * Move entries to DB/admin config when runtime-editable templates are needed.
 */
public final class SmsTemplates {

    private static final Map<OrderNotificationType, String> ORDER_TEMPLATES =
            new EnumMap<>(OrderNotificationType.class);

    static {
        ORDER_TEMPLATES.put(OrderNotificationType.ORDER_CONFIRMED,
                "Your order #{orderNumber} has been confirmed. Thank you for shopping with us!");
        ORDER_TEMPLATES.put(OrderNotificationType.PAYMENT_CONFIRMED,
                "Payment received for order #{orderNumber}. Thank you!");
        ORDER_TEMPLATES.put(OrderNotificationType.ORDER_PACKED,
                "Your order #{orderNumber} has been packed and is ready to ship.");
        ORDER_TEMPLATES.put(OrderNotificationType.ORDER_SHIPPED,
                "Your order #{orderNumber} has been shipped and is on its way.");
        ORDER_TEMPLATES.put(OrderNotificationType.OUT_FOR_DELIVERY,
                "Your order #{orderNumber} is out for delivery today.");
        ORDER_TEMPLATES.put(OrderNotificationType.ORDER_DELIVERED,
                "Your order #{orderNumber} has been delivered. Thank you for shopping with us!");
        ORDER_TEMPLATES.put(OrderNotificationType.ORDER_CANCELLED,
                "Your order #{orderNumber} has been cancelled. Contact us for assistance.");
        ORDER_TEMPLATES.put(OrderNotificationType.RETURN_INITIATED,
                "Return for order #{orderNumber} has been initiated. We will process it shortly.");
        ORDER_TEMPLATES.put(OrderNotificationType.RETURN_APPROVED,
                "Your return request for order #{orderNumber} has been approved. We will arrange a pickup shortly.");
        ORDER_TEMPLATES.put(OrderNotificationType.RETURN_REJECTED,
                "Your return request for order #{orderNumber} could not be approved. Please contact us for assistance.");
        ORDER_TEMPLATES.put(OrderNotificationType.REFUND_COMPLETED,
                "Refund for order #{orderNumber} has been processed and will reflect in 5-7 business days.");
    }

    private SmsTemplates() {}

    public static String format(OrderNotificationType type, String orderNumber) {
        return ORDER_TEMPLATES
                .getOrDefault(type, "Update for your order #{orderNumber}.")
                .replace("{orderNumber}", orderNumber);
    }

    public static String formatOtp(String otp, int expiryMinutes) {
        return "Your OTP is " + otp + ". It is valid for " + expiryMinutes +
               " minutes. Do not share it with anyone.";
    }
}

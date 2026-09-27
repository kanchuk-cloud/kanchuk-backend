package in.kanchuk.sms.template;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmsTemplatesTest {

    @Test
    void format_allTypes_containOrderNumber() {
        for (OrderNotificationType type : OrderNotificationType.values()) {
            String msg = SmsTemplates.format(type, "KCH-2026-000001");
            assertThat(msg)
                    .as("Template for %s should contain order number", type)
                    .contains("KCH-2026-000001");
        }
    }

    @Test
    void format_orderConfirmed_mentionsConfirmed() {
        assertThat(SmsTemplates.format(OrderNotificationType.ORDER_CONFIRMED, "ORD-001"))
                .containsIgnoringCase("confirmed");
    }

    @Test
    void format_orderShipped_mentionsShipped() {
        assertThat(SmsTemplates.format(OrderNotificationType.ORDER_SHIPPED, "ORD-001"))
                .containsIgnoringCase("shipped");
    }

    @Test
    void format_orderDelivered_mentionsDelivered() {
        assertThat(SmsTemplates.format(OrderNotificationType.ORDER_DELIVERED, "ORD-001"))
                .containsIgnoringCase("delivered");
    }

    @Test
    void format_orderCancelled_mentionsCancelled() {
        assertThat(SmsTemplates.format(OrderNotificationType.ORDER_CANCELLED, "ORD-001"))
                .containsIgnoringCase("cancelled");
    }

    @Test
    void formatOtp_containsOtpAndExpiry() {
        String msg = SmsTemplates.formatOtp("482913", 5);
        assertThat(msg).contains("482913");
        assertThat(msg).contains("5");
    }

    @Test
    void formatOtp_neverReturnsBlank() {
        assertThat(SmsTemplates.formatOtp("000000", 5)).isNotBlank();
    }
}

package in.kanchuk.sms;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockSmsProviderTest {

    private final MockSmsProvider provider = new MockSmsProvider();

    @Test
    void send_returnsSuccess() {
        SmsResponse resp = provider.send("9876543210", "Your OTP is 123456.");
        assertThat(resp.success()).isTrue();
    }

    @Test
    void send_doesNotThrow_forAnyInput() {
        assertThat(provider.send("9876543210", "Test message").success()).isTrue();
        assertThat(provider.send(null, "msg").success()).isTrue();
        assertThat(provider.send("1234567890", "").success()).isTrue();
    }

    // ── SmsUtil masking (used by MockSmsProvider in log output) ────────────────

    @Test
    void mask_tenDigit_showsLastFour() {
        assertThat(SmsUtil.mask("9876543210")).isEqualTo("******3210");
    }

    @Test
    void mask_exactlyFourDigits_showsAll() {
        assertThat(SmsUtil.mask("3210")).isEqualTo("******3210");
    }

    @Test
    void mask_lessThanFour_returnsObfuscated() {
        assertThat(SmsUtil.mask("123")).isEqualTo("****");
    }

    @Test
    void mask_null_returnsObfuscated() {
        assertThat(SmsUtil.mask(null)).isEqualTo("****");
    }
}

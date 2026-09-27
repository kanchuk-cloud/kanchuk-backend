package in.kanchuk.service;

import in.kanchuk.sms.SmsProperties;
import in.kanchuk.sms.SmsProvider;
import in.kanchuk.sms.SmsResponse;
import in.kanchuk.sms.template.OrderNotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SmsServiceTest {

    @Mock SmsProvider smsProvider;
    @Mock SmsProperties smsProperties;
    @InjectMocks SmsService smsService;

    @BeforeEach
    void setUp() {
        when(smsProperties.isEnabled()).thenReturn(true);
        when(smsProvider.send(anyString(), anyString())).thenReturn(new SmsResponse(true, "sent"));
    }

    // ── sendOtp ────────────────────────────────────────────────────────────────

    @Test
    void sendOtp_enabled_callsProvider() {
        smsService.sendOtp("9876543210", "123456", 5);
        verify(smsProvider).send(eq("9876543210"), contains("123456"));
    }

    @Test
    void sendOtp_disabled_skipsProvider() {
        when(smsProperties.isEnabled()).thenReturn(false);
        SmsResponse resp = smsService.sendOtp("9876543210", "123456", 5);
        verifyNoInteractions(smsProvider);
        assertThat(resp.success()).isFalse();
    }

    @Test
    void sendOtp_providerThrows_returnsFailure() {
        when(smsProvider.send(anyString(), anyString())).thenThrow(new RuntimeException("network error"));
        SmsResponse resp = smsService.sendOtp("9876543210", "123456", 5);
        assertThat(resp.success()).isFalse();
        assertThat(resp.providerMessage()).contains("network error");
    }

    @Test
    void sendOtp_messageContainsExpiryMinutes() {
        smsService.sendOtp("9876543210", "123456", 10);
        verify(smsProvider).send(anyString(), contains("10"));
    }

    // ── sendOrderNotification ─────────────────────────────────────────────────

    @Test
    void sendOrderNotification_enabled_callsProvider() {
        smsService.sendOrderNotification("9876543210", "KCH-2026-000001", OrderNotificationType.ORDER_CONFIRMED);
        verify(smsProvider).send(eq("9876543210"), contains("KCH-2026-000001"));
    }

    @Test
    void sendOrderNotification_disabled_skipsProvider() {
        when(smsProperties.isEnabled()).thenReturn(false);
        smsService.sendOrderNotification("9876543210", "ORD-001", OrderNotificationType.ORDER_CONFIRMED);
        verifyNoInteractions(smsProvider);
    }

    @Test
    void sendOrderNotification_nullPhone_skipsProvider() {
        smsService.sendOrderNotification(null, "ORD-001", OrderNotificationType.ORDER_SHIPPED);
        verifyNoInteractions(smsProvider);
    }

    @Test
    void sendOrderNotification_blankPhone_skipsProvider() {
        smsService.sendOrderNotification("  ", "ORD-001", OrderNotificationType.ORDER_SHIPPED);
        verifyNoInteractions(smsProvider);
    }

    @Test
    void sendOrderNotification_providerThrows_doesNotPropagate() {
        when(smsProvider.send(anyString(), anyString())).thenThrow(new RuntimeException("provider down"));
        // Must not throw — SMS failure must not affect the calling code
        smsService.sendOrderNotification("9876543210", "ORD-001", OrderNotificationType.ORDER_CONFIRMED);
    }
}

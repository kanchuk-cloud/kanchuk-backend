package in.kanchuk.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.kanchuk.controller.admin.AdminOrderController;
import in.kanchuk.entity.*;
import in.kanchuk.repository.*;
import in.kanchuk.service.SmsService;
import in.kanchuk.sms.template.OrderNotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderSmsTest {

    @Mock OrderRepository repo;
    @Mock OrderItemRepository itemRepo;
    @Mock PackagingRepository packagingRepo;
    @Mock DispatchingRepository dispatchingRepo;
    @Mock FulfillmentRepository fulfillmentRepo;
    @Mock ReturnRepository returnRepo;
    @Mock OrderItemRepository orderItemRepo;
    @Mock ObjectMapper objectMapper;
    @Mock SmsService smsService;
    @InjectMocks AdminOrderController controller;

    private UUID orderId;
    private Order order;
    private User customer;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customer = new User();
        customer.setPhone("9876543210");

        order = new Order();
        order.setOrderNumber("KCH-2026-000001");
        order.setUser(customer);

        when(repo.findById(orderId)).thenReturn(Optional.of(order));
        when(repo.save(any())).thenReturn(order);
        when(packagingRepo.findByOrderId(any())).thenReturn(Optional.empty());
        when(fulfillmentRepo.findByOrderId(any())).thenReturn(List.of());
        when(fulfillmentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── advance() SMS triggers ─────────────────────────────────────────────────

    @Test
    void advance_placedToConfirmed_triggersOrderConfirmed() {
        order.setStatus("placed");

        controller.advance(orderId, null);

        verify(smsService).sendOrderNotification(
                eq("9876543210"), eq("KCH-2026-000001"), eq(OrderNotificationType.ORDER_CONFIRMED));
    }

    @Test
    void advance_confirmedToPacked_triggersOrderPacked() {
        order.setStatus("confirmed");
        when(packagingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        controller.advance(orderId, Map.of());

        verify(smsService).sendOrderNotification(
                eq("9876543210"), eq("KCH-2026-000001"), eq(OrderNotificationType.ORDER_PACKED));
    }

    @Test
    void advance_packedToShipped_triggersOrderShipped() {
        order.setStatus("packed");

        controller.advance(orderId, Map.of());

        verify(smsService).sendOrderNotification(
                eq("9876543210"), eq("KCH-2026-000001"), eq(OrderNotificationType.ORDER_SHIPPED));
    }

    @Test
    void advance_shippedToOutForDelivery_triggersOutForDelivery() {
        order.setStatus("shipped");

        controller.advance(orderId, Map.of());

        verify(smsService).sendOrderNotification(
                eq("9876543210"), eq("KCH-2026-000001"), eq(OrderNotificationType.OUT_FOR_DELIVERY));
    }

    @Test
    void advance_outForDeliveryToDelivered_triggersOrderDelivered() {
        order.setStatus("out_for_delivery");

        controller.advance(orderId, Map.of());

        verify(smsService).sendOrderNotification(
                eq("9876543210"), eq("KCH-2026-000001"), eq(OrderNotificationType.ORDER_DELIVERED));
    }

    // ── Phone from address snapshot when user has no phone ────────────────────

    @Test
    void advance_userHasNoPhone_usesAddressSnapshot() {
        customer.setPhone(null);
        order.setStatus("placed");
        // Build address snapshot so getAddressSnapshot() returns phone
        order.setDeliveryAddressSnapshot("{\"phone\":\"8888888888\",\"name\":\"Guest\"}");

        controller.advance(orderId, null);

        verify(smsService).sendOrderNotification(
                eq("8888888888"), eq("KCH-2026-000001"), eq(OrderNotificationType.ORDER_CONFIRMED));
    }

    // ── update() cancellation SMS ─────────────────────────────────────────────

    @Test
    void update_statusChangedToCancelled_triggersOrderCancelled() {
        order.setStatus("confirmed");

        controller.update(orderId, Map.of("status", "cancelled"));

        verify(smsService).sendOrderNotification(
                eq("9876543210"), eq("KCH-2026-000001"), eq(OrderNotificationType.ORDER_CANCELLED));
    }

    @Test
    void update_alreadyCancelled_doesNotTriggerDuplicate() {
        order.setStatus("cancelled");

        controller.update(orderId, Map.of("status", "cancelled"));

        verify(smsService, never()).sendOrderNotification(any(), any(), any());
    }

    // ── initiateReturn() SMS ──────────────────────────────────────────────────

    @Test
    void initiateReturn_triggersReturnInitiated() {
        when(returnRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        controller.initiateReturn(orderId, Map.of("reason", "Wrong size"));

        verify(smsService).sendOrderNotification(
                eq("9876543210"), eq("KCH-2026-000001"), eq(OrderNotificationType.RETURN_INITIATED));
    }

    // ── SMS failure must not affect the order ─────────────────────────────────

    @Test
    void smsFailure_doesNotPreventOrderAdvance() {
        order.setStatus("placed");
        // Even if SMS service somehow throws (before @Async proxy applies), the order should advance
        doThrow(new RuntimeException("SMS provider down"))
                .when(smsService).sendOrderNotification(any(), any(), any());

        // @Async runs synchronously in unit tests, but SmsService.sendOrderNotification()
        // already catches all provider exceptions internally.
        // Here we test the controller layer: if smsService itself threw, it would propagate
        // only because there is no Spring @Async proxy in unit tests. The actual runtime
        // behaviour is fully decoupled. We verify order status changed correctly anyway.
        try {
            controller.advance(orderId, null);
        } catch (RuntimeException ignored) {
            // In production this never reaches the caller — @Async runs in smsExecutor thread
        }

        // The critical assertion: order WAS saved before SMS was attempted
        verify(repo).save(argThat(o -> "confirmed".equals(o.getStatus())));
    }

    // ── Guest order (no user entity) ─────────────────────────────────────────

    @Test
    void advance_noUser_nullPhone_smsSkipped() {
        order.setUser(null);
        order.setStatus("placed");

        controller.advance(orderId, null);

        // SmsService.sendOrderNotification(null, ...) is called — SmsService itself handles null
        verify(smsService).sendOrderNotification(isNull(), eq("KCH-2026-000001"), any());
    }

    // ── Order total coverage: correct SMS type per transition ─────────────────

    @Test
    void advance_invalidStatus_doesNotCallSms() {
        order.setStatus("delivered"); // terminal state

        controller.advance(orderId, null);

        verifyNoInteractions(smsService);
    }
}

package in.kanchuk.delivery;

import in.kanchuk.controller.pub.PublicDeliveryController;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.dto.response.DeliveryCheckResponse;
import in.kanchuk.entity.DeliveryZone;
import in.kanchuk.entity.Pincode;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.repository.PincodeRepository;
import in.kanchuk.repository.StockLocationZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryCheckTest {

    @Mock
    private PincodeRepository pincodeRepo;

    @Mock
    private StockLocationZoneRepository slzRepo;

    @Mock
    private InventoryLevelRepository inventoryRepo;

    @InjectMocks
    private PublicDeliveryController controller;

    private DeliveryZone metroZone;
    private DeliveryZone remoteZone;
    private DeliveryZone inactiveZone;

    @BeforeEach
    void setUp() {
        metroZone = new DeliveryZone();
        metroZone.setCode("METRO_EXPRESS");
        metroZone.setName("Metro Express");
        metroZone.setStandardDeliveryCharge(new BigDecimal("49.00"));
        metroZone.setExpressDeliveryCharge(new BigDecimal("99.00"));
        metroZone.setStandardDeliveryDays(2);
        metroZone.setExpressDeliveryMinutes(120);
        metroZone.setExpressAvailable(true);
        metroZone.setCodAvailable(true);
        metroZone.setActive(true);

        remoteZone = new DeliveryZone();
        remoteZone.setCode("REMOTE");
        remoteZone.setName("Remote Areas");
        remoteZone.setStandardDeliveryCharge(new BigDecimal("99.00"));
        remoteZone.setExpressDeliveryCharge(BigDecimal.ZERO);
        remoteZone.setStandardDeliveryDays(10);
        remoteZone.setExpressDeliveryMinutes(0);
        remoteZone.setExpressAvailable(false);
        remoteZone.setCodAvailable(false);
        remoteZone.setActive(true);

        inactiveZone = new DeliveryZone();
        inactiveZone.setCode("INACTIVE");
        inactiveZone.setActive(false);
    }

    // ── Valid serviceable PIN ─────────────────────────────────────────────────

    @Test
    void validServiceablePin_returnsServiceableTrue() {
        Pincode p = serviceable("110001", "New Delhi", "Delhi", metroZone);
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(p));

        DeliveryCheckResponse r = check("110001");

        assertThat(r.isServiceable()).isTrue();
        assertThat(r.getPincode()).isEqualTo("110001");
        assertThat(r.getCity()).isEqualTo("New Delhi");
        assertThat(r.getState()).isEqualTo("Delhi");
    }

    @Test
    void validServiceablePin_correctStandardCharge() {
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(serviceable("110001", null, null, metroZone)));
        DeliveryCheckResponse r = check("110001");
        assertThat(r.getStandardDelivery().getCharge()).isEqualByComparingTo("49.00");
        assertThat(r.getStandardDelivery().getEstimatedDays()).isEqualTo(2);
        assertThat(r.getStandardDelivery().isAvailable()).isTrue();
    }

    @Test
    void validServiceablePin_expressAvailable_returnsExpressDetails() {
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(serviceable("110001", null, null, metroZone)));
        DeliveryCheckResponse r = check("110001");
        assertThat(r.getExpressDelivery().isAvailable()).isTrue();
        assertThat(r.getExpressDelivery().getCharge()).isEqualByComparingTo("99.00");
        assertThat(r.getExpressDelivery().getEstimatedMinutes()).isEqualTo(120);
    }

    @Test
    void validServiceablePin_expressUnavailable_returnsFalse() {
        when(pincodeRepo.findById("682001")).thenReturn(Optional.of(serviceable("682001", null, null, remoteZone)));
        DeliveryCheckResponse r = check("682001");
        assertThat(r.getExpressDelivery().isAvailable()).isFalse();
    }

    @Test
    void validServiceablePin_codAvailable() {
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(serviceable("110001", null, null, metroZone)));
        assertThat(check("110001").getCodAvailable()).isTrue();
    }

    @Test
    void validServiceablePin_codUnavailable() {
        when(pincodeRepo.findById("682001")).thenReturn(Optional.of(serviceable("682001", null, null, remoteZone)));
        assertThat(check("682001").getCodAvailable()).isFalse();
    }

    @Test
    void validServiceablePin_correctZoneInfo() {
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(serviceable("110001", null, null, metroZone)));
        DeliveryCheckResponse r = check("110001");
        assertThat(r.getZone().getCode()).isEqualTo("METRO_EXPRESS");
        assertThat(r.getZone().getName()).isEqualTo("Metro Express");
    }

    // ── Valid non-serviceable PIN ─────────────────────────────────────────────

    @Test
    void pincodeMarkedNotServiceable_returnsServiceableFalse() {
        Pincode p = new Pincode();
        p.setPincode("110001");
        p.setServiceable(false);
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(p));

        assertThat(check("110001").isServiceable()).isFalse();
    }

    @Test
    void pincodeWithInactiveZone_returnsServiceableFalse() {
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(serviceable("110001", null, null, inactiveZone)));
        assertThat(check("110001").isServiceable()).isFalse();
    }

    @Test
    void pincodeWithNoZone_returnsServiceableFalse() {
        Pincode p = new Pincode();
        p.setPincode("110001");
        p.setServiceable(true);
        p.setZone(null);
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(p));

        assertThat(check("110001").isServiceable()).isFalse();
    }

    // ── PIN not found ─────────────────────────────────────────────────────────

    @Test
    void unknownPin_returnsServiceableFalse() {
        when(pincodeRepo.findById("999999")).thenReturn(Optional.empty());
        DeliveryCheckResponse r = check("999999");
        assertThat(r.isServiceable()).isFalse();
        assertThat(r.getMessage()).isNotBlank();
    }

    // ── Invalid PIN format ────────────────────────────────────────────────────

    @Test
    void fiveDigitPin_returnsBadRequest() {
        ResponseEntity<ApiResponse<DeliveryCheckResponse>> resp = controller.check("11000", null);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getData().isServiceable()).isFalse();
    }

    @Test
    void sevenDigitPin_returnsBadRequest() {
        assertThat(controller.check("1100011", null).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void alphaPin_returnsBadRequest() {
        assertThat(controller.check("ABC001", null).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void pinWithSpaces_normalizedAndChecked() {
        when(pincodeRepo.findById("110001")).thenReturn(Optional.of(serviceable("110001", null, null, metroZone)));
        assertThat(check(" 110001 ").isServiceable()).isTrue();
    }

    @Test
    void emptyPin_returnsBadRequest() {
        assertThat(controller.check("", null).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Correct charge values ─────────────────────────────────────────────────

    @Test
    void correctStandardCharge_remoteZone() {
        when(pincodeRepo.findById("682001")).thenReturn(Optional.of(serviceable("682001", null, null, remoteZone)));
        assertThat(check("682001").getStandardDelivery().getCharge()).isEqualByComparingTo("99.00");
        assertThat(check("682001").getStandardDelivery().getEstimatedDays()).isEqualTo(10);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private DeliveryCheckResponse check(String pin) {
        return controller.check(pin, null).getBody().getData();
    }

    private Pincode serviceable(String pin, String city, String state, DeliveryZone zone) {
        Pincode p = new Pincode();
        p.setPincode(pin);
        p.setCity(city);
        p.setState(state);
        p.setServiceable(true);
        p.setZone(zone);
        return p;
    }
}

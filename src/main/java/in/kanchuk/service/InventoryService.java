package in.kanchuk.service;

import in.kanchuk.entity.*;
import in.kanchuk.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryLevelRepository inventoryRepo;
    private final StockMovementRepository movementRepo;

    /**
     * Atomically adjusts stock and records a movement.
     * qtyChange: positive = stock in, negative = stock out.
     */
    @Transactional
    public InventoryLevel adjustStock(UUID listingId,
                                      UUID locationId,
                                      int qtyChange,
                                      String movementType,
                                      String referenceType,
                                      UUID referenceId,
                                      String notes,
                                      AdminUser performedBy) {

        InventoryLevel level = inventoryRepo.findByListingIdAndLocationId(listingId, locationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No inventory level found for listing " + listingId + " at location " + locationId));

        int before = level.getQuantityOnHand();
        int after  = before + qtyChange;
        if (after < 0) after = 0;

        level.setQuantityOnHand(after);
        inventoryRepo.save(level);

        StockMovement movement = new StockMovement();
        movement.setListing(level.getListing());
        movement.setLocation(level.getLocation());
        movement.setMovementType(movementType);
        movement.setQtyBefore(before);
        movement.setQtyChange(qtyChange);
        movement.setQtyAfter(after);
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        movement.setNotes(notes);
        movement.setCreatedBy(performedBy);
        movementRepo.save(movement);

        return level;
    }
}

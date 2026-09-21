package in.kanchuk.repository;

import in.kanchuk.entity.InventoryLevel;
import in.kanchuk.entity.StockLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, UUID> {

    @Query(value = """
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl JOIN FETCH pl.variant pv JOIN FETCH pv.product p
        JOIN FETCH il.location loc
        ORDER BY p.name ASC, pv.sku ASC
        """,
        countQuery = "SELECT COUNT(il) FROM InventoryLevel il")
    Page<InventoryLevel> findAllEagerPaged(Pageable pageable);

    @Query("""
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl
        JOIN FETCH pl.variant pv
        JOIN FETCH pv.product p
        JOIN FETCH il.location loc
        WHERE pv.product.id = :productId
          AND pv.deletedAt IS NULL
          AND pl.deletedAt IS NULL
        ORDER BY pv.sku ASC, loc.name ASC
        """)
    List<InventoryLevel> findByProductId(@Param("productId") UUID productId);

    @Query("""
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl
        JOIN FETCH pl.variant pv
        JOIN FETCH pv.product p
        LEFT JOIN FETCH p.category cat
        JOIN FETCH il.location loc
        WHERE (COALESCE(:search, '') = ''
               OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND pv.deletedAt IS NULL
          AND pl.deletedAt IS NULL
        ORDER BY p.name ASC, pv.sku ASC
        """)
    List<InventoryLevel> findAllEager(
            @Param("search") String search,
            @Param("categoryId") UUID categoryId);

    @Query("""
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl
        JOIN FETCH pl.variant pv
        JOIN FETCH pv.product p
        LEFT JOIN FETCH p.category cat
        JOIN FETCH il.location loc
        WHERE (COALESCE(:search, '') = ''
               OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND loc.id = :locationId
          AND (:categoryId IS NULL OR p.category.id = :categoryId)
          AND pv.deletedAt IS NULL
          AND pl.deletedAt IS NULL
        ORDER BY p.name ASC, pv.sku ASC
        """)
    List<InventoryLevel> findAllEagerByLocation(
            @Param("search") String search,
            @Param("locationId") UUID locationId,
            @Param("categoryId") UUID categoryId);

    @Query("""
        SELECT COALESCE(SUM(il.quantityOnHand - il.quantityReserved), 0)
        FROM InventoryLevel il
        JOIN il.listing pl
        JOIN pl.variant pv
        WHERE (pv.sku = :sku OR pv.sku LIKE CONCAT(:sku, '-%'))
          AND il.location.id = :locationId
          AND pl.isActive = true
          AND pl.deletedAt IS NULL
          AND pv.isActive = true
          AND pv.deletedAt IS NULL
        """)
    Long findAvailableQtyBySkuAndLocation(@Param("sku") String sku,
                                          @Param("locationId") UUID locationId);

    @Query("SELECT il FROM InventoryLevel il WHERE il.listing.id = :listingId AND il.location.id = :locationId")
    Optional<InventoryLevel> findByListingIdAndLocationId(@Param("listingId") UUID listingId,
                                                          @Param("locationId") UUID locationId);

    @Query(value = """
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl JOIN FETCH pl.variant pv JOIN FETCH pv.product p
        JOIN FETCH il.location loc
        WHERE il.listing.id = :listingId ORDER BY loc.name ASC
        """,
        countQuery = "SELECT COUNT(il) FROM InventoryLevel il WHERE il.listing.id = :listingId")
    Page<InventoryLevel> findByListingId(@Param("listingId") UUID listingId, Pageable pageable);

    @Query(value = """
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl JOIN FETCH pl.variant pv JOIN FETCH pv.product p
        JOIN FETCH il.location loc
        WHERE il.location.id = :locationId ORDER BY p.name ASC
        """,
        countQuery = "SELECT COUNT(il) FROM InventoryLevel il WHERE il.location.id = :locationId")
    Page<InventoryLevel> findByLocationId(@Param("locationId") UUID locationId, Pageable pageable);

    @Query(value = """
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl JOIN FETCH pl.variant pv JOIN FETCH pv.product p
        JOIN FETCH il.location loc
        WHERE il.listing.id = :listingId AND il.location.id = :locationId
        """,
        countQuery = "SELECT COUNT(il) FROM InventoryLevel il WHERE il.listing.id = :listingId AND il.location.id = :locationId")
    Page<InventoryLevel> findByListingIdAndLocationId(@Param("listingId") UUID listingId,
                                                      @Param("locationId") UUID locationId, Pageable pageable);

    @Query(value = """
        SELECT il FROM InventoryLevel il
        JOIN FETCH il.listing pl JOIN FETCH pl.variant pv JOIN FETCH pv.product p
        JOIN FETCH il.location loc
        WHERE il.quantityOnHand <= il.lowStockThreshold ORDER BY il.quantityOnHand ASC
        """,
        countQuery = "SELECT COUNT(il) FROM InventoryLevel il WHERE il.quantityOnHand <= il.lowStockThreshold")
    Page<InventoryLevel> findLowStock(Pageable pageable);

    @Query("""
        SELECT DISTINCT il.location
        FROM InventoryLevel il
        JOIN il.listing pl
        JOIN pl.variant pv
        WHERE (pv.sku = :sku OR pv.sku LIKE CONCAT(:sku, '-%'))
          AND il.location.isActive = true
          AND il.location.deletedAt IS NULL
          AND pl.isActive = true
          AND pl.deletedAt IS NULL
          AND pv.isActive = true
          AND pv.deletedAt IS NULL
          AND (il.quantityOnHand - il.quantityReserved) > 0
        """)
    List<StockLocation> findHubsWithStockBySku(@Param("sku") String sku);
}

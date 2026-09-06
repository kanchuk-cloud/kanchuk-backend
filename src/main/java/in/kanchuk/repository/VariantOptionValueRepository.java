package in.kanchuk.repository;

import in.kanchuk.entity.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VariantOptionValueRepository extends JpaRepository<VariantOptionValue, VariantOptionValue.VariantOptionValueId> {

    @Query("SELECT vov FROM VariantOptionValue vov " +
           "JOIN FETCH vov.optionValue ov " +
           "JOIN FETCH ov.optionType " +
           "WHERE vov.id.variantId = :variantId")
    List<VariantOptionValue> findByVariantIdWithDetails(@Param("variantId") UUID variantId);

    @Modifying
    @Query("DELETE FROM VariantOptionValue vov WHERE vov.id.optionValueId = :optionValueId")
    void deleteByOptionValueId(@Param("optionValueId") UUID optionValueId);
}

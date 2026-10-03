package in.kanchuk.repository;

import in.kanchuk.entity.Testimonial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TestimonialRepository extends JpaRepository<Testimonial, UUID> {

    List<Testimonial> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT t FROM Testimonial t WHERE " +
           "LOWER(t.customerName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.city) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.productName) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Testimonial> search(@Param("q") String q, Pageable pageable);
}

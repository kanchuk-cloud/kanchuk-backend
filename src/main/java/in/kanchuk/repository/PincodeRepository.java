package in.kanchuk.repository;

import in.kanchuk.entity.Pincode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PincodeRepository extends JpaRepository<Pincode, String> {
    Page<Pincode> findByPincodeContaining(String pincode, Pageable pageable);
}

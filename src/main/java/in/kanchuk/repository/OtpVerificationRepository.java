package in.kanchuk.repository;

import in.kanchuk.entity.OtpPurpose;
import in.kanchuk.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, UUID> {

    /** Returns the most-recent un-verified, non-expired OTP for the given mobile+purpose. */
    @Query("SELECT o FROM OtpVerification o WHERE o.mobileNumber = :mobile " +
           "AND o.purpose = :purpose AND o.verifiedAt IS NULL AND o.expiresAt > :now " +
           "ORDER BY o.createdAt DESC")
    Optional<OtpVerification> findActiveOtp(
            @Param("mobile") String mobileNumber,
            @Param("purpose") OtpPurpose purpose,
            @Param("now") OffsetDateTime now);

    /** Returns the latest OTP record (any state) to check resend cooldown. */
    Optional<OtpVerification> findTopByMobileNumberAndPurposeOrderByCreatedAtDesc(
            String mobileNumber, OtpPurpose purpose);

    /** Marks all un-verified OTPs for a mobile+purpose as used, so only one active OTP exists. */
    @Modifying
    @Query("UPDATE OtpVerification o SET o.verifiedAt = :now " +
           "WHERE o.mobileNumber = :mobile AND o.purpose = :purpose AND o.verifiedAt IS NULL")
    void invalidateActive(
            @Param("mobile") String mobileNumber,
            @Param("purpose") OtpPurpose purpose,
            @Param("now") OffsetDateTime now);
}

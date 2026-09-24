package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByUserId(Long userId);
    List<Donation> findByStatus(Donation.PaymentStatus status);

    /**
     * Find donation by order ID and payment gateway.
     * Used for efficient lookup during payment verification/capture.
     */
    Optional<Donation> findByOrderIdAndPaymentGateway(String orderId, Donation.PaymentGateway paymentGateway);

    // ===== Analytics aggregation queries (DB-side; never load whole table) =====

    long countByStatus(Donation.PaymentStatus status);

    long countByPaymentGateway(Donation.PaymentGateway paymentGateway);

    /** Successful amount/count per single currency. Row: [currency, count, sum]. */
    @Query("SELECT d.currency, COUNT(d), SUM(d.amount) FROM Donation d "
            + "WHERE d.status = :status GROUP BY d.currency ORDER BY d.currency")
    List<Object[]> successByCurrency(@Param("status") Donation.PaymentStatus status);

    /** Amount across all statuses per single currency. Row: [currency, sum]. */
    @Query("SELECT d.currency, SUM(d.amount) FROM Donation d GROUP BY d.currency ORDER BY d.currency")
    List<Object[]> totalByCurrency();

    /** Successful amount/count per initiative label. Row: [initiative, count, sum]. */
    @Query("SELECT d.initiative, COUNT(d), SUM(d.amount) FROM Donation d "
            + "WHERE d.status = :status AND d.initiative IS NOT NULL "
            + "GROUP BY d.initiative ORDER BY d.initiative")
    List<Object[]> successByInitiative(@Param("status") Donation.PaymentStatus status);

    /**
     * Successful amount/count per month within a single currency. Row:
     * [yearMonth, currency, count, sum]. Uses PostgreSQL to_char for the month
     * bucket; different currencies are kept as separate rows (never combined).
     */
    @Query(value = "SELECT to_char(d.created_at, 'YYYY-MM') AS ym, d.currency, COUNT(*) AS cnt, SUM(d.amount) AS total "
            + "FROM donations d WHERE d.status = 'SUCCESS' "
            + "GROUP BY to_char(d.created_at, 'YYYY-MM'), d.currency "
            + "ORDER BY ym, d.currency", nativeQuery = true)
    List<Object[]> successByMonthCurrency();
}

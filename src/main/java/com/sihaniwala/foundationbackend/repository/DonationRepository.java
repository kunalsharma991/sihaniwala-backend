package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

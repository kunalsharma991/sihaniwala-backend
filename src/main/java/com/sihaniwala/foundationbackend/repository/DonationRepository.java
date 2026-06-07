package com.sihaniwala.foundationbackend.repository;

import com.sihaniwala.foundationbackend.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByUserId(Long userId);
    List<Donation> findByStatus(Donation.PaymentStatus status);
}

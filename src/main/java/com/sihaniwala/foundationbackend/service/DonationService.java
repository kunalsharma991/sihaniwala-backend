package com.sihaniwala.foundationbackend.service;

import com.sihaniwala.foundationbackend.dto.PaymentRequest;
import com.sihaniwala.foundationbackend.entity.Donation;
import com.sihaniwala.foundationbackend.entity.User;
import com.sihaniwala.foundationbackend.exception.BadRequestException;
import com.sihaniwala.foundationbackend.exception.ResourceNotFoundException;
import com.sihaniwala.foundationbackend.repository.DonationRepository;
import com.sihaniwala.foundationbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final StripeService stripeService;
    private final EmailService emailService;

    public Map<String, String> createPaymentIntent(PaymentRequest request, String userEmail) {
        try {
            Map<String, String> paymentData = stripeService.createPaymentIntent(
                    request.getAmount(),
                    request.getCurrency() != null ? request.getCurrency() : "INR"
            );

            User user = null;
            if (userEmail != null) {
                user = userRepository.findByEmail(userEmail).orElse(null);
            }

            Donation donation = Donation.builder()
                    .user(user)
                    .donorName(request.getDonorName())
                    .donorEmail(request.getDonorEmail() != null ? request.getDonorEmail() : userEmail)
                    .amount(request.getAmount())
                    .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                    .initiative(request.getInitiative())
                    .anonymous(request.isAnonymous())
                    .recurring(request.isRecurring())
                    .stripePaymentIntentId(paymentData.get("paymentIntentId"))
                    .status(Donation.PaymentStatus.PENDING)
                    .build();

            donationRepository.save(donation);
            log.info("Donation record created with PaymentIntent: {}", paymentData.get("paymentIntentId"));
            return paymentData;

        } catch (Exception e) {
            log.error("Failed to create payment intent: {}", e.getMessage());
            throw new BadRequestException("Failed to create payment intent: " + e.getMessage());
        }
    }

    public Donation confirmDonation(String paymentIntentId) {
        Donation donation = donationRepository.findAll().stream()
                .filter(d -> paymentIntentId.equals(d.getStripePaymentIntentId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found for payment intent"));

        boolean verified = stripeService.verifyPayment(paymentIntentId);
        if (verified) {
            donation.setStatus(Donation.PaymentStatus.SUCCESS);
            donationRepository.save(donation);
            try {
                emailService.sendDonationReceipt(
                        donation.getDonorEmail(),
                        donation.getDonorName(),
                        donation.getAmount().toPlainString(),
                        donation.getCurrency()
                );
            } catch (Exception ignored) {}
        } else {
            donation.setStatus(Donation.PaymentStatus.FAILED);
            donationRepository.save(donation);
        }
        return donation;
    }

    public List<Donation> getUserDonations(Long userId) {
        return donationRepository.findByUserId(userId);
    }

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public Donation getDonationById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
    }
}

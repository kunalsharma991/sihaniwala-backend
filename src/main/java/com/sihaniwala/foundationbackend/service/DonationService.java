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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final RazorpayService razorpayService;
    private final PayPalService payPalService;
    private final EmailService emailService;

    // ========== RAZORPAY ==========

    public Map<String, Object> createRazorpayOrder(PaymentRequest request) {
        try {
            String currency = request.getCurrency() != null ? request.getCurrency() : "INR";

            // Create Razorpay order
            Map<String, Object> orderData = razorpayService.createOrder(
                    request.getAmount(), currency, null);

            // Save donation as PENDING
            Donation donation = Donation.builder()
                    .donorName(request.getDonorName())
                    .donorEmail(request.getDonorEmail())
                    .amount(request.getAmount())
                    .currency(currency)
                    .initiative(request.getInitiative())
                    .anonymous(request.isAnonymous())
                    .recurring(request.isRecurring())
                    .paymentGateway(Donation.PaymentGateway.RAZORPAY)
                    .orderId((String) orderData.get("orderId"))
                    .status(Donation.PaymentStatus.PENDING)
                    .build();
            donationRepository.save(donation);

            log.info("Razorpay donation record created with orderId: {}", orderData.get("orderId"));
            return orderData;

        } catch (Exception e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new BadRequestException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    public Donation verifyRazorpayPayment(String orderId, String paymentId, String signature) {
        Donation donation = donationRepository.findByOrderIdAndPaymentGateway(orderId, Donation.PaymentGateway.RAZORPAY)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found for order: " + orderId));

        boolean verified = razorpayService.verifyPayment(orderId, paymentId, signature);
        if (verified) {
            donation.setPaymentId(paymentId);
            donation.setPaymentSignature(signature);
            donation.setStatus(Donation.PaymentStatus.SUCCESS);
            donation.setCompletedAt(java.time.LocalDateTime.now());
            donationRepository.save(donation);
            sendReceipt(donation);
            log.info("Razorpay payment verified for donation {}", donation.getId());
        } else {
            donation.setStatus(Donation.PaymentStatus.FAILED);
            donation.setFailureReason("Payment signature verification failed");
            donationRepository.save(donation);
            log.warn("Razorpay payment verification failed for order {}", orderId);
        }
        return donation;
    }

    // ========== PAYPAL ==========

    public Map<String, Object> createPaypalOrder(PaymentRequest request) {
        try {
            String currency = request.getCurrency() != null ? request.getCurrency() : "USD";

            // Create PayPal order
            Map<String, Object> orderData = payPalService.createOrder(request.getAmount(), currency);

            // Save donation as PENDING
            Donation donation = Donation.builder()
                    .donorName(request.getDonorName())
                    .donorEmail(request.getDonorEmail())
                    .amount(request.getAmount())
                    .currency(currency)
                    .initiative(request.getInitiative())
                    .anonymous(request.isAnonymous())
                    .recurring(request.isRecurring())
                    .paymentGateway(Donation.PaymentGateway.PAYPAL)
                    .orderId((String) orderData.get("orderId"))
                    .status(Donation.PaymentStatus.PENDING)
                    .build();
            donationRepository.save(donation);

            log.info("PayPal donation record created with orderId: {}", orderData.get("orderId"));
            return orderData;

        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create PayPal order: {}", e.getMessage());
            throw new BadRequestException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    public Donation capturePaypalPayment(String orderId) {
        Donation donation = donationRepository.findByOrderIdAndPaymentGateway(orderId, Donation.PaymentGateway.PAYPAL)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found for PayPal order: " + orderId));

        try {
            Map<String, Object> captureData = payPalService.captureOrder(orderId);
            String status = (String) captureData.get("status");

            if ("COMPLETED".equalsIgnoreCase(status)) {
                String paymentId = (String) captureData.get("paymentId");
                donation.setPaymentId(paymentId);
                donation.setStatus(Donation.PaymentStatus.SUCCESS);
                donation.setCompletedAt(java.time.LocalDateTime.now());
                donationRepository.save(donation);
                sendReceipt(donation);
                log.info("PayPal payment captured for donation {}, paymentId: {}", donation.getId(), paymentId);
            } else {
                donation.setStatus(Donation.PaymentStatus.FAILED);
                donation.setFailureReason("PayPal payment status: " + status);
                donationRepository.save(donation);
                log.warn("PayPal payment not completed for order {}: status={}", orderId, status);
            }
        } catch (Exception e) {
            donation.setStatus(Donation.PaymentStatus.FAILED);
            donation.setFailureReason(e.getMessage());
            donationRepository.save(donation);
            log.error("PayPal capture failed for order {}: {}", orderId, e.getMessage());
        }
        return donation;
    }

    // ========== COMMON ==========

    public List<Donation> getAllDonations() {
        return donationRepository.findAll();
    }

    public Donation getDonationById(Long id) {
        return donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
    }

    public List<Donation> getUserDonations(Long userId) {
        return donationRepository.findByUserId(userId);
    }

    private void sendReceipt(Donation donation) {
        try {
            emailService.sendDonationReceipt(
                    donation.getDonorEmail(),
                    donation.getDonorName(),
                    donation.getAmount().toPlainString(),
                    donation.getCurrency()
            );
        } catch (Exception e) {
            log.error("Failed to send donation receipt: {}", e.getMessage());
        }
    }
}

package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.dto.PaymentRequest;
import com.sihaniwala.foundationbackend.entity.Donation;
import com.sihaniwala.foundationbackend.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    // ========== RAZORPAY ENDPOINTS ==========

    @PostMapping("/payments/razorpay/order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRazorpayOrder(
            @RequestBody PaymentRequest request) {
        Map<String, Object> result = donationService.createRazorpayOrder(request);
        return ResponseEntity.ok(ApiResponse.ok("Razorpay order created", result));
    }

    @PostMapping("/payments/razorpay/verify")
    public ResponseEntity<ApiResponse<Donation>> verifyRazorpayPayment(
            @RequestBody Map<String, String> body) {
        String orderId = body.get("razorpay_order_id");
        String paymentId = body.get("razorpay_payment_id");
        String signature = body.get("razorpay_signature");

        Donation donation = donationService.verifyRazorpayPayment(orderId, paymentId, signature);
        String message = donation.getStatus() == Donation.PaymentStatus.SUCCESS
                ? "Payment verified successfully"
                : "Payment verification failed";
        return ResponseEntity.ok(ApiResponse.ok(message, donation));
    }

    // ========== PAYPAL ENDPOINTS ==========

    @PostMapping("/payments/paypal/order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPaypalOrder(
            @RequestBody PaymentRequest request) {
        Map<String, Object> result = donationService.createPaypalOrder(request);
        return ResponseEntity.ok(ApiResponse.ok("PayPal order created", result));
    }

    @PostMapping("/payments/paypal/capture/{orderId}")
    public ResponseEntity<ApiResponse<Donation>> capturePaypalOrder(
            @PathVariable String orderId) {
        Donation donation = donationService.capturePaypalPayment(orderId);
        return ResponseEntity.ok(ApiResponse.ok("PayPal payment captured", donation));
    }

    @PostMapping("/payments/paypal/create-order")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPaypalOrderLegacy(
            @RequestBody PaymentRequest request) {
        Map<String, Object> result = donationService.createPaypalOrder(request);
        return ResponseEntity.ok(ApiResponse.ok("PayPal order created", result));
    }

    @PostMapping("/payments/paypal/capture-order")
    public ResponseEntity<ApiResponse<Donation>> capturePaypalOrderLegacy(
            @RequestBody Map<String, String> body) {
        String orderId = body.get("orderId");
        Donation donation = donationService.capturePaypalPayment(orderId);
        return ResponseEntity.ok(ApiResponse.ok("PayPal payment captured", donation));
    }

    // ========== COMMON ENDPOINTS ==========

    @GetMapping("/donations")
    public ResponseEntity<ApiResponse<List<Donation>>> getAllDonations() {
        List<Donation> donations = donationService.getAllDonations();
        return ResponseEntity.ok(ApiResponse.ok(donations));
    }

    @GetMapping("/donations/{id}")
    public ResponseEntity<ApiResponse<Donation>> getDonation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(donationService.getDonationById(id)));
    }
}

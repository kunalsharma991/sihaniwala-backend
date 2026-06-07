package com.sihaniwala.foundationbackend.controller;

import com.sihaniwala.foundationbackend.dto.ApiResponse;
import com.sihaniwala.foundationbackend.dto.PaymentRequest;
import com.sihaniwala.foundationbackend.entity.Donation;
import com.sihaniwala.foundationbackend.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping("/payments/create-payment-intent")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPaymentIntent(
            @RequestBody PaymentRequest request, Principal principal) {
        String email = principal != null ? principal.getName() : null;
        Map<String, String> result = donationService.createPaymentIntent(request, email);
        return ResponseEntity.ok(ApiResponse.ok("Payment intent created", result));
    }

    @PostMapping("/payments/confirm")
    public ResponseEntity<ApiResponse<Donation>> confirmPayment(@RequestBody Map<String, String> body) {
        Donation donation = donationService.confirmDonation(body.get("paymentIntentId"));
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed", donation));
    }

    @GetMapping("/donations")
    public ResponseEntity<ApiResponse<List<Donation>>> getUserDonations(Principal principal) {
        // This will get user ID from the principal - simplified for now
        List<Donation> donations = donationService.getAllDonations();
        return ResponseEntity.ok(ApiResponse.ok(donations));
    }

    @GetMapping("/donations/{id}")
    public ResponseEntity<ApiResponse<Donation>> getDonation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(donationService.getDonationById(id)));
    }
}

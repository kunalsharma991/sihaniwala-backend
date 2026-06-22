package com.sihaniwala.foundationbackend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            razorpayClient = new RazorpayClient(keyId, keySecret);
            log.info("Razorpay client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
        }
    }

    public Map<String, Object> createOrder(BigDecimal amount, String currency, String receiptId) {
        try {
            // Razorpay expects amount in paise (smallest currency unit)
            int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", currency != null ? currency.toUpperCase() : "INR");
            orderRequest.put("receipt", receiptId != null ? receiptId : "rcpt_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1); // Auto-capture

            Order order = razorpayClient.orders.create(orderRequest);
            log.info("Razorpay order created: " + order.get("id"));

            return Map.of(
                    "orderId", order.get("id"),
                    "amount", amountInPaise,
                    "currency", order.get("currency"),
                    "keyId", keyId
            );
        } catch (Exception e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean verified = Utils.verifyPaymentSignature(options, keySecret);
            log.info("Razorpay payment verification for order {}: {}", orderId, verified);
            return verified;
        } catch (Exception e) {
            log.error("Razorpay signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}

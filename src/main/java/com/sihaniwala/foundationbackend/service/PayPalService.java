package com.sihaniwala.foundationbackend.service;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * PayPal payment service for processing international donations.
 * Uses PayPal Checkout SDK 2.0.0 with support for USD, EUR, GBP, and INR.
 */
@Service
@Slf4j
public class PayPalService {

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode;

    private PayPalHttpClient payPalHttpClient;

    // Supported currencies for PayPal transactions
    private static final List<String> SUPPORTED_CURRENCIES = List.of("USD", "EUR", "GBP", "INR");

    /**
     * Initialize PayPal client on application startup.
     * Uses SandboxEnvironment for testing, LiveEnvironment for production.
     */
    @PostConstruct
    public void init() {
        try {
            PayPalEnvironment environment;
            if ("live".equalsIgnoreCase(mode)) {
                environment = new PayPalEnvironment.Live(clientId, clientSecret);
                log.info("PayPal client initialized in LIVE mode");
            } else {
                environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
                log.info("PayPal client initialized in SANDBOX mode");
            }
            payPalHttpClient = new PayPalHttpClient(environment);
        } catch (Exception e) {
            log.error("Failed to initialize PayPal client: {}", e.getMessage(), e);
            throw new RuntimeException("PayPal initialization failed", e);
        }
    }

    /**
     * Creates a PayPal order for donation payment.
     *
     * @param amount   The donation amount
     * @param currency The currency code (USD, EUR, GBP, or INR)
     * @return Map containing orderId and approveLink for redirect
     * @throws RuntimeException if order creation fails or currency is unsupported
     */
    public Map<String, Object> createOrder(BigDecimal amount, String currency) {
        // Validate currency
        String currencyCode = currency != null ? currency.toUpperCase() : "USD";
        if (!SUPPORTED_CURRENCIES.contains(currencyCode)) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + currencyCode + ". Supported: " + SUPPORTED_CURRENCIES
            );
        }

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        try {
            log.info("Creating PayPal order: amount={}, currency={}", amount, currencyCode);

            OrdersCreateRequest request = new OrdersCreateRequest();
            request.prefer("return=representation");
            request.requestBody(buildOrderRequest(amount, currencyCode));

            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();

            // Extract approval link from HATEOAS links
            String approveLink = order.links().stream()
                    .filter(link -> "approve".equals(link.rel()))
                    .findFirst()
                    .map(link -> link.href())
                    .orElseThrow(() -> new RuntimeException("Approval link not found in PayPal response"));

            log.info("PayPal order created successfully: orderId={}, status={}", order.id(), order.status());

            return Map.of(
                    "orderId", order.id(),
                    "approveLink", approveLink
            );

        } catch (IOException e) {
            log.error("PayPal order creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create PayPal order: " + e.getMessage(), e);
        }
    }

    /**
     * Captures a PayPal order after buyer approval.
     *
     * @param orderId The PayPal order ID to capture
     * @return Map containing:
     *         - status: COMPLETED or other status
     *         - paymentId: PayPal capture transaction ID (if successful)
     *         - amount: captured amount (if successful)
     *         - currency: currency code (if successful)
     * @throws RuntimeException if capture request fails
     */
    public Map<String, Object> captureOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }

        try {
            log.info("Capturing PayPal order: orderId={}", orderId);

            OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
            request.requestBody(new OrderRequest());

            HttpResponse<Order> response = payPalHttpClient.execute(request);
            Order order = response.result();

            String status = order.status();
            log.info("PayPal order capture result: orderId={}, status={}", orderId, status);

            if ("COMPLETED".equalsIgnoreCase(status)) {
                // Extract capture details from first purchase unit
                String paymentId = null;
                String capturedAmount = null;
                String capturedCurrency = null;
                
                if (!order.purchaseUnits().isEmpty()) {
                    var purchaseUnit = order.purchaseUnits().get(0);
                    if (purchaseUnit.payments() != null && 
                        purchaseUnit.payments().captures() != null && 
                        !purchaseUnit.payments().captures().isEmpty()) {
                        
                        var capture = purchaseUnit.payments().captures().get(0);
                        paymentId = capture.id();
                        capturedAmount = capture.amount().value();
                        capturedCurrency = capture.amount().currencyCode();
                        
                        log.info("Capture details: paymentId={}, amount={} {}, status={}",
                                paymentId, capturedAmount, capturedCurrency, capture.status());
                    }
                }
                
                return Map.of(
                        "status", "COMPLETED",
                        "paymentId", paymentId != null ? paymentId : "",
                        "amount", capturedAmount != null ? capturedAmount : "0",
                        "currency", capturedCurrency != null ? capturedCurrency : ""
                );
            } else {
                log.warn("PayPal order not completed: orderId={}, status={}", orderId, status);
                return Map.of(
                        "status", status != null ? status : "UNKNOWN",
                        "paymentId", "",
                        "amount", "",
                        "currency", ""
                );
            }

        } catch (IOException e) {
            log.error("PayPal order capture failed: orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to capture PayPal payment: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the order request body for PayPal API.
     */
    private OrderRequest buildOrderRequest(BigDecimal amount, String currency) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        // Set amount
        AmountWithBreakdown amountBreakdown = new AmountWithBreakdown()
                .currencyCode(currency)
                .value(amount.toPlainString());

        // Create purchase unit
        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                .amountWithBreakdown(amountBreakdown)
                .description("Sihaniwala Foundation Donation")
                .customId("donation_" + System.currentTimeMillis());

        orderRequest.purchaseUnits(List.of(purchaseUnit));

        // Set application context
        ApplicationContext applicationContext = new ApplicationContext()
                .brandName("Sihaniwala Foundation")
                .landingPage("BILLING")
                .shippingPreference("NO_SHIPPING")
                .userAction("PAY_NOW");

        orderRequest.applicationContext(applicationContext);

        return orderRequest;
    }
}

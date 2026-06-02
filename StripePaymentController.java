package com.careertrack.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*")
public class StripePaymentController {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> data) {
        try {
            if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Stripe secret key is not configured"));
            }
            Stripe.apiKey = stripeSecretKey;

            Long amount = Long.valueOf(data.get("amount").toString());
            String currency = data.getOrDefault("currency", "usd").toString();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .addPaymentMethodType("card")
                .build();

            PaymentIntent intent = PaymentIntent.create(params);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "clientSecret", intent.getClientSecret(),
                "paymentIntentId", intent.getId()
            ));
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Stripe error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

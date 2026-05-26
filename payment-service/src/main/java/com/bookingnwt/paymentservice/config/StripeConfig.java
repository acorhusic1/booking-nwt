package com.bookingnwt.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe SDK konfiguracija — postavlja API key globalno za biblioteku.
 * Test kljuc se daje kroz env varijablu STRIPE_SECRET_KEY (vidi
 * docker-compose.yml i README za uputstvo kako napraviti test nalog).
 *
 * Ako kljuc nije postavljen, koristi placeholder — Stripe pozivi ce
 * vracati "Invalid API Key" do trenutka kad se pravi kljuc postavi.
 */
@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.secret-key:sk_test_PLACEHOLDER_REPLACE_ME}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        if (stripeSecretKey.contains("PLACEHOLDER")) {
            log.warn("⚠️ STRIPE_SECRET_KEY nije postavljen — Stripe pozivi će padati. Postavi env varijablu sa test kljucem (sk_test_...).");
        } else {
            log.info("✅ Stripe SDK inicijalizovan ({})",
                    stripeSecretKey.startsWith("sk_test_") ? "TEST MODE" : "LIVE MODE");
        }
    }
}

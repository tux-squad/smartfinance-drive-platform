package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.payment.stripe;

import com.smartfinance.smartfinancedriveplatform.billing.application.outboundservices.StripePaymentGatewayService;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StripePaymentGatewayServiceImpl implements StripePaymentGatewayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripePaymentGatewayServiceImpl.class);

    private final String apiKey;
    private final String defaultSuccessUrl;
    private final String defaultCancelUrl;
    private final List<String> allowedOrigins;

    public StripePaymentGatewayServiceImpl(
            @Value("${stripe.api-key:}") String apiKey,
            @Value("${stripe.success-url:http://localhost:5173/billing/success?session_id={CHECKOUT_SESSION_ID}}") String defaultSuccessUrl,
            @Value("${stripe.cancel-url:http://localhost:5173/billing/cancel}") String defaultCancelUrl) {
        this(apiKey, defaultSuccessUrl, defaultCancelUrl, "http://localhost:3000,http://localhost:4200,http://localhost:5173,http://localhost:8080,http://success,http://cancel");
    }

    public StripePaymentGatewayServiceImpl(
            @Value("${stripe.api-key:}") String apiKey,
            @Value("${stripe.success-url:http://localhost:5173/billing/success?session_id={CHECKOUT_SESSION_ID}}") String defaultSuccessUrl,
            @Value("${stripe.cancel-url:http://localhost:5173/billing/cancel}") String defaultCancelUrl,
            @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173,http://localhost:8080}") String allowedOriginsStr) {
        this.apiKey = apiKey;
        this.defaultSuccessUrl = defaultSuccessUrl;
        this.defaultCancelUrl = defaultCancelUrl;
        if (allowedOriginsStr != null && !allowedOriginsStr.isBlank()) {
            this.allowedOrigins = Arrays.stream(allowedOriginsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            this.allowedOrigins = List.of("http://localhost:3000", "http://localhost:4200", "http://localhost:5173", "http://localhost:8080");
        }
        if (apiKey != null && !apiKey.isBlank()) {
            Stripe.apiKey = apiKey;
        }
    }

    @Override
    public String createCustomer(String email, String name) {
        ensureStripeConfigured();
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .build();
            Customer customer = Customer.create(params);
            return customer.getId();
        } catch (StripeException e) {
            LOGGER.error("Failed to create Stripe customer for email {}: {}", email, e.getMessage());
            throw new DomainValidationException("billing.error.stripeCustomerCreationFailed");
        }
    }

    @Override
    public String createCheckoutSession(String stripeCustomerId, String stripePriceId, String clientReferenceId, String successUrl, String cancelUrl) {
        ensureStripeConfigured();

        String resolvedSuccessUrl = validateAndResolveUrl(successUrl, defaultSuccessUrl);
        String resolvedCancelUrl = validateAndResolveUrl(cancelUrl, defaultCancelUrl);

        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setCustomer(stripeCustomerId)
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(resolvedSuccessUrl)
                    .setCancelUrl(resolvedCancelUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(stripePriceId)
                                    .setQuantity(1L)
                                    .build()
                    );

            if (clientReferenceId != null && !clientReferenceId.isBlank()) {
                builder.setClientReferenceId(clientReferenceId);
            }

            Session session = Session.create(builder.build());
            return session.getUrl();
        } catch (StripeException e) {
            LOGGER.error("Failed to create Stripe checkout session: {}", e.getMessage());
            throw new DomainValidationException("billing.error.stripeCheckoutSessionFailed");
        }
    }

    private String validateAndResolveUrl(String candidateUrl, String fallbackUrl) {
        String targetUrl = (candidateUrl != null && !candidateUrl.isBlank()) ? candidateUrl : fallbackUrl;
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new DomainValidationException("billing.error.invalidRedirectUrl");
        }
        try {
            String urlWithoutQuery = targetUrl.contains("?") ? targetUrl.substring(0, targetUrl.indexOf("?")) : targetUrl;
            URI uri = new URI(urlWithoutQuery);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new DomainValidationException("billing.error.invalidRedirectUrl");
            }
            String authority = uri.getAuthority();
            if (authority == null || authority.isBlank()) {
                throw new DomainValidationException("billing.error.invalidRedirectUrl");
            }
            String origin = scheme.toLowerCase() + "://" + authority.toLowerCase();
            boolean isAllowed = allowedOrigins.stream().anyMatch(allowed -> {
                String normalizedAllowed = allowed.toLowerCase().trim();
                return origin.equalsIgnoreCase(normalizedAllowed) || targetUrl.toLowerCase().startsWith(normalizedAllowed);
            });
            if (!isAllowed) {
                LOGGER.warn("Blocked untrusted Stripe checkout redirect URL: {}", targetUrl);
                throw new DomainValidationException("billing.error.untrustedRedirectUrl");
            }
            return targetUrl;
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Malformed Stripe checkout redirect URL: {}", targetUrl);
            throw new DomainValidationException("billing.error.invalidRedirectUrl");
        }
    }

    private void ensureStripeConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("Stripe API key is not configured. Stripe operations are disabled.");
            throw new DomainValidationException("billing.error.stripeNotConfigured");
        }
        Stripe.apiKey = apiKey;
    }
}

package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate Limiting Filter for protecting sensitive authentication endpoints against brute-force attacks.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    private static class RequestCounter {
        final long windowStartTimestamp;
        final AtomicInteger count;

        RequestCounter(long windowStartTimestamp) {
            this.windowStartTimestamp = windowStartTimestamp;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isRateLimitedPath(path)) {
            String clientIp = getClientIP(request);
            long currentMinute = Instant.now().getEpochSecond() / 60;
            String key = clientIp + ":" + path + ":" + currentMinute;

            RequestCounter counter = requestCounts.compute(key, (k, v) -> {
                if (v == null || v.windowStartTimestamp != currentMinute) {
                    return new RequestCounter(currentMinute);
                }
                v.count.incrementAndGet();
                return v;
            });

            if (counter.count.get() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                            "status": 429,
                            "error": "Too Many Requests",
                            "message": "Rate limit exceeded for authentication requests. Please try again in a minute."
                        }
                        """);
                return;
            }

            if (requestCounts.size() > 1000) {
                requestCounts.entrySet().removeIf(entry -> entry.getValue().windowStartTimestamp < currentMinute - 2);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPath(String path) {
        return path.startsWith("/api/v1/auth/sign-in") ||
               path.startsWith("/api/v1/auth/forgot-password") ||
               path.startsWith("/api/v1/auth/sign-up") ||
               path.startsWith("/api/v1/partners/sunat") ||
               (path.startsWith("/api/v1/billing") && !path.startsWith("/api/v1/billing/webhooks"));
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

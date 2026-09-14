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
        return path.startsWith("/api/v1/auth/sessions") ||
               path.startsWith("/api/v1/auth/tokens") ||
               path.startsWith("/api/v1/auth/password-recoveries") ||
               path.startsWith("/api/v1/auth/password-resets") ||
               path.startsWith("/api/v1/auth/registrations") ||
               path.startsWith("/api/v1/auth/google") ||
               path.startsWith("/api/v1/partners/sunat") ||
               (path.startsWith("/api/v1/billing") && !path.startsWith("/api/v1/billing/webhooks"));
    }

    private String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr == null || remoteAddr.isBlank()) {
            remoteAddr = "unknown";
        }
        // Only evaluate X-Forwarded-For if request originates from a local or private proxy IP
        if (isLocalOrTrustedProxy(remoteAddr)) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isBlank()) {
                String[] ips = xfHeader.split(",");
                String clientIp = ips[0].trim();
                if (!clientIp.isEmpty()) {
                    return clientIp;
                }
            }
        }
        return remoteAddr;
    }

    private boolean isLocalOrTrustedProxy(String ip) {
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)
                || ip.startsWith("10.") || ip.startsWith("172.") || ip.startsWith("192.168.");
    }
}

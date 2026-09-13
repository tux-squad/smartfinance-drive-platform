package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.pipeline;

import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.authorization.sbc.services.UserDetailsServiceImpl;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts HTTP requests, extracts JWT Bearer token, validates it,
 * and sets authentication in {@link SecurityContextHolder}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   UserDetailsServiceImpl userDetailsService,
                                   com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.jwt.services.TokenBlacklistService tokenBlacklistService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = parseBearerToken(request);

            if (StringUtils.hasText(token)) {
                String jti = tokenService.getJtiFromToken(token);
                if (tokenBlacklistService.isBlacklisted(token) || (jti != null && tokenBlacklistService.isBlacklisted(jti))) {
                    log.warn("Access attempt with blacklisted token or JTI");
                    filterChain.doFilter(request, response);
                    return;
                }

                if (tokenService.validateAccessToken(token)) {
                    String username = tokenService.getUsernameFromToken(token);
                    String userId = tokenService.getUserIdFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new com.smartfinance.smartfinancedriveplatform.shared.infrastructure.security.SecurityUtils.AuthenticatedUserDetails(userId, username));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}

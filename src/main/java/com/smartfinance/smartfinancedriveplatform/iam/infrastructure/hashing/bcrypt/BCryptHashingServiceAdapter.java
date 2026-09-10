package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.hashing.bcrypt;

import com.smartfinance.smartfinancedriveplatform.iam.domain.services.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Infrastructure implementation of {@link HashingService} using Spring Security's {@link PasswordEncoder}.
 */
@Component
public class BCryptHashingServiceAdapter implements HashingService {

    private final PasswordEncoder passwordEncoder;

    public BCryptHashingServiceAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}

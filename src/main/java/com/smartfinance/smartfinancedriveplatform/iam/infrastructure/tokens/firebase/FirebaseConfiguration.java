package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.tokens.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Base64;

/**
 * Spring configuration for initializing FirebaseApp and FirebaseAuth beans.
 * Supports credentials supplied via Base64 environment variable, file path, or Project ID.
 */
@Configuration
public class FirebaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

    @Value("${firebase.project-id:}")
    private String projectId;

    @Value("${firebase.credentials.base64:}")
    private String credentialsBase64;

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Bean
    public FirebaseApp firebaseApp() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();
            GoogleCredentials credentials = null;

            if (credentialsBase64 != null && !credentialsBase64.isBlank()) {
                byte[] decoded = Base64.getDecoder().decode(credentialsBase64.trim());
                credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(decoded));
                log.info("Loaded Firebase credentials from Base64 configuration");
            } else if (credentialsPath != null && !credentialsPath.isBlank()) {
                credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath.trim()));
                log.info("Loaded Firebase credentials from file path: [{}]", credentialsPath);
            } else {
                try {
                    credentials = GoogleCredentials.getApplicationDefault();
                    log.info("Loaded Firebase credentials from Google Application Default Credentials");
                } catch (Exception e) {
                    log.debug("No ApplicationDefaultCredentials available for Firebase: {}", e.getMessage());
                }
            }

            if (credentials != null) {
                optionsBuilder.setCredentials(credentials);
            }

            if (projectId != null && !projectId.isBlank()) {
                optionsBuilder.setProjectId(projectId.trim());
            }

            if (credentials == null && (projectId == null || projectId.isBlank())) {
                log.warn("FirebaseApp initialization skipped: neither credentials nor firebase.project-id are configured.");
                return null;
            }

            FirebaseApp app = FirebaseApp.initializeApp(optionsBuilder.build());
            log.info("FirebaseApp initialized successfully with name [{}]", app.getName());
            return app;
        } catch (Exception e) {
            log.error("Failed to initialize FirebaseApp: {}", e.getMessage());
            return null;
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth(@Autowired(required = false) FirebaseApp firebaseApp) {
        if (firebaseApp == null) {
            log.warn("FirebaseAuth bean will not be available: FirebaseApp is null");
            return null;
        }
        return FirebaseAuth.getInstance(firebaseApp);
    }
}

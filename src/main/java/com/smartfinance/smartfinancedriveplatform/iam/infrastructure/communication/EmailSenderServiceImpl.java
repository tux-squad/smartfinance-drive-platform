package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailSenderService;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.dto.BrevoEmailRequest;
import com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication.dto.BrevoEmailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Transactional email service implementation for dispatching OTP verification codes via Brevo v3 HTTPS API (Port 443)
 * with graceful fallback to JavaMailSender SMTP and emulated dispatch.
 */
@Service
public class EmailSenderServiceImpl implements EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final RestClient restClient;
    private final String brevoApiKey;
    private final String brevoSenderEmail;
    private final String brevoSenderName;
    private final String fromEmail;
    private final boolean allowEmulated;

    @org.springframework.beans.factory.annotation.Autowired
    public EmailSenderServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider,
            @Value("${brevo.api-key:}") String brevoApiKey,
            @Value("${brevo.base-url:https://api.brevo.com}") String brevoBaseUrl,
            @Value("${brevo.sender-email:aldomachaccasoto@gmail.com}") String brevoSenderEmail,
            @Value("${brevo.sender-name:SmartFinance Drive}") String brevoSenderName,
            @Value("${app.mail.allow-emulated:true}") boolean allowEmulated,
            @Value("${spring.mail.username:no-reply@smartfinance.drive.pe}") String fromEmail) {
        this.mailSenderProvider = mailSenderProvider;
        this.brevoApiKey = brevoApiKey != null ? brevoApiKey.trim() : "";
        this.brevoSenderEmail = brevoSenderEmail != null && !brevoSenderEmail.isBlank() ? brevoSenderEmail.trim() : "aldomachaccasoto@gmail.com";
        this.brevoSenderName = brevoSenderName != null && !brevoSenderName.isBlank() ? brevoSenderName.trim() : "SmartFinance Drive";
        this.allowEmulated = allowEmulated;
        this.fromEmail = fromEmail;

        String baseUrl = brevoBaseUrl != null && !brevoBaseUrl.isBlank() ? brevoBaseUrl.trim() : "https://api.brevo.com";
        RestClient.Builder builder = (restClientBuilderProvider != null && restClientBuilderProvider.getIfAvailable() != null)
                ? restClientBuilderProvider.getIfAvailable()
                : RestClient.builder();
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public EmailSenderServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, boolean allowEmulated, String fromEmail) {
        this(mailSenderProvider, null, "", "https://api.brevo.com", "aldomachaccasoto@gmail.com", "SmartFinance Drive", allowEmulated, fromEmail);
    }

    public EmailSenderServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, boolean allowEmulated) {
        this(mailSenderProvider, allowEmulated, "no-reply@smartfinance.drive.pe");
    }

    public EmailSenderServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this(mailSenderProvider, true, "no-reply@smartfinance.drive.pe");
    }

    public EmailSenderServiceImpl(RestClient restClient, String brevoApiKey, String brevoSenderEmail, String brevoSenderName) {
        this.mailSenderProvider = null;
        this.restClient = restClient;
        this.brevoApiKey = brevoApiKey != null ? brevoApiKey.trim() : "";
        this.brevoSenderEmail = brevoSenderEmail != null ? brevoSenderEmail.trim() : "aldomachaccasoto@gmail.com";
        this.brevoSenderName = brevoSenderName != null ? brevoSenderName.trim() : "SmartFinance Drive";
        this.allowEmulated = true;
        this.fromEmail = "no-reply@smartfinance.drive.pe";
    }

    @Override
    public void sendCorporateVerificationOtp(String toEmail, String recipientName, String entityName, String otpCode, int expirationMinutes) {
        log.info("Sending B2B corporate OTP verification code to [{}] for entity [{}] (expires in {}m)",
                toEmail, entityName, expirationMinutes);

        String subject = "SmartFinance Drive - Código de Verificación Corporativa B2B";
        String htmlBody = buildHtmlBody(recipientName, entityName, otpCode, expirationMinutes);

        if (dispatchViaBrevo(toEmail, recipientName, subject, htmlBody)) {
            return;
        }

        dispatchViaSmtp(toEmail, subject, htmlBody);
    }

    @Override
    public void sendEmailVerificationOtp(String toEmail, String otpCode, int expirationMinutes) {
        log.info("Sending Email OTP verification code to [{}] (expires in {}m)", toEmail, expirationMinutes);

        String subject = "SmartFinance Drive - Código de Verificación de Correo";
        String htmlBody = buildHtmlBodyForEmailVerification(otpCode, expirationMinutes);

        if (dispatchViaBrevo(toEmail, toEmail, subject, htmlBody)) {
            return;
        }

        dispatchViaSmtp(toEmail, subject, htmlBody);
    }

    private boolean dispatchViaBrevo(String toEmail, String recipientName, String subject, String htmlBody) {
        if (brevoApiKey.isBlank()) {
            return false;
        }

        try {
            String targetName = recipientName != null && !recipientName.isBlank() ? recipientName : toEmail;
            BrevoEmailRequest payload = new BrevoEmailRequest(
                    new BrevoEmailRequest.BrevoSender(brevoSenderName, brevoSenderEmail),
                    List.of(new BrevoEmailRequest.BrevoRecipient(toEmail, targetName)),
                    subject,
                    htmlBody
            );

            log.info("Dispatching email OTP to [{}] via Brevo HTTPS REST API", toEmail);

            BrevoEmailResponse response = restClient.post()
                    .uri("/v3/smtp/email")
                    .header("api-key", brevoApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(BrevoEmailResponse.class);

            if (response != null && response.messageId() != null) {
                log.info("Email verification OTP successfully dispatched via Brevo HTTPS API (messageId: {}) to [{}]",
                        response.messageId(), toEmail);
                return true;
            }
            log.warn("Received empty messageId from Brevo API for [{}]", toEmail);
            return false;
        } catch (Exception e) {
            log.error("Failed to dispatch email via Brevo HTTPS API to [{}]: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private void dispatchViaSmtp(String toEmail, String subject, String htmlBody) {
        JavaMailSender mailSender = mailSenderProvider != null ? mailSenderProvider.getIfAvailable() : null;
        if (mailSender == null) {
            if (allowEmulated) {
                log.info("JavaMailSender is not configured. Emulated email OTP dispatch for [{}]", toEmail);
                return;
            }
            log.error("JavaMailSender is not configured and email emulation is disabled. Failed to dispatch OTP to [{}]", toEmail);
            throw new IllegalStateException("iam.error.email.serviceUnavailable");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Verification OTP email successfully dispatched via SMTP to [{}]", toEmail);
        } catch (MessagingException | RuntimeException e) {
            log.error("Failed to dispatch email via SMTP to [{}]: {}", toEmail, e.getMessage());
            throw new RuntimeException("iam.error.email.dispatchFailed", e);
        }
    }

    private String buildHtmlBodyForEmailVerification(String otpCode, int expirationMinutes) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Verificación de Correo Electrónico</title>
                </head>
                <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; margin: 0; padding: 30px;">
                    <div style="max-width: 580px; margin: 0 auto; background-color: #1e293b; border-radius: 12px; border: 1px solid #334155; padding: 32px; box-shadow: 0 10px 25px rgba(0,0,0,0.3);">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h1 style="color: #38bdf8; margin: 0; font-size: 26px; letter-spacing: 0.5px;">SmartFinance Drive</h1>
                            <p style="color: #94a3b8; margin: 4px 0 0 0; font-size: 14px;">Plataforma de Financiamiento y Crédito Vehicular</p>
                        </div>
                        <div style="background-color: #0f172a; border-radius: 8px; padding: 20px; margin-bottom: 24px; border-left: 4px solid #38bdf8;">
                            <h2 style="color: #f1f5f9; margin: 0 0 8px 0; font-size: 18px;">Verificación de Correo Electrónico</h2>
                            <p style="color: #cbd5e1; margin: 0; font-size: 14px; line-height: 1.5;">
                                Se ha solicitado la verificación de tu dirección de correo electrónico en SmartFinance Drive.
                            </p>
                        </div>
                        <div style="text-align: center; margin: 32px 0;">
                            <p style="color: #94a3b8; font-size: 13px; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 1px;">Tu código de seguridad de 6 dígitos:</p>
                            <div style="display: inline-block; background: linear-gradient(135deg, #0284c7, #2563eb); color: #ffffff; font-size: 36px; font-weight: 800; letter-spacing: 8px; padding: 16px 36px; border-radius: 10px; box-shadow: 0 4px 15px rgba(37,99,235,0.4);">
                                %s
                            </div>
                            <p style="color: #f59e0b; font-size: 13px; margin-top: 12px;">⏰ Válido durante %d minutos (uso único).</p>
                        </div>
                        <div style="border-top: 1px solid #334155; padding-top: 20px; color: #64748b; font-size: 12px; line-height: 1.5;">
                            <p style="margin: 0 0 6px 0;">⚠️ <strong>Aviso de Seguridad:</strong> Si no solicitaste este código, puedes ignorar este mensaje con seguridad. Ningún colaborador de SmartFinance Drive te solicitará este código.</p>
                            <p style="margin: 0;">© 2026 SmartFinance Drive Platform. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(otpCode, expirationMinutes);
    }

    private String buildHtmlBody(String recipientName, String entityName, String otpCode, int expirationMinutes) {
        String greeting = (recipientName != null && !recipientName.isBlank()) ? recipientName : "Representante Corporativo";
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Verificación Corporativa B2B</title>
                </head>
                <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #0f172a; color: #f8fafc; margin: 0; padding: 30px;">
                    <div style="max-width: 580px; margin: 0 auto; background-color: #1e293b; border-radius: 12px; border: 1px solid #334155; padding: 32px; box-shadow: 0 10px 25px rgba(0,0,0,0.3);">
                        <div style="text-align: center; margin-bottom: 24px;">
                            <h1 style="color: #38bdf8; margin: 0; font-size: 26px; letter-spacing: 0.5px;">SmartFinance Drive</h1>
                            <p style="color: #94a3b8; margin: 4px 0 0 0; font-size: 14px;">Plataforma B2B de Financiamiento y Concesionarias</p>
                        </div>
                        <div style="background-color: #0f172a; border-radius: 8px; padding: 20px; margin-bottom: 24px; border-left: 4px solid #38bdf8;">
                            <h2 style="color: #f1f5f9; margin: 0 0 8px 0; font-size: 18px;">Verificación de Identidad Corporativa</h2>
                            <p style="color: #cbd5e1; margin: 0; font-size: 14px; line-height: 1.5;">
                                Estimado(a) <strong>%s</strong>, se ha solicitado la vinculación y validación institucional para la entidad:
                            </p>
                            <p style="color: #38bdf8; font-size: 16px; font-weight: bold; margin: 8px 0 0 0;">%s</p>
                        </div>
                        <div style="text-align: center; margin: 32px 0;">
                            <p style="color: #94a3b8; font-size: 13px; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 1px;">Tu código de seguridad de 6 dígitos:</p>
                            <div style="display: inline-block; background: linear-gradient(135deg, #0284c7, #2563eb); color: #ffffff; font-size: 36px; font-weight: 800; letter-spacing: 8px; padding: 16px 36px; border-radius: 10px; box-shadow: 0 4px 15px rgba(37,99,235,0.4);">
                                %s
                            </div>
                            <p style="color: #f59e0b; font-size: 13px; margin-top: 12px;">⏰ Válido durante %d minutos (uso único).</p>
                        </div>
                        <div style="border-top: 1px solid #334155; padding-top: 20px; color: #64748b; font-size: 12px; line-height: 1.5;">
                            <p style="margin: 0 0 6px 0;">⚠️ <strong>Aviso de Seguridad:</strong> Si no solicitaste esta vinculación, por favor ignora este correo. Ningún colaborador de SmartFinance Drive te solicitará este código.</p>
                            <p style="margin: 0;">© 2026 SmartFinance Drive Platform. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(greeting, entityName, otpCode, expirationMinutes);
    }
}

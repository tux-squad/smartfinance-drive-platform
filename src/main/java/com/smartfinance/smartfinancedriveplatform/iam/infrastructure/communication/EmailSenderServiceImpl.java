package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import com.smartfinance.smartfinancedriveplatform.iam.application.outboundservices.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Transactional email service implementation for dispatching corporate verification OTP codes.
 */
@Service
public class EmailSenderServiceImpl implements EmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    private final String fromEmail;
    private final boolean allowEmulated;

    @org.springframework.beans.factory.annotation.Autowired
    public EmailSenderServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.allow-emulated:true}") boolean allowEmulated,
            @Value("${spring.mail.username:no-reply@smartfinance.drive.pe}") String fromEmail) {
        this.mailSenderProvider = mailSenderProvider;
        this.allowEmulated = allowEmulated;
        this.fromEmail = fromEmail;
    }

    public EmailSenderServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider, boolean allowEmulated) {
        this(mailSenderProvider, allowEmulated, "no-reply@smartfinance.drive.pe");
    }

    public EmailSenderServiceImpl(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this(mailSenderProvider, true, "no-reply@smartfinance.drive.pe");
    }

    @Override
    public void sendCorporateVerificationOtp(String toEmail, String recipientName, String entityName, String otpCode, int expirationMinutes) {
        log.info("Sending B2B corporate OTP verification code to [{}] for entity [{}] (expires in {}m)",
                toEmail, entityName, expirationMinutes);

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
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
            helper.setSubject("SmartFinance Drive - Código de Verificación Corporativa B2B");

            String htmlBody = buildHtmlBody(recipientName, entityName, otpCode, expirationMinutes);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Verification OTP email successfully dispatched to [{}]", toEmail);
        } catch (MessagingException | RuntimeException e) {
            log.error("Failed to dispatch email via SMTP to [{}]: {}", toEmail, e.getMessage());
            throw new RuntimeException("iam.error.email.dispatchFailed", e);
        }
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

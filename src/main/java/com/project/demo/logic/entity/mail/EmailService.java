package com.project.demo.logic.entity.mail;

import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.Method;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

/**
 * Servicio para el envío de correos electrónicos usando la API de SendGrid.
 * Permite enviar códigos de verificación y otros mensajes transaccionales.
 */
@Service
public class EmailService {
    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    /**
     * Envía un código de verificación al correo electrónico indicado usando SendGrid.
     * @param toEmail Correo destino.
     * @param code Código de verificación a enviar.
     */
    public void sendVerificationCode(String toEmail, String code) {
        Email from = new Email(fromEmail);
        String subject = "Password Reset Verification Code";
        Email to = new Email(toEmail);
        String contentText = "Your verification code is: " + code + "\n\nThis code will expire in 15 minutes.";
        Content content = new Content("text/plain", contentText);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}

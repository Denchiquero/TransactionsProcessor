package com.example.reportservice.service;

import com.example.reportservice.model.EmailRequest;
import com.example.reportservice.model.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendEmail(EmailRequest emailRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailRequest.getTo());
            helper.setFrom(emailRequest.getFrom());
            helper.setSubject(emailRequest.getSubject());

            String htmlContent = generateHtmlContent(emailRequest.getTemplateName(), emailRequest.getVariables());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email успешно отправлен на: {}", emailRequest.getTo());
        } catch (MessagingException e) {
            log.error("Ошибка при отправке email на: {}", emailRequest.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String generateHtmlContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    public void sendOrderConfirmationEmail(OrderDTO order) {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setFrom("karburai.psdnz@mail.ru");
        emailRequest.setTo(order.getCustomerEmail());
        emailRequest.setSubject("Подтверждение заказа #" + order.getOrderId());
        emailRequest.setTemplateName("order-confirmation");
        emailRequest.setVariables(Map.of(
                "order", order,
                "customerName", order.getCustomerName()
        ));

        sendEmail(emailRequest);
        log.info("Email подтверждения заказа отправлен для: {}", order.getOrderId());
    }
}

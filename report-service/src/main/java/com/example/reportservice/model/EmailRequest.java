// [file name]: EmailRequest.java
package com.example.reportservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class EmailRequest {

    @NotBlank(message = "Email получателя обязателен")
    @Email(message = "Некорректный формат email")
    private String to;

    private String from;

    @NotBlank(message = "Тема письма обязательна")
    private String subject;

    private String templateName;

    @NotNull(message = "Данные для шаблона обязательны")
    private Map<String, Object> variables;

    // Конструкторы
    public EmailRequest() {}

    public EmailRequest(String to, String from, String subject, String templateName, Map<String, Object> variables) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.templateName = templateName;
        this.variables = variables;
    }

    // Getters and Setters
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
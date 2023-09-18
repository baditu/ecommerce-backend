package com.ecommerce.ecommercebackend.service;

import com.ecommerce.ecommercebackend.exception.EmailFailureExeception;
import com.ecommerce.ecommercebackend.model.Customer;
import com.ecommerce.ecommercebackend.model.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${email.from}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String url;
    private JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    private SimpleMailMessage makeMailMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromAddress);
        return simpleMailMessage;
    }

    public void sendVerificationEmail(VerificationToken verificationToken) throws EmailFailureExeception {
        SimpleMailMessage message = makeMailMessage();
        message.setTo(verificationToken.getCustomer().getEmail());
        message.setSubject("Verify your email to activate you account.");
        message.setText("Please follow the link below to verify your email. \n" + url + "/auth/verify?token=" + verificationToken.getToken());

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new EmailFailureExeception();
        }
    }

    public void sendPaswordResetEmail(Customer user, String token) throws EmailFailureExeception {
        SimpleMailMessage message = makeMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Your password reset request link.");
        message.setText("You requested a password reset on our webiste. Please " +
                "find the link below to be able to reset your password.\n" +
                "/auth/reset?token=" + token);

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new EmailFailureExeception();
        }
    }
}

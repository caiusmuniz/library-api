package com.example.demo.service.impl;

import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;

    @Value("{application.mail.default-remetent}")
    private String remetent;

    @Override
    public void sendMails(String subject, String message, List<String> mailsList) {
        String[] mails = mailsList.toArray(new String[mailsList.size()]);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(remetent);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setTo(mails);

        javaMailSender.send(mailMessage);
    }
}

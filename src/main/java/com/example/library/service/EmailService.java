package com.example.library.service;

import java.util.List;

public interface EmailService {
    void sendMails(String subject, String message, List<String> mailsList);
}

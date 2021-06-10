package com.example.library.service;

import com.example.library.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private static final String CRON_LATE_LOANS = "0 0 0 1/1 * ?";

    private final LoanService loanService;
    private final EmailService emailService;

    @Value("${application.mail.lateloans.subject}")
    private String subject;

    @Value("${application.mail.lateloans.message}")
    private String message;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendMailToLateLoans() {
        List<Loan> allLateLoans = loanService.getAllLateLoans();
        List<String> mailsList = allLateLoans.stream()
                .map(loan -> loan.getCustomerEmail())
                .collect(Collectors.toList());

        emailService.sendMails(subject, message, mailsList);
    }
}

package com.github.gianlucampos.service;

import com.github.gianlucampos.exception.GmailNotifierException;
import com.github.gianlucampos.notifier.GmailNotifier;
import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GmailService {

    private final GmailNotifier gmailNotifier;

    public void sendEmail() {
        String stockName = "AAPL";
        double stockPrice = 180.25;

        try {
            String subject = MessageFormat.format("SELL {0}", stockName);
            String body = MessageFormat.format("SELL THIS STOCK {0} NOW, THE PRICE IS {1}", stockName, stockPrice);
            gmailNotifier.sendEmail(subject, body);
            log.info("Email sent was sucessfull");
        } catch (Exception e) {
            log.error("Email not sent...", e);
            throw new GmailNotifierException("Email not sent", e);
        }
    }
}

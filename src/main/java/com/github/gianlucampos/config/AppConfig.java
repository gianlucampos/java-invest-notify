package com.github.gianlucampos.config;

import com.github.gianlucampos.auth.GmailOAuth;
import com.github.gianlucampos.notifier.GmailNotifier;
import com.github.gianlucampos.service.GmailService;

public class AppConfig {

    private static final GmailOAuth gmailOAuth;
    private static final GmailNotifier gmailNotifier;
    private static final GmailService gmailService;

    static {
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String refreshToken = System.getenv("REFRESH_TOKEN");
        String emailSender = System.getenv("EMAIL_SENDER");
        String emailReceiver = System.getenv("EMAIL_RECEIVER");

        gmailOAuth = new GmailOAuth(clientId, clientSecret, refreshToken);
        gmailNotifier = new GmailNotifier(emailSender, emailReceiver, gmailOAuth);
        gmailService = new GmailService(gmailNotifier);
    }

    public static GmailService gmailService() {
        return gmailService;
    }
}

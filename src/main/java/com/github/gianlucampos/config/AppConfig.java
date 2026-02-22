package com.github.gianlucampos.config;

import com.github.gianlucampos.auth.GmailOAuth;
import com.github.gianlucampos.notifier.GmailNotifier;
import com.github.gianlucampos.provider.HoldingsProvider;
import com.github.gianlucampos.repository.UsaApiRepository;
import com.github.gianlucampos.repository.UsaApiRepositoryImpl;
import com.github.gianlucampos.service.GmailService;
import com.github.gianlucampos.service.RuleService;

public class AppConfig {

    private static final GmailOAuth gmailOAuth;
    private static final GmailNotifier gmailNotifier;
    private static final GmailService gmailService;
    private static final RuleService ruleService;
    private static final HoldingsProvider holdingsProvider;
    private static final UsaApiRepository usaApiRepository;

    static {
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String refreshToken = System.getenv("REFRESH_TOKEN");
        String emailSender = System.getenv("EMAIL_SENDER");
        String emailReceiver = System.getenv("EMAIL_RECEIVER");
        String urlUsaApi = System.getenv("USA_API_URL");
        String portfolioJson = System.getenv("PORTFOLIO_JSON");

        gmailOAuth = new GmailOAuth(clientId, clientSecret, refreshToken);
        gmailNotifier = new GmailNotifier(emailSender, emailReceiver, gmailOAuth);
        gmailService = new GmailService(gmailNotifier);
        holdingsProvider = new HoldingsProvider(portfolioJson);
        usaApiRepository = new UsaApiRepositoryImpl(urlUsaApi);
        ruleService = new RuleService(gmailService, holdingsProvider, usaApiRepository);
    }

    public static RuleService ruleService() {
        return ruleService;
    }
}

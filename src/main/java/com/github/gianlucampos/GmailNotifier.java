package com.github.gianlucampos;

import com.google.auth.oauth2.UserCredentials;
import com.sun.mail.smtp.SMTPTransport;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GmailNotifier {

    private static final String EMAIL_SENDER = System.getenv("EMAIL_SENDER");
    private static final String EMAIL_RECEIVER = System.getenv("EMAIL_RECEIVER");
    private static final String CLIENT_ID = System.getenv("CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("CLIENT_SECRET");
    private static final String REFRESH_TOKEN = System.getenv("REFRESH_TOKEN");

    public static void main(String[] args) throws Exception {
        String stockName = "AAPL";
        double stockPrice = 180.25;

        UserCredentials creds = UserCredentials.newBuilder()
            .setClientId(CLIENT_ID)
            .setClientSecret(CLIENT_SECRET)
            .setRefreshToken(REFRESH_TOKEN)
            .build();

        String accessToken = creds.refreshAccessToken().getTokenValue();

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_SENDER));
        message.setRecipients(Message.RecipientType.TO, EMAIL_RECEIVER);
        message.setSubject(MessageFormat.format("SELL {0}", stockName));
        message.setText(MessageFormat.format("SELL THIS STOCK {0} NOW, THE PRICE IS {1}", stockName, stockPrice));

        SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
        transport.connect("smtp.gmail.com", EMAIL_SENDER, accessToken);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();

        log.info("Email sent was sucessfull");
    }
}

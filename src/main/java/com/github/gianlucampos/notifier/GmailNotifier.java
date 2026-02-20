package com.github.gianlucampos.notifier;

import com.github.gianlucampos.auth.GmailOAuth;
import com.sun.mail.smtp.SMTPTransport;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GmailNotifier {

    private final String emailSender;
    private final String emailReceiver;
    private final GmailOAuth gmailOAuth;

    public void sendEmail(String subject, String body) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth.mechanisms", "XOAUTH2");

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailSender));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailReceiver));
        message.setSubject(subject);
        message.setText(body);

        SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
        transport.connect("smtp.gmail.com", emailSender, gmailOAuth.getAccessToken());
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}

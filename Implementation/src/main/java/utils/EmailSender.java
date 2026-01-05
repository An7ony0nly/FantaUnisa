package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "fantaunisa23@gmail.com";
    private static final String PASSWORD = "qioasajbdzsxlntw";

    public static void sendVerificationEmail(String toEmail, String token) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Conferma Registrazione FantaUnisa");

            String link = "http://localhost:80/FantaUnisa/ActivationServlet?token=" + token;

            String htmlContent = "<h3>Benvenuto nel Fantacalcio!</h3>"
                    + "<p>Clicca sul link sottostante per attivare il tuo account:</p>"
                    + "<a href='" + link + "'>CONFERMA REGISTRAZIONE</a>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Invio (in un thread separato per non bloccare la pagina)
            new Thread(() -> {
                try {
                    Transport.send(message);
                    System.out.println("Email inviata a: " + toEmail);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
        public static void sendResetEmail(String toEmail, String token) {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", HOST);
            props.put("mail.smtp.port", PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(USERNAME));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Reset Password - FantaUnisa");

                String link = "http://localhost:80/FantaUnisa/ResetPasswordServlet?token=" + token;

                String htmlContent = "<h3>Hai richiesto di reimpostare la password?</h3>"
                        + "<p>Se sei stato tu, clicca qui sotto (il link scade in 30 minuti):</p>"
                        + "<a href='" + link + "'>REIMPOSTA PASSWORD</a>"
                        + "<p>Se non sei stato tu, ignora questa email.</p>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                new Thread(() -> {
                    try {
                        Transport.send(message);
                        System.out.println("Email inviata a: " + toEmail);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }).start();

            } catch (MessagingException e) {
                e.printStackTrace();
            }
    }
}
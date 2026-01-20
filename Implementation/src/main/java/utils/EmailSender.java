package utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
/*+*/
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

    public static void sendSecurityAlert(String toEmail) {
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

            message.setSubject("AVVISO DI SICUREZZA: Password modificata - FantaUnisa");

            String timestamp = java.time.LocalDateTime.now().toString();

            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<h2 style='color: #d9534f;'>Modifica Password Rilevata</h2>"
                    + "<p>Ciao,</p>"
                    + "<p>Ti informiamo che la password del tuo account FantaUnisa associato a <b>" + toEmail + "</b> è stata appena modificata.</p>"
                    + "<p><strong>Data e Ora:</strong> " + timestamp + "</p>"
                    + "<hr>"
                    + "<p style='background-color: #fff3cd; padding: 10px; border-left: 5px solid #ffc107;'>"
                    + "<strong>SE NON SEI STATO TU:</strong><br>"
                    + "Qualcuno potrebbe aver violato il tuo account. Contatta immediatamente l'amministratore o reimposta subito la password tramite la funzione 'Password Dimenticata'."
                    + "</p>"
                    + "<p>Se sei stato tu, puoi ignorare questa email.</p>"
                    + "<p>Il Team di FantaUnisa</p>"
                    + "</div>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            new Thread(() -> {
                try {
                    Transport.send(message);
                    System.out.println("Security Alert inviato a: " + toEmail);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println(" Errore invio Security Alert: " + e.getMessage());
        }
    }
}
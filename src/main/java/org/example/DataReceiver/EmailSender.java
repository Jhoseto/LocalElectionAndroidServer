package org.example.DataReceiver;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.SimpleEmail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailSender{

    private static final String EMAIL_USERNAME = "konstantinse38@gmail.com";
    private static final String EMAIL_PASSWORD = "bsiu sxqx ozgp sxud";

    private static Email configureCommonEmailSettings() {
        Email email = new SimpleEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(587);
        email.setAuthenticator(new DefaultAuthenticator(EMAIL_USERNAME, EMAIL_PASSWORD));
        email.setStartTLSEnabled(true);
        return email;
    }

    // Метод за изпращане на регистрационен имейл
    public static void sendEmail(String to, String subject, String emailBody, Registration registration) {
        try {
            Email email = configureCommonEmailSettings();
            email.setFrom(EMAIL_USERNAME, "Регистрационна форма МЕСТНИ ИЗБОРИ 2023");
            email.setSubject(subject);
            email.setCharset("UTF-8");

            String emailContent = buildEmailContent(registration);
            email.setMsg(emailContent);

            email.addTo(to);
            email.send();

            System.out.println("Имейлът - " + registration.getEmail() + " беше изпратен успешно.");

        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }

    private static String buildEmailContent(Registration registration) {
        return "Вашата регистрация в мобилната платформа за провеждане на местни избори е УСПЕШНА.\n" +
                "Вашите данни са:\n" +
                "Име: " + registration.getUsername() + "\n" +
                "Години: " + registration.getAge() + "\n" +
                "Email: " + registration.getEmail() + "\n" +
                "Парола: " + registration.getPassword() + "\n" +
                "Верификационен КОД: " + registration.getVerificationCode() + "\n" +
                "--------------------------------------------------------\n\n" +
                "За да влезнете в системата използвайте вашият Имейл и Парола.\n" +
                "След това въведете в полето вашият уникален верификационен КОД, с който\n" +
                "ще имате право да упражните правото си на глас САМО ВЕДНЪЖ.\n" +
                "Бъдете разумни,\n" +
                "Вашият глас вече има значение !";
    }

    private static String voteEmailContent() {
        String candidateName = candidateName();
        return String.format(" Вашият глас е подаден успешно в системата за гласуване !\n" +
                "------------------------------------------------------\n" +
                "Избраният от вас кандидат за кмет е: \n"+candidateName+"\n" +
                "Време и Дата на гласуване: "+getCurrentDateTime() );
    }


    public static void sendVoteEmail(String to, String subject, String emailBody) {
        try {
            Email email = configureCommonEmailSettings();
            email.setFrom(EMAIL_USERNAME, "Успешен Вот! МЕСТНИ ИЗБОРИ 2023");
            email.setSubject(subject);
            email.setCharset("UTF-8");

            String emailContent = voteEmailContent();
            email.setMsg(emailContent);

            email.addTo(to);
            email.send();

            System.out.println("Имейлът - " + to + " беше изпратен успешно.");

        } catch (EmailException e) {
            throw new RuntimeException(e);
        }
    }
    private static String candidateName(){
        String candidaName = "";
        int candidaId = MyWebSocketServer.getCandidateId();

        switch (candidaId){
            case 1:
                candidaName = "Михал Камбарев";
                break;
            case 2:
                candidaName = "Николай Мелемов";
                break;
            case 3:
                candidaName = "Стефан Сабрутев";
                break;
        }
        return candidaName;
    }
    public static String getCurrentDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }
}

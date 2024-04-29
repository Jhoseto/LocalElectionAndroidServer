package org.example.DataReceiver;

import java.sql.*;

public class DataReceiver {
    private static final String URL = "jdbc:mysql://localhost:3306/localelection";
    private static final String USER = "Jhose";
    private static final String PASSWORD = "#Pronto123";
    private static String verificationCode;


    public static String getVerificationCode() {
        return verificationCode;
    }

    public static void setVerificationCode(String verificationCode) {
        DataReceiver.verificationCode = verificationCode;
    }

    public static boolean saveRegistration(Registration registration) {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create a connection to the database
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                if (userExists(connection, registration.getUsername(), registration.getEmail())) {
                    return false;
                }

                String sql = "INSERT INTO users_table (username, age, email, password, verificationCode, used) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, registration.getUsername());
                    preparedStatement.setInt(2, registration.getAge());
                    preparedStatement.setString(3, registration.getEmail());
                    preparedStatement.setString(4, registration.getPassword());
                    preparedStatement.setString(5, registration.getVerificationCode());
                    preparedStatement.setInt(6, 0);

                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        EmailSender.sendEmail(
                                registration.getEmail(),
                                "Verification Code",
                                getEmailBody(registration),
                                registration
                        );
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean userExists(Connection connection, String username, String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users_table WHERE username = ? OR email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }

        return false;
    }

    private static String getEmailBody(Registration registration) {
        return "You have successfully registered for the mobile platform for local elections.\n" +
                "Your details:\n" +
                "Name: " + registration.getUsername() + "\n" +
                "Age: " + registration.getAge() + "\n" +
                "Email: " + registration.getEmail() + "\n" +
                "Password: " + registration.getPassword() + "\n" +
                "Verification Code: " + registration.getVerificationCode() + "\n" +
                "--------------------------------------------------------\n\n" +
                "To log in to the system, use your email and password.\n" +
                "Then enter your unique verification code in the field, with which\n" +
                "you will have the right to exercise your voting right ONLY ONCE.\n" +
                "Be smart,\n" +
                "Your vote already matters!";
    }

    public static boolean checkLogin(String email, String password) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String sql = "SELECT * FROM users_table WHERE email = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, email);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            String storedPassword = resultSet.getString("password");

                            boolean isValid = password.equals(storedPassword);
                            System.out.println("Login check result for " + email + ": " + isValid);
                            return isValid;
                        } else {
                            System.out.println("No user found with email: " + email);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String checkVerCode(String verCode) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String sql = "SELECT * FROM users_table WHERE verificationCode = ?";

                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, verCode);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            // Проверка дали кодът е използван (ако "used" е 1)
                            int used = resultSet.getInt("used");

                            if (used == 0) {
                                System.out.println("Код за верификация " + verCode + " не е използван и е валиден.");
                                // Връщане на стойност -10-11 към клиента
                                return "10";
                            } else {
                                System.out.println("Код за верификация " + verCode + " вече е използван.");

                                return "11";
                            }
                        } else {
                            System.out.println("Такъв код не съществува: " + verCode);
                            // Връщане на стойност -0 към клиента
                            return "0";
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        // Връщане на стойност 0 в случай на грешка
        return "verResult:0";
    }

    public static boolean updateVoteResult(String verificationCode, int candidateId) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                String sql = "UPDATE users_table SET used = used + " + candidateId + " WHERE verificationCode = ? AND used = 0";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, verificationCode);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        // Вземане на електронната поща на потребителя


                        return true;
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateCandidateVotes(int candidateId) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Увеличаване броя на гласовете за конкретен кандидат в таблицата "candidates"
            String updateCandidateSql = "UPDATE candidates SET votes = votes + 1 WHERE candidate_id = ?";

            try (PreparedStatement updateCandidateStatement = connection.prepareStatement(updateCandidateSql)) {
                updateCandidateStatement.setInt(1, candidateId);
                int rowsAffected = updateCandidateStatement.executeUpdate();

                if (rowsAffected == 0) {
                    // Неуспешно обновяване на гласовете
                    throw new SQLException("Неуспешно обновяване на гласовете за кандидат с ID: " + candidateId);
                }

                // Успешно обновяване
                String voterEmail = getEmailByVerificationCode(connection, getVerificationCode());



                // Изпращане на имейл
                EmailSender.sendVoteEmail(voterEmail, "Тема на имейла", "Текст на имейла за успешно гласуване");
                System.out.println("Изпратен емайл за успешно гласуване на: " + voterEmail);
                return true;
            }
        } catch (SQLException e) {
            // Обработка на грешката и логиране
            e.printStackTrace();
            return false;
        }
    }
    private static String getEmailByVerificationCode(Connection connection, String verificationCode) throws SQLException {
        String sql = "SELECT email FROM users_table WHERE verificationCode = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, verificationCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("VRASHTANE NA EMAILA " + resultSet.getString("email"));
                    return resultSet.getString("email");
                }
            }
        }
        return null;
    }
}

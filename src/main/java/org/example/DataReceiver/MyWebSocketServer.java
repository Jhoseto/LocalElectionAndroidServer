package org.example.DataReceiver;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.net.InetSocketAddress;


public class MyWebSocketServer extends WebSocketServer {

    private static int candidateId;


    public static int getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public MyWebSocketServer(InetSocketAddress address) {
        super(address);
    }

    public static void main(String[] args) {

        MyWebSocketServer server = new MyWebSocketServer(new InetSocketAddress(2662));
        server.start();
        System.out.println("WebSocket server is running on port 2662");
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        System.out.println("WebSocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {

    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("WebSocket connection opened: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("WebSocket connection closed: " + conn.getRemoteSocketAddress()
                + ", Code: " + code + ", Reason: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("newRegistration")) {
                System.out.println("New registered user received => "+message);
                String[] registrationData = message.split(":");

                String username = registrationData[1];
                int age = Integer.parseInt(registrationData[2]);
                String email = registrationData[3];
                String password = registrationData[4];
                String verificationCode = CodeGenerator.generateCode();

                Registration registration = new Registration(username, age, email, password,verificationCode);

                boolean success = DataReceiver.saveRegistration(registration);

                if (success) {
                    System.out.println("Registration SET in Database successful"+registration);
                    conn.send("registrationResult:-1"); // Изпращане на успешен резултат към клиента
                } else {
                    System.out.println("Registration failed"+registration);
                    conn.send("registrationResult:-0"); // Изпращане на неуспешен резултат към клиента
                }
            } else {

                System.out.println("Received text message: " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error processing received message: " + e.getMessage());
        }

        if (message.startsWith("loginResult:")) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String email = parts[1];
                String password = parts[2];

                boolean isValid = DataReceiver.checkLogin(email, password);

                String responseMessage = "loginResult:" + (isValid ? "-1" : "-0");
                conn.send(responseMessage);
            }
        }

        if (message.startsWith("verResult:")) {
            String[] parts = message.split(":");
            if (parts.length == 2) {
                String verCode = parts[1];

                String result = DataReceiver.checkVerCode(verCode);

                String responseMessage = "verResult:" + result;
                conn.send(responseMessage);
            }
        }

        if (message.startsWith("voteResult:")) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String verificationCode = parts[1];
                int candidateId = Integer.parseInt(parts[2]);
                setCandidateId(candidateId);
                DataReceiver.setVerificationCode(verificationCode);

                //  обновяване на резултата от гласуването на потребилея
                boolean updateSuccess = DataReceiver.updateVoteResult(verificationCode, candidateId);

                //  обновяване на гласовете на кандидатите
                boolean candidateVotesUpdateSuccess = DataReceiver.updateCandidateVotes(candidateId);

                if (updateSuccess && candidateVotesUpdateSuccess) {
                    System.out.println("Успешно обновление на статуса на гласуването в базата данни.");
                } else {
                    System.out.println("Неуспешно обновление на статуса на гласуването в базата данни.");
                }
            }
        }
    }
}

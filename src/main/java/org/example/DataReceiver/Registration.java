package org.example.DataReceiver;
import java.io.Serializable;

public class Registration implements Serializable {

    private String username;
    private int age;
    private String email;
    private String password;
    private String verificationCode;

    public Registration(String username, int age, String email, String password,String verificationCode) {
        setUsername(username);
        setAge(age);
        setEmail(email);
        setPassword(password);
        setVerificationCode(verificationCode);

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

}

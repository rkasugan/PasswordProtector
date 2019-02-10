package com.example.a10010246.passwordprotector;

/**
 * Created by 10010246 on 5/15/2018.
 */
public class PasswordGroup {

    private String title, username, password;

    public PasswordGroup(String title, String username, String password) {
        this.title = title;
        this.username = username;
        this.password = password;
    }

    public String getTitle() {
        return title;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public void setTitle(String changeTo) { title = changeTo; }
    public void setUsername(String changeTo) { username = changeTo; }
    public void setPassword(String changeTo) { password = changeTo; }

    public String toString() {
        return "Title: " + title + ", Username: " + username + ", Password: " + password;
    }
}

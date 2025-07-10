package org.example.model;

import java.util.Arrays;
import java.util.List;

public class User {
    private String username;
    private String email;
    private String[] emailChunks;
    private String password;
    private List<String> emailChunksList;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String[] getEmailChunks() { return emailChunks; }
    public void setEmailChunks(String[] emailChunks) { this.emailChunks = emailChunks; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getEmailChunksAsList() {
        if (emailChunksList == null && emailChunks != null) {
            emailChunksList = Arrays.asList(emailChunks);
        }
        return emailChunksList;
    }

    public void setEmailChunksList(List<String> emailChunksList) {
        this.emailChunksList = emailChunksList;
        if (emailChunksList != null) {
            this.emailChunks = emailChunksList.toArray(new String[0]);
        }
    }
    @Override
    public String toString() {
        return "email='" + this.email + '\'';
    }
}
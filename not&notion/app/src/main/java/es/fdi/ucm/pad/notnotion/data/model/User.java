package es.fdi.ucm.pad.notnotion.data.model;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String email;
    private String username;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private Map<String, Object> preferences;

    // 🔹 Constructor vacío necesario para Firebase
    public User() {}

    public User(String email, String username, Timestamp createdAt, Timestamp lastLogin, String language, String theme) {
        this.email = email;
        this.username = username;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;

        this.preferences = new HashMap<>();
        this.preferences.put("language", language);
        this.preferences.put("theme", theme);
    }

    // 🔹 Getters y Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    public Map<String, Object> getPreferences() { return preferences; }
    public void setPreferences(Map<String, Object> preferences) { this.preferences = preferences; }
}

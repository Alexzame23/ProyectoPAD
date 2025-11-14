package es.fdi.ucm.pad.notnotion.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Note implements Serializable{
    private String id;
    private String title;
    private String content;
    private String folderId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private boolean isFavorite;

    // 🔹 Constructor vacío requerido por Firestore
    public Note() {}

    public Note(String id, String title, String content, String folderId, Timestamp createdAt, Timestamp updatedAt, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.folderId = folderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isFavorite = isFavorite;
    }

    // 🔹 Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
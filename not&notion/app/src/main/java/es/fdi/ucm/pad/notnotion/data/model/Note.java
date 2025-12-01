package es.fdi.ucm.pad.notnotion.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.List;

public class Note implements Serializable {

    private String id;
    private String title;
    private String folderId;
    private transient Timestamp createdAt;
    private transient Timestamp updatedAt;
    private boolean isFavorite;
    private String coverImageUrl;
    private List<ContentBlock> contentBlocks;

    public Note() {}

    public Note(String id, String title, String folderId,
                Timestamp createdAt, Timestamp updatedAt,
                boolean isFavorite, String coverImageUrl,
                List<ContentBlock> contentBlocks) {

        this.id = id;
        this.title = title;
        this.folderId = folderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isFavorite = isFavorite;
        this.coverImageUrl = coverImageUrl;
        this.contentBlocks = contentBlocks;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public List<ContentBlock> getContentBlocks() { return contentBlocks; }
    public void setContentBlocks(List<ContentBlock> contentBlocks) { this.contentBlocks = contentBlocks; }

    public String getContentAsPlainText() {
        StringBuilder sb = new StringBuilder();
        if (contentBlocks != null) {
            for (ContentBlock block : contentBlocks) {
                if (block.getType() == ContentBlock.TYPE_TEXT) {
                    sb.append(block.getTextContent()).append("\n");
                }
            }
        }
        return sb.toString();
    }
}

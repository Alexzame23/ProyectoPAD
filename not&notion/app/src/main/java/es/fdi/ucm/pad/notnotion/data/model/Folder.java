package es.fdi.ucm.pad.notnotion.data.model;

import com.google.firebase.Timestamp;

public class Folder {

    private String id;
    private String name;
    private String parentFolderId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int type;

    public Folder() {}

    public Folder(String id, String name, String parentFolderId,
                  Timestamp createdAt, Timestamp updatedAt, int type) {
        this.id = id;
        this.name = name;
        this.parentFolderId = parentFolderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(String parentFolderId) { this.parentFolderId = parentFolderId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
}

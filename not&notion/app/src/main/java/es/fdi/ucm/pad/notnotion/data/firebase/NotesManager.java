package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.data.model.ContentBlock;

public class NotesManager {

    private static final String TAG = "NotesManager";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public NotesManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getNotesPath(@NonNull String folderId) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return null;
        }
        return "users/" + uid + "/folders/" + folderId + "/notes";
    }

    public void addNote(@NonNull String title,
                        @NonNull String content,
                        @NonNull String folderId,
                        boolean isFavorite) {

        String path = getNotesPath(folderId);
        if (path == null) return;

        String noteId = UUID.randomUUID().toString();

        List<ContentBlock> contentBlocks = new ArrayList<>();

        if (content != null && !content.trim().isEmpty()) {
            ContentBlock textBlock = ContentBlock.createTextBlock(
                    content,
                    ContentBlock.STYLE_NORMAL,
                    16
            );
            contentBlocks.add(textBlock);
        }

        Note note = new Note(
                noteId,
                title,
                folderId,
                Timestamp.now(),
                Timestamp.now(),
                isFavorite,
                null,
                contentBlocks
        );

        db.collection(path)
                .document(noteId)
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota creada correctamente"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear nota", e));
    }

    public void addNoteWithBlocks(@NonNull String title,
                                  String coverImageUrl,
                                  @NonNull List<ContentBlock> contentBlocks,
                                  @NonNull String folderId,
                                  boolean isFavorite) {

        String path = getNotesPath(folderId);
        if (path == null) return;

        String noteId = UUID.randomUUID().toString();

        Note note = new Note(
                noteId,
                title,
                folderId,
                Timestamp.now(),
                Timestamp.now(),
                isFavorite,
                coverImageUrl,
                contentBlocks
        );

        db.collection(path)
                .document(noteId)
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota con bloques creada correctamente"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear nota", e));
    }

    public void updateNote(@NonNull Note note) {
        String path = getNotesPath(note.getFolderId());
        if (path == null) return;

        note.setUpdatedAt(Timestamp.now());

        db.collection(path)
                .document(note.getId())
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota actualizada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar nota", e));
    }

    public void deleteNote(@NonNull String folderId, @NonNull String noteId) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Nota eliminada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar nota", e));
    }

    public void getNotesByFolder(@NonNull String folderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener notas por carpeta", e));
    }

    public void getFavoriteNotes(@NonNull String folderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("isFavorite", true)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener notas favoritas", e));
    }

    public void getAllNotes(OnSuccessListener<QuerySnapshot> listener) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return;
        }

        db.collectionGroup("notes")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener todas las notas", e));
    }

    public void countNotesInFolder(@NonNull String folderId,
                                   @NonNull OnSuccessListener<Integer> listener) {

        String path = getNotesPath(folderId);
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(q -> listener.onSuccess(q.size()))
                .addOnFailureListener(e -> Log.e(TAG, "Error contando notas", e));
    }
}

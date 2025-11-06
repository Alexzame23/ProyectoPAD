package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

import es.fdi.ucm.pad.notnotion.data.model.Note;

public class NotesManager {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public NotesManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserNotesPath() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e("Firestore", "No hay usuario autenticado");
            return null;
        }
        return "users/" + uid + "/notes";
    }

    /**
     * Inserta una nueva nota en Firestore
     */
    public void addNote(@NonNull String title, @NonNull String content, @NonNull String folderId, boolean isFavorite) {
        String path = getUserNotesPath();
        if (path == null) return;

        String noteId = UUID.randomUUID().toString(); // genera un ID único

        Note note = new Note(
                noteId,
                title,
                content,
                folderId,
                Timestamp.now(),
                Timestamp.now(),
                isFavorite
        );

        db.collection(path)
                .document(noteId)
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Nota creada correctamente"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al crear nota", e));
    }

    /**
     * Actualiza una nota existente
     */
    public void updateNote(@NonNull Note note) {
        String path = getUserNotesPath();
        if (path == null) return;

        note.setUpdatedAt(Timestamp.now());

        db.collection(path)
                .document(note.getId())
                .set(note)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Nota actualizada"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar nota", e));
    }

    /**
     * Elimina una nota
     */
    public void deleteNote(String noteId) {
        String path = getUserNotesPath();
        if (path == null) return;

        db.collection(path)
                .document(noteId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Nota eliminada"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al eliminar nota", e));
    }

    /**
     * Obtiene todas las notas del usuario
     */
    public void getAllNotes(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserNotesPath();
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener notas", e));
    }

    /**
     * Obtiene todas las notas de una carpeta específica
     */
    public void getNotesByFolder(String folderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserNotesPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("folderId", folderId)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener notas por carpeta", e));
    }

    /**
     * Obtiene las notas marcadas como favoritas
     */
    public void getFavoriteNotes(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserNotesPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("isFavorite", true)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener notas favoritas", e));
    }
}

package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

import es.fdi.ucm.pad.notnotion.data.model.Folder;

public class FoldersManager {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FoldersManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserFoldersPath() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e("Firestore", "No hay usuario autenticado");
            return null;
        }
        return "users/" + uid + "/folders";
    }

    /**
     * Inserta una nueva carpeta
     */
    public void addFolder(@NonNull String name, String parentFolderId, int type) {
        String path = getUserFoldersPath();
        if (path == null) return;

        String folderId = UUID.randomUUID().toString();

        Folder folder = new Folder(
                folderId,
                name,
                parentFolderId, // puede ser null
                Timestamp.now(),
                Timestamp.now(),
                type
        );

        db.collection(path)
                .document(folderId)
                .set(folder)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Carpeta creada correctamente"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al crear carpeta", e));
    }

    /**
     * Actualiza una carpeta existente
     */
    public void updateFolder(@NonNull Folder folder) {
        String path = getUserFoldersPath();
        if (path == null) return;

        folder.setUpdatedAt(Timestamp.now());

        db.collection(path)
                .document(folder.getId())
                .set(folder)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Carpeta actualizada"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar carpeta", e));
    }

    /**
     * Elimina una carpeta
     */
    public void deleteFolder(String folderId) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .document(folderId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Carpeta eliminada"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al eliminar carpeta", e));
    }

    /**
     * Obtiene todas las carpetas del usuario
     */
    public void getAllFolders(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener carpetas", e));
    }

    /**
     * Obtiene todas las subcarpetas de una carpeta específica
     */
    public void getSubfolders(String parentFolderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("parentFolderId", parentFolderId)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener subcarpetas", e));
    }

    /**
     * Obtiene todas las carpetas raíz (sin padre)
     */
    public void getRootFolders(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("parentFolderId", null)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener carpetas raíz", e));
    }
}

package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import es.fdi.ucm.pad.notnotion.data.model.Folder;

public class FoldersManager {

    private static final String TAG = "FoldersManager";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FoldersManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserFoldersPath() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return null;
        }
        return "users/" + uid + "/folders";
    }

    public void createFolder(
            @NonNull String name,
            @NonNull String parentFolderId,
            int type,
            Runnable onSuccess
    ) {
        String path = getUserFoldersPath();
        if (path == null) return;
        String folderId = UUID.randomUUID().toString();
        Folder folder = new Folder(
                folderId,
                name,
                parentFolderId,
                Timestamp.now(),
                Timestamp.now(),
                type
        );

        db.collection(path)
                .document(folderId)
                .set(folder)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Carpeta creada correctamente: " + name);
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al crear carpeta", e));
    }
    public void createFolder(
            @NonNull String name,
            @NonNull String parentFolderId,
            @NonNull Runnable onSuccess
    ) {
        createFolder(name, parentFolderId, 0, onSuccess);
    }
    // -----------------------------------------------------------
    // ✔ Actualizar carpeta (sin cambios)
    // -----------------------------------------------------------
    public void updateFolder(@NonNull Folder folder) {
        String path = getUserFoldersPath();
        if (path == null) return;

        folder.setUpdatedAt(Timestamp.now());

        db.collection(path)
                .document(folder.getId())
                .set(folder)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Carpeta actualizada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar carpeta", e));
    }

    // -----------------------------------------------------------
    // ✔ Eliminar carpeta
    // -----------------------------------------------------------
    public void deleteFolder(String folderId) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .document(folderId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Carpeta eliminada"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar carpeta", e));
    }

    // -----------------------------------------------------------
    // ✔ Obtener TODAS las carpetas del usuario (sin cambios)
    // -----------------------------------------------------------
    public void getAllFolders(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener carpetas", e));
    }

    // -----------------------------------------------------------
    // ✔ Obtener subcarpetas usando "None" como raíz
    //   (MODIFICADO)
    // -----------------------------------------------------------
    public void getSubfolders(String parentFolderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("parentFolderId",
                        (parentFolderId == null || parentFolderId.equals("")) ? "None" : parentFolderId)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener subcarpetas", e));
    }

    // -----------------------------------------------------------
    // ✔ Obtener carpetas raíz → ahora compatibles con "None"
    // -----------------------------------------------------------
    public void getRootFolders(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("parentFolderId", "None")
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener carpetas raíz", e));
    }
}

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

    public void createFolder(@NonNull String name,
                             @NonNull String parentFolderId,
                             int type,
                             Runnable onSuccess) {

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
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al crear carpeta", e));
    }

    public void createFolder(@NonNull String name,
                             @NonNull String parentFolderId,
                             @NonNull Runnable onSuccess) {
        createFolder(name, parentFolderId, 0, onSuccess);
    }

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

    public void deleteFolder(String folderId) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .document(folderId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Carpeta eliminado"))
                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar carpeta", e));
    }

    public void getAllFolders(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener carpetas", e));
    }

    public void getSubfolders(String parentFolderId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        String parentId = (parentFolderId == null || parentFolderId.isEmpty())
                ? "None"
                : parentFolderId;

        db.collection(path)
                .whereEqualTo("parentFolderId", parentId)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener subcarpetas", e));
    }

    public void getRootFolders(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("parentFolderId", "None")
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener carpetas raíz", e));
    }

    public void countSubfolders(@NonNull String parentFolderId,
                                @NonNull OnSuccessListener<Integer> listener) {
        String path = getUserFoldersPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("parentFolderId", parentFolderId)
                .get()
                .addOnSuccessListener(q -> listener.onSuccess(q.size()))
                .addOnFailureListener(e -> Log.e(TAG, "Error contando subcarpetas", e));
    }

    public void deleteFolderRecursively(@NonNull String folderId, @NonNull Runnable onComplete) {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return;
        }

        String foldersPath = "users/" + uid + "/folders";
        String notesPath = "users/" + uid + "/folders/" + folderId + "/notes";

        db.collection(notesPath)
                .get()
                .addOnSuccessListener(notesSnapshot -> {
                    for (QueryDocumentSnapshot doc : notesSnapshot) {
                        doc.getReference().delete();
                    }

                    db.collection(foldersPath)
                            .whereEqualTo("parentFolderId", folderId)
                            .get()
                            .addOnSuccessListener(subfoldersSnapshot -> {
                                if (subfoldersSnapshot.isEmpty()) {
                                    db.collection(foldersPath)
                                            .document(folderId)
                                            .delete()
                                            .addOnSuccessListener(a -> onComplete.run());
                                    return;
                                }

                                final int total = subfoldersSnapshot.size();
                                final int[] completed = {0};

                                for (QueryDocumentSnapshot doc : subfoldersSnapshot) {
                                    String childFolderId = doc.getId();

                                    deleteFolderRecursively(childFolderId, () -> {
                                        completed[0]++;
                                        if (completed[0] == total) {
                                            db.collection(foldersPath)
                                                    .document(folderId)
                                                    .delete()
                                                    .addOnSuccessListener(a -> onComplete.run());
                                        }
                                    });
                                }
                            });
                });
    }
}

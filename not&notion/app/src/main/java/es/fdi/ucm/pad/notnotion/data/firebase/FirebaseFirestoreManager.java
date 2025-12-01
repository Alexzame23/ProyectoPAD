package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.User;

public class FirebaseFirestoreManager {

    private static final String TAG = "FirestoreManager";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirebaseFirestoreManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void saveOrUpdateUser(@NonNull FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        Map<String, Object> data = new HashMap<>();
        data.put("email", firebaseUser.getEmail());
        data.put("username", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Sin nombre");
        data.put("lastLogin", Timestamp.now());

        userRef.get().addOnSuccessListener(document -> {
            if (!document.exists()) {
                data.put("createdAt", Timestamp.now());
                Map<String, Object> preferences = new HashMap<>();
                preferences.put("language", "es");
                preferences.put("theme", "light");
                data.put("preferences", preferences);
            }

            userRef.set(data)
                    .addOnSuccessListener(aVoid ->
                            Log.d(TAG, "Usuario " + uid + " guardado/actualizado correctamente"))
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error al guardar usuario", e));
        });
    }

    public void getCurrentUserData(OnSuccessListener<User> listener) {
        String uid = getUserId();
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            listener.onSuccess(null);
            return;
        }

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        listener.onSuccess(user);
                    } else {
                        Log.w(TAG, "El documento del usuario no existe");
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener usuario", e);
                    listener.onSuccess(null);
                });
    }

    public void updateUserPreferences(String language, String theme) {
        String uid = getUserId();
        if (uid == null) {
            Log.e(TAG, "No hay usuario autenticado");
            return;
        }

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("language", language);
        prefs.put("theme", theme);

        db.collection("users").document(uid)
                .update("preferences", prefs)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Preferencias actualizadas correctamente"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error al actualizar preferencias", e));
    }

    public void initializeUserStructure(@NonNull FirebaseUser firebaseUser, @NonNull Runnable onComplete) {
        String uid = firebaseUser.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        Map<String, Object> data = new HashMap<>();
        data.put("email", firebaseUser.getEmail());
        data.put("username", firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Sin nombre");
        data.put("createdAt", Timestamp.now());
        data.put("lastLogin", Timestamp.now());

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("language", "es");
        preferences.put("theme", "light");
        data.put("preferences", preferences);

        userRef.set(data).addOnSuccessListener(aVoid -> {
            String folderId = "root";

            Folder rootFolder = new Folder(
                    folderId,
                    "Root",
                    "None",
                    Timestamp.now(),
                    Timestamp.now(),
                    0
            );

            userRef.collection("folders")
                    .document(folderId)
                    .set(rootFolder)
                    .addOnSuccessListener(aVoid2 -> {
                        NotesManager notesManager = new NotesManager();
                        notesManager.addNote(
                                "Nota de ejemplo",
                                "prueba",
                                folderId,
                                false
                        );

                        Log.d(TAG, "Usuario, carpeta root y nota inicial creados");
                        onComplete.run();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error creando carpeta", e));
        });
    }
}

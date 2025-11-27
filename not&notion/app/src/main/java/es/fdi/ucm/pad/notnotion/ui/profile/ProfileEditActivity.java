package es.fdi.ucm.pad.notnotion.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import es.fdi.ucm.pad.notnotion.R;

public class ProfileEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageView imgProfilePhotoEdit;
    private Button btnChangePhoto, btnSaveChanges;
    private EditText etName, etCurrentPassword, etNewPassword, etRepeatPassword;

    private Uri selectedImageUri = null;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // UI
        imgProfilePhotoEdit = findViewById(R.id.imgProfilePhotoEdit);
        btnChangePhoto      = findViewById(R.id.btnChangePhoto);
        btnSaveChanges      = findViewById(R.id.btnSaveChanges);
        etName              = findViewById(R.id.etName);
        etCurrentPassword   = findViewById(R.id.etCurrentPassword);
        etNewPassword       = findViewById(R.id.etNewPassword);
        etRepeatPassword    = findViewById(R.id.etRepeatPassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cargar nombre desde Firestore
        String uid = user.getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getString("username") != null) {
                        etName.setText(doc.getString("username"));
                    } else if (user.getDisplayName() != null) {
                        etName.setText(user.getDisplayName());
                    }
                });

        // Cargar foto (de Auth)
        Uri photoUri = user.getPhotoUrl();
        if (photoUri != null) {
            Picasso.get()
                    .load(photoUri)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(imgProfilePhotoEdit);
        } else {
            imgProfilePhotoEdit.setImageResource(R.drawable.ic_user);
        }

        // Cambiar foto
        btnChangePhoto.setOnClickListener(v -> openImagePicker());

        // Guardar cambios
        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    // ============================================
    //           SELECCIÓN DE IMAGEN
    // ============================================

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgProfilePhotoEdit.setImageURI(selectedImageUri);
        }
    }

    // ============================================
    //           GUARDAR CAMBIOS
    // ============================================

    private void saveChanges() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etName.getText().toString().trim();
        String currentPass = etCurrentPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String repeatPass = etRepeatPassword.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean wantsPasswordChange =
                !currentPass.isEmpty() || !newPass.isEmpty() || !repeatPass.isEmpty();

        if (wantsPasswordChange) {
            // Validaciones de contraseña
            if (currentPass.isEmpty() || newPass.isEmpty() || repeatPass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos de contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(repeatPass)) {
                Toast.makeText(this, "Las contraseñas nuevas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            reauthenticateAndUpdate(user, newName, currentPass, newPass);
        } else {
            // Solo nombre y/o foto
            updateProfileData(user, newName, null);
        }
    }

    // ============================================
    //     REAUTENTICACIÓN + CAMBIO CONTRASEÑA
    // ============================================

    private void reauthenticateAndUpdate(FirebaseUser user, String newName,
                                         String currentPass, String newPass) {

        String email = user.getEmail();
        if (email == null) {
            Toast.makeText(this, "No se puede reautenticar sin email", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSaveChanges.setEnabled(false);

        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPass))
                .addOnSuccessListener(unused -> updateProfileData(user, newName, newPass))
                .addOnFailureListener(e -> {
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================
    //     ACTUALIZAR NOMBRE / FOTO / PASSWORD
    // ============================================

    private void updateProfileData(FirebaseUser user, String newName, @Nullable String newPass) {

        String uid = user.getUid();

        // 1) Subir foto si se ha elegido una
        if (selectedImageUri != null) {
            StorageReference ref = storage.getReference()
                    .child("profile_photos/" + uid + ".jpg");

            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri ->
                                    applyFinalUpdates(user, newName, newPass, uri.toString())))
                    .addOnFailureListener(e -> {
                        btnSaveChanges.setEnabled(true);
                        Toast.makeText(this, "Error subiendo la foto", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Sin cambio de foto
            applyFinalUpdates(user, newName, newPass, null);
        }
    }

    private void applyFinalUpdates(FirebaseUser user, String newName,
                                   @Nullable String newPass, @Nullable String photoUrl) {

        String uid = user.getUid();

        // --- Actualizar perfil de FirebaseAuth ---
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName);

        if (photoUrl != null) {
            builder.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(builder.build())
                .addOnSuccessListener(unused -> {

                    // --- Actualizar Firestore ---
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("username", newName);
                    if (photoUrl != null) {
                        updates.put("photoUrl", photoUrl);
                    }

                    db.collection("users").document(uid)
                            .update(updates)
                            .addOnSuccessListener(unused2 -> {

                                // --- Cambiar contraseña si procede ---
                                if (newPass != null && !newPass.isEmpty()) {
                                    user.updatePassword(newPass)
                                            .addOnSuccessListener(unused3 -> {
                                                btnSaveChanges.setEnabled(true);
                                                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                btnSaveChanges.setEnabled(true);
                                                Toast.makeText(this, "Error actualizando contraseña", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    btnSaveChanges.setEnabled(true);
                                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                            })
                            .addOnFailureListener(e -> {
                                btnSaveChanges.setEnabled(true);
                                Toast.makeText(this, "Error actualizando Firestore", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    btnSaveChanges.setEnabled(true);
                    Toast.makeText(this, "Error actualizando perfil", Toast.LENGTH_SHORT).show();
                });
    }
}

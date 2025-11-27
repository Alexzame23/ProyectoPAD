package es.fdi.ucm.pad.notnotion.ui.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfilePhoto;
    private TextView tvEmail, tvName, tvUid;
    private Button btnLogout, btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- Referencias UI ---
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        tvEmail         = findViewById(R.id.tvEmail);
        tvName          = findViewById(R.id.tvName);
        tvUid           = findViewById(R.id.tvUid);
        btnLogout       = findViewById(R.id.btnLogout);
        btnEditProfile  = findViewById(R.id.btnEditProfile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Si no hay usuario → login
        if (user == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Cargar info inicial
        updateUserUI(user);

        // Logout
        btnLogout.setOnClickListener(v -> logout());

        // Botón editar perfil
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileEditActivity.class);
            startActivity(intent);
        });

        // Botón atrás
        findViewById(R.id.btnGoBackCalendar).setOnClickListener(v -> finish());
    }


    // ********************************************************************
    //   ACTUALIZA LA UI CON LOS DATOS DEL USUARIO
    // ********************************************************************
    private void updateUserUI(FirebaseUser user) {
        tvEmail.setText("Correo: " + user.getEmail());
        tvName.setText("Nombre: " + (user.getDisplayName() != null ?
                user.getDisplayName() : "No disponible"));
        tvUid.setText("UID: " + user.getUid());

        // Foto
        Uri photoUri = user.getPhotoUrl();
        if (photoUri != null) {
            Picasso.get()
                    .load(photoUri)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .into(imgProfilePhoto);
        } else {
            imgProfilePhoto.setImageResource(R.drawable.ic_user);
        }
    }


    // ********************************************************************
    //   LOGOUT
    // ********************************************************************
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this); // Cierra sesión Google
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

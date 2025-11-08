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
    private TextView tvEmail, tvName, tvUid, tvVerified;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);
        tvUid = findViewById(R.id.tvUid);
        tvVerified = findViewById(R.id.tvVerified);
        btnLogout = findViewById(R.id.btnLogout);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvEmail.setText("Correo: " + user.getEmail());
            tvName.setText("Nombre: " + (user.getDisplayName() != null ? user.getDisplayName() : "No disponible"));
            tvUid.setText("UID: " + user.getUid());
            tvVerified.setText(user.isEmailVerified() ? "Correo verificado ✅" : "Correo no verificado ❌");

            Uri photoUri = user.getPhotoUrl();
            if (photoUri != null) {
                Picasso.get()
                        .load(photoUri)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(imgProfilePhoto);
            }
        } else {
            Toast.makeText(this, "No hay usuario activo", Toast.LENGTH_SHORT).show();
        }

        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

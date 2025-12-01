package es.fdi.ucm.pad.notnotion.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;
import es.fdi.ucm.pad.notnotion.utils.UserProfileHelper;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfilePhoto;
    private TextView tvEmail, tvName, tvUid;
    private Button btnLogout, btnEditProfile;
    private UserProfileHelper profileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);
        tvUid = findViewById(R.id.tvUid);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No hay usuario logueado.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        profileHelper = new UserProfileHelper();

        tvEmail.setText("Correo: " + user.getEmail());
        tvUid.setText("UID: " + user.getUid());

        profileHelper.applyToViews(tvName, imgProfilePhoto);

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileEditActivity.class))
        );

        btnLogout.setOnClickListener(v -> logout());

        findViewById(R.id.btnGoBackCalendar).setOnClickListener(v -> finish());
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

package es.fdi.ucm.pad.notnotion.ui.user_logging;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    // 1) CREA EL LAUNCHER USANDO EL CONTRATO DE FIREBASEUI
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(
                    new FirebaseAuthUIActivityResultContract(),
                    this::onSignInResult
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Si ya hay un usuario autenticado, ir directamente al MainActivity
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            goToMainActivity();
        } else {
            handleEmailLinkIntent(); // Por si abrimos la app desde un enlace de correo
            launchEmailLinkSignIn(); // Lanzamos el flujo de login
        }
    }
    private void launchEmailLinkSignIn() {

        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        "es.fdi.ucm.pad.notnotion",
                        true,   // instalar si no está
                        null)
                .setHandleCodeInApp(true) // importante
                .setUrl("https://padm-84832fcd.web.app/emailSignIn")
                .build();

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder()
                        .enableEmailLinkSignIn()
                        .setActionCodeSettings(actionCodeSettings)
                        .build()
        );

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground) // opcional
                .setTheme(R.style.Base_Theme_Notnotion)     // opcional
                .setIsSmartLockEnabled(false)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private void handleEmailLinkIntent() {
        if (AuthUI.canHandleIntent(getIntent())) {
            if (getIntent().getExtras() != null) {
                String link = getIntent().getExtras().getString("email_link_sign_in");
                if (link != null) {
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build()
                    );

                    Intent signInIntent = AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build();

                    signInLauncher.launch(signInIntent);
                }
            }
        }
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        int resultCode = result.getResultCode();
        IdpResponse response = result.getIdpResponse();

        if (resultCode == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            goToMainActivity();
        } else {
            if (response == null) {
                System.out.println("Inicio de sesión cancelado por el usuario.");
            } else {
                System.out.println("Error de autenticación: " + response.getError());
            }
        }
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

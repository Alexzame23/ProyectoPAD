package es.fdi.ucm.pad.notnotion.ui.user_register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        TextView goToLogin = findViewById(R.id.tv_go_to_login);
        goToLogin.setOnClickListener(v -> goToLoginActivity());

        ImageButton btnLanguage = findViewById(R.id.btn_language);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // opcional: cierra la pantalla de registro
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(es.fdi.ucm.pad.notnotion.utils.LocaleHelper.applyLocale(newBase));
    }

    private void showLanguageDialog() {
        final String[] languages = {"Español", "English"};
        final String[] codes = {"es", "en"};

        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_language))
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = codes[which];
                    es.fdi.ucm.pad.notnotion.utils.LocaleHelper.setLocale(this, selectedLang);
                    recreate(); // Recarga la actividad con el nuevo idioma
                })
                .show();
    }
}

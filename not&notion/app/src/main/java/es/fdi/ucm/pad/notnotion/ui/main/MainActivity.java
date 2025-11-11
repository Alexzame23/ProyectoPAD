package es.fdi.ucm.pad.notnotion.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.auth.AuthUI;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.calendar.CalendarFragment;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Contenedor para cargar las pantallas principales
        FrameLayout contentContainer = findViewById(R.id.contentContainer);
        if (contentContainer != null) {
            getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);

            // Ajuste para pantallas Edge-to-Edge
            ViewCompat.setOnApplyWindowInsetsListener(contentContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }

        // BottomNavigationView (solo si existe en el layout)
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null && contentContainer != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                contentContainer.removeAllViews();

                if (id == R.id.nav_notes) {
                    getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);
                } else if (id == R.id.nav_calendar) {
                    getLayoutInflater().inflate(R.layout.calendar_main, contentContainer, true);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.contentContainer, new CalendarFragment())
                            .commit();
                }
                return true;
            });
        }

        // Firebase y botón de perfil
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ImageButton btnPerfil = findViewById(R.id.btnPerfil);
        ImageButton btnNotes = findViewById(R.id.btnNotes);
        ImageButton btnCalendar = findViewById(R.id.btnCalendar);
        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Uri photoUri = user.getPhotoUrl();
                Picasso.get()
                        .load(photoUri)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(btnPerfil);
            }

            // Menú del perfil
            btnPerfil.setOnClickListener(v -> showProfileMenu(btnPerfil));
        } else {
            // Si no hay usuario logueado, mandamos a LoginActivity
            btnPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
       if(btnNotes!=null){
           btnNotes.setOnClickListener(v -> {
               // Limpiamos el FrameLayout
               contentContainer.removeAllViews();
               // Inflamos la pantalla de Notes
               getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);
           });
       }
        if(btnCalendar!=null){
            btnCalendar.setOnClickListener(v -> {
                // Limpiamos el FrameLayout
                contentContainer.removeAllViews();
                // Inflamos la pantalla de Notes
                getLayoutInflater().inflate(R.layout.calendar_main, contentContainer, true);
            });
        }
    }


    private void showProfileMenu(ImageButton anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Cerrar sesión"); // Solo una opción por ahora

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Cerrar sesión")) {
                logout();
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this); // Por si usó Google Sign-In
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ya no hacemos signOut automático
    }
}

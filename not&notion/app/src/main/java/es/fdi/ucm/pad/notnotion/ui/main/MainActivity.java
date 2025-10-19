package es.fdi.ucm.pad.notnotion.ui.main;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import es.fdi.ucm.pad.notnotion.R;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;      // ✅ Variable global

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajuste para pantallas Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        /*
        Esto para hacer que el recyclerView muestre los items de 3 en 3
        recyclerView = findViewById(R.id.recyclerItems);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
         */
    }
}
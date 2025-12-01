package es.fdi.ucm.pad.notnotion.ui.main;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.adapter.FoldersAdapter;
import es.fdi.ucm.pad.notnotion.data.adapter.NotesAdapter;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.firebase.FirebaseFirestoreManager;
import es.fdi.ucm.pad.notnotion.data.firebase.FoldersManager;
import es.fdi.ucm.pad.notnotion.data.firebase.NotesManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.data.model.Folder;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.data.model.User;
import es.fdi.ucm.pad.notnotion.ui.Fragments.CalendarFragment;
import es.fdi.ucm.pad.notnotion.ui.Fragments.EditNoteActivity;
import es.fdi.ucm.pad.notnotion.ui.profile.ProfileActivity;
import es.fdi.ucm.pad.notnotion.ui.user_logging.LoginActivity;
import es.fdi.ucm.pad.notnotion.utils.UserProfileHelper;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestoreManager firestoreManager;
    private FoldersManager foldersManager;
    private NotesManager notesManager;

    private User currentUser;
    private Folder currentFolder;

    private FoldersAdapter foldersAdapter;
    private NotesAdapter notesAdapter;

    private RecyclerView recyclerFolders;
    private RecyclerView recyclerNotes;

    private final List<String> routePath = new ArrayList<>();
    private final List<Folder> navigationStack = new ArrayList<>();

    private EditText busquedaBarra;
    private TextView routeTextShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FrameLayout contentContainer = findViewById(R.id.contentContainer);
        FirebaseFirestore.setLoggingEnabled(true);

        firestoreManager = new FirebaseFirestoreManager();
        foldersManager = new FoldersManager();
        notesManager = new NotesManager();

        getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);

        busquedaBarra = contentContainer.findViewById(R.id.busquedaBarra);

        recyclerFolders = contentContainer.findViewById(R.id.recyclerFolders);
        recyclerNotes = contentContainer.findViewById(R.id.recyclerNotes);

        foldersAdapter = new FoldersAdapter();
        notesAdapter = new NotesAdapter();

        notesAdapter.setOnNoteClickListener(this::mostrarPantallaEdicion);

        recyclerFolders.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerNotes.setLayoutManager(new GridLayoutManager(this, 3));

        recyclerFolders.setAdapter(foldersAdapter);
        recyclerNotes.setAdapter(notesAdapter);

        ImageButton btnGoBack = contentContainer.findViewById(R.id.btnGoBack);
        if (btnGoBack != null) btnGoBack.setOnClickListener(v -> goBack());

        ImageButton btnAddNote = contentContainer.findViewById(R.id.btnAddNote);
        if (btnAddNote != null) {
            btnAddNote.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(MainActivity.this, btnAddNote);
                popup.getMenu().add("Nueva carpeta");
                popup.getMenu().add("Nueva nota");

                popup.setOnMenuItemClickListener(itm -> {
                    if (itm.getTitle().equals("Nueva carpeta")) {
                        createNewFolderDialog();
                        return true;
                    }
                    if (itm.getTitle().equals("Nueva nota")) {
                        createNewNoteDialog();
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }

        ImageButton btnPerfil = findViewById(R.id.btnPerfil);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            firestoreManager.getCurrentUserData(userData -> currentUser = userData);

            UserProfileHelper helper = new UserProfileHelper();
            helper.loadUserPhotoInto(btnPerfil);

            btnPerfil.setOnClickListener(v ->
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        } else {
            btnPerfil.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            });
        }

        currentFolder = new Folder("root", "Root", "None", null, null, 0);
        loadFolderContent(currentFolder);

        foldersAdapter.setOnFolderClickListener(folder -> {
            navigationStack.add(currentFolder);
            loadFolderContent(folder);
        });

        foldersAdapter.setOnFolderLongClickListener((folder, view) -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, view);
            popup.getMenu().add(0, 0, 0, "Renombrar");
            popup.getMenu().add(0, 1, 1, "Eliminar");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    confirmRenameFolder(folder);
                    return true;
                }
                if (item.getItemId() == 1) {
                    confirmDeleteFolder(folder);
                    return true;
                }
                return false;
            });

            popup.show();
        });

        notesAdapter.setOnNoteLongClickListener((note, view) -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, view);
            popup.getMenu().add(0, 0, 0, "Renombrar");
            popup.getMenu().add(0, 1, 1, "Asociar a fecha");
            popup.getMenu().add(0, 2, 2, "Eliminar");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    confirmRenameNote(note);
                    return true;
                }
                if (item.getItemId() == 1) {
                    abrirSelectorFecha(note);
                    return true;
                }
                if (item.getItemId() == 2) {
                    confirmDeleteNote(note);
                    return true;
                }
                return false;
            });

            popup.show();
        });

        NavigationBarView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {

            contentContainer.removeAllViews();

            if (item.getItemId() == R.id.nav_notes) {
                volverAlExploradorConUI();
            } else if (item.getItemId() == R.id.nav_calendar) {
                getLayoutInflater().inflate(R.layout.calendar_main, contentContainer, true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.contentContainer, new CalendarFragment())
                        .commit();
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentFolder != null) {
            loadFolderContent(currentFolder);
        }
    }

    private void abrirSelectorFecha(Note note) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dateDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timeDialog = new TimePickerDialog(
                            this,
                            (timeView, hour, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                long millis = calendar.getTimeInMillis();
                                crearEventoDesdeNota(note, millis);
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timeDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dateDialog.show();
    }

    private void crearEventoDesdeNota(Note note, long millis) {
        Timestamp ts = new Timestamp(new java.util.Date(millis));
        CalendarEventsManager evManager = new CalendarEventsManager();

        evManager.addEvent(
                note.getTitle(),
                "",
                ts,
                ts,
                note.getId(),
                0,
                false,
                null
        );
        Toast.makeText(this, "Nota asociada al calendario", Toast.LENGTH_SHORT).show();
    }

    private void createNewNoteDialog() {
        if (currentFolder == null) return;

        final EditText input = new EditText(this);
        input.setHint("Título de la nota");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Crear nueva nota")
                .setView(input)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String noteTitle = input.getText().toString().trim();
                    if (noteTitle.isEmpty()) noteTitle = "Nueva nota";

                    notesManager.addNote(noteTitle, "", currentFolder.getId(), false);
                    loadFolderContent(currentFolder);
                    Toast.makeText(this, "Nota creada correctamente", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarPantallaEdicion(Note note) {
        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.EXTRA_NOTE, note);
        intent.putExtra(EditNoteActivity.EXTRA_FOLDER_ID, currentFolder.getId());
        startActivity(intent);
    }

    private void volverAlExploradorConUI() {
        FrameLayout contentContainer = findViewById(R.id.contentContainer);
        contentContainer.removeAllViews();
        getLayoutInflater().inflate(R.layout.notes_main, contentContainer, true);

        busquedaBarra = contentContainer.findViewById(R.id.busquedaBarra);
        busquedaBarra.setText("");
        String query = busquedaBarra.getText().toString().trim();

        recyclerFolders = contentContainer.findViewById(R.id.recyclerFolders);
        recyclerNotes = contentContainer.findViewById(R.id.recyclerNotes);

        recyclerFolders.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerNotes.setLayoutManager(new GridLayoutManager(this, 3));

        recyclerFolders.setAdapter(foldersAdapter);
        recyclerNotes.setAdapter(notesAdapter);

        foldersAdapter.setOnFolderClickListener(folder -> {
            navigationStack.add(currentFolder);
            loadFolderContent(folder);
        });

        notesAdapter.setOnNoteClickListener(this::mostrarPantallaEdicion);

        ImageButton btnGoBack = contentContainer.findViewById(R.id.btnGoBack);
        if (btnGoBack != null) btnGoBack.setOnClickListener(v -> goBack());

        ImageButton btnAddNote = contentContainer.findViewById(R.id.btnAddNote);
        if (btnAddNote != null) {
            btnAddNote.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(MainActivity.this, btnAddNote);
                popup.getMenu().add("Nueva carpeta");
                popup.getMenu().add("Nueva nota");

                popup.setOnMenuItemClickListener(itm -> {
                    if (itm.getTitle().equals("Nueva carpeta")) {
                        createNewFolderDialog();
                        return true;
                    }
                    if (itm.getTitle().equals("Nueva nota")) {
                        Note newNote = new Note();
                        newNote.setId(null);
                        mostrarPantallaEdicion(newNote);
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }

        if (currentFolder != null) loadFolderContent(currentFolder);
        foldersAdapter.filter(query);
        notesAdapter.filter(query);
    }

    private void goBack() {
        if (navigationStack.isEmpty()) return;
        Folder prev = navigationStack.remove(navigationStack.size() - 1);

        if (!routePath.isEmpty())
            routePath.remove(routePath.size() - 1);

        loadFolderContent(prev);
    }

    private void createNewFolderDialog() {
        final EditText input = new EditText(this);
        input.setHint("Nombre de la carpeta");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Crear nueva carpeta")
                .setView(input)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "Nueva carpeta";
                    createFolder(name);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createFolder(String name) {
        if (currentFolder == null) return;

        foldersManager.createFolder(name, currentFolder.getId(), () ->
                loadFolderContent(currentFolder)
        );
    }

    private void loadFolderContent(Folder folder) {
        currentFolder = folder;

        updateRouteText();

        foldersManager.getSubfolders(folder.getId(), folderSnapshot -> {
            List<Folder> subfolders = new ArrayList<>();
            for (QueryDocumentSnapshot doc : folderSnapshot) {
                subfolders.add(doc.toObject(Folder.class));
            }

            foldersAdapter.setFolders(subfolders);
            loadNotesForFolder(folder);
        });
    }

    private void updateRouteText() {
        routeTextShow = findViewById(R.id.routeTextShow);
        if (routeTextShow == null) return;

        if (currentFolder.getId().equals("root")) {
            routePath.clear();
            routeTextShow.setText("C:/root");
            return;
        }

        if (routePath.isEmpty()
                || !routePath.get(routePath.size() - 1).equals(currentFolder.getName())) {
            routePath.add(currentFolder.getName());
        }

        StringBuilder sb = new StringBuilder("C:/root");
        for (String name : routePath) {
            sb.append("/").append(name);
        }

        routeTextShow.setText(sb.toString());
    }

    private void loadNotesForFolder(Folder folder) {
        notesManager.getNotesByFolder(folder.getId(), noteSnapshot -> {
            List<Note> notes = new ArrayList<>();
            for (QueryDocumentSnapshot doc : noteSnapshot) {
                Note note = doc.toObject(Note.class);
                note.setId(doc.getId());
                notes.add(note);
            }
            notesAdapter.setNotes(notes);
        });
    }

    private void confirmDeleteFolder(@NonNull Folder folder) {
        foldersManager.countSubfolders(folder.getId(), subCount ->
                notesManager.countNotesInFolder(folder.getId(), noteCount -> {

                    int total = subCount + noteCount;
                    String msg = "Esta carpeta contiene " + total + " elementos"
                            + " (" + subCount + " carpetas, " + noteCount + " notas)."
                            + "\n\nSe eliminarán TODOS los elementos dentro de ella.";

                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Eliminar carpeta \"" + folder.getName() + "\"")
                            .setMessage(msg)
                            .setPositiveButton("Eliminar", (d, w) -> {
                                foldersManager.deleteFolderRecursively(folder.getId(), () -> {
                                    runOnUiThread(() -> {
                                        loadFolderContent(currentFolder);
                                        Toast.makeText(this, "Carpeta eliminada", Toast.LENGTH_SHORT).show();
                                    });
                                });
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                })
        );
    }

    private void confirmDeleteNote(@NonNull Note note) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar nota \"" + note.getTitle() + "\"")
                .setMessage("¿Seguro que quieres eliminar esta nota?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteNoteWithEvents(note))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmRenameNote(@NonNull Note note) {
        final EditText input = new EditText(this);
        input.setText(note.getTitle());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Renombrar nota")
                .setView(input)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    note.setTitle(newName);
                    note.setUpdatedAt(Timestamp.now());
                    notesManager.updateNote(note);
                    loadFolderContent(currentFolder);
                    Toast.makeText(this, "Nota renombrada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmRenameFolder(@NonNull Folder folder) {
        final EditText input = new EditText(this);
        input.setText(folder.getName());

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Renombrar carpeta")
                .setView(input)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    folder.setName(newName);
                    folder.setUpdatedAt(Timestamp.now());
                    foldersManager.updateFolder(folder);
                    loadFolderContent(currentFolder);
                    Toast.makeText(this, "Carpeta renombrada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteNoteWithEvents(@NonNull Note note) {
        CalendarEventsManager evManager = new CalendarEventsManager();

        evManager.getEventsByNote(note.getId(), querySnapshot -> {

            if (!querySnapshot.isEmpty()) {
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    CalendarEvent ev = doc.toObject(CalendarEvent.class);
                    ev.setId(doc.getId());
                    evManager.deleteEvent(ev, () -> {});
                }
            }

            notesManager.deleteNote(note.getFolderId(), note.getId());
            Toast.makeText(this, "Nota eliminada", Toast.LENGTH_SHORT).show();
            loadFolderContent(currentFolder);
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance().signOut(this);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}

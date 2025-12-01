package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.UUID;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.ContentBlock;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.ui.views.TextEditorView;
import es.fdi.ucm.pad.notnotion.utils.ImageHelper;

public class EditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_FOLDER_ID = "extra_folder_id";

    private EditText etTitle;
    private FrameLayout coverContainer;
    private ImageView coverImage;
    private TextView btnAddCover;
    private TextEditorView textEditor;

    private TextView btnBold;
    private TextView btnItalic;
    private FrameLayout btnUnderline;
    private TextView btnTextSize;

    private FloatingActionButton btnBack;
    private FloatingActionButton btnAddContent;

    private Note note;
    private String folderId;

    private boolean isBoldActive = false;
    private boolean isItalicActive = false;
    private boolean isUnderlineActive = false;

    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> pickCoverLauncher;
    private ActivityResultLauncher<Intent> pickContentImageLauncher;
    private ActivityResultLauncher<Intent> pickDocumentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupActivityLaunchers();
        setupListeners();

        Intent intent = getIntent();
        note = (Note) intent.getSerializableExtra(EXTRA_NOTE);
        folderId = intent.getStringExtra(EXTRA_FOLDER_ID);

        if (note != null && folderId != null) {
            loadNoteData();
            return;
        }

        String noteId = intent.getStringExtra("noteId");
        if (noteId != null) {
            loadNoteFromFirestore(noteId);
            return;
        }

        Toast.makeText(this, "Error: datos incompletos", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initializeViews() {
        etTitle = findViewById(R.id.etTitle);
        coverContainer = findViewById(R.id.coverContainer);
        coverImage = findViewById(R.id.coverImage);
        btnAddCover = findViewById(R.id.btnAddCover);
        textEditor = findViewById(R.id.richEditor);

        btnBold = findViewById(R.id.btnBold);
        btnItalic = findViewById(R.id.btnItalic);
        btnUnderline = findViewById(R.id.btnUnderline);
        btnTextSize = findViewById(R.id.btnTextSize);

        btnBack = findViewById(R.id.btnBack);
        btnAddContent = findViewById(R.id.btnAddContent);
    }

    private void setupActivityLaunchers() {
        pickCoverLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        String base64Image = ImageHelper.convertImageToBase64(this, selectedUri);

                        if (base64Image != null) {
                            note.setCoverImageUrl(base64Image);
                            coverImage.setVisibility(View.VISIBLE);
                            btnAddCover.setVisibility(View.GONE);

                            Bitmap bitmap = ImageHelper.convertBase64ToBitmap(base64Image);
                            if (bitmap != null) coverImage.setImageBitmap(bitmap);
                        } else {
                            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        pickContentImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        String base64Image = ImageHelper.convertImageToBase64(this, selectedUri);

                        if (base64Image != null) {
                            textEditor.addImageBlock(base64Image);
                        } else {
                            Toast.makeText(this, "Error al procesar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        pickDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri documentUri = result.getData().getData();
                        handleDocumentSelected(documentUri);
                    }
                }
        );
    }

    private void loadNoteData() {
        if (note.getTitle() != null) etTitle.setText(note.getTitle());

        if (note.getCoverImageUrl() != null && !note.getCoverImageUrl().isEmpty()) {
            coverImage.setVisibility(View.VISIBLE);
            btnAddCover.setVisibility(View.GONE);

            Bitmap bitmap = ImageHelper.convertBase64ToBitmap(note.getCoverImageUrl());
            if (bitmap != null) coverImage.setImageBitmap(bitmap);
        }

        if (note.getContentBlocks() != null) {
            textEditor.loadContent(note.getContentBlocks());
        } else {
            textEditor.loadContent(new ArrayList<>());
        }
    }

    private void setupListeners() {
        coverContainer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickCoverLauncher.launch(intent);
        });

        btnBold.setOnClickListener(v -> {
            isBoldActive = !isBoldActive;
            updateButtonState(btnBold, isBoldActive);
            updateTextStyle();
        });

        btnItalic.setOnClickListener(v -> {
            isItalicActive = !isItalicActive;
            updateButtonState(btnItalic, isItalicActive);
            updateTextStyle();
        });

        btnUnderline.setOnClickListener(v -> {
            isUnderlineActive = !isUnderlineActive;
            updateButtonStateUnderline();
            updateTextStyle();
        });

        btnTextSize.setOnClickListener(v -> showTextSizeDialog());

        btnAddContent.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnAddContent);
            popup.getMenu().add(0, 1, 0, "Añadir imagen");
            popup.getMenu().add(0, 2, 1, "Añadir documento");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickContentImageLauncher.launch(intent);
                    return true;
                } else if (item.getItemId() == 2) {
                    openDocumentPicker();
                    return true;
                }
                return false;
            });

            popup.show();
        });

        btnBack.setOnClickListener(v -> saveNoteAndFinish());
    }

    private void openDocumentPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "text/plain"
        });
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pickDocumentLauncher.launch(intent);
    }

    private void handleDocumentSelected(Uri uri) {
        if (uri == null) return;

        try {
            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
            try {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (SecurityException ignored) {}

            String name = getFileName(uri);
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "*/*";

            textEditor.addDocumentBlock(name, uri.toString(), mimeType);
            Toast.makeText(this, "Documento añadido", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error al añadir documento", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) result = cursor.getString(idx);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }

        if (result == null) {
            String path = uri.getLastPathSegment();
            if (path != null) {
                int slash = path.lastIndexOf('/');
                result = (slash >= 0) ? path.substring(slash + 1) : path;
            } else {
                result = "documento";
            }
        }

        return result;
    }

    private void updateTextStyle() {
        int style = ContentBlock.STYLE_NORMAL;

        if (isBoldActive && isItalicActive && isUnderlineActive) {
            style = ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE;
        } else if (isBoldActive && isItalicActive) {
            style = ContentBlock.STYLE_BOLD_ITALIC;
        } else if (isBoldActive && isUnderlineActive) {
            style = ContentBlock.STYLE_BOLD_UNDERLINE;
        } else if (isItalicActive && isUnderlineActive) {
            style = ContentBlock.STYLE_ITALIC_UNDERLINE;
        } else if (isBoldActive) {
            style = ContentBlock.STYLE_BOLD;
        } else if (isItalicActive) {
            style = ContentBlock.STYLE_ITALIC;
        } else if (isUnderlineActive) {
            style = ContentBlock.STYLE_UNDERLINE;
        }

        textEditor.setCurrentTextStyle(style);
    }

    private void updateButtonState(TextView button, boolean active) {
        button.setBackgroundColor(getResources().getColor(
                active ? R.color.light_yellow : android.R.color.transparent));
    }

    private void updateButtonStateUnderline() {
        btnUnderline.setBackgroundColor(getResources().getColor(
                isUnderlineActive ? R.color.light_yellow : android.R.color.transparent));
    }

    private void showTextSizeDialog() {
        String[] sizes = {"12sp", "14sp", "16sp", "18sp", "20sp", "24sp", "28sp", "32sp"};
        int[] sizeValues = {12, 14, 16, 18, 20, 24, 28, 32};

        new AlertDialog.Builder(this)
                .setTitle("Tamaño de texto")
                .setItems(sizes, (dialog, which) -> {
                    textEditor.setCurrentTextSize(sizeValues[which]);
                    btnTextSize.setText(sizes[which]);
                })
                .show();
    }

    private void saveNoteAndFinish() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: usuario no logueado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return;
        }

        note.setTitle(title);
        note.setContentBlocks(textEditor.getContentBlocks());
        note.setFolderId(folderId);
        note.setUpdatedAt(Timestamp.now());

        saveToFirestore(user.getUid());
    }

    private void loadNoteFromFirestore(String noteId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collectionGroup("notes")
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        if (!doc.getId().equals(noteId)) continue;

                        note = doc.toObject(Note.class);
                        note.setId(noteId);

                        String[] parts = doc.getReference().getPath().split("/");
                        folderId = parts[3];

                        loadNoteData();
                        return;
                    }

                    Toast.makeText(this, "Nota no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar nota", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void saveToFirestore(String userId) {
        String noteId = note.getId();

        if (noteId == null || noteId.isEmpty()) {
            String newNoteId = UUID.randomUUID().toString();
            note.setId(newNoteId);
            note.setCreatedAt(Timestamp.now());

            db.collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folderId)
                    .collection("notes")
                    .document(newNoteId)
                    .set(note)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Nota creada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al crear nota", Toast.LENGTH_SHORT).show());

        } else {
            db.collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folderId)
                    .collection("notes")
                    .document(noteId)
                    .set(note, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al guardar nota", Toast.LENGTH_SHORT).show());
        }
    }
}

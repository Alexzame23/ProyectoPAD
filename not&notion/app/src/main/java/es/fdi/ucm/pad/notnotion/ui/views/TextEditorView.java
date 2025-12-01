package es.fdi.ucm.pad.notnotion.ui.views;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.ContentBlock;
import es.fdi.ucm.pad.notnotion.utils.ImageHelper;

public class TextEditorView extends LinearLayout {

    private static final String TAG = "TextEditorView";

    private Context context;
    private EditText mainEditor;

    private int currentTextStyle = ContentBlock.STYLE_NORMAL;
    private int currentTextSize = 16;
    private int styleStartPosition = 0;

    private boolean isUpdatingText = false;
    private final List<Object> activeSpans = new ArrayList<>();
    private final List<String> insertedImages = new ArrayList<>();

    public TextEditorView(Context context) {
        super(context);
        init(context);
    }

    public TextEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context ctx) {
        this.context = ctx;
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        createMainEditor();
    }

    private void createMainEditor() {
        mainEditor = new EditText(context);
        mainEditor.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainEditor.setBackground(null);
        mainEditor.setTextColor(context.getResources().getColor(R.color.black));
        mainEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
        mainEditor.setHint("Escribe tu nota aquí...");
        mainEditor.setMovementMethod(LinkMovementMethod.getInstance());
        mainEditor.setLinksClickable(true);

        mainEditor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdatingText) return;

                int pos = mainEditor.getSelectionStart();
                if (pos > styleStartPosition) {
                    applyStyleToRange(editable, styleStartPosition, pos);
                }
            }
        });

        addView(mainEditor);
    }

    private void applyStyleToRange(Editable editable, int start, int end) {
        if (start >= end || start < 0 || end > editable.length()) return;

        isUpdatingText = true;
        applyCurrentStyle(editable, start, end);
        isUpdatingText = false;
    }

    private void applyCurrentStyle(Editable editable, int start, int end) {
        if (currentTextSize != 16) {
            AbsoluteSizeSpan span = new AbsoluteSizeSpan(currentTextSize, true);
            editable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            activeSpans.add(span);
        }

        switch (currentTextStyle) {
            case ContentBlock.STYLE_BOLD:
                addSpan(editable, new StyleSpan(Typeface.BOLD), start, end);
                break;
            case ContentBlock.STYLE_ITALIC:
                addSpan(editable, new StyleSpan(Typeface.ITALIC), start, end);
                break;
            case ContentBlock.STYLE_BOLD_ITALIC:
                addSpan(editable, new StyleSpan(Typeface.BOLD_ITALIC), start, end);
                break;
            case ContentBlock.STYLE_UNDERLINE:
                addSpan(editable, new UnderlineSpan(), start, end);
                break;
            case ContentBlock.STYLE_BOLD_UNDERLINE:
                addSpan(editable, new StyleSpan(Typeface.BOLD), start, end);
                addSpan(editable, new UnderlineSpan(), start, end);
                break;
            case ContentBlock.STYLE_ITALIC_UNDERLINE:
                addSpan(editable, new StyleSpan(Typeface.ITALIC), start, end);
                addSpan(editable, new UnderlineSpan(), start, end);
                break;
            case ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE:
                addSpan(editable, new StyleSpan(Typeface.BOLD_ITALIC), start, end);
                addSpan(editable, new UnderlineSpan(), start, end);
                break;
        }
    }

    private void addSpan(Editable editable, Object span, int start, int end) {
        editable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        activeSpans.add(span);
    }

    private void closeActiveSpans() {
        if (activeSpans.isEmpty()) return;

        Editable editable = mainEditor.getText();
        int current = mainEditor.getSelectionStart();

        isUpdatingText = true;

        for (Object span : activeSpans) {
            int s = editable.getSpanStart(span);
            if (s < 0) continue;

            editable.removeSpan(span);

            if (span instanceof StyleSpan) {
                editable.setSpan(
                        new StyleSpan(((StyleSpan) span).getStyle()),
                        s, current,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } else if (span instanceof UnderlineSpan) {
                editable.setSpan(
                        new UnderlineSpan(),
                        s, current,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            } else if (span instanceof AbsoluteSizeSpan) {
                editable.setSpan(
                        new AbsoluteSizeSpan(((AbsoluteSizeSpan) span).getSize(), true),
                        s, current,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        activeSpans.clear();
        isUpdatingText = false;
    }

    public void setCurrentTextStyle(int style) {
        closeActiveSpans();
        currentTextStyle = style;
        styleStartPosition = mainEditor.getSelectionStart();
    }

    public void setCurrentTextSize(int size) {
        closeActiveSpans();
        currentTextSize = size;
        styleStartPosition = mainEditor.getSelectionStart();
    }

    public void loadContent(List<ContentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            mainEditor.setText("");
            insertedImages.clear();
            return;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        insertedImages.clear();

        for (ContentBlock block : blocks) {
            if (block.getType() == ContentBlock.TYPE_TEXT) {
                String text = block.getTextContent();
                if (text == null || text.isEmpty()) continue;

                int start = builder.length();
                builder.append(text);
                int end = builder.length();

                applyStyleToBlock(builder, start, end, block.getTextStyle(), block.getTextSize());
            }

            if (block.getType() == ContentBlock.TYPE_IMAGE) {
                String base64 = block.getMediaUrl();
                if (base64 != null && !base64.isEmpty()) {
                    addImageBlock(base64);
                }
            }
        }

        isUpdatingText = true;
        mainEditor.setText(builder);
        mainEditor.setSelection(builder.length());
        isUpdatingText = false;
    }

    private void applyStyleToBlock(SpannableStringBuilder builder, int start, int end, int style, int size) {
        if (size != 16) {
            builder.setSpan(new AbsoluteSizeSpan(size, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        switch (style) {
            case ContentBlock.STYLE_BOLD:
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case ContentBlock.STYLE_ITALIC:
                builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case ContentBlock.STYLE_BOLD_ITALIC:
                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case ContentBlock.STYLE_UNDERLINE:
                builder.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case ContentBlock.STYLE_BOLD_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case ContentBlock.STYLE_ITALIC_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            case ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
        }
    }

    public List<ContentBlock> getContentBlocks() {
        List<ContentBlock> blocks = new ArrayList<>();

        Editable editable = mainEditor.getText();
        String text = editable.toString();

        if (!text.trim().isEmpty()) {
            int pos = 0;
            int length = text.length();

            while (pos < length) {
                int next = findNextStyleChange(editable, pos);
                String segment = text.substring(pos, next);
                if (!segment.isEmpty()) {
                    blocks.add(ContentBlock.createTextBlock(
                            segment,
                            getStyleAtPosition(editable, pos),
                            getSizeAtPosition(editable, pos)
                    ));
                }
                pos = next;
            }
        }

        for (String b64 : insertedImages) {
            blocks.add(ContentBlock.createImageBlock(b64));
        }

        return blocks;
    }

    private int findNextStyleChange(Editable editable, int start) {
        int length = editable.length();
        Object[] spans = editable.getSpans(start, length, Object.class);
        int nearest = length;

        for (Object span : spans) {
            int s = editable.getSpanStart(span);
            int e = editable.getSpanEnd(span);

            if (s > start && s < nearest) nearest = s;
            if (e > start && e < nearest) nearest = e;
        }

        return nearest;
    }

    private int getStyleAtPosition(Editable editable, int pos) {
        if (pos >= editable.length()) return ContentBlock.STYLE_NORMAL;

        boolean bold = false, italic = false;
        boolean underline = editable.getSpans(pos, pos + 1, UnderlineSpan.class).length > 0;

        for (StyleSpan span : editable.getSpans(pos, pos + 1, StyleSpan.class)) {
            int st = span.getStyle();
            if (st == Typeface.BOLD) bold = true;
            if (st == Typeface.ITALIC) italic = true;
            if (st == Typeface.BOLD_ITALIC) {
                bold = true;
                italic = true;
            }
        }

        if (bold && italic && underline) return ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE;
        if (bold && italic) return ContentBlock.STYLE_BOLD_ITALIC;
        if (bold && underline) return ContentBlock.STYLE_BOLD_UNDERLINE;
        if (italic && underline) return ContentBlock.STYLE_ITALIC_UNDERLINE;
        if (bold) return ContentBlock.STYLE_BOLD;
        if (italic) return ContentBlock.STYLE_ITALIC;
        if (underline) return ContentBlock.STYLE_UNDERLINE;

        return ContentBlock.STYLE_NORMAL;
    }

    private int getSizeAtPosition(Editable editable, int pos) {
        if (pos >= editable.length()) return 16;

        AbsoluteSizeSpan[] sizes = editable.getSpans(pos, pos + 1, AbsoluteSizeSpan.class);
        return sizes.length > 0 ? sizes[0].getSize() : 16;
    }

    public void addImageBlock(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) return;

        insertedImages.add(base64Image);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(VERTICAL);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        container.setLayoutParams(params);
        container.setTag(base64Image);

        ImageView imageView = new ImageView(context);
        LayoutParams imgParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                600
        );
        imageView.setLayoutParams(imgParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Bitmap bitmap = ImageHelper.convertBase64ToBitmap(base64Image);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.icon_note);
        }

        container.addView(imageView);

        container.setOnLongClickListener(v -> {
            showDeleteImageDialog(container);
            return true;
        });

        container.setOnTouchListener((v, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    container.setAlpha(0.7f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    container.setAlpha(1f);
                    break;
            }
            return false;
        });

        int index = indexOfChild(mainEditor);
        addView(container, index);
    }

    private void showDeleteImageDialog(View imageContainer) {
        new android.app.AlertDialog.Builder(context)
                .setTitle("Eliminar imagen")
                .setMessage("¿Eliminar esta imagen?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Eliminar", (d, w) -> {
                    String b64 = (String) imageContainer.getTag();
                    insertedImages.remove(b64);
                    removeView(imageContainer);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    public void addDocumentBlock(String displayName, String uriString, String mimeType) {
        if (uriString == null) return;

        SpannableString span = new SpannableString(displayName != null ? displayName : "documento");
        Uri uri = Uri.parse(uriString);
        String finalMime = mimeType != null ? mimeType : "*/*";

        span.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, finalMime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(Intent.createChooser(intent, "Abrir documento"));
                } catch (ActivityNotFoundException e) {
                    android.widget.Toast.makeText(context, "No hay app para abrir este archivo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(true);
                ds.setColor(context.getResources().getColor(android.R.color.holo_blue_dark));
            }
        }, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        mainEditor.append("\n");
        mainEditor.append(span);
        mainEditor.append("\n");
    }

    public void changeStyleAndCreateNewBlock(int style) {
        setCurrentTextStyle(style);
    }

    public void changeSizeAndCreateNewBlock(int size) {
        setCurrentTextSize(size);
    }
}

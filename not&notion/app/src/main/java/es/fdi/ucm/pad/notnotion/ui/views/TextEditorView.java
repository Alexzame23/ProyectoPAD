package es.fdi.ucm.pad.notnotion.ui.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.ContentBlock;

// Vista para el editor de texto, splannable para aplicar en el momento
public class TextEditorView extends LinearLayout {

    private static final String TAG = "TextEditorView";

    private Context context;
    private EditText mainEditor;

    // Estado del estilo actual
    private int currentTextStyle = ContentBlock.STYLE_NORMAL;
    private int currentTextSize = 16;

    // Posición donde empezó el estilo actual
    private int styleStartPosition = 0;

    // Flag para evitar bucles infinitos
    private boolean isUpdatingText = false;

    // Rastrear spans activos que se están extendiendo
    private List<Object> activeSpans = new ArrayList<>();

    public TextEditorView(Context context) {
        super(context);
        init(context);
    }

    public TextEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setOrientation(VERTICAL);
        setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        createMainEditor();
    }

    private void createMainEditor() {
        mainEditor = new EditText(context);
        mainEditor.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainEditor.setBackground(null);
        mainEditor.setTextColor(context.getResources().getColor(R.color.black));
        mainEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentTextSize);
        mainEditor.setHint("Escribe tu nota aquí...");

        mainEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (isUpdatingText) {
                    return;
                }

                int currentPosition = mainEditor.getSelectionStart();

                if (currentPosition > styleStartPosition) {
                    applyStyleToRange(editable, styleStartPosition, currentPosition);
                }
            }
        });

        addView(mainEditor);
    }

    // Aplica el estilo actual a un rango de texto
    private void applyStyleToRange(Editable editable, int start, int end) {
        if (start >= end || start < 0 || end > editable.length()) {
            return;
        }

        isUpdatingText = true;

        // NO limpiar spans - solo extender los activos
        applyCurrentStyle(editable, start, end);

        isUpdatingText = false;
    }

    // Aplica el estilo actual al rango especificadp
    private void applyCurrentStyle(Editable editable, int start, int end) {
        // Aplicar tamaño
        if (currentTextSize != 16) {
            AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(currentTextSize, true);
            editable.setSpan(sizeSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            activeSpans.add(sizeSpan);
        }

        // Aplicar estilos de texto
        switch (currentTextStyle) {
            case ContentBlock.STYLE_BOLD:
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                editable.setSpan(boldSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldSpan);
                break;

            case ContentBlock.STYLE_ITALIC:
                StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);
                editable.setSpan(italicSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(italicSpan);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC:
                StyleSpan boldItalicSpan = new StyleSpan(Typeface.BOLD_ITALIC);
                editable.setSpan(boldItalicSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldItalicSpan);
                break;

            case ContentBlock.STYLE_UNDERLINE:
                UnderlineSpan underlineSpan = new UnderlineSpan();
                editable.setSpan(underlineSpan, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(underlineSpan);
                break;

            case ContentBlock.STYLE_BOLD_UNDERLINE:
                StyleSpan boldSpan2 = new StyleSpan(Typeface.BOLD);
                UnderlineSpan underlineSpan2 = new UnderlineSpan();
                editable.setSpan(boldSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                editable.setSpan(underlineSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldSpan2);
                activeSpans.add(underlineSpan2);
                break;

            case ContentBlock.STYLE_ITALIC_UNDERLINE:
                StyleSpan italicSpan2 = new StyleSpan(Typeface.ITALIC);
                UnderlineSpan underlineSpan3 = new UnderlineSpan();
                editable.setSpan(italicSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                editable.setSpan(underlineSpan3, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(italicSpan2);
                activeSpans.add(underlineSpan3);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE:
                StyleSpan boldItalicSpan2 = new StyleSpan(Typeface.BOLD_ITALIC);
                UnderlineSpan underlineSpan4 = new UnderlineSpan();
                editable.setSpan(boldItalicSpan2, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                editable.setSpan(underlineSpan4, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                activeSpans.add(boldItalicSpan2);
                activeSpans.add(underlineSpan4);
                break;
        }
    }

    // Cierra los spans activos convirtiéndolos de INCLUSIVE a EXCLUSIVE
    private void closeActiveSpans() {
        if (activeSpans.isEmpty()) {
            return;
        }

        Editable editable = mainEditor.getText();
        int currentPosition = mainEditor.getSelectionStart();

        isUpdatingText = true;

        // Para cada span activo, removerlo y recrearlo como EXCLUSIVE
        for (Object span : activeSpans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);

            // Solo si el span está activo
            if (spanStart >= 0 && spanEnd >= 0) {
                // Remover el span
                editable.removeSpan(span);

                // Recrear como EXCLUSIVE_EXCLUSIVE
                if (span instanceof StyleSpan) {
                    StyleSpan styleSpan = (StyleSpan) span;
                    StyleSpan newSpan = new StyleSpan(styleSpan.getStyle());
                    editable.setSpan(newSpan, spanStart, currentPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (span instanceof UnderlineSpan) {
                    UnderlineSpan newSpan = new UnderlineSpan();
                    editable.setSpan(newSpan, spanStart, currentPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (span instanceof AbsoluteSizeSpan) {
                    AbsoluteSizeSpan oldSpan = (AbsoluteSizeSpan) span;
                    AbsoluteSizeSpan newSpan = new AbsoluteSizeSpan(oldSpan.getSize(), true);
                    editable.setSpan(newSpan, spanStart, currentPosition,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }

        // Limpiar la lista de spans activos
        activeSpans.clear();

        isUpdatingText = false;

        Log.d(TAG, "Spans cerrados en posición: " + currentPosition);
    }

    // Cambia el estilo actual
    public void setCurrentTextStyle(int style) {
        // Cerrar spans activos antes de cambiar de estilo
        closeActiveSpans();

        this.currentTextStyle = style;
        this.styleStartPosition = mainEditor.getSelectionStart();

        Log.d(TAG, "Estilo cambiado a: " + style + " en posición: " + styleStartPosition);
    }

    // Cambia el tamaño actual
    public void setCurrentTextSize(int size) {
        // Cerrar spans activos antes de cambiar de tamaño
        closeActiveSpans();

        this.currentTextSize = size;
        this.styleStartPosition = mainEditor.getSelectionStart();

        Log.d(TAG, "Tamaño cambiado a: " + size + " en posición: " + styleStartPosition);
    }

    public int getCurrentTextStyle() {
        return currentTextStyle;
    }

    public int getCurrentTextSize() {
        return currentTextSize;
    }

    // Carga contenido desde una lista de bloques
    public void loadContent(List<ContentBlock> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            mainEditor.setText("");
            return;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (ContentBlock block : blocks) {
            if (block.getType() == ContentBlock.TYPE_TEXT) {
                String text = block.getTextContent();
                if (text == null || text.isEmpty()) {
                    continue;
                }

                int start = builder.length();
                builder.append(text);
                int end = builder.length();

                applyStyleToBlock(builder, start, end, block.getTextStyle(), block.getTextSize());
            }
        }

        isUpdatingText = true;
        mainEditor.setText(builder);
        mainEditor.setSelection(mainEditor.getText().length());
        isUpdatingText = false;
    }

    // Aplica estilo a un bloque específico al cargar
    private void applyStyleToBlock(SpannableStringBuilder builder, int start, int end,
                                   int style, int size) {
        // Al cargar, usar EXCLUSIVE_EXCLUSIVE para que no se extiendan
        if (size != 16) {
            builder.setSpan(new AbsoluteSizeSpan(size, true), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        switch (style) {
            case ContentBlock.STYLE_BOLD:
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_ITALIC:
                builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC:
                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_UNDERLINE:
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_BOLD_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_ITALIC_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;

            case ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE:
                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new UnderlineSpan(), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
        }
    }

    // Obtiene el contenido como lista de bloques
    public List<ContentBlock> getContentBlocks() {
        List<ContentBlock> blocks = new ArrayList<>();

        Editable editable = mainEditor.getText();
        String text = editable.toString();

        if (text.trim().isEmpty()) {
            return blocks;
        }

        int length = text.length();
        int currentPos = 0;

        while (currentPos < length) {
            int nextChange = findNextStyleChange(editable, currentPos);
            String segmentText = text.substring(currentPos, nextChange);
            int style = getStyleAtPosition(editable, currentPos);
            int size = getSizeAtPosition(editable, currentPos);

            if (!segmentText.isEmpty()) {
                blocks.add(ContentBlock.createTextBlock(segmentText, style, size));
                Log.d(TAG, "Bloque guardado: '" + segmentText + "' estilo=" + style + " tamaño=" + size);
            }

            currentPos = nextChange;
        }

        Log.d(TAG, "Total bloques guardados: " + blocks.size());
        return blocks;
    }

    private int findNextStyleChange(Editable editable, int start) {
        int length = editable.length();
        Object[] spans = editable.getSpans(start, length, Object.class);

        int nearestChange = length;

        for (Object span : spans) {
            int spanStart = editable.getSpanStart(span);
            int spanEnd = editable.getSpanEnd(span);

            if (spanStart > start && spanStart < nearestChange) {
                nearestChange = spanStart;
            }

            if (spanEnd > start && spanEnd < nearestChange) {
                nearestChange = spanEnd;
            }
        }

        return nearestChange;
    }

    private int getStyleAtPosition(Editable editable, int position) {
        if (position >= editable.length()) {
            return ContentBlock.STYLE_NORMAL;
        }

        StyleSpan[] styleSpans = editable.getSpans(position, position + 1, StyleSpan.class);
        UnderlineSpan[] underlineSpans = editable.getSpans(position, position + 1, UnderlineSpan.class);

        boolean isBold = false;
        boolean isItalic = false;
        boolean isUnderline = underlineSpans.length > 0;

        for (StyleSpan span : styleSpans) {
            int style = span.getStyle();
            if (style == Typeface.BOLD) {
                isBold = true;
            } else if (style == Typeface.ITALIC) {
                isItalic = true;
            } else if (style == Typeface.BOLD_ITALIC) {
                isBold = true;
                isItalic = true;
            }
        }

        if (isBold && isItalic && isUnderline) {
            return ContentBlock.STYLE_BOLD_ITALIC_UNDERLINE;
        } else if (isBold && isItalic) {
            return ContentBlock.STYLE_BOLD_ITALIC;
        } else if (isBold && isUnderline) {
            return ContentBlock.STYLE_BOLD_UNDERLINE;
        } else if (isItalic && isUnderline) {
            return ContentBlock.STYLE_ITALIC_UNDERLINE;
        } else if (isBold) {
            return ContentBlock.STYLE_BOLD;
        } else if (isItalic) {
            return ContentBlock.STYLE_ITALIC;
        } else if (isUnderline) {
            return ContentBlock.STYLE_UNDERLINE;
        } else {
            return ContentBlock.STYLE_NORMAL;
        }
    }

    private int getSizeAtPosition(Editable editable, int position) {
        if (position >= editable.length()) {
            return 16;
        }

        AbsoluteSizeSpan[] sizeSpans = editable.getSpans(position, position + 1, AbsoluteSizeSpan.class);

        if (sizeSpans.length > 0) {
            return sizeSpans[0].getSize();
        }

        return 16;
    }

    public void addImageBlock(String imageUrl) {
        mainEditor.append("\n[Imagen: " + imageUrl + "]\n");
    }

    // Métodos de compatibilidad
    public void changeStyleAndCreateNewBlock(int style) {
        setCurrentTextStyle(style);
    }

    public void changeSizeAndCreateNewBlock(int size) {
        setCurrentTextSize(size);
    }
}
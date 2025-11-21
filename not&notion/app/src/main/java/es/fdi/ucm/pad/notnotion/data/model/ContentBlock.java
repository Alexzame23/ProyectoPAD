package es.fdi.ucm.pad.notnotion.data.model;

import java.io.Serializable;

// Crea bloques de contenido para las notas. Texto, imagen y PDF. (Estas ultimas no funcionan)
public class ContentBlock implements Serializable {

    // Tipos de bloque
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_PDF = 2;

    // Estilos de texto
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    public static final int STYLE_BOLD_ITALIC = 3;
    public static final int STYLE_UNDERLINE = 4;
    public static final int STYLE_BOLD_UNDERLINE = 5;
    public static final int STYLE_ITALIC_UNDERLINE = 6;
    public static final int STYLE_BOLD_ITALIC_UNDERLINE = 7;

    private int type; // TYPE_TEXT, TYPE_IMAGE o TYPE_PDF
    private String textContent; // Solo si es TYPE_TEXT
    private String mediaUrl; // URL de Firebase Storage (para IMAGE o PDF)
    private int textStyle; // STYLE_NORMAL, STYLE_BOLD, etc.
    private int textSize; // Tamaño en sp (14, 18, 24, etc.)

    // Constructor vacío para Firestore
    public ContentBlock() {
        this.type = TYPE_TEXT;
        this.textStyle = STYLE_NORMAL;
        this.textSize = 16; // tamaño por defecto
    }

    // Constructor para crear bloque de texto
    public static ContentBlock createTextBlock(String text, int style, int size) {
        ContentBlock block = new ContentBlock();
        block.type = TYPE_TEXT;
        block.textContent = text;
        block.textStyle = style;
        block.textSize = size;
        return block;
    }

    // Constructor para crear bloque de imagen
    public static ContentBlock createImageBlock(String imageUrl) {
        ContentBlock block = new ContentBlock();
        block.type = TYPE_IMAGE;
        block.mediaUrl = imageUrl;
        return block;
    }

    // Constructor para crear bloque de PDF
    public static ContentBlock createPdfBlock(String pdfUrl) {
        ContentBlock block = new ContentBlock();
        block.type = TYPE_PDF;
        block.mediaUrl = pdfUrl;
        return block;
    }

    // Getters y setters
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public int getTextStyle() { return textStyle; }
    public void setTextStyle(int textStyle) { this.textStyle = textStyle; }

    public int getTextSize() { return textSize; }
    public void setTextSize(int textSize) { this.textSize = textSize; }
}
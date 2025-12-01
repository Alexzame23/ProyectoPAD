package es.fdi.ucm.pad.notnotion.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final int MAX_IMAGE_SIZE = 800;
    private static final int COMPRESSION_QUALITY = 70;

    public static String convertImageToBase64(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) return null;

            Bitmap resizedBitmap = resizeBitmap(originalBitmap, MAX_IMAGE_SIZE);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, stream);
            byte[] byteArray = stream.toByteArray();

            String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

            originalBitmap.recycle();
            resizedBitmap.recycle();
            stream.close();

            return base64;

        } catch (Exception e) {
            Log.e(TAG, "Error convertImageToBase64", e);
            return null;
        }
    }

    public static Bitmap convertBase64ToBitmap(String base64String) {
        try {
            if (base64String == null || base64String.isEmpty()) return null;

            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

        } catch (Exception e) {
            Log.e(TAG, "Error convertBase64ToBitmap", e);
            return null;
        }
    }

    private static Bitmap resizeBitmap(Bitmap original, int maxSize) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= maxSize && height <= maxSize) return original;

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);

        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }

    public static boolean isValidBase64(String str) {
        if (str == null || str.isEmpty()) return false;

        try {
            Base64.decode(str, Base64.DEFAULT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

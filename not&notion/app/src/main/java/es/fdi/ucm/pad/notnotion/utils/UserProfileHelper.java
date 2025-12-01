package es.fdi.ucm.pad.notnotion.utils;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import es.fdi.ucm.pad.notnotion.R;

public class UserProfileHelper {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public interface OnUserLoaded {
        void onLoaded(String username, Bitmap photoBitmap);
    }

    public void loadUserProfile(OnUserLoaded callback) {
        if (user == null) {
            callback.onLoaded("No disponible", null);
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String username = doc.getString("username");

                    if (username == null || username.trim().isEmpty()) {
                        username = user.getDisplayName();
                    }
                    if (username == null) username = "Sin nombre";

                    Bitmap photoBitmap = null;
                    String base64 = doc.getString("photoBase64");

                    if (base64 != null && !base64.isEmpty()) {
                        photoBitmap = ImageHelper.convertBase64ToBitmap(base64);
                    }

                    callback.onLoaded(username, photoBitmap);
                })
                .addOnFailureListener(e -> {
                    String fallbackName = user.getDisplayName() != null
                            ? user.getDisplayName()
                            : "Sin nombre";

                    callback.onLoaded(fallbackName, null);
                });
    }

    public void loadUserPhotoInto(ImageView imageView) {
        if (user == null) {
            imageView.setImageResource(R.drawable.ic_user);
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String base64 = doc.getString("photoBase64");

                    if (base64 != null && !base64.isEmpty()) {
                        Bitmap bmp = ImageHelper.convertBase64ToBitmap(base64);
                        if (bmp != null) {
                            imageView.setImageBitmap(bmp);
                            return;
                        }
                    }

                    if (user.getPhotoUrl() != null) {
                        Picasso.get()
                                .load(user.getPhotoUrl())
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.ic_user);
                    }
                });
    }

    public void applyToViews(TextView tvName, ImageView imgProfile) {
        loadUserProfile((username, bitmap) -> {
            tvName.setText(username);

            if (bitmap != null) {
                imgProfile.setImageBitmap(bitmap);
            } else if (user.getPhotoUrl() != null) {
                Picasso.get()
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.ic_user);
            }
        });
    }
}

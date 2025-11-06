package es.fdi.ucm.pad.notnotion.data.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;

public class CalendarEventsManager {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public CalendarEventsManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private String getUserEventsPath() {
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e("Firestore", "No hay usuario autenticado");
            return null;
        }
        return "users/" + uid + "/calendar_events";
    }

    /**
     * Crea un nuevo evento en el calendario del usuario
     */
    public void addEvent(@NonNull String title, @NonNull String description,
                         @NonNull Timestamp startDate, @NonNull Timestamp endDate,
                         String noteId, int reminderMinutes,
                         boolean isRecurring, String recurrencePattern) {

        String path = getUserEventsPath();
        if (path == null) return;

        String eventId = UUID.randomUUID().toString();

        CalendarEvent event = new CalendarEvent(
                eventId,
                title,
                description,
                startDate,
                endDate,
                noteId,
                reminderMinutes,
                isRecurring,
                recurrencePattern,
                Timestamp.now(),
                Timestamp.now()
        );

        db.collection(path)
                .document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Evento creado correctamente"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al crear evento", e));
    }

    /**
     * Actualiza un evento existente
     */
    public void updateEvent(@NonNull CalendarEvent event) {
        String path = getUserEventsPath();
        if (path == null) return;

        event.setUpdatedAt(Timestamp.now());

        db.collection(path)
                .document(event.getId())
                .set(event)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Evento actualizado"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar evento", e));
    }

    /**
     * Elimina un evento del calendario
     */
    public void deleteEvent(String eventId) {
        String path = getUserEventsPath();
        if (path == null) return;

        db.collection(path)
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Evento eliminado"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error al eliminar evento", e));
    }

    /**
     * Obtiene todos los eventos del usuario
     */
    public void getAllEvents(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserEventsPath();
        if (path == null) return;

        db.collection(path)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener eventos", e));
    }

    /**
     * Obtiene los eventos vinculados a una nota específica
     */
    public void getEventsByNote(String noteId, OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserEventsPath();
        if (path == null) return;

        db.collection(path)
                .whereEqualTo("noteId", noteId)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener eventos por nota", e));
    }

    /**
     * Obtiene los eventos futuros (posteriores a 'ahora')
     */
    public void getUpcomingEvents(OnSuccessListener<QuerySnapshot> listener) {
        String path = getUserEventsPath();
        if (path == null) return;

        db.collection(path)
                .whereGreaterThan("startDate", Timestamp.now())
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener eventos futuros", e));
    }
}

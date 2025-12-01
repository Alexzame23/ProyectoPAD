package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;
import es.fdi.ucm.pad.notnotion.ui.notifications.NotificationScheduler;

/**
 * Receiver que maneja la acción de posponer desde la notificación
 */
public class SnoozeActionReceiver extends BroadcastReceiver {

    private static final String TAG = "SnoozeActionReceiver";

    // Tiempo fijo 5 minutos
    private static final int SNOOZE_MINUTES = 5;
    private static final long SNOOZE_MILLIS = SNOOZE_MINUTES * 60 * 1000L;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Acción de snooze recibida");

        String eventId = intent.getStringExtra("eventId");

        if (eventId == null) {
            Log.e(TAG, "eventId es null");
            return;
        }

        // Cargar el evento desde Firebase
        CalendarEventsManager eventsManager = new CalendarEventsManager();
        eventsManager.getEventById(eventId, event -> {
            if (event == null) {
                Log.e(TAG, "Evento no encontrado");
                return;
            }

            if (!event.canSnooze()) {
                Toast.makeText(context,
                        context.getString(R.string.limite_postponimientos_evento),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            handleSnooze(context, event, eventsManager);
        });
    }

    /**
     * Maneja el postponimiento de la alarma
     */
    private void handleSnooze(Context context, CalendarEvent event, CalendarEventsManager eventsManager) {

        // Cancelar la notificación actual
        NotificationHelper.cancelEventNotifications(context, event.getId());

        // Incrementar contador de pos
        event.incrementSnoozeCount();

        // Calcular tiempo de nueva alarma
        long snoozeTimeMillis = System.currentTimeMillis() + SNOOZE_MILLIS;

        // Actualizar evento en Firebase
        eventsManager.updateEvent(event, () -> {

            // Programar nueva alarma
            NotificationScheduler.scheduleSnoozeAlarm(context, event, snoozeTimeMillis);

            // Mostrar confirmación al usuario
            Toast.makeText(context,
                    "Alarma pospuesta " + SNOOZE_MINUTES + " minutos",
                    Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Alarma pospuesta correctamente" +
                    "\n   Evento: " + event.getTitle() +
                    "\n   Snooze #" + event.getSnoozeCount() +
                    "\n   Próxima alarma: " + new java.util.Date(snoozeTimeMillis));
        });
    }
}
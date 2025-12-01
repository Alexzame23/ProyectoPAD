package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;

public class SnoozeActionReceiver extends BroadcastReceiver {

    private static final String TAG = "SnoozeActionReceiver";
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

        CalendarEventsManager eventsManager = new CalendarEventsManager();
        eventsManager.getEventById(eventId, event -> {
            if (event == null) {
                Log.e(TAG, "Evento no encontrado");
                return;
            }

            if (!event.canSnooze()) {
                Toast.makeText(
                        context,
                        "Has alcanzado el límite de postponimientos para este evento",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            handleSnooze(context, event, eventsManager);
        });
    }

    private void handleSnooze(Context context, CalendarEvent event, CalendarEventsManager eventsManager) {
        NotificationHelper.cancelEventNotifications(context, event.getId());

        event.incrementSnoozeCount();
        long snoozeTimeMillis = System.currentTimeMillis() + SNOOZE_MILLIS;

        eventsManager.updateEvent(event, () -> {
            NotificationScheduler.scheduleSnoozeAlarm(context, event, snoozeTimeMillis);

            Toast.makeText(
                    context,
                    "⏰ Alarma pospuesta " + SNOOZE_MINUTES + " minutos",
                    Toast.LENGTH_SHORT
            ).show();

            Log.d(
                    TAG,
                    "Alarma pospuesta: " +
                            event.getTitle() +
                            " | snooze #" + event.getSnoozeCount()
            );
        });
    }
}

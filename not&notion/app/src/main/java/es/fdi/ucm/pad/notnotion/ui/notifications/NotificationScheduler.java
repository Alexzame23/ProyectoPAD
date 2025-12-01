package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;
import es.fdi.ucm.pad.notnotion.ui.events.EventEditActivity;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    public static void scheduleNotifications(Context context, CalendarEvent event) {

        if (event.isNotificationsEnabled() && event.getNotificationTimes() != null) {
            long eventTimeMillis = event.getStartDate().toDate().getTime();

            for (int i = 0; i < event.getNotificationTimes().size(); i++) {
                long millisBeforeEvent = event.getNotificationTimes().get(i);
                long triggerTime = eventTimeMillis - millisBeforeEvent;

                if (triggerTime > System.currentTimeMillis()) {
                    scheduleElapsedNotification(context, event, triggerTime, i, false);
                } else {
                    Log.d(TAG, "Notificación previa descartada (tiempo pasado)");
                }
            }
        }

        if (event.isNotifyAtEventTime()) {
            long eventTimeMillis = event.getStartDate().toDate().getTime();
            if (eventTimeMillis > System.currentTimeMillis()) {
                scheduleEventTimeAlarm(context, event, eventTimeMillis);
            } else {
                Log.d(TAG, "Alarma del momento descartada (tiempo pasado)");
            }
        }
    }

    private static void scheduleElapsedNotification(
            Context context,
            CalendarEvent event,
            long targetMillis,
            int index,
            boolean isEventTime
    ) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long now = System.currentTimeMillis();
        long elapsedNow = SystemClock.elapsedRealtime();
        long delay = targetMillis - now;
        long triggerAt = elapsedNow + delay;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDescription", event.getDescription());
        intent.putExtra("eventTimeMillis", event.getStartDate().toDate().getTime());
        intent.putExtra("soundType", event.getNotificationSound());
        intent.putExtra("isEventTimeAlarm", isEventTime);
        intent.putExtra("isSnoozed", false);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                generateRequestCode(event.getId(), index),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi
                );
            }

            Log.d(TAG, "Notificación programada en " + (delay / 1000) + "s");
        } catch (SecurityException e) {
            Log.e(TAG, "Error: permiso SCHEDULE_EXACT_ALARM denegado", e);
        }
    }

    private static void scheduleEventTimeAlarm(Context context, CalendarEvent event, long eventTimeMillis) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Intent principal que se dispara cuando se activa la alarma
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDescription", event.getDescription());
        intent.putExtra("eventTimeMillis", eventTimeMillis);
        intent.putExtra("soundType", event.getEventTimeNotificationSound());
        intent.putExtra("isEventTimeAlarm", true);
        intent.putExtra("isSnoozed", false);

        // Usa tu propio generador de request codes
        int requestCode = generateEventTimeRequestCode(event.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                // Intent separado para abrir el evento al pulsar la alarma desde pantalla bloqueada
                Intent showIntent = new Intent(context, EventEditActivity.class);
                showIntent.putExtra("eventId", event.getId());
                showIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent showPendingIntent = PendingIntent.getActivity(
                        context,
                        requestCode + 1000, // Evita colisiones
                        showIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                AlarmManager.AlarmClockInfo alarmClockInfo =
                        new AlarmManager.AlarmClockInfo(eventTimeMillis, showPendingIntent);

                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

                Log.d(TAG, "ALARMA programada con setAlarmClock: " + event.getTitle() +
                        " Hora: " + new java.util.Date(eventTimeMillis));

            } else {

                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        eventTimeMillis,
                        pendingIntent
                );

                Log.d(TAG, "ALARMA programada (API < 21) para " + event.getTitle());
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Error programando alarma del evento", e);
        }
    }


    public static void cancelNotifications(Context context, CalendarEvent event) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        if (event.getNotificationTimes() != null) {
            for (int i = 0; i < event.getNotificationTimes().size(); i++) {
                PendingIntent pi = PendingIntent.getBroadcast(
                        context,
                        generateRequestCode(event.getId(), i),
                        new Intent(context, NotificationReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                alarmManager.cancel(pi);
            }
        }

        PendingIntent eventTimePI = PendingIntent.getBroadcast(
                context,
                generateEventTimeRequestCode(event.getId()),
                new Intent(context, NotificationReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(eventTimePI);

        NotificationHelper.cancelEventNotifications(context, event.getId());

        for (int i = 0; i <= event.getSnoozeCount(); i++) {
            PendingIntent snoozePI = PendingIntent.getBroadcast(
                    context,
                    generateSnoozeRequestCode(event.getId(), i),
                    new Intent(context, NotificationReceiver.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(snoozePI);
        }

        Log.d(TAG, "Notificaciones canceladas para evento " + event.getId());
    }

    public static void rescheduleNotifications(Context context, CalendarEvent event) {
        cancelNotifications(context, event);
        scheduleNotifications(context, event);
    }

    public static void scheduleSnoozeAlarm(Context context, CalendarEvent event, long targetMillis) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long now = System.currentTimeMillis();
        long elapsedNow = SystemClock.elapsedRealtime();
        long delay = targetMillis - now;
        long triggerAt = elapsedNow + delay;

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventTitle", event.getTitle());
        intent.putExtra("eventDescription", event.getDescription());
        intent.putExtra("eventTimeMillis", event.getStartDate().toDate().getTime());
        intent.putExtra("soundType", event.getEventTimeNotificationSound());
        intent.putExtra("isEventTimeAlarm", true);
        intent.putExtra("isSnoozed", true);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                generateSnoozeRequestCode(event.getId(), event.getSnoozeCount()),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pi
                );
            }

            Log.d(TAG, "Snooze #" + event.getSnoozeCount() + " programado (" + delay / 1000 + "s)");
        } catch (SecurityException e) {
            Log.e(TAG, "Error programando snooze", e);
        }
    }

    private static int generateRequestCode(String eventId, int index) {
        return (eventId.hashCode() * 100) + index;
    }

    private static int generateEventTimeRequestCode(String eventId) {
        return eventId.hashCode() * 1000;
    }

    private static int generateSnoozeRequestCode(String eventId, int snoozeNumber) {
        return (eventId.hashCode() * 2000) + snoozeNumber;
    }
}

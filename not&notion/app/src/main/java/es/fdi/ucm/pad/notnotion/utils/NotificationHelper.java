package es.fdi.ucm.pad.notnotion.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.ui.events.EventEditActivity;
import es.fdi.ucm.pad.notnotion.ui.notifications.AlarmFullScreenActivity;
import es.fdi.ucm.pad.notnotion.ui.notifications.SnoozeActionReceiver;

public class NotificationHelper {

    private static final String CHANNEL_ID = "event_notifications";
    private static final String CHANNEL_NAME = "Recordatorios de Eventos";
    private static final String CHANNEL_DESC = "Notificaciones para eventos del calendario";

    private static final String ALARM_CHANNEL_ID = "event_alarms";
    private static final String ALARM_CHANNEL_NAME = "Alarmas de Eventos";
    private static final String ALARM_CHANNEL_DESC = "Alarmas en el momento del evento";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;

        NotificationChannel reminderChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        reminderChannel.setDescription(CHANNEL_DESC);
        reminderChannel.enableVibration(true);
        reminderChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
        manager.createNotificationChannel(reminderChannel);

        NotificationChannel alarmChannel = new NotificationChannel(
                ALARM_CHANNEL_ID,
                ALARM_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        alarmChannel.setDescription(ALARM_CHANNEL_DESC);
        alarmChannel.enableVibration(true);
        alarmChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000, 500, 1000});
        alarmChannel.setBypassDnd(true);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        alarmChannel.setSound(alarmSound, attributes);
        manager.createNotificationChannel(alarmChannel);
    }

    public static void showEventNotification(
            Context context,
            String eventId,
            String eventTitle,
            String eventDescription,
            long eventTimeMillis,
            String soundType
    ) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Intent intent = new Intent(context, EventEditActivity.class);
        intent.putExtra("eventId", eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                eventId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(eventTitle)
                .setContentText(eventDescription.isEmpty()
                        ? "Tienes un evento próximo"
                        : eventDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_EVENT);

        if ("silent".equals(soundType)) {
            builder.setSilent(true);
        } else {
            builder.setSound(RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION));
        }

        String timeText = android.text.format.DateFormat.format("HH:mm", eventTimeMillis).toString();

        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                (eventDescription.isEmpty() ? "" : eventDescription + "\n\n") +
                        "📅 Hora: " + timeText));

        manager.notify(generateNotificationId(eventId), builder.build());
    }

    public static void showEventAlarm(
            Context context,
            String eventId,
            String eventTitle,
            String eventDescription,
            String soundType,
            boolean isSnoozed
    ) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        Intent fullScreenIntent = new Intent(context, AlarmFullScreenActivity.class);
        fullScreenIntent.putExtra("eventId", eventId);
        fullScreenIntent.putExtra("eventTitle", eventTitle);
        fullScreenIntent.putExtra("eventDescription", eventDescription);
        fullScreenIntent.putExtra("eventTimeMillis", System.currentTimeMillis());
        fullScreenIntent.putExtra("soundType", soundType);
        fullScreenIntent.putExtra("isSnoozed", isSnoozed);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                eventId.hashCode(),
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent openIntent = new Intent(context, EventEditActivity.class);
        openIntent.putExtra("eventId", eventId);

        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                eventId.hashCode() + 2,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, SnoozeActionReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE_ALARM");
        snoozeIntent.putExtra("eventId", eventId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                eventId.hashCode() + 1,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = (isSnoozed ? "⏰🔁 " : "⏰ ") + eventTitle;
        String content = eventDescription.isEmpty()
                ? "¡Es el momento del evento!"
                : eventDescription;

        if (isSnoozed) content = "(Pospuesto) " + content;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(false)
                .setOngoing(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .addAction(R.drawable.ic_snooze, "Posponer 5 min", snoozePendingIntent)
                .addAction(R.drawable.ic_open, "Abrir", openPendingIntent);

        if ("silent".equals(soundType)) {
            builder.setSilent(true);
        } else if ("alarm".equals(soundType)) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        } else {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText((isSnoozed ? "🔁 POSPUESTO\n\n" : "¡AHORA!\n\n") +
                                (eventDescription.isEmpty() ? eventTitle : eventDescription)))
                .setColor(0xFFFF0000)
                .setColorized(true);

        manager.notify(generateAlarmId(eventId), builder.build());
    }

    public static void showToast(Context context, String message) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    public static void cancelEventNotifications(Context context, String eventId) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        manager.cancel(generateNotificationId(eventId));
        manager.cancel(generateAlarmId(eventId));
    }

    private static int generateNotificationId(String eventId) {
        return eventId.hashCode();
    }

    private static int generateAlarmId(String eventId) {
        return (eventId.hashCode() * 10) + 1;
    }
}

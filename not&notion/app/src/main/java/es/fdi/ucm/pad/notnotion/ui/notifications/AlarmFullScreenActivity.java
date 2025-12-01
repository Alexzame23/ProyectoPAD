package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;

public class AlarmFullScreenActivity extends AppCompatActivity {

    private static final String TAG = "AlarmFullScreen";

    private TextView tvEventTitle, tvEventDescription, tvEventTime, tvSnoozeInfo;
    private Button btnSnooze, btnDismiss;

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private boolean isVibrating;

    private String eventId;
    private CalendarEvent currentEvent;
    private CalendarEventsManager eventsManager;

    private SimpleDateFormat timeFormat, dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupWindowFlags();
        setContentView(R.layout.activity_alarm_fullscreen);

        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        eventsManager = new CalendarEventsManager();

        initViews();
        loadEventData();
        startAlarmSoundAndVibration();
    }

    private void setupWindowFlags() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);

            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        } else {
            Window w = getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void initViews() {
        tvEventTitle = findViewById(R.id.tvAlarmTitle);
        tvEventDescription = findViewById(R.id.tvAlarmDescription);
        tvEventTime = findViewById(R.id.tvAlarmTime);
        tvSnoozeInfo = findViewById(R.id.tvAlarmSnoozeInfo);

        btnSnooze = findViewById(R.id.btnAlarmSnooze);
        btnDismiss = findViewById(R.id.btnAlarmDismiss);

        btnSnooze.setOnClickListener(v -> handleSnooze());
        btnDismiss.setOnClickListener(v -> handleDismiss());
    }

    private void loadEventData() {
        Intent intent = getIntent();

        eventId = intent.getStringExtra("eventId");
        String title = intent.getStringExtra("eventTitle");
        String description = intent.getStringExtra("eventDescription");
        long eventTimeMillis = intent.getLongExtra("eventTimeMillis", 0);
        boolean isSnoozed = intent.getBooleanExtra("isSnoozed", false);

        if (eventId == null || title == null) {
            finish();
            return;
        }

        tvEventTitle.setText(title);

        if (description != null && !description.isEmpty()) {
            tvEventDescription.setText(description);
            tvEventDescription.setVisibility(View.VISIBLE);
        } else {
            tvEventDescription.setVisibility(View.GONE);
        }

        if (eventTimeMillis > 0) {
            Date date = new Date(eventTimeMillis);
            tvEventTime.setText(timeFormat.format(date) + " • " + dateFormat.format(date));
        }

        tvSnoozeInfo.setVisibility(isSnoozed ? View.VISIBLE : View.GONE);
        if (isSnoozed) tvSnoozeInfo.setText("🔁 Alarma pospuesta");

        eventsManager.getEventById(eventId, event -> {
            currentEvent = event;
            if (event == null) return;

            if (event.getSnoozeCount() > 0) {
                tvSnoozeInfo.setVisibility(View.VISIBLE);
                tvSnoozeInfo.setText(
                        "🔁 Pospuesto " + event.getSnoozeCount() +
                                " vez" + (event.getSnoozeCount() > 1 ? "es" : "")
                );
            }

            if (!event.canSnooze()) {
                btnSnooze.setEnabled(false);
                btnSnooze.setText("Límite alcanzado");
            }
        });
    }

    private void startAlarmSoundAndVibration() {
        String soundType = getIntent().getStringExtra("soundType");

        if (!"silent".equals(soundType)) {
            try {
                Uri uri = "alarm".equals(soundType)
                        ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        : RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, uri);

                AudioAttributes attr = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(attr);

                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (am != null) {
                    int max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
                }

                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();

            } catch (Exception e) {
                Log.e(TAG, "Error reproduciendo sonido", e);
            }
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 500, 1000, 500, 1000};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
            } else {
                vibrator.vibrate(pattern, 0);
            }

            isVibrating = true;
        }
    }

    private void stopAlarmSoundAndVibration() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }

        if (vibrator != null && isVibrating) {
            vibrator.cancel();
            isVibrating = false;
        }
    }

    private void handleSnooze() {
        NotificationHelper.cancelEventNotifications(this, eventId);

        if (currentEvent != null) {
            if (!currentEvent.canSnooze()) {
                NotificationHelper.showToast(this, "Has alcanzado el límite de postponimientos");
                handleDismiss();
                return;
            }

            currentEvent.incrementSnoozeCount();
            long snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000L);

            eventsManager.updateEvent(currentEvent, () ->
                    NotificationScheduler.scheduleSnoozeAlarm(this, currentEvent, snoozeTime)
            );

            NotificationHelper.showToast(this, "⏰ Alarma pospuesta 5 minutos");
        }

        stopAlarmSoundAndVibration();
        finish();
    }

    private void handleDismiss() {
        NotificationHelper.cancelEventNotifications(this, eventId);

        if (currentEvent != null && currentEvent.getSnoozeCount() > 0) {
            currentEvent.resetSnoozeCount();
            eventsManager.updateEvent(currentEvent, () -> {});
        }

        stopAlarmSoundAndVibration();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAlarmSoundAndVibration();
    }

    @Override
    public void onBackPressed() {
        // Deshabilitado para evitar cierre accidental
    }
}

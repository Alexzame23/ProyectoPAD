package es.fdi.ucm.pad.notnotion.ui.events;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.ui.notifications.NotificationConfigDialog;
import es.fdi.ucm.pad.notnotion.ui.notifications.NotificationScheduler;
import es.fdi.ucm.pad.notnotion.utils.NotificationHelper;

public class EventEditActivity extends AppCompatActivity {

    private EditText editTitle, editDescription;
    private Button btnPickDate, btnPickTime, btnSave, btnDelete;
    private ImageButton btnConfigureNotifications;

    private TextView tvNotificationStatus;
    private CalendarEventsManager eventsManager;
    private long selectedDateMillis;
    private CalendarEvent currentEvent;

    private Calendar selectedCal;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    private boolean tempNotificationsEnabled = false;
    private List<Long> tempNotificationTimes;
    private String tempSoundType = "default";

    private boolean tempNotifyAtEventTime = false;
    private String tempEventTimeSoundType = "alarm";

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_edit);

        NotificationHelper.createNotificationChannel(this);
        setupPermissionLauncher();
        checkFullScreenIntentPermission();

        editTitle = findViewById(R.id.editEventTitle);
        editDescription = findViewById(R.id.editEventDescription);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSaveEvent);
        btnDelete = findViewById(R.id.btnDeleteEvent);
        btnConfigureNotifications = findViewById(R.id.btnConfigureNotifications);
        tvNotificationStatus = findViewById(R.id.tvNotificationStatus);

        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        selectedCal = Calendar.getInstance();

        eventsManager = new CalendarEventsManager();

        String eventId = getIntent().getStringExtra("eventId");

        if (eventId != null) {
            btnDelete.setVisibility(View.VISIBLE);
            eventsManager.getEventById(eventId, event -> {
                currentEvent = event;

                editTitle.setText(event.getTitle());
                editDescription.setText(event.getDescription());

                selectedCal.setTime(event.getStartDate().toDate());
                btnPickDate.setText(dateFormat.format(selectedCal.getTime()));
                btnPickTime.setText(timeFormat.format(selectedCal.getTime()));

                tempNotificationsEnabled = event.isNotificationsEnabled();
                tempNotificationTimes = event.getNotificationTimes();
                tempSoundType = event.getNotificationSound();
                tempNotifyAtEventTime = event.isNotifyAtEventTime();
                tempEventTimeSoundType = event.getEventTimeNotificationSound();

                updateNotificationStatusText();

                btnDelete.setOnClickListener(v -> deleteEvent());
            });

        } else {
            btnDelete.setVisibility(View.GONE);
            selectedDateMillis = getIntent().getLongExtra("selectedDate", -1);

            if (selectedDateMillis == -1) {
                Toast.makeText(this, "Error: No se recibió fecha", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            selectedCal.setTimeInMillis(selectedDateMillis);
            btnPickDate.setText(dateFormat.format(selectedCal.getTime()));
            btnPickTime.setText(timeFormat.format(selectedCal.getTime()));

            updateNotificationStatusText();
        }

        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnPickTime.setOnClickListener(v -> openTimePicker());
        btnConfigureNotifications.setOnClickListener(v -> openNotificationConfigDialog());
        btnSave.setOnClickListener(v -> saveEvent());

        ImageButton btnGoBack = findViewById(R.id.btnGoBackCalendar);
        btnGoBack.setOnClickListener(v -> finish());
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Permiso denegado. Las notificaciones no funcionarán", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void openNotificationConfigDialog() {
        NotificationConfigDialog dialog = new NotificationConfigDialog(
                this,
                (enabled, times, soundType, notifyAtEventTime, eventTimeSoundType) -> {

                    tempNotificationsEnabled = enabled;
                    tempNotificationTimes = times;
                    tempSoundType = soundType;
                    tempNotifyAtEventTime = notifyAtEventTime;
                    tempEventTimeSoundType = eventTimeSoundType;

                    updateNotificationStatusText();

                    if (enabled || notifyAtEventTime) {
                        checkAndRequestPermissions();
                    }
                }
        );

        dialog.setCurrentConfig(
                tempNotificationsEnabled,
                tempNotificationTimes,
                tempSoundType,
                tempNotifyAtEventTime,
                tempEventTimeSoundType
        );

        dialog.show();
    }

    private void updateNotificationStatusText() {
        int total = 0;

        if (tempNotificationsEnabled && tempNotificationTimes != null) {
            total += tempNotificationTimes.size();
        }

        if (tempNotifyAtEventTime) {
            total += 1;
        }

        if (total > 0) {
            String text = total + " recordatorio" + (total > 1 ? "s" : "");
            if (tempNotifyAtEventTime) text += " (incluye alarma)";
            tvNotificationStatus.setText(text);
            tvNotificationStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvNotificationStatus.setText("Sin recordatorios");
            tvNotificationStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void checkFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null && !manager.canUseFullScreenIntent()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                Toast.makeText(this,
                        "Activa 'Mostrar a pantalla completa' para las alarmas",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openDatePicker() {
        int y = selectedCal.get(Calendar.YEAR);
        int m = selectedCal.get(Calendar.MONTH);
        int d = selectedCal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    selectedCal.set(year, month, day);
                    btnPickDate.setText(dateFormat.format(selectedCal.getTime()));
                    openTimePicker();
                },
                y, m, d
        );

        dialog.show();
    }

    private void openTimePicker() {
        int h = selectedCal.get(Calendar.HOUR_OF_DAY);
        int min = selectedCal.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hour, minute) -> {
                    selectedCal.set(Calendar.HOUR_OF_DAY, hour);
                    selectedCal.set(Calendar.MINUTE, minute);
                    btnPickTime.setText(timeFormat.format(selectedCal.getTime()));
                },
                h, min, true
        );

        dialog.show();
    }

    private void saveEvent() {
        String title = editTitle.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp start = new Timestamp(selectedCal.getTime());

        if (currentEvent == null) {
            CalendarEvent newEvent = new CalendarEvent();
            newEvent.setTitle(title);
            newEvent.setDescription(desc);
            newEvent.setStartDate(start);
            newEvent.setEndDate(start);
            newEvent.setNotificationsEnabled(tempNotificationsEnabled);
            newEvent.setNotificationTimes(tempNotificationTimes);
            newEvent.setNotificationSound(tempSoundType);
            newEvent.setNotifyAtEventTime(tempNotifyAtEventTime);
            newEvent.setEventTimeNotificationSound(tempEventTimeSoundType);

            eventsManager.addEventWithNotifications(newEvent, eventId -> {
                newEvent.setId(eventId);
                NotificationScheduler.scheduleNotifications(this, newEvent);
                Toast.makeText(this, "Evento creado", Toast.LENGTH_SHORT).show();
                finish();
            });

        } else {
            currentEvent.setTitle(title);
            currentEvent.setDescription(desc);
            currentEvent.setStartDate(start);
            currentEvent.setEndDate(start);
            currentEvent.setNotificationsEnabled(tempNotificationsEnabled);
            currentEvent.setNotificationTimes(tempNotificationTimes);
            currentEvent.setNotificationSound(tempSoundType);
            currentEvent.setNotifyAtEventTime(tempNotifyAtEventTime);
            currentEvent.setEventTimeNotificationSound(tempEventTimeSoundType);

            eventsManager.updateEvent(currentEvent, () -> {
                NotificationScheduler.rescheduleNotifications(this, currentEvent);
                Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    private void deleteEvent() {
        if (currentEvent == null) return;

        NotificationScheduler.cancelNotifications(this, currentEvent);

        eventsManager.deleteEvent(currentEvent, () -> {
            Toast.makeText(this, "Evento eliminado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

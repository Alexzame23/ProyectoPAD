package es.fdi.ucm.pad.notnotion.ui.notifications;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Notification;

public class NotificationConfigDialog extends Dialog {

    private CheckBox chkEnableNotifications, chkNotifyAtEventTime;
    private LinearLayout layoutNotificationOptions, layoutEventTimeOptions;
    private RecyclerView recyclerNotifications;
    private TextView tvNoNotifications;
    private Button btnAddNotification, btnSave, btnCancel;

    private RadioGroup radioGroupSound, radioGroupEventTimeSound;
    private RadioButton radioDefault, radioSilent;
    private RadioButton radioAlarmSound, radioDefaultSound, radioSilentSound;

    private Spinner spinnerMaxSnooze;

    private NotificationAdapter adapter;
    private OnNotificationConfigListener listener;

    private boolean notificationsEnabled;
    private List<Notification> currentItems;
    private String currentSoundType;

    private boolean notifyAtEventTime;
    private String eventTimeSoundType;

    public interface OnNotificationConfigListener {
        void onConfigSaved(
                boolean enabled,
                List<Long> times,
                String soundType,
                boolean notifyAtEventTime,
                String eventTimeSoundType
        );
    }

    public NotificationConfigDialog(@NonNull Context context, OnNotificationConfigListener listener) {
        super(context);
        this.listener = listener;
        this.currentItems = new ArrayList<>();
        this.currentSoundType = "default";
        this.notifyAtEventTime = false;
        this.eventTimeSoundType = "alarm";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notification_config);

        initViews();
        setupRecyclerView();
        setupListeners();
        setupMaxSnoozeSpinner();
        loadCurrentConfig();
    }

    private void initViews() {
        chkEnableNotifications = findViewById(R.id.chkEnableNotifications);
        chkNotifyAtEventTime = findViewById(R.id.chkNotifyAtEventTime);

        layoutNotificationOptions = findViewById(R.id.layoutNotificationOptions);
        layoutEventTimeOptions = findViewById(R.id.layoutEventTimeOptions);

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        tvNoNotifications = findViewById(R.id.tvNoNotifications);
        btnAddNotification = findViewById(R.id.btnAddNotification);

        radioGroupSound = findViewById(R.id.radioGroupSound);
        radioDefault = findViewById(R.id.radioDefault);
        radioSilent = findViewById(R.id.radioSilent);

        radioGroupEventTimeSound = findViewById(R.id.radioGroupEventTimeSound);
        radioAlarmSound = findViewById(R.id.radioAlarmSound);
        radioDefaultSound = findViewById(R.id.radioDefaultSound);
        radioSilentSound = findViewById(R.id.radioSilentSound);

        spinnerMaxSnooze = findViewById(R.id.spinnerMaxSnooze);

        btnSave = findViewById(R.id.btnSaveConfig);
        btnCancel = findViewById(R.id.btnCancelConfig);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter((item, position) -> {
            adapter.removeItem(position);
            updateEmptyState();
        });

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerNotifications.setAdapter(adapter);
    }

    private void setupListeners() {
        chkEnableNotifications.setOnCheckedChangeListener((b, checked) ->
                layoutNotificationOptions.setVisibility(checked ? View.VISIBLE : View.GONE)
        );

        chkNotifyAtEventTime.setOnCheckedChangeListener((b, checked) ->
                layoutEventTimeOptions.setVisibility(checked ? View.VISIBLE : View.GONE)
        );

        btnAddNotification.setOnClickListener(v -> openAddNotificationDialog());
        btnSave.setOnClickListener(v -> saveConfiguration());
        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void setupMaxSnoozeSpinner() {
        String[] options = {
                "Sin límite",
                "Máximo 1 vez",
                "Máximo 2 veces",
                "Máximo 3 veces",
                "Máximo 5 veces"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                options
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaxSnooze.setAdapter(adapter);
    }

    private void loadCurrentConfig() {
        chkEnableNotifications.setChecked(notificationsEnabled);
        layoutNotificationOptions.setVisibility(notificationsEnabled ? View.VISIBLE : View.GONE);

        adapter.setItems(currentItems);
        updateEmptyState();

        if ("silent".equals(currentSoundType)) radioSilent.setChecked(true);
        else radioDefault.setChecked(true);

        chkNotifyAtEventTime.setChecked(notifyAtEventTime);
        layoutEventTimeOptions.setVisibility(notifyAtEventTime ? View.VISIBLE : View.GONE);

        if ("alarm".equals(eventTimeSoundType)) radioAlarmSound.setChecked(true);
        else if ("silent".equals(eventTimeSoundType)) radioSilentSound.setChecked(true);
        else radioDefaultSound.setChecked(true);
    }

    private void openAddNotificationDialog() {
        AddNotificationDialog dialog = new AddNotificationDialog(getContext(), item -> {
            adapter.addItem(item);
            updateEmptyState();
        });

        dialog.show();
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        tvNoNotifications.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerNotifications.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void saveConfiguration() {
        boolean enabled = chkEnableNotifications.isChecked();

        List<Long> times = new ArrayList<>();
        if (enabled) {
            for (Notification n : adapter.getItems()) {
                times.add(n.getMillisBeforeEvent());
            }
        }

        String sound = radioSilent.isChecked() ? "silent" : "default";

        boolean notifyTime = chkNotifyAtEventTime.isChecked();
        String eventTimeSound = "default";

        if (radioAlarmSound.isChecked()) eventTimeSound = "alarm";
        else if (radioSilentSound.isChecked()) eventTimeSound = "silent";

        if (listener != null) {
            listener.onConfigSaved(enabled, times, sound, notifyTime, eventTimeSound);
        }

        dismiss();
    }

    public void setCurrentConfig(
            boolean enabled,
            List<Long> times,
            String sound,
            boolean notifyAtEventTime,
            String eventTimeSoundType
    ) {
        notificationsEnabled = enabled;
        currentSoundType = sound != null ? sound : "default";

        currentItems = new ArrayList<>();
        if (times != null) {
            for (Long millis : times) {
                currentItems.add(new Notification(millis));
            }
        }

        this.notifyAtEventTime = notifyAtEventTime;
        this.eventTimeSoundType = eventTimeSoundType != null ? eventTimeSoundType : "alarm";
    }
}

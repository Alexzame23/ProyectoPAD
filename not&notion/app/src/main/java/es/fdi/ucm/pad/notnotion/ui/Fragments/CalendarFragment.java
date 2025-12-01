package es.fdi.ucm.pad.notnotion.ui.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.firebase.CalendarEventsManager;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;
import es.fdi.ucm.pad.notnotion.ui.events.EventAdapter;
import es.fdi.ucm.pad.notnotion.ui.events.EventEditActivity;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private long selectedDateMillis;
    private RecyclerView recyclerEvents;
    private EventAdapter eventAdapter;
    private CalendarEventsManager eventsManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        View btnAddEvent = view.findViewById(R.id.btnAddEvent);
        recyclerEvents = view.findViewById(R.id.recyclerEvents);

        recyclerEvents.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        eventAdapter = new EventAdapter();
        recyclerEvents.setAdapter(eventAdapter);

        eventAdapter.setOnEventClickListener(event -> {
            Intent intent = new Intent(getContext(), EventEditActivity.class);
            intent.putExtra("eventId", event.getId());
            startActivity(intent);
        });

        eventsManager = new CalendarEventsManager();

        selectedDateMillis = calendarView.getDate();
        loadEventsForThisDay(selectedDateMillis);

        calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            selectedDateMillis = cal.getTimeInMillis();
            loadEventsForThisDay(selectedDateMillis);
        });

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EventEditActivity.class);
            intent.putExtra("selectedDate", selectedDateMillis);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedDateMillis != 0) {
            loadEventsForThisDay(selectedDateMillis);
        }
    }

    private void loadEventsForThisDay(long millis) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(millis);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);

        long dayStart = start.getTimeInMillis();

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(millis);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);

        long dayEnd = end.getTimeInMillis();

        eventsManager.getAllEvents(query -> {
            List<CalendarEvent> events = new ArrayList<>();

            for (QueryDocumentSnapshot doc : query) {
                CalendarEvent ev = doc.toObject(CalendarEvent.class);
                ev.setId(doc.getId());

                long time = ev.getStartDate().toDate().getTime();
                if (time >= dayStart && time <= dayEnd) {
                    events.add(ev);
                }
            }

            eventAdapter.setEvents(events);
        });
    }
}

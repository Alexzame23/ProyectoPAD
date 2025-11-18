package es.fdi.ucm.pad.notnotion.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.CalendarEvent;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<CalendarEvent> events = new ArrayList<>();

    public interface OnEventClickListener {
        void onEventClick(CalendarEvent event);
    }

    private OnEventClickListener listener;

    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false); // Crea un layout para evento
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<CalendarEvent> newEvents) {
        events.clear();
        if (newEvents != null) events.addAll(newEvents);
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        TextView hour;

        EventViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvEventTitle);
            description = itemView.findViewById(R.id.tvEventDescription);
            hour = itemView.findViewById(R.id.textHour);
        }
    }
}

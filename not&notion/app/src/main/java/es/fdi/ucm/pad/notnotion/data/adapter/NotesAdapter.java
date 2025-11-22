package es.fdi.ucm.pad.notnotion.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Note;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();

    // ------------------- CLICK NORMAL --------------------
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    private OnNoteClickListener listener;

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    // ------------------- LONG CLICK --------------------
    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note, View view);
    }

    private OnNoteLongClickListener longClickListener;

    public void setOnNoteLongClickListener(OnNoteLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    // -----------------------------------------------------

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.titleView.setText(note.getTitle());

        if (note.getCoverImageUrl() != null && !note.getCoverImageUrl().isEmpty()) {
            holder.coverImageView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(note.getCoverImageUrl())
                    .placeholder(R.drawable.icon_note)
                    .error(R.drawable.icon_note)
                    .resize(200, 200)
                    .centerCrop()
                    .into(holder.coverImageView);
        } else {
            holder.coverImageView.setVisibility(View.GONE);
        }

        // CLICK NORMAL
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(note);
        });

        // LONG CLICK
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onNoteLongClick(note, v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        if (newNotes != null)
            notes.addAll(newNotes);
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageView coverImageView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.noteTitle);
            coverImageView = itemView.findViewById(R.id.noteCoverImage);
        }
    }
}

package es.fdi.ucm.pad.notnotion.data.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import es.fdi.ucm.pad.notnotion.R;
import es.fdi.ucm.pad.notnotion.data.model.Note;
import es.fdi.ucm.pad.notnotion.utils.ImageHelper;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private final List<Note> notes = new ArrayList<>();
    private final List<Note> fullList = new ArrayList<>();

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    private OnNoteClickListener listener;

    public void setOnNoteClickListener(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note, View view);
    }

    private OnNoteLongClickListener longClickListener;

    public void setOnNoteLongClickListener(OnNoteLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.titleView.setText(note.getTitle());

        String coverUrl = note.getCoverImageUrl();

        if (coverUrl != null && !coverUrl.isEmpty()) {
            holder.coverImageView.setVisibility(View.VISIBLE);
            holder.defaultIconView.setVisibility(View.GONE);

            if (ImageHelper.isValidBase64(coverUrl)) {
                Bitmap bitmap = ImageHelper.convertBase64ToBitmap(coverUrl);
                if (bitmap != null) {
                    holder.coverImageView.setImageBitmap(bitmap);
                } else {
                    holder.coverImageView.setVisibility(View.GONE);
                    holder.defaultIconView.setVisibility(View.VISIBLE);
                }
            } else {
                Picasso.get()
                        .load(coverUrl)
                        .placeholder(R.drawable.icon_note)
                        .error(R.drawable.icon_note)
                        .resize(200, 200)
                        .centerCrop()
                        .into(holder.coverImageView);
            }
        } else {
            holder.coverImageView.setVisibility(View.GONE);
            holder.defaultIconView.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onNoteClick(note);
        });

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
        fullList.clear();
        notes.clear();

        if (newNotes != null) {
            fullList.addAll(newNotes);
            notes.addAll(newNotes);
        }

        notifyDataSetChanged();
    }

    public void filter(String text) {
        notes.clear();

        if (text == null || text.trim().isEmpty()) {
            notes.addAll(fullList);
        } else {
            String query = text.toLowerCase();
            for (Note n : fullList) {
                String title = n.getTitle();
                if (title != null && title.toLowerCase().contains(query)) {
                    notes.add(n);
                }
            }
        }

        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView;
        final ImageView coverImageView;
        final ImageView defaultIconView;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.noteTitle);
            coverImageView = itemView.findViewById(R.id.noteCoverImage);
            defaultIconView = itemView.findViewById(R.id.noteIcon);
        }
    }
}

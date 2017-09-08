package com.andhikasrimadeva.myapp.note;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andhikasrimadeva.myapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotesFragment extends Fragment {

    private DatabaseReference mNotesDatabase;

    private RecyclerView notesList;

    public NotesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_notes, container, false);

        notesList = (RecyclerView) mainView.findViewById(R.id.notes_list);
        mNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes");

        notesList.setHasFixedSize(true);
        notesList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inflate the layout for this fragment
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Notes, NotesViewHolder> notesRecyclerViewAdapter = new FirebaseRecyclerAdapter<Notes, NotesViewHolder>(
                Notes.class,
                R.layout.single_note_layout,
                NotesViewHolder.class,
                mNotesDatabase
        ) {
            @Override
            protected void populateViewHolder(NotesViewHolder viewHolder, Notes model, int position) {

                viewHolder.setNoteTitle(model.getTitle());
                viewHolder.setNoteContent(model.getContent());
            }
        };

        notesList.setAdapter(notesRecyclerViewAdapter);
    }

    static class NotesViewHolder extends RecyclerView.ViewHolder{

        View mainView;
        TextView noteTitle;
        TextView noteContent;

        public NotesViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            noteTitle = (TextView) mainView.findViewById(R.id.single_note_title);
            noteContent = (TextView) mainView.findViewById(R.id.single_note_content);

        }

        public void setNoteTitle(String title) {
            noteTitle.setText(title);
        }

        public void setNoteContent(String content) {
            noteContent.setText(content);
        }
    }
}

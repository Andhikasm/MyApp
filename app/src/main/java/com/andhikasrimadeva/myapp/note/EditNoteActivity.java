package com.andhikasrimadeva.myapp.note;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.andhikasrimadeva.myapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity {

    private EditText noteTitle;
    private EditText noteContent;
    private DatabaseReference mNotesDatabase;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        noteTitle = (EditText) findViewById(R.id.note_title_edittext);
        noteContent = (EditText) findViewById(R.id.note_content_edittext);

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mNotesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(user_id);

        if (getIntent().getStringExtra("Note_id") != null){
            editNote();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.save_note_menu:
                saveNote();
                finish();

        }
        return super.onOptionsItemSelected(item);
    }

    private void editNote(){
        String note_id = getIntent().getStringExtra("Note_id");
        DatabaseReference nodeRef = mNotesDatabase.child(note_id);
        nodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = dataSnapshot.child("title").getValue().toString();
                String content = dataSnapshot.child("content").getValue().toString();

                noteTitle.setText(title);
                noteContent.setText(content);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void saveNote(){
        Query query = mNotesDatabase.push();
        DatabaseReference newNoteDatabaseRef = query.getRef();

        String title = noteTitle.getText().toString().trim();
        String content = noteContent.getText().toString().trim();

        Map map = new HashMap();
        map.put("title", title);
        map.put("content", content);

        newNoteDatabaseRef.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(getApplicationContext(), "New note has been saved", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

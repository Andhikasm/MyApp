package com.andhikasrimadeva.myapp;

import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class NoteActivity extends Fragment {

    private boolean mIsViewingOrUpdating; //state of the activity
    private long mNoteCreationTime;
    private String mFileName;
    private Note mLoadedNote = null;
    private View parentView;

    private EditText mEtTitle;
    private EditText mEtContent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.parentView = inflater.inflate(R.layout.activity_note, container, false).getRootView();

        mEtTitle = (EditText) parentView.findViewById(R.id.note_et_title);
        mEtContent = (EditText) parentView.findViewById(R.id.note_et_content);
        setHasOptionsMenu(true);
        //check if view/edit note bundle is set, otherwise user wants to create new note
        if(getArguments() != null){
            mFileName = getArguments().getString(Utilities.EXTRAS_NOTE_FILENAME);
        }

//        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
        if(mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
            mLoadedNote = Utilities.getNoteByFileName(getContext(), mFileName);
            if (mLoadedNote != null) {
                //update the widgets from the loaded note
                mEtTitle.setText(mLoadedNote.getTitle());
                mEtContent.setText(mLoadedNote.getContent());
                mNoteCreationTime = mLoadedNote.getDateTime();
                mIsViewingOrUpdating = true;
            }
        } else { //user wants to create a new note
            mNoteCreationTime = System.currentTimeMillis();
            mIsViewingOrUpdating = false;
        }
        return parentView;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_note);
//
//        mEtTitle = (EditText) findViewById(R.id.note_et_title);
//        mEtContent = (EditText) findViewById(R.id.note_et_content);
//
//        //check if view/edit note bundle is set, otherwise user wants to create new note
//        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
//        if(mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
//            mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), mFileName);
//            if (mLoadedNote != null) {
//                //update the widgets from the loaded note
//                mEtTitle.setText(mLoadedNote.getTitle());
//                mEtContent.setText(mLoadedNote.getContent());
//                mNoteCreationTime = mLoadedNote.getDateTime();
//                mIsViewingOrUpdating = true;
//            }
//        } else { //user wants to create a new note
//            mNoteCreationTime = System.currentTimeMillis();
//            mIsViewingOrUpdating = false;
//        }
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        //load menu based on the state we are in (new, view/update/delete)
//        if(mIsViewingOrUpdating) { //user is viewing or updating a note
//            getMenuInflater().inflate(R.menu.menu_note_view, menu);
//        } else { //user wants to create a new note
//            getMenuInflater().inflate(R.menu.menu_note_add, menu);
//        }
//        return true;
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //load menu based on the state we are in (new, view/update/delete)
        if(mIsViewingOrUpdating) { //user is viewing or updating a note
            inflater.inflate(R.menu.menu_note_view, menu);
        } else { //user wants to create a new note
            inflater.inflate(R.menu.menu_note_add, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save_note: //save the note
            case R.id.action_update: //or update :P
                validateAndSaveNote();
                break;

            case R.id.action_delete:
                actionDelete();
                break;

            case R.id.action_cancel: //cancel the note
                actionCancel();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    /**
//     * Back button press is same as cancel action...so should be handled in the same manner!
//     */
//    @Override
//    public void onBackPressed() {
//        actionCancel();
//    }
//

    /**
     * Handle delete action
     */
    private void actionDelete() {
        //ask user if he really wants to delete the note!
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(getContext())
                .setTitle("delete note")
                .setMessage("really delete the note?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mLoadedNote != null && Utilities.deleteFile(getContext(), mFileName)) {
                            Toast.makeText(getContext(), mLoadedNote.getTitle() + " is deleted"
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "can not delete the note '" + mLoadedNote.getTitle() + "'"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        //finish();
                    }
                })
                .setNegativeButton("NO", null); //do nothing on clicking NO button :P

        dialogDelete.show();
    }

    /**
     * Handle cancel action
     */
    private void actionCancel() {

        if(!checkNoteAltred()) { //if note is not altered by user (user only viewed the note/or did not write anything)
            getActivity().finish(); //just exit the activity and go back to MainActivity
        } else { //we want to remind user to decide about saving the changes or not, by showing a dialog
            AlertDialog.Builder dialogCancel = new AlertDialog.Builder(getContext())
                    .setTitle("discard changes...")
                    .setMessage("are you sure you do not want to save changes to this note?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish(); //just go back to main activity
                        }
                    })
                    .setNegativeButton("NO", null); //null = stay in the activity!
            dialogCancel.show();
        }
    }

    /**
     * Check to see if a loaded note/new note has been changed by user or not
     * @return true if note is changed, otherwise false
     */
    private boolean checkNoteAltred() {
        if(mIsViewingOrUpdating) { //if in view/update mode
            return mLoadedNote != null && (!mEtTitle.getText().toString().equalsIgnoreCase(mLoadedNote.getTitle())
                    || !mEtContent.getText().toString().equalsIgnoreCase(mLoadedNote.getContent()));
        } else { //if in new note mode
            return !mEtTitle.getText().toString().isEmpty() || !mEtContent.getText().toString().isEmpty();
        }
    }

    /**
     * Validate the title and content and save the note and finally exit the activity and go back to MainActivity
     */
    private void validateAndSaveNote() {

        //get the content of widgets to make a note object
        String title = mEtTitle.getText().toString();
        String content = mEtContent.getText().toString();

        //see if user has entered anything :D lol
        if(title.isEmpty()) { //title
            Toast.makeText(getContext(), "please enter a title!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        if(content.isEmpty()) { //content
            Toast.makeText(getContext(), "please enter a content for your note!"
                    , Toast.LENGTH_SHORT).show();
            return;
        }

        //set the creation time, if new note, now, otherwise the loaded note's creation time
        if(mLoadedNote != null) {
            mNoteCreationTime = mLoadedNote.getDateTime();
        } else {
            mNoteCreationTime = System.currentTimeMillis();
        }

        //finally save the note!
        if(Utilities.saveNote(getContext(), new Note(mNoteCreationTime, title, content))) { //success!
            //tell user the note was saved!
            Toast.makeText(getContext(), "note has been saved", Toast.LENGTH_SHORT).show();
        } else { //failed to save the note! but this should not really happen :P :D :|
            Toast.makeText(getContext(), "can not save the note. make sure you have enough space " +
                    "on your device", Toast.LENGTH_SHORT).show();
        }

        //getActivity().finish(); //exit the activity, should return us to MainActivity
    }
}

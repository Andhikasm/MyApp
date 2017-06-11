package com.andhikasrimadeva.myapp;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NotebookActivity extends Fragment{

    private ListView mListViewNotes;
    private View parentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.activity_notebook, container, false).getRootView();

        mListViewNotes = (ListView) parentView.findViewById(R.id.main_listview);
        setHasOptionsMenu(true);
        return parentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("Notebook");
        super.onViewCreated(view, savedInstanceState);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_calculator2);
//        setTitle("Notebook");
////        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
////        setSupportActionBar(toolbar);
////
////        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
////        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
////                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
////        mDrawer.addDrawerListener(toggle);
////        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//
//        mListViewNotes = (ListView) findViewById(R.id.main_listview);
//    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_notebook, menu);
    }



    @Override
    public void onResume() {
        super.onResume();

        //load saved notes into the listview
        //first, reset the listview
        mListViewNotes.setAdapter(null);
        ArrayList<Note> notes = Utilities.getAllSavedNotes(getContext());

        //sort notes from new to old
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note lhs, Note rhs) {
                if(lhs.getDateTime() > rhs.getDateTime()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        if(notes != null && notes.size() > 0) { //check if we have any notes!
            final NoteAdapter na = new NoteAdapter(getContext(), R.layout.notebook_note, notes);
            mListViewNotes.setAdapter(na);

            //set click listener for items in the list, by clicking each item the note should be loaded into NoteActivity
            mListViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //run the NoteActivity in view/edit mode
                    String fileName = ((Note) mListViewNotes.getItemAtPosition(position)).getDateTime()
                            + Utilities.FILE_EXTENSION;

//                    Intent viewNoteIntent = new Intent(getContext(), NoteActivity.class);
//                    viewNoteIntent.putExtra(Utilities.EXTRAS_NOTE_FILENAME, fileName);
//                    startActivity(viewNoteIntent);

                    Fragment noteActivity = new NoteActivity();
                    Bundle bundle = new Bundle();
                    bundle.putString(Utilities.EXTRAS_NOTE_FILENAME, fileName);
                    noteActivity.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content_main, noteActivity);
                    fragmentTransaction.commit();
                }
            });
        } else { //remind user that we have no notes!
            Toast.makeText(getContext(), "you have no saved notes!\ncreate some new notes :)"
                    , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.action_create: //run NoteActivity in new note mode
//                startActivity(new Intent(this, NoteActivity.class));
                fragment = new NoteActivity();
                break;
        }

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_main, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        return super.onOptionsItemSelected(item);
    }
}

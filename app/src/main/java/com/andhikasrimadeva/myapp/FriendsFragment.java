package com.andhikasrimadeva.myapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;
    private View mainView;

    private DatabaseReference friendsDatabase;
    private DatabaseReference usersDatabase;
    private String current_user_id;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(current_user_id);
        //friendsDatabase.keepSynced(true);
        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        //usersDatabase.keepSynced(true);

        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.single_user_layout,
                FriendsViewHolder.class,
                friendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
               viewHolder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();

                usersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);

                        }

                        viewHolder.setName(userName);
                        viewHolder.setUserImage(userThumb, getContext());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friendsList.setAdapter(friendsRecyclerViewAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mainView;
        TextView userNameView;
        TextView userStatusView;
        CircleImageView userImageView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            userNameView = (TextView) mainView.findViewById(R.id.user_single_name);
            userStatusView = (TextView) mainView.findViewById(R.id.user_single_status);
            userImageView = (CircleImageView) mainView.findViewById(R.id.user_single_image);
        }

        public void setDate(String date){
            userStatusView.setText(date);
        }

        public void setName(String name){

            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.ic_avatar).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mainView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }
}

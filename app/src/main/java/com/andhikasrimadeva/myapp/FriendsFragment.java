package com.andhikasrimadeva.myapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;
    private View mainView;

    private DatabaseReference friendsDatabase;
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

        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();


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


    }
}

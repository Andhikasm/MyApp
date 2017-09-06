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
public class RequestsFragment extends Fragment {

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mUsersDatabase;
    private String user_id;

    private RecyclerView requestsList;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_requests, container, false);

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestsList = (RecyclerView) mainView.findViewById(R.id.requests_list);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(user_id);

        requestsList.setHasFixedSize(true);
        requestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inflate the layout for this fragment
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestsViewHolder> requestsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Requests, RequestsViewHolder>(
                Requests.class,
                R.layout.single_request_layout,
                RequestsViewHolder.class,
                mFriendReqDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Requests model, int position) {
                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();

                        viewHolder.setName(userName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        requestsList.setAdapter(requestsRecyclerViewAdapter);
    }

    static class RequestsViewHolder extends RecyclerView.ViewHolder{

        View mainView;
        TextView userNameView;
        TextView userStatusView;
        CircleImageView userImageView;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            userNameView = (TextView) mainView.findViewById(R.id.user_single_name);
            userStatusView = (TextView) mainView.findViewById(R.id.user_single_status);
            userImageView = (CircleImageView) mainView.findViewById(R.id.user_single_image);

        }

        public void setName(String name){

            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.ic_avatar).into(userImageView);

        }
    }

}

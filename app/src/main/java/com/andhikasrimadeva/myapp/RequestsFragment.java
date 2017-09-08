package com.andhikasrimadeva.myapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mRootRef;
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
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mRootRef = FirebaseDatabase.getInstance().getReference();

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
                mFriendReqDatabase.child(user_id)
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, Requests model, int position) {
                final String list_user_id = getRef(position).getKey();
                String request_type = model.getRequest_type();
                if(request_type.equals("received")) {
                    viewHolder.sendButton.setVisibility(View.INVISIBLE);
                    viewHolder.acceptButton.setVisibility(View.VISIBLE);
                    viewHolder.declineButton.setVisibility(View.VISIBLE);

                    viewHolder.acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            acceptRequest(list_user_id, "Request Accepted");
                        }
                    });

                    viewHolder.declineButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            declineRequest(list_user_id, "Request Declined");
                        }
                    });
                }
                else {
                    viewHolder.sendButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            declineRequest(list_user_id, "Request Canceled");
                        }
                    });
                }


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
        Button sendButton;
        Button acceptButton;
        Button declineButton;

        public RequestsViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            userNameView = (TextView) mainView.findViewById(R.id.user_single_name);
            userStatusView = (TextView) mainView.findViewById(R.id.user_single_status);
            userImageView = (CircleImageView) mainView.findViewById(R.id.user_single_image);
            sendButton = (Button) mainView.findViewById(R.id.send_request_button);
            acceptButton = (Button) mainView.findViewById(R.id.accept_request_button);
            declineButton = (Button) mainView.findViewById(R.id.decline_request_button);
        }

        public void setName(String name){

            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.ic_avatar).into(userImageView);

        }
    }

    private void declineRequest(String user_id, final String toastMessage) {

        Map declineReqMap = new HashMap();
        declineReqMap.put("Friend_req/" + user_id + "/" + this.user_id, null);
        declineReqMap.put("Friend_req/" + this.user_id + "/" + user_id, null);

        mRootRef.updateChildren(declineReqMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null) {
                    Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void acceptRequest(String user_id, final String toastMessage) {

        final String currentDate = DateFormat.getDateInstance().format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put("Friends/" + this.user_id + "/" + user_id + "/date", currentDate);
        friendsMap.put("Friends/" + user_id + "/"  + this.user_id + "/date", currentDate);
        friendsMap.put("Friend_req/" + this.user_id + "/" + user_id, null);
        friendsMap.put("Friend_req/" + user_id + "/" + this.user_id, null);

        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if (databaseError == null) {
                    Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }



}

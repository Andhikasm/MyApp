package com.andhikasrimadeva.myapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private DatabaseReference mMessagesDatabase;
    private DatabaseReference mUsersDatabase;
    private String user_id;

    private RecyclerView chatsList;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_chats, container, false);

        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mMessagesDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(user_id);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = (RecyclerView) mainView.findViewById(R.id.chats_list);

        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chats, ChatsViewHolder> chatsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>(
                Chats.class,
                R.layout.single_user_layout,
                ChatsViewHolder.class,
                mMessagesDatabase
        ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Chats model, int position) {
                final String list_user_id = getRef(position).getKey();
                Query query = mMessagesDatabase.child(list_user_id).orderByKey().limitToLast(1);
                query.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        viewHolder.setChat(dataSnapshot.getChildren().iterator().next().child("message").getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(userName);

                        viewHolder.setUserImage(userThumb, getContext());

                        viewHolder.mainView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

        };

        chatsList.setAdapter(chatsRecyclerViewAdapter);
    }

    static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mainView;
        TextView userNameView;
        TextView userStatusView;
        CircleImageView userImageView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            userNameView = (TextView) mainView.findViewById(R.id.user_single_name);
            userStatusView = (TextView) mainView.findViewById(R.id.user_single_status);
            userStatusView.setVisibility(View.VISIBLE);
            userImageView = (CircleImageView) mainView.findViewById(R.id.user_single_image);
        }

        public void setChat(String chat){
            userStatusView.setText(chat);
        }

        public void setName(String name){

            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.ic_avatar).into(userImageView);

        }

    }

}

package com.example.rahul.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivitiy extends AppCompatActivity {


    private RecyclerView users_recycler;
    private Toolbar users_appbar;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_activitiy);
        users_recycler= (RecyclerView) findViewById(R.id.users_recycler);
        users_appbar= (Toolbar) findViewById(R.id.users_appbar);
        setSupportActionBar(users_appbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        users_recycler.setLayoutManager(new LinearLayoutManager(this));
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(Users.class,R.layout.users_single_layout
        ,UsersViewHolder.class,databaseReference) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setUserImage(model.getImage(),getApplicationContext());

                final String user_id = getRef(position).getKey(); //fetch all detiail of particular


                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(UsersActivitiy.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        users_recycler.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }
        public void setName(String name)
        {
            TextView userName=(TextView) mView.findViewById(R.id.user_name);
            userName.setText(name);
        }
        public void setStatus(String status)
        {
            TextView userStatus=(TextView) mView.findViewById(R.id.user_status);
            userStatus.setText(status);
        }
        public void setUserImage(String thumb_image, Context context)
        {
            CircleImageView circleImageView=(CircleImageView) mView.findViewById(R.id.user_image);
            Picasso.with(context).load(thumb_image).placeholder(R.mipmap.ic_launcher).into(circleImageView);
        }
    }
}

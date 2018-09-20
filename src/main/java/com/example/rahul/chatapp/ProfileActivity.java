package com.example.rahul.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayId;
    private ImageView profile_img;
    private TextView profile_name,profile_status,profile_mutual;
    private DatabaseReference databaseReference;
    private Button profile_send,profile_decline;
    private ProgressDialog progressDialog;
    private int mCurrent_state;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mNotificationDtabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mDisplayId= (TextView) findViewById(R.id.profile_display_name);
        final String user_id = getIntent().getStringExtra("user_id");
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDtabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        mDisplayId.setText(user_id);
        profile_img= (ImageView) findViewById(R.id.profile_img);
        profile_name= (TextView) findViewById(R.id.profile_name);
        profile_status= (TextView) findViewById(R.id.profile_status);
        profile_mutual= (TextView) findViewById(R.id.profile_mutual);
        profile_send= (Button) findViewById(R.id.profile_send);
        mCurrent_state=Constants.Type.not_friends;
        profile_decline= (Button) findViewById(R.id.profile_decline);
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Loading User Info");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("Image").getValue().toString();
                profile_name.setText(name);
                profile_status.setText(status);
                Picasso.with(ProfileActivity.this).load(image).into(profile_img);

                ///--------fRIENDS LIST/REQUEST FEATURE
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(user_id))
                            {
                                String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                                if(req_type.equals("received"))
                                {
                                    mCurrent_state=Constants.Type.req_received;
                                    profile_send.setText("Accept Friend Request");
                                    profile_decline.setVisibility(View.VISIBLE);
                                    profile_decline.setEnabled(true);
                                }
                                else if(req_type.equals("sent"))
                                {
                                    mCurrent_state=Constants.Type.req_sent;
                                    profile_send.setText("Cancel Friend Request");
                                    profile_decline.setVisibility(View.GONE);
                                    profile_decline.setEnabled(false);
                                }
                                progressDialog.dismiss();
                            }
                            else
                            {
                                mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(user_id))
                                        {
                                            mCurrent_state=Constants.Type.friends;
                                            profile_send.setText("Unfriend");
                                            progressDialog.dismiss();
                                            profile_decline.setVisibility(View.GONE);
                                            profile_decline.setEnabled(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progressDialog.dismiss();
                                    }
                                });
                            }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });

        profile_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile_send.setEnabled(false);


                //-------NOT FRIENDS--------------------///////
                if(mCurrent_state==Constants.Type.not_friends)
                {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent").
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type")
                                                .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                HashMap<String,String> notifiactionData=new HashMap<>();
                                                notifiactionData.put("from",mCurrentUser.getUid());
                                                notifiactionData.put("type","request");
                                                 mNotificationDtabase.child(user_id).push().setValue(notifiactionData)
                                                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                             @Override
                                                             public void onSuccess(Void aVoid) {
                                                                 profile_send.setEnabled(true);
                                                                 mCurrent_state=Constants.Type.req_sent;
                                                                 profile_send.setText("Cancel Friend Request");
                                                                 profile_decline.setVisibility(View.GONE);
                                                                 profile_decline.setEnabled(false);
                                                             }
                                                         });


                                                Toast.makeText(ProfileActivity.this, "Request Sent Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                //-------CANCEL  FRIENDS REQUEST--------------------///////
                if(mCurrent_state==Constants.Type.req_sent)
                {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    profile_send.setEnabled(true);
                                    mCurrent_state=Constants.Type.not_friends;
                                    profile_send.setText("Send Friend Request");
                                    profile_decline.setVisibility(View.GONE);
                                    profile_decline.setEnabled(false);
                                }
                            });
                        }
                    });

                }
                //////----Req_Received State-------///////
                if(mCurrent_state==Constants.Type.req_received)
                {
                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).setValue(currentDate).
                                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            profile_send.setEnabled(true);
                                                                            mCurrent_state=Constants.Type.friends;
                                                                            profile_send.setText("Unfriend");
                                                                            profile_decline.setVisibility(View.GONE);
                                                                            profile_decline.setEnabled(false);
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                }


                ///--Unfriends
                if(mCurrent_state==Constants.Type.friends)
                {
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                   mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue()
                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   profile_send.setEnabled(true);
                                                   mCurrent_state=Constants.Type.not_friends;
                                                   profile_send.setText("Send Friend Request");
                                                   profile_decline.setVisibility(View.GONE);
                                                   profile_decline.setEnabled(false);
                                               }
                                           });
                                }
                            }
                    );
                }

            }
        });

    }


}

package com.example.rahul.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar status_appbar;
    private EditText status_input;
    private Button status_save;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        status_appbar= (Toolbar) findViewById(R.id.status_appbar);
        status_input= (EditText) findViewById(R.id.status_input);
        status_save= (Button) findViewById(R.id.status_save);

        status_input.setText(status);
        setSupportActionBar(status_appbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mDatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        status_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress=new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please Wait");
                mProgress.show();
                String status = status_input.getText().toString();
                if(status.isEmpty())
                {
                    mProgress.dismiss();
                    status_input.setError("Required");
                    status_input.requestFocus();
                    return;
                }
                else {
                    mDatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mProgress.dismiss();
                                Toast.makeText(StatusActivity.this, "Successfull", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                mProgress.dismiss();
                                Toast.makeText(StatusActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}

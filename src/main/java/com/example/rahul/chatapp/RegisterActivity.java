package com.example.rahul.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button reg_create_button;
    private EditText reg_display_name,reg_email,reg_password;
    private FirebaseAuth mAuth;
    private Toolbar register_toobar;


    //progressDialog
    private ProgressDialog mRegProgress;

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg_display_name= (EditText) findViewById(R.id.reg_display_name);
        reg_email= (EditText) findViewById(R.id.reg_email);
        reg_password= (EditText) findViewById(R.id.reg_password);
        reg_create_button= (Button) findViewById(R.id.reg_create_button);
        register_toobar= (Toolbar) findViewById(R.id.register_toobar);
        setSupportActionBar(register_toobar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress=new ProgressDialog(this);


        mAuth= FirebaseAuth.getInstance();
        reg_create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }
    private void registerUser()
    {
        String name= reg_display_name.getText().toString();
        String email = reg_email.getText().toString();
        String password=reg_password.getText().toString();
        if(name.isEmpty())
        {
            reg_display_name.setError("Name Required");
            reg_display_name.requestFocus();
            return;
        }
        if(email.isEmpty())
        {
            reg_email.setError("Email Required");
            reg_email.requestFocus();
            return;
        }
        if(password.isEmpty())
        {
            reg_password.setError("Password Required");
            reg_password.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            reg_email.setError("Enter Valid Email");
            reg_email.requestFocus();
            return;
        }
        if(password.length()<6)
        {
            reg_password.setError("Password  Must Greater than 5 ");
            reg_password.requestFocus();
            return;
        }
        mRegProgress.setTitle("Registering User");
        mRegProgress.setMessage("Please Wait");
        mRegProgress.setCancelable(false);
        mRegProgress.show();

        registerWithFireBase(name,email,password);
    }

    private void registerWithFireBase(final String name, String email, String password)
    {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(

                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful())
                        {
                            FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                            String uid=current_user.getUid();
                            String token = FirebaseInstanceId.getInstance().getToken();
                            mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String ,String> hashMap=new HashMap<>();
                            hashMap.put("name",name);
                            hashMap.put("status","Hi there i am using hey chat app!");
                            hashMap.put("Image","default");
                            hashMap.put("thumb_image","default");
                            hashMap.put("device_token",token);
                            mDatabase.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        mRegProgress.dismiss();
                                        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                        }
                        else {
                            mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage()+"", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }
}

package com.example.rahul.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText login_email,login_password;
    private Button login_create_button;
    private Toolbar login_toobar;
    private ProgressDialog mRegProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        login_email= (EditText) findViewById(R.id.login_email);
        login_password= (EditText) findViewById(R.id.login_password);
        login_create_button= (Button) findViewById(R.id.login_create_button);
        login_toobar= (Toolbar) findViewById(R.id.login_toobar);
        mRegProgress=new ProgressDialog(this);
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        setSupportActionBar(login_toobar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        login_create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

    }
    private void loginUser()
    {
        String email = login_email.getText().toString();
        String password=login_password.getText().toString();
        if(email.isEmpty())
        {
            login_email.setError("Email Required");
            login_email.requestFocus();
            return;
        }
        if(password.isEmpty())
        {
            login_password.setError("Password Required");
            login_password.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            login_email.setError("Enter Valid Email");
            login_email.requestFocus();
            return;
        }
        if(password.length()<6)
        {
            login_password.setError("Password  Must Greater than 5 ");
            login_password.requestFocus();
            return;
        }
        mRegProgress.setTitle("Registering User");
        mRegProgress.setMessage("Please Wait");
        mRegProgress.setCancelable(false);
        mRegProgress.show();
        loginWithFireBase(email,password);
    }
    private void loginWithFireBase(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    mRegProgress.dismiss();
                    String currentUser = mAuth.getCurrentUser().getUid();
                    String token = FirebaseInstanceId.getInstance().getToken();
                    mUserDatabase.child(currentUser).child("device_token").setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });

                }
                else {
                    mRegProgress.hide();
                    Toast.makeText(LoginActivity.this, task.getException().getMessage()+"", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

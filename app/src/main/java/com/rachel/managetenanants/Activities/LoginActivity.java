package com.rachel.managetenanants.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rachel.managetenanants.Classes.Tenant;
import com.rachel.managetenanants.R;

public class LoginActivity extends AppCompatActivity {
    private Button signUpButton;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private String email;
    private String password;
    private int chosenType;
    private final String KEY = "userChoice";
    private final String SenderKey = "ISent";
    private FirebaseDatabase database;
    private String actualUserType;
    private final String KeyUserType = "type";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        chosenType = getIntent().getIntExtra(KEY,0);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        signUpButton = findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            //change activity
            @Override
            public void onClick(View v) {
                Intent sendRegister = new Intent(LoginActivity.this, RegistrationActivity.class);
                sendRegister.putExtra(KEY, chosenType);
                startActivity(sendRegister);
            }
        });

        loginButton = findViewById(R.id.buttonLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        checkType(userId);

    }

    private void checkType(String userId){
        DatabaseReference myRef = database.getReference("typeDefined");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    DataSnapshot user = dataSnapshot.child(userId);
                    String userType = user.getValue(String.class);
                    actualUserType = userType;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void signIn(){
        email = ((EditText)findViewById(R.id.Email)).getText().toString();
        password = ((EditText)findViewById(R.id.Password)).getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            sendMetoMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendMetoMain(){
        Intent sendUserUI = new Intent(LoginActivity.this, MainActivity.class);
        sendUserUI.putExtra(KeyUserType, actualUserType);
        // unique id
        sendUserUI.putExtra(SenderKey, "login");
        startActivity(sendUserUI);
    }

}
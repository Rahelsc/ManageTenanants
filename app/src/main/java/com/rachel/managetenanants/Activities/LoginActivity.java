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
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        checkType(userId);

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

    }

    // to check which type the current user is in order to load the correct fragment
    private void checkType(String userId){
        Log.d("dov", "checkType: ");
        DatabaseReference myRef = database.getReference("typeDefined");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    DataSnapshot user = dataSnapshot.child(userId);
                    String userType = user.getValue(String.class);
                    actualUserType = userType;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // sign in - if successful sends the user to main, with the data of which fragment to load,
    // based on variable - actualUserType
    private void signIn(){

        Log.d("mom", "signIn: ");
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
        Log.d("dovv", "signIn: 2");
    }


    private void sendMetoMain(){
        Log.d("shai", "sendMetoMain: ");
        Intent sendUserUI = new Intent(LoginActivity.this, MainActivity.class);
        // type of user being sent to main to load correct fragment
        sendUserUI.putExtra(KeyUserType, actualUserType);
        // unique id of sender
        sendUserUI.putExtra(SenderKey, "login");
        startActivity(sendUserUI);
    }

}
package com.rachel.managetenanants.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
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

    private TextView p;
    private TextView rp;
    private Button confirm;
    private ImageButton exitForm;
    private EditText e;
    private EditText pas;
    private View passwordChangeLayout;

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        chosenType = getIntent().getIntExtra(KEY,0);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        getAllFormInputs();

        sharedPreferences = getPreferences(MODE_PRIVATE);

        if( sharedPreferences.getString("keyUser" , null ) != null)
        {
            e.setText(sharedPreferences.getString("keyUser" , null ));
            pas.setText(sharedPreferences.getString("keyPass" , null ));
        }

        signUpButton.setOnClickListener(new View.OnClickListener() {
            //change activity
            @Override
            public void onClick(View v) {
                Intent sendRegister = new Intent(LoginActivity.this, RegistrationActivity.class);
                sendRegister.putExtra(KEY, chosenType);
                startActivity(sendRegister);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    // get all input by id - globally for this Activity
    @SuppressLint("WrongViewCast")
    private void getAllFormInputs(){
        signUpButton = findViewById(R.id.buttonSignUp);
        loginButton = findViewById(R.id.buttonLogin);
        p = findViewById(R.id.newPassword);
        rp = findViewById(R.id.retypeNewPassword);
        confirm = findViewById(R.id.buttonChangePassword);
        exitForm = findViewById(R.id.buttonLeaveChangePassForm);
        e = findViewById(R.id.Email);
        pas = findViewById(R.id.Password);
        passwordChangeLayout = findViewById(R.id.passwordChangeLayout);
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
                    Log.d("new try", actualUserType);
                    sendMetoMain();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // sign in - if successful sends the user to main, with the data of which fragment to load,
    // based on variable - actualUserType
    private void signIn(){
        email = ((EditText)findViewById(R.id.Email)).getText().toString();
        password = ((EditText)findViewById(R.id.Password)).getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("keyUser" , email);
                            editor.putString("keyPass" , password);
                            editor.apply();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();
                            checkType(userId);
                            // Sign in success, update UI with the signed-in user's information
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

    // sets in motion the change password sequence, by making the change password form visible
    public void changePasswordVisible(View view) {
        signUpButton.setVisibility(View.GONE);
        loginButton.setVisibility(View.GONE);
        passwordChangeLayout.setVisibility(View.VISIBLE);
        exitForm.setVisibility(View.VISIBLE);
    }

    // the action of changing the password happens here
    public void changePasswordAction(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        email = e.getText().toString();
        password = pas.getText().toString();
        String pass = p.getText().toString();
        String rePass = rp.getText().toString();
        if (!email.equals("") && !password.equals("") && !pass.equals("") && !rePass.equals("") && pass.equals(rePass)){
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user.updatePassword(pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("hezi", "Password updated");
                                            visibleGoneOfPasswordForm();
                                            Toast.makeText(LoginActivity.this, "Password Changed, please re-authenticate to login",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.d("off", "Error password not updated");
                                        }
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // to make the change password form disappear
    public void leaveChangePassword(View view) {
        visibleGoneOfPasswordForm();
    }

    private void visibleGoneOfPasswordForm(){
        p.setText("");
        rp.setText("");
        e.setText("");
        pas.setText("");
        signUpButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.VISIBLE);
        passwordChangeLayout.setVisibility(View.GONE);
        exitForm.setVisibility(View.GONE);
    }
}
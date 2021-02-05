package com.rachel.managetenanants.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rachel.managetenanants.Classes.HomeOwnerAssociation;
import com.rachel.managetenanants.Classes.Person;
import com.rachel.managetenanants.Classes.Tenant;
import com.rachel.managetenanants.Fragments.HomeAssociationDetailsFragment;
import com.rachel.managetenanants.Fragments.TenantDetailsFragment;
import com.rachel.managetenanants.R;

import java.util.Arrays;
import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {
    private Button registerButton;
    private FirebaseAuth mAuth;
    private String email;
    private String password;
    private int chosenType;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private final String KEY = "userChoice";
    private Fragment target;
    private FirebaseDatabase database;
    private final String SenderKey = "ISent";
    private String actualUserType;
    private final String KeyUserType = "type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        chosenType = getIntent().getIntExtra(KEY,0);
        database = FirebaseDatabase.getInstance();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        // choosing which fragment to add to the activity based on previous choice - tenant / homeowner
        switch (chosenType){
            case 1:
                fragmentTransaction.add(R.id.tenantOrHome,new TenantDetailsFragment()).addToBackStack(null).commit();
            break;
            case 2:
                fragmentTransaction.add(R.id.tenantOrHome,new HomeAssociationDetailsFragment()).addToBackStack(null).commit();
            break;
        }

    }



    public void checkIfUserExists(View view){
        String currentId = ((EditText)findViewById(R.id.Id)).getText().toString();
        DatabaseReference ref = database.getReference("ids");

        // checks if an id already exists - and based on it decides if to create the user
        ref.addValueEventListener(new ValueEventListener() {
            boolean userDoesntExist = true;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot data:dataSnapshot.getChildren()){
                        String id = data.getValue(String.class);
                        if (currentId.equals(id)){
                            userDoesntExist = false;
                        }
                    }
                }
                if (userDoesntExist){
                    register(currentId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistrationActivity.this, "failed", Toast.LENGTH_LONG).show();
            }
        });

    }

    public void register(String id){

        email = ((EditText)findViewById(R.id.Email2)).getText().toString();
        password = ((EditText)findViewById(R.id.Password2)).getText().toString();

        String firstName = ((EditText)findViewById(R.id.FirstName)).getText().toString();
        String lastName = ((EditText)findViewById(R.id.LastName)).getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {



                         if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid(); // get current user id
                            // updating a path for the user's id - for easier access to the id data for check if exists
                            DatabaseReference myRef1 = database.getReference("ids").child(uid);
                            myRef1.setValue(id);

                            // getting the current fragment that's active on the activity
                            target = fragmentManager.getFragments().get(0);
                            if (chosenType == 1){
                                // registerCall gets the inputs from the tenants fragment
                                String response = ((TenantDetailsFragment)target).registerCall();
                                Tenant t = new Tenant(firstName, lastName, id, Integer.parseInt(response));
                                DatabaseReference myRef = database.getReference("Tenants").child(uid);
                                myRef.setValue(t);
                                actualUserType = t.getClass().getName();

                                // create a path for the type of user and save in {uid <-> type}
                                DatabaseReference myRef3 = database.getReference("typeDefined").child(uid);
                                myRef3.setValue(actualUserType);


                            } else {
                                // registerCallSeniority gets the input from the homeowners fragment
                                String response = ((HomeAssociationDetailsFragment)target).registerCallSeniority();
                                HomeOwnerAssociation h = new HomeOwnerAssociation(firstName, lastName, id, Integer.parseInt(response));
                                DatabaseReference myRef = database.getReference("HomeAssociationCommittee").child(uid);
                                myRef.setValue(h);
                                actualUserType = h.getClass().getName();

                                // create a path for the type of user and save in {uid <-> type}
                                DatabaseReference myRef3 = database.getReference("typeDefined").child(uid);
                                myRef3.setValue(actualUserType);
                            }

                            sendMetoMain();

                        } else {
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendMetoMain(){
        Intent sendUserUI = new Intent(RegistrationActivity.this, MainActivity.class);
        sendUserUI.putExtra(KeyUserType, actualUserType);
        // unique id
        sendUserUI.putExtra(SenderKey, "Register");
        startActivity(sendUserUI);
    }

}
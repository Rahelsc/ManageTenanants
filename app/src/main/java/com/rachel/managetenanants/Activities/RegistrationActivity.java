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
import android.widget.EditText;
import android.widget.TextView;
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
import com.rachel.managetenanants.Classes.HomeOwnerAssociation;
import com.rachel.managetenanants.Classes.Tenant;
import com.rachel.managetenanants.Fragments.HomeAssociationDetailsFragment;
import com.rachel.managetenanants.Fragments.TenantDetailsFragment;
import com.rachel.managetenanants.R;

public class RegistrationActivity extends AppCompatActivity {
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

    private ValueEventListener apartmentListener;
    private ValueEventListener idListener;

    private DatabaseReference refApartmentNumbersOnly;
    private DatabaseReference refIdsOnly;

    private EditText firstNameEdit;
    private String firstNameText;
    private EditText lastNameEdit;
    private String lastNameText;
    private EditText idEdit;
    private String idText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        chosenType = getIntent().getIntExtra(KEY,0);
        database = FirebaseDatabase.getInstance();

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        firstNameEdit = findViewById(R.id.FirstName);
        firstNameText = firstNameEdit.getText().toString();
        lastNameEdit = findViewById(R.id.LastName);
        lastNameText = lastNameEdit.getText().toString();
        idEdit = findViewById(R.id.Id);
        idText = idEdit.getText().toString();

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


    // ----------------------------------
    public void makeRequired(View view){
        Log.d("hezzzzzzziiiiiii", firstNameText);
        if (firstNameText != null && lastNameText != null && idText != null){
            checkIfUserExists();
        }
        else{
            Toast.makeText(RegistrationActivity.this, "You must input all details to proceed with the registration",
                    Toast.LENGTH_LONG).show();
            mAuth.getCurrentUser().delete(); // remove the user from the authentication db if user not created
        }
    }

    // checks if an id already exists - and based on it decides if to create the user
    public void checkIfUserExists(){
        refIdsOnly = database.getReference("ids");
        String idText1 = idEdit.getText().toString();

        idListener = new ValueEventListener() {
            boolean userDoesntExist = true;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // removing the event listener to make sure it only happens once
                // otherwise it has time to check again for the user just created
                refIdsOnly.removeEventListener(idListener);
                if (dataSnapshot.exists()){
                    for (DataSnapshot data:dataSnapshot.getChildren()){
                        String id = data.getValue(String.class);
                        if (idText1.equals(id)){
                            userDoesntExist = false;
                        }
                    }
                }
                if (userDoesntExist){
                    register(idText1);
                }
                else
                    Toast.makeText(RegistrationActivity.this, "duplicate id number", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistrationActivity.this, "failed", Toast.LENGTH_LONG).show();
            }
        };
        refIdsOnly.addValueEventListener(idListener);
    }

    // makes sure apartment number isn't duplicate
    // and only then create the tenant

    // ------------------------------check!!!!!!!!!!!!!!!!!!
    private void againstDuplicateApartment(String numberOfApartment, String uid){
        refApartmentNumbersOnly = database.getReference("apartmentNumbersOnly");
        apartmentListener =new ValueEventListener() {
            boolean apartmentDoesntExist = true;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // removing the event listener to make sure it only happens once
                // otherwise it has time to check again for the user just created
                refApartmentNumbersOnly.removeEventListener(apartmentListener);
                if (dataSnapshot.exists()){
                    for (DataSnapshot data:dataSnapshot.getChildren()){
                        String number = data.getValue(String.class);
                        if (numberOfApartment.equals(number)){
                            apartmentDoesntExist = false;
                        }
                    }
                }
                if (apartmentDoesntExist){
                    DatabaseReference myRef2 = database.getReference("apartmentNumbersOnly").child(uid);
                    myRef2.setValue(numberOfApartment);
                    Tenant t = new Tenant(firstNameText, lastNameText, idText, Integer.parseInt(numberOfApartment));
                    DatabaseReference myRef = database.getReference("Tenants").child(uid);
                    myRef.setValue(t);
                    actualUserType = t.getClass().getName();
                    // create a path for the type of user and save in {uid <-> type}
                    DatabaseReference myRef3 = database.getReference("typeDefined").child(uid);
                    myRef3.setValue(actualUserType);
                    sendMetoMain();
                }
                else{
                    deleteLeftOversFromDB(uid, "duplicate apartment number");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RegistrationActivity.this, "failed", Toast.LENGTH_LONG).show();
            }
        };
        refApartmentNumbersOnly.addValueEventListener(apartmentListener);
    }


    public void register(String id){
        email = ((EditText)findViewById(R.id.Email2)).getText().toString();
        password = ((EditText)findViewById(R.id.Password2)).getText().toString();

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
                                String apartmentNumber = ((EditText)findViewById(R.id.apartmentNumber)).getText().toString();
                                if (!equals(apartmentNumber))
                                    againstDuplicateApartment(apartmentNumber, uid);
                                // happens if apartment number field isn't filled
                                else {
                                    deleteLeftOversFromDB(uid, "You must fill out your apartment number");
                                }
                            } else {
                                // registerCallSeniority gets the input from the homeowners fragment
                                String seniority = ((EditText)findViewById(R.id.TextSeniority)).getText().toString();
                                HomeOwnerAssociation h;
                                if (!seniority.equals("")) {
                                    h = new HomeOwnerAssociation(firstNameText, lastNameText, idText, Integer.parseInt(seniority));
                                    DatabaseReference myRef = database.getReference("HomeAssociationCommittee").child(uid);
                                    myRef.setValue(h);
                                    actualUserType = h.getClass().getName();
                                    // create a path for the type of user and save in {uid <-> type}
                                    DatabaseReference myRef3 = database.getReference("typeDefined").child(uid);
                                    myRef3.setValue(actualUserType);
                                    sendMetoMain();
                                }
                                // happens if seniority field isn't filled
                                else {
                                    deleteLeftOversFromDB(uid, "You must fill out your seniority");
                                }
                            }
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // a function to delete left over data - that didn't pass all check from the database
    private void deleteLeftOversFromDB(String uid, String message){
        Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_LONG).show();
        database.getReference("ids").child(uid).removeValue(); //remove the id from ids if user wasn't created eventually
        mAuth.getCurrentUser().delete(); // remove the user from the authentication db if user not created
    }

    // loads the activity with the relevant fragment
    private void sendMetoMain(){
        Intent sendUserUI = new Intent(RegistrationActivity.this, MainActivity.class);
        sendUserUI.putExtra(KeyUserType, actualUserType);
        // unique id
        sendUserUI.putExtra(SenderKey, "Register");
        startActivity(sendUserUI);
    }

}
package com.rachel.managetenanants.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rachel.managetenanants.Classes.Tenant;
import com.rachel.managetenanants.Fragments.ChoiceOfUserFragment;
import com.rachel.managetenanants.Fragments.ManagementFragment;
import com.rachel.managetenanants.Fragments.TenantFragment;
import com.rachel.managetenanants.R;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private final String KEY = "userChoice";
    private final String SenderKey = "ISent";

    private String caller = null;
    private String actualUserType;
    private final String KeyUserType = "type";

    private EditText apartment;
    private EditText month;
    private EditText payment;
    private Button update;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    final HashMap<String, Integer> monthsPaid = new HashMap<>();
    final HashMap<String, ArrayList> monthsPaidPerApartment = new HashMap<>();
    final HashMap<String, HashMap<String,String>> apartmentPlusPayments = new HashMap<>();

    private TextView name;
    private TextView AN;
    private TextView payments;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        updateLocalHashes();

        caller = getIntent().getStringExtra(SenderKey);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        // only if it's the first load of the application show this fragment (choose whether you're a tenant or a home association)
        if (caller==null){
            fragmentTransaction.add(R.id.fragmentPlacementMain,new ChoiceOfUserFragment())
                    .addToBackStack(null).commit();
        }

        // when this activity is reached from either the login or the registration activity,
        // the caller string will contain a unique id from the sender and so will not be null
        // at this point a choice must be made what fragment to show,
        // based on the type of user we're handling
        else if (caller!=null){
            actualUserType = getIntent().getStringExtra(KeyUserType);
            Log.d("yoohoo", "please?: "+actualUserType);
            switch (actualUserType){
                case "com.rachel.managetenanants.Classes.Tenant":
                    fragmentTransaction.replace(R.id.fragmentPlacementMain,new TenantFragment()).addToBackStack(null).commit();
                    getTenantDetails();
                    break;
                case "com.rachel.managetenanants.Classes.HomeOwnerAssociation":
                    fragmentTransaction.replace(R.id.fragmentPlacementMain,new ManagementFragment()).addToBackStack(null).commit();
                    break;
            }
        }
    }

    // passing from main to login
    public void loadLogin(int selectedId) {
        // if selectedId is 0, it means that there was no choice made, so user can't continue
        if (selectedId != 0){
            Intent send = new Intent(MainActivity.this, LoginActivity.class);
            // passing to login activity the user's choice
            send.putExtra(KEY, selectedId);
            startActivity(send);
        }
    }

    //for home owner association
    // triggered on click of the plus button - makes the mini form of updating payments appear
    public void visiblePayment(View view) {
        apartment = findViewById(R.id.plus1);
        month = findViewById(R.id.plus2);
        payment = findViewById(R.id.plus3);
        update = findViewById(R.id.updateButton);
        apartment.setVisibility(View.VISIBLE);
        month.setVisibility(View.VISIBLE);
        payment.setVisibility(View.VISIBLE);
        update.setVisibility(View.VISIBLE);
    }

    // gets called from the home owner form
    // updates the relevant apartment payment
    public void paymentUpdate(View view) {
        String apartmentNum = apartment.getText().toString();
        String monthPaid = month.getText().toString();
        String sumPaid = payment.getText().toString();
        HashMap<String, String> p = new HashMap<String, String>();
        p.put(monthPaid, sumPaid);
        DatabaseReference myRef = database.getReference("apartmentPayment/"+apartmentNum+"/"+monthPaid);
        myRef.setValue(sumPaid);
    }

    // for home owner association form
    // set the building payment field
    public void updateBuildingPayments(View view){
        TextView buildingIncome = findViewById(R.id.response_income);
        buildingIncome.setText(monthsPaid.toString());
    }

    // updates the local hashes that save the current state of db to hashes
    // specifically saves the payment in two states -
    // one hash for apartment to months key value pair
    // and another hash for summing all payments of the building per month
    private void updateLocalHashes(){
        DatabaseReference ref = database.getReference().child("apartmentPayment");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                monthsPaid.clear();
                monthsPaidPerApartment.clear();
                if (dataSnapshot.exists()){
                    for (DataSnapshot data:dataSnapshot.getChildren()){
                        String s = data.getKey();
                        monthsPaidPerApartment.put(s, new ArrayList<String>());
                        apartmentPlusPayments.put(s, new HashMap<String, String>());
                        for (DataSnapshot d:data.getChildren()){
                            String k = d.getKey();
                            String h = d.getValue().toString();
                            if (monthsPaid.get(k)!=null)
                                monthsPaid.put(k, monthsPaid.get(k)+Integer.parseInt(h));
                            else monthsPaid.put(k, Integer.parseInt(h));
                            monthsPaidPerApartment.get(s).add(k);
                            apartmentPlusPayments.get(s).put(k,h);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    // home owner form
    // sets the apartments paid field
    public void getApartmentMonthsPaid(View view) {
        String apartNum = ((EditText)findViewById(R.id.apartmentNumberToCheck)).getText().toString();
        TextView response_payment = findViewById(R.id.reponse_rent_from_1);
        Log.d("trial1", apartNum);
        if (apartNum != null && monthsPaidPerApartment.get(apartNum) != null)
        {
            Log.d("trial", "offff");
            response_payment.setText(monthsPaidPerApartment.get(apartNum).toString());
        }

        else response_payment.setText("No payment received");
    }

    // home owner
    // gets all paid months of a certain apartment
    public void getAllPaymentsPerTenant(View view) {
        TextView allPaid = findViewById(R.id.response_all_paid);
        allPaid.setText(monthsPaidPerApartment.toString());
    }

    // tenant form
    // sets all details for the tenant form
    private void getTenantDetails(){
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();

        DatabaseReference ref = database.getReference("Tenants").child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Tenant ten = dataSnapshot.getValue(Tenant.class);
                    name = findViewById(R.id.fullTenantName);
                    name.setText(ten.getFirstName()+" "+ten.getLastName());
                    AN = findViewById(R.id.ApartmentNumberFromDB);
                    AN.setText(String.valueOf(ten.getApartmentNumber()));
                    payments = findViewById(R.id.MonthlyPaymentFromDB);
                    if (apartmentPlusPayments.get(String.valueOf(ten.getApartmentNumber())) != null){
                        payments.setText(apartmentPlusPayments.get(String.valueOf(ten.getApartmentNumber())).toString());
                    }
                    else payments.setText("No payments received");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_LONG).show();
            }
        });


    }
}
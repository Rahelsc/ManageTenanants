package com.rachel.managetenanants.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rachel.managetenanants.Classes.BuildingDataAdapter;
import com.rachel.managetenanants.Classes.BuildingIncomeDataModel;
import com.rachel.managetenanants.Classes.Tenant;
import com.rachel.managetenanants.Fragments.ChoiceOfUserFragment;
import com.rachel.managetenanants.Fragments.ManagementFragment;
import com.rachel.managetenanants.Fragments.TenantFragment;
import com.rachel.managetenanants.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// to display the name of the month instead of a number representation
enum Months{
    JANUARY("1"),
    FEBRUARY("2"),
    MARCH("3"),
    APRIL("4"),
    MAY("5"),
    JUNE("6"),
    JULY("7"),
    AUGUST("8"),
    SEPTEMBER("9"),
    OCTOBER("10"),
    NOVEMBER("11"),
    DECEMBER("12");

    public final String value;

    private static final Map<String, Months> BY_VALUE = new HashMap<>();

    Months(String value) {
        this.value = value;
    }

    // caching the key-value pairs of the enum using a static operation and a hashMap
    static {
        for (Months e: values()) {
            BY_VALUE.put(e.value, e);
        }
    }

    // will return the value of enum based on key(string representation of the month number)
    public static Months valueOfLabel(String label) {
        return BY_VALUE.get(label);
    }
}

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
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    final HashMap<String, Integer> monthsPaid = new HashMap<>();
    final HashMap<String, ArrayList> monthsPaidPerApartment = new HashMap<>();
    final HashMap<String, HashMap<String,String>> apartmentPlusPayments = new HashMap<>();

    private TextView name;
    private TextView AN;
    private TextView payments;
    private TableRow tableRow;

    private ProgressBar progressBar;
    private Button signOutButton;

    private RecyclerView paymentsPresentation;
    private ArrayList<BuildingIncomeDataModel> incomeArrayList;
    private BuildingDataAdapter buildingDataAdapter;


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
            progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE); // make progressbar visible
            signOutButton = findViewById(R.id.signOutButton);
            signOutButton.setVisibility(View.VISIBLE);

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
            // delay the dismissal of the progress bar
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);
                }
            }, 2000);
        }
    }

    // activated by the onSelectedItem eventListener in the management fragment
    // sets the visibility of all items in fragment based on choice in spinner
    public void handleSelection(View view, int position){
        Log.d("click", "works? ");
        View payingView = findViewById(R.id.payingView);
        paymentsPresentation = findViewById(R.id.recycleBuildingIncome);
        View allPaymentsPerTenantView = findViewById(R.id.allPaymentsPerTenantView);
        View apartmentMonthsPaidView = findViewById(R.id.apartmentMonthsPaidView);

        switch (position){
            case 1:
                apartmentMonthsPaidView.setVisibility(View.VISIBLE);
                payingView.setVisibility(View.GONE);
                paymentsPresentation.setVisibility(View.GONE);
                allPaymentsPerTenantView.setVisibility(View.GONE);
                break;
            case 2:
                allPaymentsPerTenantView.setVisibility(View.VISIBLE);
                paymentsPresentation.setVisibility(View.GONE);
                apartmentMonthsPaidView.setVisibility(View.GONE);
                payingView.setVisibility(View.GONE);
                getAllPaymentsPerTenant();
                break;
            case 3:
                payingView.setVisibility(View.VISIBLE);
                paymentsPresentation.setVisibility(View.GONE);
                allPaymentsPerTenantView.setVisibility(View.GONE);
                apartmentMonthsPaidView.setVisibility(View.GONE);
                break;
            case 4:
                paymentsPresentation.setVisibility(View.VISIBLE);
                apartmentMonthsPaidView.setVisibility(View.GONE);
                payingView.setVisibility(View.GONE);
                allPaymentsPerTenantView.setVisibility(View.GONE);
                updateBuildingPayments();
                break;

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

    // gets called from the home owner form
    // updates the relevant apartment payment
    public void paymentUpdate(View view) {
        apartment = findViewById(R.id.apartmentToAdd);
        month = findViewById(R.id.monthToAdd);
        payment = findViewById(R.id.paymentToAdd);
        String apartmentNum = apartment.getText().toString();
        String monthPaid = month.getText().toString();
        String sumPaid = payment.getText().toString();
        DatabaseReference myRef;
        if (Integer.parseInt(monthPaid)>0 && Integer.parseInt(monthPaid)<13){
            myRef = database.getReference("apartmentPayment/"+apartmentNum+"/"+monthPaid);
            myRef.setValue(sumPaid);
            Toast.makeText(MainActivity.this, "Database Updated", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Invalid month  \nplease try again", Toast.LENGTH_LONG).show();
        }
    }

    // for home owner association form
    // set the building payment field
    public void updateBuildingPayments(){
        paymentsPresentation.setHasFixedSize(true);
        paymentsPresentation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        incomeArrayList = new ArrayList<>();
        // looping over the data and updating the arraylist
        for (String key: monthsPaid.keySet()) {
            incomeArrayList.add(new BuildingIncomeDataModel(Months.valueOfLabel(key).toString(), String.valueOf(monthsPaid.get(key))));
        }
        // creating the data adapter and associating it with the arraylist containing the data
        buildingDataAdapter = new BuildingDataAdapter(incomeArrayList, this);
        // connecting the visual representation if the recycle view with the adapter
        paymentsPresentation.setAdapter(buildingDataAdapter);
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
            response_payment.setText(monthsPaidPerApartment.get(apartNum).toString());
        }

        else response_payment.setText("No payment received");
    }

    // home owner
    // gets all paid months of a certain apartment
    public void getAllPaymentsPerTenant() {
        TextView allPaid = findViewById(R.id.allPaymentsPerTenantView);
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
                    if (apartmentPlusPayments.get(String.valueOf(ten.getApartmentNumber())) != null){
                        // loop through all the values in the paid months of this particular apartment
                        for (String key:
                                apartmentPlusPayments.get(String.valueOf(ten.getApartmentNumber())).keySet()) {
                            String res="";
                            res+=" "+Months.valueOfLabel(key); // saves month's name to display
                            res+=" "+apartmentPlusPayments.get(String.valueOf(ten.getApartmentNumber())).get(key); // save sum to display
                            paymentsField(Months.valueOfLabel(key)); // find out which field we need to show

                            // sets visibility for the required field
                            tableRow.setVisibility(View.VISIBLE);
                            payments.setVisibility(View.VISIBLE);

                            // actual payment displayed per month
                            payments.setText(res);
                        }
                    }
                    else {
                        payments = findViewById(R.id.JANUARY);
                        tableRow = findViewById(R.id.R1);
                        tableRow.setVisibility(View.VISIBLE);
                        payments.setText("No payments received");
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_LONG).show();
            }

        });
    }

    // for tenants => checks what month is marked as paid and based on that finds the appropriate label
    public void paymentsField(Months m){
        switch (m){
            case JANUARY:
                payments = findViewById(R.id.JANUARY);
                tableRow = findViewById(R.id.R1);
                break;
            case FEBRUARY:
                payments = findViewById(R.id.FEBRUARY);
                tableRow = findViewById(R.id.R2);
                break;
            case MARCH:
                payments = findViewById(R.id.MARCH);
                tableRow = findViewById(R.id.R3);
                break;
            case APRIL:
                payments = findViewById(R.id.APRIL);
                tableRow = findViewById(R.id.R4);
                break;
            case MAY:
                payments = findViewById(R.id.MAY);
                tableRow = findViewById(R.id.R5);
                break;
            case JUNE:
                payments = findViewById(R.id.JUNE);
                tableRow = findViewById(R.id.R6);
                break;
            case JULY:
                payments = findViewById(R.id.JULY);
                tableRow = findViewById(R.id.R7);
                break;
            case AUGUST:
                payments = findViewById(R.id.AUGUST);
                tableRow = findViewById(R.id.R8);
                break;
            case SEPTEMBER:
                payments = findViewById(R.id.SEPTEMBER);
                tableRow = findViewById(R.id.R9);
                break;
            case OCTOBER:
                payments = findViewById(R.id.OCTOBER);
                tableRow = findViewById(R.id.R10);
                break;
            case NOVEMBER:
                payments = findViewById(R.id.NOVEMBER);
                tableRow = findViewById(R.id.R11);
                break;
            case DECEMBER:
                payments = findViewById(R.id.DECEMBER);
                tableRow = findViewById(R.id.R12);
                break;
        }
    }

    // signing out of app and using intent going to login
    public void signOut(View view) {
        mAuth.signOut();
        Intent userSignOut = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(userSignOut);
    }
}
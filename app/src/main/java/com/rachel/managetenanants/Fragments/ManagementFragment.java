package com.rachel.managetenanants.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.rachel.managetenanants.Activities.MainActivity;
import com.rachel.managetenanants.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManagementFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManagementFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private Spinner dropdown_menu;
    private String[] choices;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ManagementFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ManagementFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManagementFragment newInstance(String param1, String param2) {
        ManagementFragment fragment = new ManagementFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_management, container, false);
        // create the drop down menu
        dropdown_menu = view.findViewById(R.id.dropdown_menu);
        choices = getResources().getStringArray(R.array.choices);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown_menu.setAdapter(adapter);
        dropdown_menu.setOnItemSelectedListener(this); // adding the event listener to the spinner
        return view;
    }


    // methods implemented for the adapter view interface
    // onSelected item helps as an event listener to choice in spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == dropdown_menu.getId())
        {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.handleSelection(view, position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
package com.rachel.managetenanants.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.rachel.managetenanants.Activities.MainActivity;
import com.rachel.managetenanants.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChoiceOfUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChoiceOfUserFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private RadioGroup choice;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChoiceOfUserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChoiceOfUserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChoiceOfUserFragment newInstance(String param1, String param2) {
        ChoiceOfUserFragment fragment = new ChoiceOfUserFragment();
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
        View view = inflater.inflate(R.layout.fragment_choice_of_user, container, false);

        choice = (RadioGroup) view.findViewById(R.id.RadioChoice);

        // getting the choice of the user, whether he's a tenant or  part of the home association

        // getting the views of the radio buttons
        RadioButton tenant = (RadioButton)view.findViewById(R.id.ChooseTenant);
        RadioButton homeAssociation = (RadioButton)view.findViewById(R.id.ChooseHomeOwnerAssociation);
        // defining a final array to allow it to change within the anonymous class.
        // the 1st index contains user choice or 0 if none chosen
        final int[] selectedId = {0};
        choice.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(RadioGroup group, int checkedId) {
                  if (tenant.isChecked()) {
                      selectedId[0] = 1;
                  } else if (homeAssociation.isChecked()) {
                      selectedId[0] = 2;
                  }
              }
          });

        // setting a button to activate the login activity from main activity
        Button send = view.findViewById(R.id.buttonNext);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                // passing the actual selection of the user
                mainActivity.loadLogin(selectedId[0]);
            }
        });
        return view;
    }

}
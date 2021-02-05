package com.rachel.managetenanants.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rachel.managetenanants.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeAssociationDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeAssociationDetailsFragment extends Fragment {

    private View view;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeAssociationDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeAssociationDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeAssociationDetailsFragment newInstance(String param1, String param2) {
        HomeAssociationDetailsFragment fragment = new HomeAssociationDetailsFragment();
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
        view = inflater.inflate(R.layout.fragment_home_association_details, container, false);
        return view;
    }

    // a function that returns the contents of the input of this fragment
    public String registerCallSeniority(){
        String seniority = ((EditText)view.findViewById(R.id.TextSeniority)).getText().toString();
        if (seniority.isEmpty())
            return null;
        return seniority;
    }
}
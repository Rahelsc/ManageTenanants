package com.rachel.managetenanants.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rachel.managetenanants.R;

import java.util.ArrayList;

public class BuildingDataAdapter extends RecyclerView.Adapter<BuildingDataAdapter.ViewHolder> {
    private ArrayList<BuildingIncomeDataModel> dataModalArrayList;
    private Context context;

    // constructor class for our Adapter
    public BuildingDataAdapter(ArrayList<BuildingIncomeDataModel> dataModalArrayList, Context context) {
        this.dataModalArrayList = dataModalArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public BuildingDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // passing our layout file for displaying our card item
        return new BuildingDataAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.building_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BuildingDataAdapter.ViewHolder holder, int position) {
        // setting data to our views in Recycler view items.
        BuildingIncomeDataModel modal = dataModalArrayList.get(position);
        holder.month.setText(modal.getMonth());
        holder.sum.setText(modal.getSum());
    }

    @Override
    public int getItemCount() {
        // returning the size of array list.
        return dataModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // creating variables for our
        // views of recycler items.
        private TextView month;
        private TextView sum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing the views of recycler views.
            month = itemView.findViewById(R.id.month);
            sum = itemView.findViewById(R.id.sum);
        }
    }
}

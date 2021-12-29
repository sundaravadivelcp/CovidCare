package com.example.CovidCare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserDataAdapter extends RecyclerView.Adapter<UserDataAdapter.ViewHolder>{

    Context context;
    List<UserData> userList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.db_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserDataAdapter.ViewHolder holder, int position) {
        holder.heartrate.setText(Integer.toString((int)this.userList.get(position).heartRate));
        holder.breathingrate.setText(Integer.toString((int) this.userList.get(position).breathingRate));
        holder.fever.setText(Integer.toString((int) this.userList.get(position).fever));
        holder.cough.setText(Integer.toString((int) this.userList.get(position).cough));
        holder.tiredness.setText(Integer.toString((int) this.userList.get(position).tiredness));
        holder.shortofbreath.setText(Integer.toString((int) this.userList.get(position).shortnessOfBreath));
        holder.muscleache.setText(Integer.toString((int) this.userList.get(position).muscleAches));
        holder.chills.setText(Integer.toString((int) this.userList.get(position).chills));
        holder.sorethroat.setText(Integer.toString((int) this.userList.get(position).soreThroat));
        holder.runnynose.setText(Integer.toString((int) this.userList.get(position).runningNose));
        holder.headache.setText(Integer.toString((int) this.userList.get(position).headache));
        holder.chestpain.setText(Integer.toString((int) this.userList.get(position).chestPain));

    }

    @Override
    public int getItemCount() {
        return this.userList.size();
    }

    public class  ViewHolder extends RecyclerView.ViewHolder{
        TextView heartrate, breathingrate, fever, cough, tiredness, shortofbreath, muscleache, chills, sorethroat, runnynose, headache, chestpain;
        public ViewHolder(View view){
            super(view);
            heartrate = view.findViewById(R.id.heart_rate);
            breathingrate = view.findViewById(R.id.breathing_rate);
            fever = view.findViewById(R.id.fever);
            cough = view.findViewById(R.id.cough);
            tiredness = view.findViewById(R.id.tiredness);
            shortofbreath = view.findViewById(R.id.short_of_breath);
            muscleache = view.findViewById(R.id.muscle_ache);
            chills = view.findViewById(R.id.chills);
            sorethroat = view.findViewById(R.id.sore_throat);
            runnynose = view.findViewById(R.id.runny_nose);
            headache = view.findViewById(R.id.headache);
            chestpain = view.findViewById(R.id.chest_pain);
        }
    }

    public UserDataAdapter(Context context, List<UserData> list) {
        this.context = context;
        this.userList = list;
    }
}
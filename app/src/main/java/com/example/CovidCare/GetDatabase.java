package com.example.CovidCare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class GetDatabase extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_database);
        recyclerView = findViewById(R.id.recyclerview);
        getData();

    }

    private void getData() {
        recyclerView.setAdapter(new UserDataAdapter(getApplicationContext(),UserDatabase.getInstance(getApplicationContext()).userInfoDao().getAllData()));
    }
}
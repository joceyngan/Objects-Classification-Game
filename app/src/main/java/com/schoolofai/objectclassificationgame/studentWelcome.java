package com.schoolofai.objectclassificationgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class studentWelcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_welcome);
        initView();
        initListener();
    }

    private void initView() {
    }

    private void initListener() {
        findViewById(R.id.enterRoomBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplication(), ClassifierActivity.class));
            }
        });
    }
}

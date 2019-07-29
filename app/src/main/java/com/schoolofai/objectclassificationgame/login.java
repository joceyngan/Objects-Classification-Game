package com.schoolofai.objectclassificationgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity implements View.OnClickListener {
    private Button buttonStudent, buttonTutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initListener();
    }

    private void initListener() {
        findViewById(R.id.studentBtn).setOnClickListener(this);
        findViewById(R.id.tutorBtn).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.studentBtn:
                startActivity(new Intent(this, studentWelcome.class));
                break;
            case R.id.tutorBtn:
                //Test
                break;
        }
    }
}

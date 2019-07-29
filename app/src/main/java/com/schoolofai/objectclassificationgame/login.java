package com.schoolofai.objectclassificationgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class login extends AppCompatActivity implements View.OnClickListener {
    private Button btnStudent, btnTutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnStudent = findViewById(R.id.btnStudent);
        btnTutor = findViewById(R.id.btnTutor);

        initListener();
    }

    private void initListener() {
        btnStudent.setOnClickListener(this);
        btnTutor.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnStudent:
                startActivity(new Intent(this, studentWelcome.class));
                break;
            case R.id.btnTutor:
                //Test
                break;
        }
    }
}

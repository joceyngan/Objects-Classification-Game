package com.schoolofai.objectclassificationgame;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.schoolofai.objectclassificationgame.student.studentBase;
import com.schoolofai.objectclassificationgame.tutor.tutorWelcome;

public class login extends AppCompatActivity implements View.OnClickListener {
    private Button btnStudent, btnTutor;
    private String m_Text = "",code="123";

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
                startActivity(new Intent(this, studentBase.class));
                break;
            case R.id.btnTutor:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Please enter the authorization codes\n");

                final EditText input = new EditText(this);

                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);
                builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            Log.e("Test", input.getText().toString());
                        if (m_Text.equals(code)){
                            startActivity(new Intent(getApplicationContext(), tutorWelcome.class));
                            m_Text="";
                        }
                        else {
                            aboutDialogBox();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                //builder.show();

                startActivity(new Intent(getApplicationContext(), tutorWelcome.class));
                break;
        }
    }
    public void aboutDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About");
        builder.setMessage("Incorrect code, please try again!");
        builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

}

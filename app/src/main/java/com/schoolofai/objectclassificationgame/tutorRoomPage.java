package com.schoolofai.objectclassificationgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class tutorRoomPage extends AppCompatActivity implements View.OnClickListener{

    private Button enterRoomBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_room_page);

        enterRoomBtn = (Button)findViewById(R.id.enterRoomBtn);
        initListener();

    }
    private void initListener() {
        enterRoomBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.enterRoomBtn:
                startActivity(new Intent(this, tutorWelcome.class));
                break;
        }
    }
}
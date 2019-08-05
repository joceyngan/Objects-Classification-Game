package com.schoolofai.objectclassificationgame.tutor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.Random;

import static com.schoolofai.objectclassificationgame.tutor.ReadyRoomFragment.listenerChange;

public class tutorWelcome extends AppCompatActivity implements View.OnClickListener {

    protected static String roomNum;

    protected static Button buttonCreate, buttonStart, buttonLeaveRoom;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_welcome);

        initView();
        initListener();

        getSupportFragmentManager().beginTransaction().add(R.id.tutorFragmentView, new RoomNumberFragment()).commit();
    }

    protected static void randomRoomNumber() {
        Random random = new Random();
        roomNum = Integer.toString(random.nextInt(8999) + 1000);

    }

    private void initView() {
        buttonCreate = findViewById(R.id.buttonCreate);
        buttonStart = findViewById(R.id.buttonStart);
        buttonLeaveRoom = findViewById(R.id.buttonLeaveRoom);

    }

    private void initListener() {
        buttonCreate.setOnClickListener(this);
        buttonCreate.setClickable(false);
        buttonStart.setOnClickListener(this);
        buttonStart.setClickable(false);
        buttonLeaveRoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCreate:
                buttonCreate.setVisibility(View.GONE);
                buttonStart.setVisibility(View.VISIBLE);
                buttonStart.setClickable(false);
                getSupportFragmentManager().beginTransaction().replace(R.id.tutorFragmentView, new ReadyRoomFragment()).commit();
                break;
            case R.id.buttonStart:
                Log.e("buttonStart", "Clicked");

                buttonStart.setVisibility(View.GONE);
                listenerChange.remove();
                break;
            case R.id.buttonLeaveRoom:
                break;
        }
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tutorWelcome.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        db.collection("rooms").document(roomNum).delete();
        super.onDestroy();
    }
}
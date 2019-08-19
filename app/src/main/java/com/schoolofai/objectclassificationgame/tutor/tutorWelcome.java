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
import com.google.firebase.firestore.SetOptions;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.Random;

import static com.schoolofai.objectclassificationgame.tutor.ReadyRoomFragment.listenerChange;

public class tutorWelcome extends AppCompatActivity implements View.OnClickListener {

    protected static String roomNum;
    protected static Room room;

    protected static Button buttonCreate, buttonStart, buttonLeaveRoom;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_welcome);

        initView();
        initListener();
        room = new Room();
        startService(new Intent(this, DeleteRoomOnQuit.class));

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
                buttonLeaveRoom.setVisibility(View.VISIBLE);
                listenerChange.remove();
                room.UpdateAllPlayerStatus(3);
                room.setStatus(1);
                db.collection("rooms").document(roomNum).set(room, SetOptions.merge());
                getSupportFragmentManager().beginTransaction().replace(R.id.tutorFragmentView, new PlayingRoomFragment()).commit();
                break;
            case R.id.buttonLeaveRoom:
                Log.e("buttonLeave", "Clicked");
                onBackPressed();
                break;
        }
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Warning!")
                .setMessage("Room will be closed when you exit.\nAre you sure want to exit?")
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
    protected void onStop() {

        Log.e("Stop", "Stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.collection("rooms").document(roomNum).delete();
        Log.e("Welcome onDestroy", "onDestroy");
        if (listenerChange !=null){
            listenerChange.remove();
        }
        room = null;
    }
}

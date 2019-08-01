package com.schoolofai.objectclassificationgame;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.List;

import javax.annotation.Nullable;

public class tutorRoomPage extends AppCompatActivity{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView listView;
    private Room room = new Room();
    private int roomNumber;
    private String TAG = "tutorRoomPage";

    private Button buttonStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_room_page);
        Intent intent = getIntent();
        roomNumber = intent.getIntExtra("roomNumber" , 0);
        Log.e(TAG, "room: " + roomNumber);

        listView = findViewById(R.id.teamList);

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                room.setStatus(1);
                db.collection("rooms").document(Integer.toString(roomNumber)).set(room);
            }
        });

        db.collection("rooms").document(Integer.toString(roomNumber)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Log.e("Listener Error", "34, ", e );
                    return;
                }
                room = documentSnapshot.toObject(Room.class);
                List<Player> players = room.getPlayers();

                Log.e(TAG, "Player: " + players.size());

            }
        });

    }
}
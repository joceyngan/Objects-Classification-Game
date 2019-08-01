package com.schoolofai.objectclassificationgame;


import android.os.Bundle;
import android.util.Log;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_room_page);

        listView = findViewById(R.id.teamList);

        db.collection("rooms").document("1234").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Log.e("Listener Error", "34, ", e );
                    return;
                }
                room = documentSnapshot.toObject(Room.class);
                List<Player> players = room.getPlayers();
            }
        });

    }
}
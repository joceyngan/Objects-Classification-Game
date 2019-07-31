package com.schoolofai.objectclassificationgame;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.ArrayList;
import java.util.List;

public class studentWelcome extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Room room;
    private EditText roomNumEditText, playerName;
    private List<Player> playerList;
    private Player player;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_welcome);

        player = new Player();

        initView();
        initListener();
    }

    private void initView() {
        roomNumEditText = findViewById(R.id.roomNumEditText);
        playerName = findViewById(R.id.playerName);
    }

    private void initListener() {
        findViewById(R.id.enterRoomBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinRoom();
            }
        });
    }

    private void joinRoom() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Joining Room");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.show();
        db.collection("rooms").document(roomNumEditText.getText().toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                progressDialog.dismiss();
                if (documentSnapshot.exists()){
                    room = documentSnapshot.toObject(Room.class);
                    //System.out.println(room.getRoomId());
                    player.setPlayerName(playerName.getText().toString());
                    playerList = new ArrayList<>();
                    if (room.getPlayers() != null){
                        playerList = room.getPlayers();
                    }
                    playerList.add(player);
                    db.collection("rooms").document(roomNumEditText.getText().toString()).update("players", playerList).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(getApplicationContext(), "Join success", Toast.LENGTH_LONG).show();

                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Room not exist", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

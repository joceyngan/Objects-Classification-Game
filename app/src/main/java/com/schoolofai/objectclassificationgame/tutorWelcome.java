package com.schoolofai.objectclassificationgame;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.List;

public class tutorWelcome extends AppCompatActivity implements View.OnClickListener {

    private Button enterRoomBtn;
    private EditText roomNumEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog progressDialog;
    private String roomNumString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_welcome);

        enterRoomBtn = findViewById(R.id.enterRoomBtn);
        roomNumEditText = findViewById(R.id.roomNumEditText);
        initListener();


    }

    private void initListener() {
        enterRoomBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.enterRoomBtn:
                roomNumString = roomNumEditText.getText().toString();
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Creating Room");
                progressDialog.setMessage("Please Wait");
                progressDialog.setCancelable(false);
                progressDialog.show();
                db.collection("rooms").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean checked = true;
                        System.out.println(queryDocumentSnapshots.size());

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.getId().equals(roomNumString)) {
                                checked = false;
                            }
                        }
                        if (checked){
                            createRoom();
                        }else{
                            Toast.makeText(getApplicationContext(), "Room Number is exist\nPlease choose an other one", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    }
                });
                break;
        }
    }

    private void createRoom() {
        int roomNum = Integer.parseInt(roomNumString);
        Room room = new Room();
        room.setRoomId(roomNum);
        db.collection("rooms").document(Integer.toString(roomNum)).set(room).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                startActivity(new Intent(getApplicationContext(), tutorRoomPage.class));
            }
        });
    }
}

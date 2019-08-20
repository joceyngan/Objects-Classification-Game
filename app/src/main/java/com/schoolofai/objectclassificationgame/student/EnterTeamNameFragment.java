package com.schoolofai.objectclassificationgame.student;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;
import com.schoolofai.objectclassificationgame.tutor.RoomNumberFragment;
import com.schoolofai.objectclassificationgame.tutor.tutorWelcome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.schoolofai.objectclassificationgame.student.studentBase.player;


public class EnterTeamNameFragment extends Fragment {

    private EditText editTextTeamName;
    private Button buttonEnterTeamName;
    private DocumentReference[] documentReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int count = 0,  equalcount=0;
    private String key="";
    private DocumentSnapshot[] snapshot;
    public EnterTeamNameFragment() {
    }
    public Boolean test(String input){
        Log.d("LOGGER","1");

         db.collection("player").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    documentReference= new DocumentReference [task.getResult().size()];
                    snapshot = new DocumentSnapshot[documentReference.length];
                    for (DocumentSnapshot document : task.getResult()) {
                        snapshot[count] = document;
                        String playerName1 = snapshot[count].getString("playerName");
                        if (!input.equalsIgnoreCase(playerName1)) {
                            equalcount++;
                        }
                        count++;
                    }
                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            Log.d("LOGGER", "333SIZE="+Integer.toString(equalcount));
                            if(equalcount==count) {
                                Log.d("LOGGER", "4=" + input);
                                Map<String, Object> playerName = new HashMap<>();
                                playerName.put("playerName", input);
                                key = db.collection("player").document().getId();
                                DocumentReference doc = db.collection("player").document(key);
                                transaction.set(doc, playerName);
                                Log.d("LOGGER", "70=Successful=" + key);
                            }
                            return null;
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("SUCCESS", "Transaction success!");
                                if(equalcount==count) {
                                    player.setPlayerName(input);
                                    Log.d("LOGGER", "80=Successful=" + key);
                                    player.setPlayerUid(key);
                                    getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment()).commit();
                                }else{
                                    editTextTeamName.setError("Team Name is exisit!");
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                editTextTeamName.setError("Team Name is exisit!");
                                Log.e("FAIL", "Transaction failure.", e);
                            }
                        });
                } else {
                    Log.d("ERROR", "Error getting documents: ", task.getException());
                }
            }
        });
         count=0;
        equalcount=0;
         return true;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.enter_teamname_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        editTextTeamName = view.findViewById(R.id.editTextTeamName);
        buttonEnterTeamName = view.findViewById(R.id.buttonEnterTeamName);
        buttonEnterTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String teamName = editTextTeamName.getText().toString();
                if (teamName.equals("")){
                    editTextTeamName.setError("Team Name cannot blank");
                    return;
                }else if (teamName.length() > 12){
                    editTextTeamName.setError("Team Name cannot longer then 12 character");
                    return;
                }
                editTextTeamName.setError(null);
                test(teamName);
            }
        });

    }
}

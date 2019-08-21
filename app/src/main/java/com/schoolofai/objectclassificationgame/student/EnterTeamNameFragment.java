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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.login;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;
import com.schoolofai.objectclassificationgame.tutor.RoomNumberFragment;
import com.schoolofai.objectclassificationgame.tutor.tutorWelcome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.schoolofai.objectclassificationgame.student.studentBase.player;



public class EnterTeamNameFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText editTextTeamName;
    private Button buttonEnterTeamName;
    private DocumentReference documentReference;
    private DocumentSnapshot documentSnapshot;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int count = 0,  equalcount=0;
    private String key="";
    private DocumentSnapshot[] snapshot;
    public EnterTeamNameFragment() {
    }
    public Boolean test(String input){
        Log.d("LOGGER","1");
        documentReference = db.collection("player").document("playerDoc");
                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            documentSnapshot = transaction.get(documentReference);
                            Map<String, String>data =(Map<String, String>)documentSnapshot.getData().get("player");
                            Log.d("TEST","data="+data.toString());
                            if(!data.containsValue(input)) {
                                Map<String, String>inputdata = new HashMap<>();
                                inputdata.putAll(data);
                                inputdata.remove(mAuth.getCurrentUser().getUid());
                                inputdata.put(mAuth.getCurrentUser().getUid(), input);
                                transaction.update(documentReference,"player",inputdata);
                            }
                            return null;
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("SUCCESS", "Transaction success!");
                                    player.setPlayerName(input);
                                    Log.d("LOGGER", "80=Successful=" + mAuth.getCurrentUser().getUid());
                                    player.setPlayerUid(mAuth.getCurrentUser().getUid());
                                    getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment()).commit();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                editTextTeamName.setError("Team Name is exisit!");
                                Log.e("FAIL", "Transaction failure.", e);
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
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.signInAnonymously()
                .addOnCompleteListener( getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LOGGER", "signInAnonymously:success");
                            Log.d("LOGGER","Uid="+currentUser.getUid());
                            player.setPlayerUid(currentUser.getUid());
                            // FirebaseUser user = mAuth.getCurrentUser();
                            //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LOGGER", "signInAnonymously:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            // updateUI(null);
                        }
                        // ...
                    }
                });
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

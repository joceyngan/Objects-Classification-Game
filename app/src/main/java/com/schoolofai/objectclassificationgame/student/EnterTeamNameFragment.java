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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.schoolofai.objectclassificationgame.student.studentBase.player;


public class EnterTeamNameFragment extends Fragment {
    private String TAG = "EnterTeamNameFragment";
    private EditText editTextTeamName;
    private Button buttonEnterTeamName;
    private DocumentReference documentReference;
    private DocumentSnapshot documentSnapshot;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public EnterTeamNameFragment() {
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
                if (teamName.equals("")) {
                    editTextTeamName.setError("Team Name cannot blank");
                    return;
                } else if (teamName.length() > 12) {
                    editTextTeamName.setError("Team Name cannot longer then 12 character");
                    return;
                }
                editTextTeamName.setError(null);
                player.setPlayerName(teamName);
                checkTeamName(teamName);
            }
        });
    }

    private void checkTeamName(String teamName) {
        documentReference = db.collection("players").document(teamName);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            documentSnapshot = transaction.get(documentReference);
            if (!documentSnapshot.exists()) {
                Log.w(TAG, "null document");
                transaction.set(documentReference, player);
                getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment()).commit();
                return null;
            } else {
                Timestamp timestamp = (Timestamp) documentSnapshot.get("expireDate");
                Timestamp now = new Timestamp(new Date());

                if (player.getPlayerUid().equals(documentSnapshot.get("playerUid"))) {
                    transaction.set(documentReference, player);
                    getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment()).commit();
                    return null;
                } else if (timestamp.getSeconds() < now.getSeconds()) {
                    Log.w(TAG, timestamp.getSeconds() + "Now: " + now.getSeconds() + "");
                    transaction.set(documentReference, player);
                    getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment()).commit();
                    return null;
                }
                throw new FirebaseFirestoreException("Population too high",
                        FirebaseFirestoreException.Code.ABORTED);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                editTextTeamName.setError("Team Name is exisit!");
                Log.e("FAIL", "Transaction failure.", e);
            }
        });
    }

}

package com.schoolofai.objectclassificationgame.student;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;
import com.schoolofai.objectclassificationgame.ClassifierActivity;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.List;

import static com.schoolofai.objectclassificationgame.student.studentBase.currentroom;
import static com.schoolofai.objectclassificationgame.student.studentBase.player;

public class WaitingRoomFragment extends Fragment {
    TextView tvRoomNumber;
    TextView tvRuleOne;
    TextView tvStatus;
    Button btnReady;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private ListenerRegistration listenerChange;
    private Context context;

    int readyNumber;

    public WaitingRoomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getContext();
        return inflater.inflate(R.layout.fragment_waiting_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRoomNumber = view.findViewById(R.id.roomNumberTv);
        tvRuleOne = view.findViewById(R.id.ruleOneTv);
        tvStatus = view.findViewById(R.id.readyStatustv);

        tvRoomNumber.setText("Room Number " + currentroom.getRoomId());
        String numberString = getColoredSpanned("10", "#4f8f00");
        String ruleOne = "There are " + numberString + " items\nyou need to find.";
        tvRuleOne.setText(Html.fromHtml(ruleOne));

        UpdateReadyNumber();

        documentReference = db.collection("rooms").document(currentroom.getRoomId());
        listenerChange = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (documentSnapshot != null) {
                    currentroom = documentSnapshot.toObject(Room.class);
                    UpdateReadyNumber();
                    if (currentroom.getStatus() == 1) {
                        listenerChange.remove();
                        Intent intent = new Intent(context, ClassifierActivity.class);
                        intent.putExtra("roomNumber", currentroom.getRoomId());
                        intent.putExtra("player", player);
                        startActivity(intent);
                    }
                }
            }
        });

        btnReady = view.findViewById(R.id.btnReady);
        btnReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(documentReference);
                        currentroom = snapshot.toObject(Room.class);
                        currentroom.UpdateStatus(player.getPlayerUid(), 1);
                        transaction.set(documentReference, currentroom);
                        return null;
                    }
                });
            }
        });


    }

    private void UpdateReadyNumber() {
        List<Player> playerlist = currentroom.getPlayers();
        readyNumber = 0;
        for (Player player : playerlist) {
            if (player.getStatus() == 1) {
                readyNumber++;
            }
        }
        tvStatus.setText(readyNumber + " / " + playerlist.size());
    }

    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    @Override
    public void onDetach() {
        if (listenerChange != null) listenerChange.remove();
        super.onDetach();
    }
}

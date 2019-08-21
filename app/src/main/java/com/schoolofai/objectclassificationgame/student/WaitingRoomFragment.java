package com.schoolofai.objectclassificationgame.student;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.Timer;
import java.util.TimerTask;

import static com.schoolofai.objectclassificationgame.student.studentBase.currentroom;
import static com.schoolofai.objectclassificationgame.student.studentBase.player;

public class WaitingRoomFragment extends Fragment  implements IOnBackPressed{
    public android.app.Fragment commit;
    private TextView tvRoomNumber;
    private TextView tvRuleOne;
    private TextView tvStatus;
    private Button btnReady;
private List<Player> playerList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    private ListenerRegistration listenerChange;
    private Context context;
    private LinearLayout linearLayout, countTimeLayout;
    private TextView countTime;

    private int readyNumber;

    private Timer timer;

    public WaitingRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public boolean onBackPressed() {
        if (getFragmentManager().findFragmentByTag("WaitingRoomFragment") != null && getFragmentManager().findFragmentByTag("WaitingRoomFragment").isVisible()){
            //do remove function here
            player.getPlayerUid();
            Log.e("UID", player.getPlayerUid());
            documentReference = db.collection("rooms").document(currentroom.getRoomId());
            db.runTransaction((Transaction.Function<Void>) transaction -> {
            playerList.remove(player.getPlayerUid());
                transaction.update(documentReference, "players", playerList);
                //
                return null;
            });

            getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment(),"RoomListFragment").commit();
        }
        return true;
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
        linearLayout = view.findViewById(R.id.linearLayout9);
        countTimeLayout = view.findViewById(R.id.countTimeLayout);
        countTime = view.findViewById(R.id.countTimeTv);

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
                    if (currentroom !=null){
                        Log.e("currentroom", "not null");
                        UpdateReadyNumber();
                        if (currentroom.getStatus() == 1) {
                            listenerChange.remove();
                            linearLayout.setVisibility(View.GONE);
                            btnReady.setVisibility(View.GONE);
                            tvRoomNumber.setVisibility(View.GONE);
                            countTimeLayout.setVisibility(View.VISIBLE);
                            countTime.setText("3");
                            new CountDownTimer(4000, 100) {

                                public void onTick(long millisUntilFinished) {
                                    if (millisUntilFinished < 1000){
                                        countTime.setText("GO");
                                    }else if(millisUntilFinished < 2000){
                                        countTime.setText("1");
                                    }else if (millisUntilFinished < 3000){
                                        countTime.setText("2");
                                    }else {
                                        countTime.setText("3");
                                    }
                                }

                                public void onFinish() {
                                    Intent intent = new Intent(context, ClassifierActivity.class);
                                    intent.putExtra("roomNumber", currentroom.getRoomId());
                                    intent.putExtra("player", player);
                                    startActivity(intent);
                                    getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment(),"RoomListFragment").commit();
                                }
                            }.start();


                        }
                    }else{
                        listenerChange.remove();
                        getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment(),"RoomListFragment").commit();
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

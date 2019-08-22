package com.schoolofai.objectclassificationgame.customview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TeamListAdapter extends ArrayAdapter<Player> {

    private Context context;
    private List<Player> playerList;
    private Button btnKick;

    private DocumentReference documentReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Room room;
    private Player player;
    private ListenerRegistration listenerChange;

    private int arrayIndex;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        player = playerList.get(position);

        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.item_teamlist, parent, false);

        TextView tvTeamName = listItem.findViewById(R.id.tvTeamName);
        TextView tvStatus = listItem.findViewById(R.id.tvStatus);
        btnKick = listItem.findViewById(R.id.btnKick);
        btnKick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAlertDialog();
            }
        });

        tvTeamName.setText(player.getPlayerName());
        switch (player.getStatus()) {
            case 0:
                tvStatus.setText("Waiting");
                break;
            case 1:
                tvStatus.setText("Ready");
                break;
            case 2:
                tvStatus.setText("Status: Completed");
                break;
        }
        ;
        return listItem;
    }

    public TeamListAdapter(@NonNull Context context, List<Player> list, Room room) {
        super(context, R.layout.item_teamlist, list);
        this.context = context;
        this.playerList = list;
        this.room = room;
    }

    private void displayAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Are you sure to kick " + player.getPlayerName() + "?\n");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                documentReference = db.collection("rooms").document(room.getRoomId());
                db.runTransaction((Transaction.Function<Void>) transaction -> {
                    DocumentSnapshot snapshot = transaction.get(documentReference);
                    Room roomtmp = snapshot.toObject(Room.class);
                    roomtmp.kickPlayer(player.getPlayerUid());
                    transaction.set(documentReference, roomtmp);
                    return null;
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("SUCCESS", "Transaction success!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("FAIL", "Transaction failure.", e);
                    }
                });
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).show();
    }
}

package com.schoolofai.objectclassificationgame.student;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.schoolofai.objectclassificationgame.ClassifierActivity;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.customview.RoomListAdapter;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.ArrayList;
import java.util.List;

import static com.schoolofai.objectclassificationgame.student.studentBase.currentroom;
import static com.schoolofai.objectclassificationgame.student.studentBase.player;

public class RoomListFragment extends Fragment {

    private ListView listView;
    private List<Room> roomlist;
    private List<Player> playerList;
    private RoomListAdapter roomListAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private DocumentReference documentReference;
    private ListenerRegistration listenerChange;

    private Button btnSinglePlay;

    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        context = getContext();
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.room_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.roomListView);
        collectionReference = db.collection("rooms");
        listenerChange = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots.size() > 0){
                    roomlist = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                        Room roomtmp = documentSnapshot.toObject(Room.class);
                        if (roomtmp.getPlayers().size() < 4 && roomtmp.getStatus()==0){
                            roomlist.add(roomtmp);
                        }
                    }
                    roomListAdapter = new RoomListAdapter(view.getContext(), roomlist);
                    listView.setAdapter(roomListAdapter);
                }
            }
        });
        btnSinglePlay = view.findViewById(R.id.btnSinglePlay);
        btnSinglePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ClassifierActivity.class);
                intent.putExtra("roomNumber", currentroom.getRoomId());
                intent.putExtra("player", player);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            currentroom = roomlist.get(position);
            documentReference = db.collection("rooms").document(roomlist.get(position).getRoomId());
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot snapshot = transaction.get(documentReference);

                Room roomtmp = snapshot.toObject(Room.class);
                playerList = new ArrayList<>();
                if (roomtmp.getPlayers() != null) {
                    playerList = roomtmp.getPlayers();
                }
                Log.e("Playerlist1", Integer.toString(playerList.size()));
                if (playerList.size() < 4 && roomtmp.getStatus() == 0){
                    Log.e("Playerlist2", Integer.toString(playerList.size()));
                    player.setPlayerUid(Integer.toString(playerList.size()));
                    playerList.add(player);
                    transaction.update(documentReference, "players", playerList);
                    Log.e("Playerlist1", Integer.toString(playerList.size()));
                }else{
                    Log.e("Fail", "Team Full");
                }


                return null;
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e("SUCCESS", "Transaction success!");
                    listenerChange.remove();
                    getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new WaitingRoomFragment(),"WaitingRoomFragment").commit();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,"Team is full", Toast.LENGTH_LONG).show();
                    Log.e("FAIL", "Transaction failure.", e);
                }
            });

        });

    }
}

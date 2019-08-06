package com.schoolofai.objectclassificationgame.student;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.schoolofai.objectclassificationgame.ClassifierActivity;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.customview.RoomListAdapter;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.ArrayList;
import java.util.List;

import static com.schoolofai.objectclassificationgame.student.studentBase.player;

public class RoomListFragment extends Fragment {

    private ListView listView;
    private List<Room> roomlist = new ArrayList<>();
    private List<Player> playerList;
    private RoomListAdapter roomListAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private DocumentReference documentReference;

    protected static ListenerRegistration listenerChange;

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
                    for (QueryDocumentSnapshot documentSnapshot: queryDocumentSnapshots){
                         roomlist.add(documentSnapshot.toObject(Room.class));
                    }
                    roomListAdapter = new RoomListAdapter(view.getContext(), roomlist);
                    listView.setAdapter(roomListAdapter);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                documentReference = db.collection("rooms").document(roomlist.get(position).getRoomId());
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Room room = documentSnapshot.toObject(Room.class);
                            playerList = new ArrayList<>();
                            if (room.getPlayers() != null) {
                                playerList = room.getPlayers();
                            }
                            player.setPlayerUid(Integer.toString(playerList.size()));
                            playerList.add(player);
                            documentReference.update("players", playerList).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    listenerChange.remove();
                                    listenerChange = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.e("Listener Error", "34, ", e);
                                                return;
                                            }

                                            Room room = documentSnapshot.toObject(Room.class);
                                            if (room != null){
                                                if (room.getStatus() == 1) {
                                                    Log.e("RoomList" , "Start");
                                                    //startActivity(new Intent(getApplicationContext(), ClassifierActivity.class));
                                                }
                                            }
                                        }
                                    });

                                }
                            });
                        } else {
                            //Toast.makeText(getApplicationContext(), "Room not exist", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
}

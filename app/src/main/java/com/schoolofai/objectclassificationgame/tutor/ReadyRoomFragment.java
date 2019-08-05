package com.schoolofai.objectclassificationgame.tutor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.customview.TeamListAdapter;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.List;

import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.buttonStart;
import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.roomNum;

public class ReadyRoomFragment extends Fragment {
    private ListView listView;
    private Room room = new Room();
    private TextView roomNumberTextView, readyValueTv;
    private TeamListAdapter teamListAdapter;
    private int playerCount, readyCount;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    protected static ListenerRegistration listenerChange;


    public ReadyRoomFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ready_room_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomNumberTextView = view.findViewById(R.id.roomNumberTextView);
        readyValueTv = view.findViewById(R.id.readyValueTv);
        roomNumberTextView.setText("Rooms " + roomNum);
        documentReference = db.collection("rooms").document(roomNum);

        listView = view.findViewById(R.id.playerList);

        listenerChange = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null){
                    Log.e("Listener Error", "34, ", e );
                    return;
                }
                room = documentSnapshot.toObject(Room.class);
                if (room != null){
                    List<Player> players = room.getPlayers();
                    teamListAdapter = new TeamListAdapter(view.getContext(), players);
                    listView.setAdapter(teamListAdapter);
                    playerCount = players.size();
                    readyCount = 0;
                    for (Player player: players){
                        if (player.getStatus()==1){
                            readyCount++;
                        }
                    }
                    readyValueTv.setText(readyCount + " / " + playerCount);
                    if (readyCount == playerCount && readyCount !=0){
                        buttonStart.setClickable(true);
                    }
                }
            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}

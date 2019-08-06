package com.schoolofai.objectclassificationgame.tutor;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
import com.schoolofai.objectclassificationgame.customview.PlayingListAdapter;
import com.schoolofai.objectclassificationgame.customview.TeamListAdapter;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

import java.util.List;

import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.buttonStart;
import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.roomNum;

public class PlayingRoomFragment extends Fragment {


    private ListView listView;
    private Room room = new Room();
    private PlayingListAdapter playingListAdapter;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;
    protected static ListenerRegistration listenerChange;


    public PlayingRoomFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.playing_room_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.playerList);

        documentReference = db.collection("rooms").document(roomNum);

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
                    playingListAdapter = new PlayingListAdapter(view.getContext(), players);
                    listView.setAdapter(playingListAdapter);
                }
            }
        });
    }
}

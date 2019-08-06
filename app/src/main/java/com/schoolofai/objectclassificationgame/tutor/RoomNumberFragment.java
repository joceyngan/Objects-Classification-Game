package com.schoolofai.objectclassificationgame.tutor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Room;

import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.buttonCreate;
import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.randomRoomNumber;
import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.roomNum;
import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.room;

public class RoomNumberFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public RoomNumberFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.room_number_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db.collection("rooms").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean checked = false;
                        randomRoomNumber();
                        while (true){
                            if (queryDocumentSnapshots.size() > 0){
                                for (QueryDocumentSnapshot doc: queryDocumentSnapshots){
                                    if (doc.getId().equals(roomNum)){
                                        randomRoomNumber();
                                        checked = false;
                                        break;
                                    }else{
                                        checked = true;
                                    }
                                }
                            }else{
                                break;
                            }
                            if (checked){
                                break;
                            }
                        }
                        room.setRoomId(roomNum);
                        db.collection("rooms").document(roomNum).set(room);
                        TextView roomNumberTextView = view.findViewById(R.id.roomNumberTextView);
                        roomNumberTextView.setText(roomNum);
                        buttonCreate.setClickable(true);
                    }
                });
    }
}


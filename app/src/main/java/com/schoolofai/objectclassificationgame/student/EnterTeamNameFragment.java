package com.schoolofai.objectclassificationgame.student;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;
import com.schoolofai.objectclassificationgame.tutor.RoomNumberFragment;

import java.util.ArrayList;

import static com.schoolofai.objectclassificationgame.student.studentBase.player;

public class EnterTeamNameFragment extends Fragment {

    private EditText editTextTeamName;
    private Button buttonEnterTeamName;
    private DocumentReference documentReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;
    private String name;
    public EnterTeamNameFragment() {

    }

    public void test(){
        Log.d("LOGGER","1");
        documentReference = db.collection("player").document("01");
        Log.d("LOGGER","2");
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            Log.d("LOGGER","3");
            DocumentSnapshot snapshot = transaction.get(documentReference);
            Log.d("LOGGER","4");
            String nametmp = snapshot.toString();

            Log.d("LOGGER",nametmp);
            if (new Player(nametmp).getPlayerName() != null) {
                name = new Player(nametmp).getPlayerName();

            }
            return null;
        });

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
        test();
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

                player.setPlayerName(editTextTeamName.getText().toString());
                getFragmentManager().beginTransaction().replace(R.id.studentFragmentLayout, new RoomListFragment()).commit();
            }
        });
    }
}

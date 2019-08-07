package com.schoolofai.objectclassificationgame.student;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.tutor.RoomNumberFragment;

import static com.schoolofai.objectclassificationgame.student.studentBase.player;

public class EnterTeamNameFragment extends Fragment {

    private EditText editTextTeamName;
    private Button buttonEnterTeamName;

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

package com.schoolofai.objectclassificationgame.student;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

public class studentBase extends AppCompatActivity {
    protected static Player player = new Player();
    protected static Room currentroom = new Room();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_base);
        getSupportFragmentManager().beginTransaction().add(R.id.studentFragmentLayout, new EnterTeamNameFragment()).commit();
    }
}

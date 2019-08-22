package com.schoolofai.objectclassificationgame.student;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;

public class studentBase extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    protected static Player player = new Player();
    protected static Room currentroom = new Room();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_base);
        if (user == null){
            mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    user = authResult.getUser();
                    if (user != null) {
                        player.setPlayerUid(user.getUid());
                    }
                }
            });
        }else{
            player.setPlayerUid(user.getUid());
        }
        getSupportFragmentManager().beginTransaction().add(R.id.studentFragmentLayout, new EnterTeamNameFragment()).commit();
    }
}

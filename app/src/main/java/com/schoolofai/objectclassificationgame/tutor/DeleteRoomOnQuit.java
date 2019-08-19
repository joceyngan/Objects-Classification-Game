package com.schoolofai.objectclassificationgame.tutor;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.schoolofai.objectclassificationgame.tutor.tutorWelcome.roomNum;

public class DeleteRoomOnQuit extends Service {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isDelete = false;
    private String roomNumForDelete;
    private SharedPreferences sharedPreferences;

    public DeleteRoomOnQuit() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("service", MODE_PRIVATE);
        isDelete = sharedPreferences.getBoolean("isDelete", false);
        roomNumForDelete = sharedPreferences.getString("roomNum", "0");
        System.out.println(isDelete);
        System.out.println(roomNumForDelete);
        Log.w("DeleteRoomOnQuit", "Service Started");
        if (isDelete){
            db.collection("rooms").document(roomNumForDelete).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Log.w("Firebase:" , "Success");
                    }else{
                        Log.w("Firebase:" , "Failure");
                    }
                    if (task.isCanceled()){
                        Log.w("Firebase:" , "Cancel");
                    }
                    sharedPreferences.edit()
                            .putBoolean("isDelete", false)
                            .apply();
                }
            });
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.w("DeleteRoomOnQuit", "Rebind");
    }

    @Override
    public void onDestroy() {
        Log.w("DeleteRoomOnQuit", "Service Destoryed");

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.w("DeleteRoomOnQuit", "Service Task Removed");
        sharedPreferences.edit()
                .putBoolean("isDelete", true)
                .putString("roomNum", roomNum)
                .apply();

    }
}

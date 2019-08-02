package com.schoolofai.objectclassificationgame.customview;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamListAdapter extends ArrayAdapter<Player> {

    private Context context;
    private List<Player> playerList;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        Player player = playerList.get(position);

        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.item_teamlist, parent,false);

        TextView tvTeamName = listItem.findViewById(R.id.tvTeamName);
        TextView tvStatus = listItem.findViewById(R.id.tvStatus);
        TextView tvCompleted = listItem.findViewById(R.id.tvCompleted);
        if ((position +1 ) % 2 == 0){
            listItem.setBackgroundColor(Color.parseColor("#f2f1ef"));
        }

        tvTeamName.setText(player.getPlayerName());
        tvCompleted.setText("Completed:" + player.getCompletedItem() + " / 10");
        switch (player.getStatus()){
            case 0:
                tvStatus.setText("Status: Waiting Start");
                break;
            case 1:
                tvStatus.setText("Status: Playing");
                break;
            case 2:
                tvStatus.setText("Status: Completed");
                break;
        };
        return listItem;
    }

    public TeamListAdapter(@NonNull Context context, List<Player> list) {
        super(context, R.layout.item_teamlist, list);
        this.context = context;
        this.playerList = list;
    }
}

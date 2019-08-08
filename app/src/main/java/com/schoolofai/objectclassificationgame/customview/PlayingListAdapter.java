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

import java.util.Comparator;
import java.util.List;

public class PlayingListAdapter extends ArrayAdapter<Player> {

    private Context context;
    private List<Player> playerList;

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        playerList.sort(Comparator.comparing(Player::getCompletedTime));
        View listItem = convertView;
        Player player = playerList.get(position);

        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.item_playinglist, parent,false);

        TextView tvTeamName = listItem.findViewById(R.id.tvTeamName);
        TextView tvCompleted = listItem.findViewById(R.id.tvCompleted);
        TextView tvStatus = listItem.findViewById(R.id.tvStatus);



        tvCompleted.setText(player.getCompletedItem() + " / 10");
        tvTeamName.setText(player.getPlayerName());
        switch (player.getStatus()){
            case 0:
                tvStatus.setText("Waiting");
                break;
            case 1:
                tvStatus.setText("Ready");
                break;
            case 2:
                tvCompleted.setText("Completed");
                tvStatus.setText(player.getCompletedTime());
                break;
            case 3:
                tvStatus.setText("Playing");
                break;
            case 4:
                tvStatus.setText("Left");
                break;
            case 5:
                break;
        };
        return listItem;
    }

    public PlayingListAdapter(@NonNull Context context, List<Player> list) {
        super(context, R.layout.item_teamlist, list);
        this.context = context;
        this.playerList = list;
    }

}

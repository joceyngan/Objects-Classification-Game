package com.schoolofai.objectclassificationgame.customview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.schoolofai.objectclassificationgame.R;
import com.schoolofai.objectclassificationgame.models.Player;
import com.schoolofai.objectclassificationgame.models.Room;


import java.util.List;

public class RoomListAdapter extends ArrayAdapter<Room> {

    private Context context;
    private List<Room> roomlist;

    public RoomListAdapter(@NonNull Context context, List<Room> roomlist) {
        super(context, R.layout.item_roomlist, roomlist);
        this.context = context;
        this.roomlist = roomlist;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        Room room = roomlist.get(position);

        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.item_roomlist, parent,false);

        TextView roomNumber = listItem.findViewById(R.id.roomNumber);
        TextView playertv = listItem.findViewById(R.id.players);

        roomNumber.setText(room.getRoomId());
        playertv.setText(room.getPlayers().size() + " / 4");

        return listItem;
    }
}

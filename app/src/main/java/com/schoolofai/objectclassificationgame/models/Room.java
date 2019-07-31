package com.schoolofai.objectclassificationgame.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private int roomId;
    private List<Player> players = new ArrayList<>();

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void joinRoom(Player player){
        players.add(player);
    }

    public Room() {
    }
}

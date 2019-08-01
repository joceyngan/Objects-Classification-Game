package com.schoolofai.objectclassificationgame.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private int roomId;
    private List<Player> players = new ArrayList<>();
    private int status = 0;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

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

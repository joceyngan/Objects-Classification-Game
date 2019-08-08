package com.schoolofai.objectclassificationgame.models;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomId;
    private List<Player> players = new ArrayList<>();
    private int status = 0;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
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

    public void UpdateStatus(String uid, int status){
        Player playertmp = players.get(Integer.parseInt(uid));
        playertmp.setStatus(status);
        players.set(Integer.parseInt(uid), playertmp);
    }

    public void UpdateAllPlayerStatus(int status){
        for (Player player: players){
            player.setStatus(status);
        }
    }

    public void UpdateCompleted(String uid, int completed){
        Player playertmp = players.get(Integer.parseInt(uid));
        playertmp.setCompletedItem(completed);
        players.set(Integer.parseInt(uid), playertmp);
    }

    public void UpdateCompleteTime(String playerUid, String finishTime) {
        Player playertmp = players.get(Integer.parseInt(playerUid));
        playertmp.setCompletedTime(finishTime);
        players.set(Integer.parseInt(playerUid), playertmp);

    }
}

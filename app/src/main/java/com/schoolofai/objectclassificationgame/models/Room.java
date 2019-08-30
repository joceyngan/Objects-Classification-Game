package com.schoolofai.objectclassificationgame.models;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Room {
    private String roomId;
    private List<Player> players = new ArrayList<>();
    private int status = 0;
    private Timestamp timestamp = new Timestamp(new Date());



    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

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
        int idx = findIndexByUid(uid);
        Player playertmp = players.get(idx);
        playertmp.setStatus(status);
        players.set(idx, playertmp);
    }

    public void UpdateAllPlayerStatus(int status){
        for (Player player: players){
            player.setStatus(status);
        }
    }

    public void UpdateCompleted(String uid, int completed){
        int idx = findIndexByUid(uid);
        Player playertmp = players.get(idx);
        playertmp.setCompletedItem(completed);
        players.set(idx, playertmp);
    }

    public void UpdateCompleteTime(String playerUid, String finishTime) {
        int idx = findIndexByUid(playerUid);
        Player playertmp = players.get(idx);
        playertmp.setCompletedTime(finishTime);
        players.set(idx, playertmp);

    }
    private int findIndexByUid(String uid){
        int idx = 0;
        for (Player checkPlayer : players){
            if (checkPlayer.getPlayerUid().equals(uid)){
                break;
            }
            idx++;
        }
        return idx;
    }

    public void kickPlayer(String uid) {
        int idx = findIndexByUid(uid);
        players.remove(idx);
    }
}

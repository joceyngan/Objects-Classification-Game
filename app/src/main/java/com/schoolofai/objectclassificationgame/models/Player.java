package com.schoolofai.objectclassificationgame.models;

import com.google.firebase.Timestamp;

public class Player {
    private String playerName;
    private int completedItem = 0;
    private int status;
    private Timestamp completedTime;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Timestamp completedTime) {
        this.completedTime = completedTime;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getCompletedItem() {
        return completedItem;
    }

    public void setCompletedItem(int completedItem) {
        this.completedItem = completedItem;
    }

    public Player() {
    }
}

package com.schoolofai.objectclassificationgame.models;

import com.google.firebase.Timestamp;

public class Player {
    private String playerName;
    private int completedItem = 0;
    private Timestamp completedTime;

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

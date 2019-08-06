package com.schoolofai.objectclassificationgame.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

public class Player implements Parcelable {
    private String playerUid;
    private String playerName;
    private int completedItem;
    private int status;
    private Timestamp completedTime;

    public String getPlayerUid() {
        return playerUid;
    }

    public void setPlayerUid(String playerUid) {
        this.playerUid = playerUid;
    }



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

    public Player(Parcel in) {
        String[] data = new String[4];
        in.readStringArray(data);
        this.playerUid = data[0];
        this.playerName = data[1];
        this.completedItem = Integer.parseInt(data[2]);
        this.status = Integer.parseInt(data[3]);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.playerUid, this.playerName, String.valueOf(this.completedItem), String.valueOf(this.status)});
    }

    public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {

        @Override
        public Player createFromParcel(Parcel source) {
            return new Player(source);  //using parcelable constructor
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };


}

package com.schoolofai.objectclassificationgame;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class tutorWelcome extends ListActivity {

    //Get the data from the database and select
    private Cursor getData() {
        String path = "/data/data/"+getPackageName()/*+"/"+studentWelcome.DATABASE_NAME*/;
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READONLY);
        return db.rawQuery("SELECT * from gamelog",null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_welcome);

        final Cursor cursor = getData();
        CursorAdapter cursorAdapter = new CursorAdapter(this,cursor,0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.activity_tutor_list, parent ,false);
            }

            //List the game data each team
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                /*String team = cursor.getString(cursor.getColumnIndexOrThrow(GameActivity.GAMELOG_COLUMN_DATE));
                String completed = cursor.getString(cursor.getColumnIndexOrThrow(GameActivity.GAMELOG_COLUMN_TIME));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(GameActivity.GAMELOG_COLUMN_OPPONENT_NAME));*/

                TextView tvTeam = (TextView)view.findViewById(R.id.tvTeam);
                TextView tvCompleted = (TextView)view.findViewById(R.id.tvCompleted);
                TextView tvTime = (TextView)view.findViewById(R.id.tvTime);

                /*tvTeam.setText(team);
                tvCompleted.setText(completed);
                tvTime.setText(time);*/
            }
        };
        setListAdapter(cursorAdapter);
    }
}

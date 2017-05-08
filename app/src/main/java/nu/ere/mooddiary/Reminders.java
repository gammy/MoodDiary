package nu.ere.mooddiary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public final class Reminders {
    public ArrayList<Reminder> reminders;

    public Reminders(SQLiteDatabase db){
        Log.d("Reminders", "Enter Reminders");
        reminders = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT id, hh, mm, dd FROM Reminders", null);
        int added = 0;

        while(cursor.moveToNext()) {
            Reminder reminder = new Reminder(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3)
                    );
            reminders.add(reminder);
            Log.d("Reminders", "Add reminder: " + cursor.getString(0));
            added++;
        }

        Log.d("Reminders", "Reminders added: " + Integer.toString(added));
    }
}
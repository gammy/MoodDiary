package nu.ere.mooddiary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public final class Reminders {
    public ArrayList<Reminder> reminders;

    public Reminders(SQLiteDatabase db){
        Log.d("Reminders", "Enter Reminders");

        /*
        reminders = List()
        For row in Reminders:
            reminder = new()
            reminder.id = row.id
            reminder.types = List()
            eventList = sql(SELECT Event FROM Reminders WHERE ReminderTimeID = row.id)
            For row in eventList:
                reminder.add(eventID)
            reminders.add(reminder)
        */
        reminders = new ArrayList<>();

        // A reminder is:
        // - time
        // - list of types

        //Cursor cursor = db.rawQuery("SELECT MAX(reminderTimeID) FROM Reminders", null);
        //int reminderGroup = cursor.getInt(0) + 1;

//        Cursor cursor = db.rawQuery("SELECT Event FROM Reminders WHERE ReminderTimeID = ", row.id);
        int added = 0;

/*
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
*/
        Log.d("Reminders", "Reminders added: " + Integer.toString(added));
    }
}
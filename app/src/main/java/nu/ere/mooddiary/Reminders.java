package nu.ere.mooddiary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

// FIXME this is fucked. What the hell am I doing.
public final class Reminders {
    private static final String LOG_PREFIX = "Reminders";
    public ArrayList<Reminder> reminders;

    ORM orm = null;

    public Reminders(ORM orm, EventTypes eventTypes){
        Log.d(LOG_PREFIX, "Enter Reminders");
        this.orm = orm;
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

        // A reminder is:
        // - time
        // - list of types

        // These comments are for *adding* a reminder - put in the Reminder class
        // Cursor cursor = db.rawQuery("SELECT MAX(reminderTimeID) FROM Reminders", null);
        // int reminderGroup = cursor.getInt(0) + 1;

        // Cursor cursor = db.rawQuery("SELECT Event FROM Reminders WHERE ReminderTimeID = ", row.id);

//        reminders = new ArrayList<>();
//
//        // This is what constitutes a reminder object
//        Cursor cursor = db.rawQuery("SELECT id, reminderID, hh, mm, dd FROM ReminderTimes", null);
//        cursor.moveToFirst();
//        long id         = cursor.getLong(0);
//        long reminderID = cursor.getLong(1);
//        int hh          = cursor.getInt(2);
//        int mm          = cursor.getInt(3);
//        int dd          = cursor.getInt(4);
//
//        ArrayList<EventType> reminderEventTypes = new ArrayList<>();
//
//        cursor = db.rawQuery("SELECT id, type FROM Reminders WHERE ReminderTimeID = ", Long.toString(reminderID));
//        cursor.moveToFirst();
//        //Reminder reminder = new Reminder(id, hh, mm, dd, eventTypes);
//
//        // Get all the associated event type ID's
//        int added = 0;
//        while(cursor.moveToNext()) {
//            long eventID = cursor.getLong(1);
//            reminderEventTypes.add(eventTypes.getByID(eventID));
//            Log.d(LOG_PREFIX, "Add reminder event type: " + eventID);
//            added++;
//        }
//        Reminder reminder = new Reminder(id, hh, mm, dd, reminderEventTypes);
//
//        Log.d(LOG_PREFIX, "Reminders added: " + Integer.toString(added));
    }

    /*
    public ArrayList<ReminderTime> getReminderTimes() {
        ArrayList<ReminderTime> reminderTimes = new ArrayList<>();
        Cursor cursor;

        cursor = orm.db.rawQuery("SELECT id, reminderID, hh, mm, dd FROM ReminderTimes", null);
        cursor.moveToFirst();

        while(cursor.moveToNext()) {
            int id         = cursor.getInt(cursor.getColumnIndex("id"));
            int reminderID = cursor.getInt(cursor.getColumnIndex("reminderID"));
            int hh         = cursor.getInt(cursor.getColumnIndex("hh"));
            int mm         = cursor.getInt(cursor.getColumnIndex("mm"));
            int dd         = cursor.getInt(cursor.getColumnIndex("dd"));
            ReminderTime reminderTime = new ReminderTime(id, reminderID, hh, mm, dd);
            reminderTimes.add(reminderTime);
        }
        cursor.close();

        return reminderTimes;
    }
    */

    public Reminder getReminderByID(long id) {
        Cursor cursor;

        // Get core reminder properties
        cursor = orm.db.rawQuery("SELECT hh, mm, dd FROM ReminderTimes WHERE ReminderTimeID = " +
                Long.toString(id), null);
        cursor.moveToFirst();
        int hh = cursor.getInt(cursor.getColumnIndex("hh"));
        int mm = cursor.getInt(cursor.getColumnIndex("mm"));
        int dd = cursor.getInt(cursor.getColumnIndex("dd"));
        cursor.close();

        // Collate event types associcated with this reminder
        ArrayList<EventType> reminderEventTypes = new ArrayList<>();

        cursor =
                orm.db.rawQuery("SELECT id, type FROM Reminders WHERE ReminderTimeID = " +
                        Long.toString(id), null);
        // FIXME assumes no errors
        cursor.moveToFirst();

        // Get all event types associated with this reminder
        while(cursor.moveToNext()) {
            int eventTypeID = cursor.getInt(cursor.getColumnIndex("type"));
            EventType eventType = orm.getEventTypes().getByID(eventTypeID);
            reminderEventTypes.add(eventType);
        }

        Reminder reminder = new Reminder(id, hh, mm, dd, reminderEventTypes);
        return(reminder);
    }
}
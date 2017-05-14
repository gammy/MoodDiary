package nu.ere.mooddiary;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public final class ReminderTimes {
    private static final String LOG_PREFIX = "ReminderTimes";
    public ArrayList<ReminderTime> reminderTimes;
    private ORM orm = null;

    public ReminderTimes(ORM orm){
        Log.d(LOG_PREFIX, "Enter ReminderTimes");
        this.orm = orm;

        // Populate ReminderTimes
        Cursor cursor;
        cursor = orm.db.rawQuery("SELECT id, reminderGroup, hour, minute FROM ReminderTimes", null);

        reminderTimes = new ArrayList<>();

        // Walk through each reminder and collate the associated measurementTypes
        while(cursor.moveToNext()) {
            Log.d(LOG_PREFIX, "reminderTimes: looping ReminderTimes");

            int id     = cursor.getInt(cursor.getColumnIndex("id"));
            int group  = cursor.getInt(cursor.getColumnIndex("reminderGroup"));
            int hour   = cursor.getInt(cursor.getColumnIndex("hour"));
            int minute = cursor.getInt(cursor.getColumnIndex("minute"));

            ReminderTime reminderTime = new ReminderTime(id, group, hour, minute);
            reminderTimes.add(reminderTime);

            Log.d(LOG_PREFIX,
                    "Reminder id" + Integer.toString(id) + ", " +
                    "group " + Integer.toString(group) + ", " +
                    "time = " + Integer.toString(hour) + ":" + Integer.toString(minute));
        }

        cursor.close();
    }

    /*
    public Reminder getReminderByID(long groupID) { // FIXME groupID you mean!
        Log.d(LOG_PREFIX, "Enter getReminderByID");
        Cursor cursor;

        // Get core reminder properties
        Log.d(LOG_PREFIX, "Got passed id: " + Long.toString(groupID));

        cursor = orm.db.rawQuery("SELECT hour, minute FROM ReminderTimes WHERE reminderGroup = " +
                Long.toString(groupID), null);
        cursor.moveToFirst();
        int hour   = cursor.getInt(cursor.getColumnIndex("hour"));
        int minute = cursor.getInt(cursor.getColumnIndex("minute"));
        cursor.close();

        // Collate event types associated with this reminder
        ArrayList<MeasurementType> reminderEventTypes = new ArrayList<>();

        cursor = orm.db.rawQuery("SELECT id, type FROM ReminderGroups WHERE reminderTime = " +
                Long.toString(groupID), null);
        // FIXME assumes no errors

        // Get all event types associated with this reminder
        while(cursor.moveToNext()) {
            int eventTypeID = cursor.getInt(cursor.getColumnIndex("type"));
            MeasurementType measurementType = orm.getMeasurementTypes().getByID(eventTypeID);
            reminderEventTypes.add(measurementType);
        }

        Reminder reminder = new Reminder(groupID, hour, minute, reminderEventTypes);
        return(reminder);
    }
    */

    public ReminderTime getByID(long id) {
        for(int i = 0; i < reminderTimes.size(); i++) {
            ReminderTime r = reminderTimes.get(i);
            if(r.id == id) {
                return(r);
            }
        }
        throw new NoSuchElementException("Unable to find an ReminderTime with id " +
                Long.toString(id));
    }
    public ArrayList<MeasurementType> getTypesByReminderTimeID(int reminderTimeID) {
        Log.d(LOG_PREFIX, "Enter getTypesByReminderTimeID");
        // Collate event types associated with this reminder
        Cursor rCursor;
        ArrayList<MeasurementType> reminderMeasurementTypes = new ArrayList<>();

        rCursor = orm.db.rawQuery("SELECT type FROM ReminderGroups WHERE reminderTime = " +
                Integer.toString(reminderTimeID), null);

        // Get all event types associated with this reminder
        while (rCursor.moveToNext()) {
            int eventTypeID = rCursor.getInt(rCursor.getColumnIndex("type"));
            Log.d(LOG_PREFIX, "   associated event type: " + Integer.toString(eventTypeID));
            MeasurementType measurementType = orm.getMeasurementTypes().getByID(eventTypeID);
            reminderMeasurementTypes.add(measurementType);
        }

        rCursor.close();

        return(reminderMeasurementTypes);
    }
}
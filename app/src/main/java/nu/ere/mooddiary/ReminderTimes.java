package nu.ere.mooddiary;

import android.database.Cursor;
import android.support.annotation.IntegerRes;
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
        Log.d(LOG_PREFIX, "Caller is looking for types for reminderTimeID " +
                Integer.toString(reminderTimeID));
        ArrayList<MeasurementType> reminderMeasurementTypes = new ArrayList<>();

        Cursor cursor = orm.db.rawQuery("SELECT type FROM ReminderGroups WHERE reminderTime = " +
                Integer.toString(reminderTimeID), null);

        // Get all measurement types associated with this reminder
        while (cursor.moveToNext()) {
            int measurementTypeId = cursor.getInt(cursor.getColumnIndex("type"));
            Log.d(LOG_PREFIX, "   associated measurement type: " + Integer.toString(measurementTypeId));
            MeasurementType measurementType = orm.getMeasurementTypes().getByID(measurementTypeId);
            reminderMeasurementTypes.add(measurementType);
        }

        cursor.close();

        return(reminderMeasurementTypes);
    }
}
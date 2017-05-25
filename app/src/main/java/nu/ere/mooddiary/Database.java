/* Mood Diary, a free Android mood tracker
 * Copyright (C) 2017 Kristian Gunstone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nu.ere.mooddiary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {
    private static final String LOG_PREFIX = "Database";
    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "moodDiary";
    public static SQLiteDatabase db;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_PREFIX, "Enter onCreate" );

        this.db = db;

        // ReminderGroups and ReminderTimes relate to entry scheduling (from the app)
        db.execSQL(
                "CREATE TABLE ReminderGroups " +
                    "(" +
                        "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "reminderGroup INTEGER NOT NULL, " +
                        "type          INTEGER NOT NULL, " +
                        "FOREIGN KEY(type) REFERENCES MeasurementTypes(id)" +
                    ")"
        );

        db.execSQL(
                "CREATE TABLE ReminderTimes " +
                    "(" +
                        "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "reminderGroup INTEGER NOT NULL, " + // Not unique
                        "hour          INTEGER NOT NULL, " +
                        "minute        INTEGER NOT NULL " +
                    ")"
        );

        // Primitives
        db.execSQL(
                "CREATE TABLE EntityPrimitives " +
                "(" +
                    "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name          STRING NOT NULL, " +
                    "isNumber      INTEGER NOT NULL, " +
                    "enabled       INTEGER DEFAULT 1 " +
                ")"
        );

        // Types
        db.execSQL(
                "CREATE TABLE MeasurementTypes " +
                "(" +
                    "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "listOrder     INTEGER DEFAULT 0, " + // ui display order
                    "name          STRING NOT NULL, " +
                    "entity        INTEGER, " +
                    "minValue      INTEGER DEFAULT 0, " +
                    "maxValue      INTEGER DEFAULT 100, " +
                    "defaultValue  INTEGER DEFAULT 0, " +
                    "metadata      STRING, " +
                    "enabled       INTEGER DEFAULT 1, " +
                        "FOREIGN KEY(entity) REFERENCES EntityPrimitives(id)" +
                ")"
        );

        // Events
        db.execSQL(
                "CREATE TABLE Events " +
                "(" +
                    "id         INTEGER PRIMARY KEY AUTOINCREMENT," + // autoincrement and unique
                    "date       INTEGER," +
                    "type       INTEGER NOT NULL, " +
                    "value      STRING NOT NULL," + // Even for passing integers, etc
                        "FOREIGN KEY(type) REFERENCES MeasurementTypes(id) " +
                ")"
        );

        /* Default Values */

        // Primitives
        // (These placeholder ID_'s are later loaded by EntityPrimitives - any code after
        //  this can just use EntityPrimitives.get..)
        int ID_RANGE_NORMAL = 1; // Assuming the primary key in EntityPrimitives is 1! :)
        int ID_RANGE_CENTER = 2;
        int ID_NUMBER       = 3;
        int ID_TEXT         = 4;

        addPrimitive("range_normal", true);
        addPrimitive("range_center", true);
        addPrimitive("number",       true);
        addPrimitive("text",        false);

        addMeasurementType(ID_RANGE_CENTER,  10, "Mood",               -50,    50, 0, "stock");
        addMeasurementType(ID_RANGE_NORMAL,  20, "Anxiety",              0,   100, 0, "stock");
        addMeasurementType(ID_RANGE_NORMAL,  30, "Irritability",         0,   100, 0, "stock");
        addMeasurementType(ID_RANGE_NORMAL,  40, "Lack of Focus",        0,   100, 0, "stock");
        addMeasurementType(ID_RANGE_CENTER,  50, "Energy",             -50,    50, 0, "stock");
        addMeasurementType(ID_TEXT,          60, "Note",                -1,   -1, -1, "stock");
        /*
        addMeasurementType(ID_NUMBER,        6, "Lamotrigine (100mg)",  0,   50,  0, "gammy");
        addMeasurementType(ID_NUMBER,        7, "Sertraline (25mg)",    0,   50,  0, "gammy");

        addMeasurementType(ID_RANGE_CENTER,  9, "Humör",              -50,    50, 0, "tomten");
        addMeasurementType(ID_RANGE_NORMAL, 10, "Ångest",               0,   100, 0, "tomten");
        addMeasurementType(ID_RANGE_NORMAL, 11, "Energi",               0,   100, 0, "tomten");
        addMeasurementType(ID_RANGE_NORMAL, 12, "Paranoia",             0,   100, 0, "tomten");
        */

        // Reminders

        ArrayList<Integer> reminderEventList = new ArrayList<>();
        reminderEventList.add(1);
        //reminderEventList.add(1); // Mood
        //reminderEventList.add(2); // Anxiety
        //reminderEventList.add(3); // Irritability
        //reminderEventList.add(5); // Sleep
        addReminder(10, 0, reminderEventList); // 10am

        reminderEventList = new ArrayList<>();
        reminderEventList.add(1);
        reminderEventList.add(2);
        //reminderEventList.add(1); // Mood
        //reminderEventList.add(2); // Anxiety
        //reminderEventList.add(3); // Irritability
        addReminder(15, 0, reminderEventList); // 3pm

        reminderEventList = new ArrayList<>();
        reminderEventList.add(1);
        reminderEventList.add(2);
        reminderEventList.add(3);
        //reminderEventList.add(1); // Mood
        //reminderEventList.add(2); // Anxiety
        //reminderEventList.add(3); // Irritability
        //reminderEventList.add(6); // Alcohol
        //reminderEventList.add(9); // Note
        addReminder(21, 0, reminderEventList); // 9pm
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_PREFIX, "Enter onUpgrade" );

        if(oldVersion == 2 && newVersion == 3) {
            // DANGER WILL ROBINSON!
            // Oldest implementation of onUpgrade starts here
            db.execSQL("DROP TABLE IF EXISTS Reminders"); // OLD, no longer exists in schema
            db.execSQL("DROP TABLE IF EXISTS ReminderGroups");
            db.execSQL("DROP TABLE IF EXISTS ReminderTimes");
            db.execSQL("DROP TABLE IF EXISTS EntityPrimitives");
            db.execSQL("DROP TABLE IF EXISTS MeasurementTypes");
            db.execSQL("DROP TABLE IF EXISTS Events");
            onCreate(db);
        } else if(oldVersion == 3 && newVersion == 4) {
            db.execSQL("DELETE FROM MeasurementTypes WHERE id = 8");
            db.execSQL("UPDATE MeasurementTypes SET listOrder = 12 WHERE id = 14");
        } // and so on
    }

    /**
     * Add a new entityPrimitive
     *
     * @param name      A string identifier for this primitive
     * @param isNumber  Whether or not this primitive will hold numbers or (presumably) text
     *                  (this has
     * ...// - Save widget data down to the entrylist
     */
    public void addPrimitive(String name, boolean isNumber) {
        Log.d(LOG_PREFIX, "INSERT INTO EntityPrimitives (name, isNumber) VALUES (?, ?)");
        String sql = "INSERT INTO EntityPrimitives (name, isNumber) VALUES (?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, name);
        statement.bindLong(  2, isNumber ? 1 : 0);
        statement.executeInsert();
    }

    /**
     * Insert a new measurement type into the MeasurementTypes table
     *
     * @param entity    Entity ID which matches an EntityPrimitives id
     * @param order     A number representing in what order the measurement type should be rendered (UX)
     * @param name      Canonical name of the new type
     * @param min       Minimum allowed value (used to render UI)
     * @param max       Maximum allowed value (used to render UI)
     * @param dfl       Default value         (used to render UI)
     * @param meta      Metadata string: currently unused
     * ...// - Save widget data down to the entrylist
     */
    public void addMeasurementType(int entity, int order,
                                   String name, int min, int max, int dfl, String meta) {
        Log.d(LOG_PREFIX, "Enter addMeasurementType" );

        String sql = "INSERT INTO " +
                         "MeasurementTypes " +
                     "(" +
                         "entity, " +
                         "listOrder, " +
                         "name, " +
                         "minValue, " +
                         "maxValue, " +
                         "defaultValue, " +
                         "metadata" +
                      ") " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);

        statement.bindLong(  1, (long) entity);
        statement.bindLong(  2, (long) order);
        statement.bindString(3, name);
        statement.bindLong(  4, (long) min);
        statement.bindLong(  5, (long) max);
        statement.bindLong(  6, (long) dfl);
        statement.bindString(7, meta);

        statement.executeInsert();
    }

    /**
     * Modify a measurement type.
     * Currently, on the the name can be changed.
     *
     * @param mTypeId  type ID
     * @param newName  The new name
     * @param order    The new list order
     * @param enabled
     */
    public void changeMeasurementType(int mTypeId, String newName, int order, int enabled) {
        Log.d(LOG_PREFIX, "Enter changeMeasurementType" );

        Log.d(LOG_PREFIX, "UPDATE MeasurementTypes SET "
                + "name = " + newName  + ", "
                + "listOrder = " + Integer.toString(order) + ", "
                + "enabled = " + Integer.toString(enabled) + " "
                + "WHERE id = " + Integer.toString(mTypeId));

        String sql = "UPDATE MeasurementTypes SET name = ?, listOrder = ?, enabled = ? WHERE id = ?";
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, newName);
        statement.bindLong(  2, order);
        statement.bindLong(  3, enabled);
        statement.bindLong(  4, mTypeId);
        statement.executeUpdateDelete();
        statement.close();
    }

    /**
     * Delete a measurement type.
     *
     * @param mTypeId
     */
    public void deleteMeasurementType(int mTypeId) {
        Log.d(LOG_PREFIX, "Enter deleteMeasurementType" );
        SQLiteStatement statement;

        //String sql = "UPDATE MeasurementTypes SET enabled = false WHERE id = ?";
        String sql = "DELETE FROM MeasurementTypes WHERE id = ?";
        statement = db.compileStatement(sql);
        statement.bindLong(1, mTypeId);
        statement.executeUpdateDelete();
        statement.close();
    }
    /**
     * Insert a list of events into the Events table
     * FIXME this stuff is a bit stupid
     *
     * @param entryList            An ArrayList<Entry> list of Entry objects
     * @param useFirstTimestamp    If true, all entry object times are overwritten by the first
     *                             object's time property. This makes it easier to group events
     *                             which were added simultaneously
     */
    public void addEntries(ArrayList<Entry> entryList,
                          boolean useFirstTimestamp) { // Groups event times by first t
        Log.d(LOG_PREFIX, "Enter addEvents" );

        String sql = "INSERT INTO Events (date, type, value) VALUES (?, ?, ?)";

        db.beginTransaction();

        for(int i = 0; i < entryList.size(); i++) {
            nu.ere.mooddiary.Entry entry = entryList.get(i);
            SQLiteStatement statement = db.compileStatement(sql);

            if(useFirstTimestamp) {
                statement.bindLong(1, entryList.get(0).time);
            } else {
                statement.bindLong(1, entry.time);
            }

            statement.bindLong(  2, entry.eventType);
            statement.bindString(3, entry.value);

            statement.executeInsert();
            Log.d(LOG_PREFIX,
                    "INSERT INTO Events: " + Long.toString(entry.time) + ", " +
                    Integer.toString(entry.eventType) + ", " +
                    entry.value);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Add a new reminder (and associated groups)
     *
     * @param hour    Hour in 24-hour format
     * @param minute  Minute
     * @param typeIDs A simple arraylist of eventTypeID integers
     */
    public void addReminder(int hour, int minute, ArrayList<Integer> typeIDs) {
        Log.d(LOG_PREFIX, "Enter addReminder" );
        Cursor cursor;
        String sql;
        SQLiteStatement statement;

        // Make a new reminderID
        int newReminderGroup = 0;
        cursor = db.rawQuery("SELECT MAX(reminderGroup) FROM ReminderTimes", null);
        if(cursor.moveToFirst()) {
            newReminderGroup = cursor.getInt(0);
            newReminderGroup += 1;
        }
        Log.d(LOG_PREFIX, "addReminder: newReminderGroup: " + Integer.toString(newReminderGroup));

        db.beginTransaction();

        // Insert new ReminderTime
        sql = "INSERT INTO ReminderTimes (reminderGroup, hour, minute) VALUES (?, ?, ?)";
        statement = db.compileStatement(sql);
        statement.bindLong(1, newReminderGroup);
        statement.bindLong(2, hour);
        statement.bindLong(3, minute);
        statement.executeInsert();
        statement.close();

        // Insert associated events
        sql = "INSERT INTO ReminderGroups (reminderGroup, type) VALUES (?, ?)";
        statement = db.compileStatement(sql);

        for(int i = 0; i < typeIDs.size(); i++) {
            int eventTypeID = typeIDs.get(i);
            Log.d(LOG_PREFIX, "addReminder: INSERT type: " + Integer.toString(eventTypeID));
            statement.bindLong(1, newReminderGroup);
            statement.bindLong(2, eventTypeID);
            statement.executeInsert();
        }
        statement.close();

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Change a new reminder (with associated groups)
     * This is a bit different from just doing delete/add, since that will screw up the
     * ordering. We have to reuse the original id.
     *
     * @param reminderTimeId
     * @param hour    Hour in 24-hour format
     * @param minute  Minute
     * @param typeIDs A simple arraylist of eventTypeID integers
     */
    public void changeReminder(int reminderTimeId, int hour, int minute, ArrayList<Integer> typeIDs) {
        Log.d(LOG_PREFIX, "Enter changeReminder" );
        Cursor cursor;
        String sql;
        SQLiteStatement statement;

        db.beginTransaction();

        Log.d(LOG_PREFIX, "SELECT reminderGroup FROM ReminderTimes WHERE id = " +
                Integer.toString(reminderTimeId));

        cursor = db.rawQuery("SELECT reminderGroup FROM ReminderTimes WHERE id = " +
                Integer.toString(reminderTimeId), null);
        cursor.moveToFirst();
        int oldReminderGroup = cursor.getInt(cursor.getColumnIndex("reminderGroup"));

        Log.d(LOG_PREFIX, "DELETE FROM ReminderGroups WHERE reminderGroup = " +
                Integer.toString(reminderTimeId));
        db.delete("ReminderGroups", "reminderGroup = ?", new String[]
                {Integer.toString(reminderTimeId)});

        Log.d(LOG_PREFIX, "INSERT INTO ReminderTimes (reminderGroup, hour, minute) VALUES (" +
                Integer.toString(oldReminderGroup) + ", " +
                Integer.toString(hour) + ", " +
                Integer.toString(minute) + ")");

        sql = "UPDATE ReminderTimes SET reminderGroup = ?, hour = ?, minute = ? WHERE id = ?";
        statement = db.compileStatement(sql);
        statement.bindLong(1, oldReminderGroup);
        statement.bindLong(2, hour);
        statement.bindLong(3, minute);
        statement.bindLong(4, reminderTimeId);
        statement.executeUpdateDelete();
        statement.close();

        // Insert associated events
        sql = "INSERT INTO ReminderGroups (reminderGroup, type) VALUES (?, ?)";
        statement = db.compileStatement(sql);

        for(int i = 0; i < typeIDs.size(); i++) {
            int eventTypeID = typeIDs.get(i);
            Log.d(LOG_PREFIX, "addReminder: INSERT INTO ReminderGroups (reminderGroup, type)" +
                    "VALUES (" +
                    Integer.toString(oldReminderGroup) + ", " +
                    Integer.toString(eventTypeID) + ")");
            statement.bindLong(1, oldReminderGroup);
            statement.bindLong(2, eventTypeID);
            statement.executeInsert();
        }
        statement.close();
        cursor.close();

        db.setTransactionSuccessful();
        db.endTransaction();
    }

     /**
     * Delete a new reminder (i.e delete reminderTime, and all associated reminderGroups)
     *
     * @param reminderTimeId
     */
    public void deleteReminder(int reminderTimeId) {
        Log.d(LOG_PREFIX, "Enter deleteReminder");
        db.beginTransaction();

        Log.d(LOG_PREFIX, "DELETE FROM ReminderTimes WHERE id = " +
                Integer.toString(reminderTimeId));
        db.delete("ReminderTimes", "id = ?", new String[]
                {Integer.toString(reminderTimeId)});

        Log.d(LOG_PREFIX, "DELETE FROM ReminderGroups WHERE reminderGroup = " +
                Integer.toString(reminderTimeId));
        db.delete("ReminderGroups", "reminderGroup = ?", new String[] {
                Integer.toString(reminderTimeId)});

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void testReminders() {
        Log.d(LOG_PREFIX, "Enter testReminders" );

        Cursor cursor;

        cursor = db.rawQuery("SELECT id, reminderGroup, hour, minute FROM ReminderTimes", null);
        Log.d(LOG_PREFIX, "testReminders: rawquery OK");

        // Walk through each reminder and collate the associated measurementTypes
        while(cursor.moveToNext()) {
            Log.d(LOG_PREFIX, "testReminders: looping ReminderTimes");

            int id     = cursor.getInt(cursor.getColumnIndex("id"));
            int group  = cursor.getInt(cursor.getColumnIndex("reminderGroup"));
            int hour   = cursor.getInt(cursor.getColumnIndex("hour"));
            int minute = cursor.getInt(cursor.getColumnIndex("minute"));
            Log.d(LOG_PREFIX,
                    "Reminder id" + Integer.toString(id) + ", " +
                    "group " + Integer.toString(group) + ", " +
                    "time = " + Integer.toString(hour) + ":" + Integer.toString(minute));

            // Collate measurement types associated with this reminder
            Cursor rCursor;
            // ArrayList<MeasurementType> reminderEventTypes = new ArrayList<>();

            rCursor = db.rawQuery("SELECT id, type FROM ReminderGroups WHERE reminderGroup = " +
                            Long.toString(id), null);

            // Get all reminder types associated with this reminder
            while (rCursor.moveToNext()) {
                int eventTypeID = rCursor.getInt(rCursor.getColumnIndex("type"));
                Log.d(LOG_PREFIX, "   associated measurement type: " + Integer.toString(eventTypeID));
                // MeasurementType measurementType = orm.getMeasurementTypes().getByID(eventTypeID);
                //reminderEventTypes.add(measurementType);
            }

            rCursor.close();
            // Reminder reminder = new Reminder(id, hour, minute, reminderEventTypes);
        }
        cursor.close();
        Log.d(LOG_PREFIX, "Leave testReminders");
    }
}

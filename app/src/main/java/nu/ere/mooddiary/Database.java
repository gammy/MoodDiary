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
    private static final int DB_VERSION = 2;
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

        // Primitives (overdesign much?)
        db.execSQL(
                "CREATE TABLE EntityPrimitives " +
                "(" +
                    "id            INTEGER PRIMARY KEY, " +
                    "name          STRING NOT NULL, " +
                    "enabled       INTEGER DEFAULT 1 " +
                ")"
        );

        // Types
        db.execSQL(
                "CREATE TABLE MeasurementTypes " +
                "(" +
                    "id            INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ui_order      INTEGER DEFAULT 0, " + // ui display order
                    "name          STRING NOT NULL, " +
                    "entity        INTEGER, " +
                    "val_min       INTEGER DEFAULT 0, " +
                    "val_max       INTEGER DEFAULT 100, " +
                    "val_dfl       INTEGER DEFAULT 0, " +
                    "metadata      STRING," +
                    "enabled       INTEGER DEFAULT 1," +
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
        int ID_RANGE_NORMAL = 1;
        int ID_RANGE_CENTER = 2;
        int ID_NUMBER       = 3;
        int ID_TEXT         = 4;
        db.execSQL("INSERT INTO EntityPrimitives (id, name) VALUES (" +
                Integer.toString(ID_RANGE_NORMAL) + ", 'range_normal')");
        db.execSQL("INSERT INTO EntityPrimitives (id, name) VALUES (" +
                Integer.toString(ID_RANGE_CENTER) + ", 'range_center')");
        db.execSQL("INSERT INTO EntityPrimitives (id, name) VALUES (" +
                Integer.toString(ID_NUMBER) + ", 'number')");
        db.execSQL("INSERT INTO EntityPrimitives (id, name) VALUES (" +
                Integer.toString(ID_TEXT) + ", 'text')");
        /*
        db.execSQL("INSERT INTO EntityPrimitives (id, name) VALUES (2, 'radio')");
        db.execSQL("INSERT INTO EntityPrimitives (id, name) VALUES (3, 'dropdown')");
        db.execSQL("INSERT INTO EntityPrimitives (id// TODO:, name) VALUES (4, 'checkbox')");
        */

        // Measurement Types
        /* The original values for these were based on a hand-written form provided by
           affektiva mottagningen:

            addMeasurementType(db, ID_RANGE,  0, "Mood",                  -3,   3, 0, "");
            addMeasurementType(db, ID_RANGE,  1, "Anxiety",                0,   3, 0, "");
            addMeasurementType(db, ID_RANGE,  2, "Irritability",           0,   3, 0, "");
            addMeasurementType(db, ID_RANGE,  3, "Lack of Concentration",  0,   3, 0, "");
            addMeasurementType(db, ID_NUMBER, 4, "Sleep (hours)",          0, 100, 0, "");
            addMeasurementType(db, ID_NUMBER, 5, "Alcohol (units)",        0, 100, 0, "");
        */

        addMeasurementType(ID_RANGE_CENTER,  0, "Mood",               -50,    50, 0, "");
        addMeasurementType(ID_RANGE_NORMAL,  1, "Anxiety",              0,   100, 0, "");
        addMeasurementType(ID_RANGE_NORMAL,  2, "Irritability",         0,   100, 0, "");
        addMeasurementType(ID_RANGE_NORMAL,  3, "Lack of Focus",        0,   100, 0, "");
        addMeasurementType(ID_NUMBER,        4, "Sleep (hours)",        0,   100, 0, "");
        addMeasurementType(ID_NUMBER,        5, "Alcohol (units)",      0,   100, 0, "");

        addMeasurementType(ID_NUMBER,        6, "Lamotrigine (100mg)",  0,   50,  0, "");
        addMeasurementType(ID_NUMBER,        7, "Sertraline (25mg)",    0,   50,  0, "");
        addMeasurementType(ID_TEXT,          8, "Note",                -1,   -1, -1, ""); // FIXME hack..

        // The idea here being that a user can add new types from the UI at some point

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
        Log.d(LOG_PREFIX, "Enter onUpgrade: Trashing everything" );
        // FIXME
        // Drop older tables if they existed
        db.execSQL("DROP TABLE IF EXISTS Reminders"); // OLD, no longer exists in schema
        db.execSQL("DROP TABLE IF EXISTS ReminderGroups");
        db.execSQL("DROP TABLE IF EXISTS ReminderTimes");
        db.execSQL("DROP TABLE IF EXISTS EntityPrimitives");
        db.execSQL("DROP TABLE IF EXISTS MeasurementTypes");
        db.execSQL("DROP TABLE IF EXISTS Events");
        // Creating tables again
        onCreate(db);
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
                         "ui_order, " +
                         "name, " +
                         "val_min, " +
                         "val_max, " +
                         "val_dfl, " +
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

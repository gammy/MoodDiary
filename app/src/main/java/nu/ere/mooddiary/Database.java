package nu.ere.mooddiary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteCursor;
import android.util.Log;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper{
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "moodDiary";

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("Database", "Enter onCreate" );

        /* Table Creation */

        // Reminders (alarms)
        db.execSQL(
                "CREATE TABLE Reminders " +
                    "(" +
                        "id       INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "hh       INTEGER NOT NULL, " +
                        "mm       INTEGER NOT NULL, " +
                        "dd       INTEGER NOT NULL" +
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

        // Types// TODO: Add parser for "option" field
        db.execSQL(
                "CREATE TABLE EventTypes " +
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
                        "FOREIGN KEY(type) REFERENCES EventTypes(id) " +
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

        // Event Types
        /* The original values for these were based on a hand-written form provided by
           affektiva mottagningen:

            addEventType(db, ID_RANGE,  0, "Mood",                  -3,   3, 0, "");
            addEventType(db, ID_RANGE,  1, "Anxiety",                0,   3, 0, "");
            addEventType(db, ID_RANGE,  2, "Irritability",           0,   3, 0, "");
            addEventType(db, ID_RANGE,  3, "Lack of Concentration",  0,   3, 0, "");
            addEventType(db, ID_NUMBER, 4, "Sleep (hours)",          0, 100, 0, "");
            addEventType(db, ID_NUMBER, 5, "Alcohol (units)",        0, 100, 0, "");
        */

        addEventType(db, ID_RANGE_CENTER,  0, "Mood",               -50,    50, 0, "");
        addEventType(db, ID_RANGE_NORMAL,  1, "Anxiety",              0,   100, 0, "");
        addEventType(db, ID_RANGE_NORMAL,  2, "Irritability",         0,   100, 0, "");
        addEventType(db, ID_RANGE_NORMAL,  3, "Lack of Focus",        0,   100, 0, "");
        addEventType(db, ID_NUMBER,        4, "Sleep (hours)",        0,   100, 0, "");
        addEventType(db, ID_NUMBER,        5, "Alcohol (units)",      0,   100, 0, "");

        addEventType(db, ID_NUMBER,        6, "Lamotrigine (100mg)",  0,   50,  0, "");
        addEventType(db, ID_NUMBER,        7, "Sertraline (25mg)",    0,   50,  0, "");
        addEventType(db, ID_TEXT,          8, "Note",                -1,   -1, -1, "");
        // The idea here being that a user can add new types from the UI at some point

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("Database", "Enter onUpgrade: Trashing everything" );
        // FIXME
        // Drop older tables if they existed
        db.execSQL("DROP TABLE IF EXISTS Reminders");
        db.execSQL("DROP TABLE IF EXISTS EntityPrimitives");
        db.execSQL("DROP TABLE IF EXISTS EventTypes");
        db.execSQL("DROP TABLE IF EXISTS Events");
        // Creating tables again
        onCreate(db);
    }

    /**
     * Insert a new event type into the EventTypes table
     *
     * @param db
     * @param entity    Entity ID which matches an EntityPrimitives id
     * @param order     A number representing in what order the event type should be rendered (UX)
     * @param name      Canonical name of the new type
     * @param min       Minimum allowed value (used to render UI)
     * @param max       Maximum allowed value (used to render UI)
     * @param dfl       Default value         (used to render UI)
     * @param meta      Metadata string: currently unused
     * ...// - Save widget data down to the entrylist
     */
    public void addEventType(SQLiteDatabase db,
                             int entity, long order,
                             String name, long min, long max, long dfl, String meta) {
        Log.d("Database", "Enter addEventType" );

        String sql = "INSERT INTO " +
                         "EventTypes " +
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

        statement.bindLong(  1, entity);
        statement.bindLong(  2, order);
        statement.bindString(3, name);
        statement.bindLong(  4, min);
        statement.bindLong(  5, max);
        statement.bindLong(  6, dfl);
        statement.bindString(7, meta);

        long rowId = statement.executeInsert();
    }

    /**
     * Insert a list of events into the Events table
     * FIXME this stuff is a bit stupid
     *
     * @param db
     * @param entryList            An ArrayList<Entry> list of Entry objects
     * @param useFirstTimestamp    If true, all entry object times are overwritten by the first
     *                             object's time property. This makes it easier to group events
     *                             which were added simultaneously
     */
    public void addEntries(SQLiteDatabase db,
                           ArrayList<Entry> entryList,
                          boolean useFirstTimestamp) { // Groups event times by first t
        Log.d("Database", "Enter addEvents" );

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
            Log.d("Database",
                    "INSERT INTO Events: " + Long.toString(entry.time) + ", " +
                    Integer.toString(entry.eventType) + ", " +
                    entry.value);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    /**
     * Insert a reminder
     *
     * @param db
     * @param hh    Hour in 24-hour format
     * @param mm    Hour in 24-hour format
     * @param dd    Hour in 7-day format (0-indexed)
     */
    public void addReminder(SQLiteDatabase db, int hh, int mm, int dd) {
        Log.d("Database", "Enter addReminder" );

        String sql = "INSERT INTO Reminders (hh, mm, dd) VALUES (?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);

        statement.bindLong(0, (long) hh);
        statement.bindLong(1, (long) mm);
        statement.bindLong(2, (long) dd);

        statement.executeInsert();
    }

}

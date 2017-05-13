// Crappy Mood Diary ORM, trying to wrap java objects with the database in a meaningful manner.
package nu.ere.mooddiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.app.Application;

// Thread-safe singleton with lazy initialization:
// It's instantiated on first invocation only.
public final class ORM extends Database {
    private static final String LOG_PREFIX = "ORM";

    private static volatile ORM instance = null;

    private static EntityPrimitives entityPrimitives = null;
    private static EventTypes eventTypes = null;
    private static ReminderTimes reminders = null;

    public static long lastSave = 0;

    SharedPreferences preferences;

    private ORM(Context context) {
        super(context);
        Log.d(LOG_PREFIX, "Create");
        db = getWritableDatabase();
        onUpgrade(db, 0, 0); // FIXME Debugging - trash db to force creation
        loadObjects();
    }

    public static synchronized ORM getInstance(Context context) {
        Log.d(LOG_PREFIX, "getInstance");
        if(instance == null) {
            Log.d(LOG_PREFIX, "creating new instance");
           instance = new ORM(context.getApplicationContext());
        } else {
            Log.d(LOG_PREFIX, "reusing old instance");
            // FIXME reload objects here..?
        }
        return instance;
    }

    /****************************************/

    public static EntityPrimitives getPrimitives() {return entityPrimitives;}
    public static EventTypes getEventTypes() {return eventTypes;}
    public static ReminderTimes getReminderTimes() {return reminders;}

    protected static synchronized void loadObjects() {
        Log.d(LOG_PREFIX, "loadObjects");

        entityPrimitives = new EntityPrimitives(db);
        eventTypes = new EventTypes(db);
        reminders = new ReminderTimes(instance);
    }
}

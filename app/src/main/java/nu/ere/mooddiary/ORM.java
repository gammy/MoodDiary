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
    private static volatile ORM instance = null;

    public static EntityPrimitives entityPrimitives;
    public static EventTypes eventTypes;
    public static Reminders reminders;

    SharedPreferences preferences;

    /****************************************/

    private ORM(Context context) {
        super(context);
        Log.d("ORM", "Create");
        db = getWritableDatabase();
        onUpgrade(db, 0, 0); // FIXME Debugging - trash db to force creation
        loadObjects();
    }

    public static synchronized ORM getInstance(Context context) {
        if(instance == null) {
           instance = new ORM(context.getApplicationContext());
        }
        return instance;
    }

    /****************************************/

    public EntityPrimitives getPrimitives() {return entityPrimitives;}
    public EventTypes getEventTypes() {return eventTypes;}
    public Reminders getReminders() {return reminders;}

    protected void loadObjects() {
        Log.d("ORM", "loadObjects");

        entityPrimitives = new EntityPrimitives(db);
        eventTypes = new EventTypes(db);
        reminders = new Reminders(db, eventTypes);
    }
}

// Crappy Mood Diary ORM, trying to wrap java objects with the database in a meaningful manner.
package nu.ere.mooddiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

public class ORM {

    public SQLiteDatabase db;
    public Database dbh;

    public EntityPrimitives entityPrimitives;
    public EventTypes eventTypes;
    public Reminders reminders;

    SharedPreferences preferences;

    private static final ORM orm = new ORM();
    public static ORM getInstance() {return orm;}

    public EntityPrimitives getPrimitives() {return entityPrimitives;}
    public EventTypes getEventTypes() {return eventTypes;}
    public Reminders getReminders() {return reminders;}
    public Database getHelper() {return dbh;}

    protected void ORM(Context context) {
        Log.d("ORM", "Create");

        dbh = new Database(context);
        db = dbh.getWritableDatabase();

        dbh.onUpgrade(db, 0, 0); // FIXME Debugging - trash db to force creation

        loadObjects();
    }

    protected void loadObjects() {
        Log.d("ORM", "loadObjects");

        entityPrimitives = new EntityPrimitives(db);
        eventTypes = new EventTypes(db);
        reminders = new Reminders(db, eventTypes);

    }
}

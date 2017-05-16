// Crappy Mood Diary ORM, trying to wrap java objects with the database in a meaningful manner.
package nu.ere.mooddiary;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

// Thread-safe singleton with lazy initialization:
// It's instantiated on first invocation only.
public final class ORM extends Database {
    private static final String LOG_PREFIX = "ORM";

    private static volatile ORM instance = null;

    private static EntityPrimitives entityPrimitives = null;
    private static MeasurementTypes measurementTypes = null;
    private static ReminderTimes reminders = null;

    public ORM(Context context) {
        super(context);
        Log.d(LOG_PREFIX, "Create");
        db = getWritableDatabase();
        //onUpgrade(db, 0, 0); // FIXME Debugging - trash db to force creation
        Toast.makeText(context, "New ORM instance", Toast.LENGTH_SHORT).show();
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
    public static MeasurementTypes getMeasurementTypes() {return measurementTypes;}
    public static ReminderTimes getReminderTimes() {return reminders;}

    protected static synchronized void loadObjects() {
        Log.d(LOG_PREFIX, "loadObjects");

        entityPrimitives = new EntityPrimitives(db);
        measurementTypes = new MeasurementTypes(db);
        reminders = new ReminderTimes(instance);
    }
}

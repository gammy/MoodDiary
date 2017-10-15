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
// Crappy Mood Diary ORM, trying to wrap java objects with the database in a meaningful manner.
package nu.ere.mooddiary;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

// Thread-safe singleton with lazy initialization:
// It's instantiated on first invocation only.
// It's not worthy of the title, but whatever; I can't think of a better one.
public final class ORM extends Database {
    private static final String LOG_PREFIX = "ORM";

    private static volatile ORM instance = null;

    private static EntityPrimitives entityPrimitives = null;
    private static MeasurementTypes measurementTypes = null;
    private static ReminderTimes reminders = null;

    public ORM(Context context) {
        super(context);
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter Create");
        db = getWritableDatabase();
        Toast.makeText(context, "New ORM instance", Toast.LENGTH_SHORT).show();

        // DELETE stupid alarms
        //instance.db.execSQL("DELETE FROM ReminderGroups");
        //instance.db.execSQL("DELETE FROM ReminderTimes");
        loadObjects();
    }

    public static synchronized ORM getInstance(Context context) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter getInstance");
        if(instance == null) {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "creating new instance");
           instance = new ORM(context.getApplicationContext());
        } else {
            Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "reusing old instance");
        }
        return instance;
    }

    /****************************************/
    public static void reload(Context context) {
        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Force reload");
        db.close();
        db.releaseReference();
        instance = null;
        getInstance(context);
    }

    public static EntityPrimitives getPrimitives() {return entityPrimitives;}
    public static MeasurementTypes getMeasurementTypes() {return measurementTypes;}
    public static ReminderTimes getReminderTimes() {return reminders;}

    protected static synchronized void loadObjects() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter loadObjects");

        entityPrimitives = new EntityPrimitives(db);
        measurementTypes = new MeasurementTypes(db);
        reminders = new ReminderTimes(instance);
    }
}

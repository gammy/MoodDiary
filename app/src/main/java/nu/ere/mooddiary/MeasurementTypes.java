package nu.ere.mooddiary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.NoSuchElementException;
import java.util.ArrayList;

public class MeasurementTypes {
    private static final String LOG_PREFIX = "MeasurementTypes";

    public ArrayList<MeasurementType> types;

    public MeasurementTypes(SQLiteDatabase db){
        Log.d(LOG_PREFIX, "Enter MeasurementTypes");
        types = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT " +
                    "id, " +
                    "ui_order, " +
                    "name, " +
                    "entity, " +
                    "val_min, " +
                    "val_max, " +
                    "val_dfl, " +
                    "enabled " +
                "FROM " +
                    "MeasurementTypes " +
                "ORDER BY " +
                    "ui_order ASC", null);
        int added = 0;

        while(cursor.moveToNext()) {

            MeasurementType type = new MeasurementType(
                    cursor.getInt(0),   // id
                    cursor.getInt(1),   // order
                    cursor.getString(2), // name
                    cursor.getInt(3),   // entity type (primitive)
                    cursor.getInt(4),   // minimum
                    cursor.getInt(5),   // maximum
                    cursor.getInt(6),   // default
                    cursor.getInt(7)    // enabled
            );
            types.add(type);
            Log.d(LOG_PREFIX, "Add type: " + type.name +
                    "(id " + Long.toString(type.id) + ", " +
                    "order " + Long.toString(type.order) + ")");
            added++;
        }

        cursor.close();

        Log.d(LOG_PREFIX, "Types added: " + Integer.toString(added));
    }

    public MeasurementType getByID(long id) {
        for(int i = 0; i < types.size(); i++) {
            MeasurementType e = types.get(i);
            if(e.id == id) {
                return(e);
            }
        }
        throw new NoSuchElementException("Unable to find an MeasurementType with id " +
                Long.toString(id));
    }

    public MeasurementType getByName(String name) {
        for(int i = 0; i < types.size(); i++) {
            MeasurementType e = types.get(i);
            if(e.name.equals(name)) {
                return(e);
            }
        }
        throw new NoSuchElementException("Unable to find an MeasurementType with name '" + name + "'");
    }
}

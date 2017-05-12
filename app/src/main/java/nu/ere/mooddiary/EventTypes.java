package nu.ere.mooddiary;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.NoSuchElementException;
import java.util.ArrayList;

public class EventTypes {
    public ArrayList<EventType> types;

    public EventTypes(SQLiteDatabase db){
        Log.d("EventTypes", "Enter EventTypes");
        types = new ArrayList<EventType>();

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
                    "EventTypes " +
                "ORDER BY " +
                    "ui_order ASC", null);
        int added = 0;

        while(cursor.moveToNext()) {

            EventType type = new EventType(
                    cursor.getLong(0),   // id
                    cursor.getLong(1),   // order
                    cursor.getString(2), // name
                    cursor.getLong(3),   // entity type (primitive)
                    cursor.getLong(4),   // minimum
                    cursor.getLong(5),   // maximum
                    cursor.getLong(6),   // default
                    cursor.getLong(7)    // enabled
            );
            types.add(type);
            Log.d("EventTypes", "Add type: " + type.name +
                    "(id " + Long.toString(type.id) + ", " +
                    "order " + Long.toString(type.order) + ")");
            added++;
        }

        cursor.close();

        Log.d("EventTypes", "Types added: " + Integer.toString(added));
    }

    public EventType getByID(long id) {
        for(int i = 0; i < types.size(); i++) {
            EventType e = types.get(i);
            if(e.id == id) {
                return(e);
            }
        }
        throw new NoSuchElementException("Unable to find an EventType with id " +
                Long.toString(id));
    }

    public EventType getByName(String name) {
        for(int i = 0; i < types.size(); i++) {
            EventType e = types.get(i);
            if(e.name.equals(name)) {
                return(e);
            }
        }
        throw new NoSuchElementException("Unable to find an EventType with name '" + name + "'");
    }
}

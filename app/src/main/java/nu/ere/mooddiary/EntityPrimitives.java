package nu.ere.mooddiary;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import android.util.Log;

public final class EntityPrimitives {
    public ArrayList<EntityPrimitive> entities;

    public EntityPrimitives(SQLiteDatabase db){
        Log.d("EntityPrimitives", "Enter EntityPrimitives");
        entities = new ArrayList<EntityPrimitive>();

        Cursor cursor = db.rawQuery("SELECT id, name, enabled FROM EntityPrimitives", null);
        int added = 0;

        while(cursor.moveToNext()) {
            EntityPrimitive primitive = new EntityPrimitive(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getLong(2)
                    );
            entities.add(primitive);
            Log.d("EntityPrimitives", "Add primitive: " + cursor.getString(1));
            added++;
        }

        cursor.close();

        Log.d("EntityPrimitives", "Primitives added: " + Integer.toString(added));
    }

    public EntityPrimitive getByID(long id) {
        for(int i = 0; i < entities.size(); i++) {
            EntityPrimitive e = entities.get(i);
            if(e.id == id) {
                return(e);
            }
        }
        throw new NoSuchElementException("Unable to find a primitive with id " +
                Long.toString(id));
    }

    public EntityPrimitive getByName(String name) {
        for(int i = 0; i < entities.size(); i++) {
            EntityPrimitive e = entities.get(i);
            if(e.name.equals(name)) {
                return(e);
            }
        }
        throw new NoSuchElementException("Unable to find a primitive with name '" + name + "'");
    }
}
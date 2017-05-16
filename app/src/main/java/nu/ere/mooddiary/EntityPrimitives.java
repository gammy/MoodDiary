package nu.ere.mooddiary;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import android.util.Log;

public final class EntityPrimitives {
    private static final String LOG_PREFIX = "EntityPrimitives";
    public ArrayList<EntityPrimitive> entities;
    private SQLiteDatabase db = null;

    public EntityPrimitives(SQLiteDatabase db){
        Log.d(LOG_PREFIX, "Enter EntityPrimitives");
        this.db = db;
        reload();
    }

    public void reload() {
        Log.d(LOG_PREFIX, "Enter reload");
        entities = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT id, name, isNumber, enabled FROM EntityPrimitives", null);
        int added = 0;

        while(cursor.moveToNext()) {
            EntityPrimitive primitive = new EntityPrimitive(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getInt(cursor.getColumnIndex("isNumber")),
                    cursor.getInt(cursor.getColumnIndex("enabled"))
            );
            entities.add(primitive);
            Log.d(LOG_PREFIX, "Add primitive: " + cursor.getString(1));
            added++;
        }

        cursor.close();

        Log.d(LOG_PREFIX, "Primitives added: " + Integer.toString(added));
    }

    public EntityPrimitive getByID(int id) {
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
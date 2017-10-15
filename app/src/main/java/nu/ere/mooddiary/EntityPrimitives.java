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
        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Enter EntityPrimitives");
        this.db = db;
        reload();
    }

    public void reload() {
        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Enter reload");
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
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Add primitive: " + cursor.getString(1));
            added++;
        }

        cursor.close();

        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Primitives added: " + Integer.toString(added));
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
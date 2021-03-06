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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter MeasurementTypes");
        types = new ArrayList<>();

        Cursor cursor = db.rawQuery(
                "SELECT " +
                    "id, " +
                    "listOrder, " +
                    "name, " +
                    "entity, " +
                    "minValue, " +
                    "maxValue, " +
                    "defaultValue, " +
                    "enabled " +
                "FROM " +
                    "MeasurementTypes " +
                "ORDER BY " +
                    "listOrder ASC", null);
        int added = 0;

        while(cursor.moveToNext()) {

            MeasurementType type = new MeasurementType(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getInt(cursor.getColumnIndex("listOrder")),
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getInt(cursor.getColumnIndex("entity")),
                    cursor.getInt(cursor.getColumnIndex("minValue")),
                    cursor.getInt(cursor.getColumnIndex("maxValue")),
                    cursor.getInt(cursor.getColumnIndex("defaultValue")),
                    cursor.getInt(cursor.getColumnIndex("enabled"))
            );
            types.add(type);
            Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Add type: " + type.name +
                    "(id " + Long.toString(type.id) + ", " +
                    "order " + Long.toString(type.order) + ") " +
                    (type.enabled == 1 ? "" : " - disabled"));
            added++;
        }

        cursor.close();

        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Types added: " + Integer.toString(added));
    }

    public MeasurementType getByID(long id) {
        for(MeasurementType m: types) {
            if(m.id == id) {
                return(m);
            }
        }

        throw new NoSuchElementException("Unable to find an MeasurementType with id " +
                Long.toString(id));
    }

    public MeasurementType getByName(String name) {
        for(MeasurementType m: types) {
            if(m.name.equals(name)) {
                return(m);
            }
        }
        throw new NoSuchElementException("Unable to find an MeasurementType with name '" + name + "'");
    }

    public ArrayList<MeasurementType> getEnabledTypes() {
        ArrayList<MeasurementType> list = new ArrayList<>();
        for(MeasurementType m: types) {
            if(m.enabled == 1) {
                list.add(m);
            }
        }
        return list;
    }
}

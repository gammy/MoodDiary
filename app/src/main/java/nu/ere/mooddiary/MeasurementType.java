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

import android.view.View;

public class MeasurementType {
    public int id;
    public int order;
    public String name;
    public int entity;
    public int min, max, dfl;
    public int enabled;
    public int totalValues;
    public int normalDefault;

    View view;

    public MeasurementType(int id, int order, String name, int entity, int min, int max, int dfl, int enabled) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.entity = entity;
        this.min = min;
        this.max = max;
        this.dfl = dfl;
        this.enabled = enabled;

        /* Most Android Widgets only allow a minimum value of 0 (i.e no negative values),
           but the database design allows for negative values. We thus need to convert them:

           -3 -2 -1  [0]  1  2  3 ->
            0  1  2  [3]  4  5  6
        */

        this.totalValues = this.max - this.min; // e.g 3 - -3 = 6,
                                                //     3 -  0 = 0

        this.normalDefault = this.dfl - this.min; // e.g 0 - -3 = -3,
                                                  //     0 -  0 = 0
    }

    public EntityPrimitive getPrimitive(EntityPrimitives entityPrimitives) {
        return entityPrimitives.getByID(this.entity);
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getView() {
        return(view);
    }
}


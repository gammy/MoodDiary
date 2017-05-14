package nu.ere.mooddiary;

import android.view.View;

public class EventType {
    public int id;
    public int order;
    public String name;
    public int entity;
    public int min, max, dfl;
    public int enabled;

    public int totalValues;
    public int normalDefault;

    public View view;

    public EventType(int id, int order, String name, int entity, int min, int max, int dfl, int enabled) {
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

    public View getView() {
        return(this.view);
    }

    public void setView(View view) {
        this.view = view;
    }

}


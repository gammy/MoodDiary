package nu.ere.mooddiary;

import java.util.ArrayList;

public class Reminder {
    public long id;
    public int hour, minute;
    public ArrayList<MeasurementType> measurementTypes;

    public Reminder(long id, int hour, int minute, ArrayList<MeasurementType> measurementTypes) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.measurementTypes = measurementTypes;
    }

    // DB Update?
}


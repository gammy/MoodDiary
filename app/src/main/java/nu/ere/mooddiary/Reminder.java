package nu.ere.mooddiary;

import java.util.ArrayList;

public class Reminder {
    public long id;
    public int hour, minute;
    public ArrayList<EventType> eventTypes;

    public Reminder(long id, int hour, int minute, ArrayList<EventType> eventTypes) {
        this.id = id;
        this.hour = hour;
        this.minute = minute;
        this.eventTypes = eventTypes;
    }

    // DB Update?
}


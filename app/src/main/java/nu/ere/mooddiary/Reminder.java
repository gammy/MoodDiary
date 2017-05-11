package nu.ere.mooddiary;

import java.util.ArrayList;

public class Reminder {
    public long id;
    public int hh, mm, dd;
    public ArrayList<EventType> eventTypes;

    public Reminder(long id, int hh, int mm, int dd, ArrayList<EventType> eventTypes) {
        this.id = id;
        this.hh = hh;
        this.mm = mm;
        this.dd = dd;
        this.eventTypes = eventTypes;
    }

    // DB Update?
}


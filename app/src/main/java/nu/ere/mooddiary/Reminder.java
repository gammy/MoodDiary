package nu.ere.mooddiary;

public class Reminder {
    public long id;
    public int hh, mm, dd;

    public Reminder(long id, int hh, int mm, int dd) {
        this.id = id;
        this.hh = hh;
        this.mm = mm;
        this.dd = dd;
    }
}


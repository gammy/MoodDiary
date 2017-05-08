package nu.ere.mooddiary;

public class Entry {
    public int eventType;
    public String value;
    public long time;

    public Entry(int eventType, String value) {
        this.eventType = eventType;
        this.value = value;
        this.time = System.currentTimeMillis() / 1000;
    }
}
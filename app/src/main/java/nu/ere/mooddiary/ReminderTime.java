package nu.ere.mooddiary;

import android.view.View;

public class ReminderTime {
    public int id;
    public int group;
    public int hour;
    public int minute;

    public View view;

    public ReminderTime(int id, int group, int hour, int minute) {
        this.id     = id;
        this.group  = group;
        this.hour   = hour;
        this.minute = minute;
    }

    public View getView() {
        return(this.view);
    }
    public void setView(View view) {
        this.view = view;
    }
}


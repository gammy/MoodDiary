package nu.ere.mooddiary;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class EventNumberClickListener implements OnClickListener {
    MainActivity activity;
    EventType eventType;
    TextView view;
    //TextInputEditText view;

    public EventNumberClickListener(MainActivity activity, TextView view, EventType eventType) {
    //public EventNumberClickListener(MainActivity activity, TextInputEditText view, EventType eventType) {
        this.activity = activity;
        this.eventType = eventType;
        this.view = view;
    }

    @Override
    public void onClick(View v)
    {
        activity.showNumberDialog(activity, view, eventType);
    }


}

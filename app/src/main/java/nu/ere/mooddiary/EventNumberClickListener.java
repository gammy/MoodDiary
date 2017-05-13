package nu.ere.mooddiary;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class EventNumberClickListener implements OnClickListener {
    Activity activity;
    EventType eventType;
    int dialogThemeID;

    //TextInputEditText view;

    public EventNumberClickListener(Activity activity, EventType eventType, int dialogThemeID) {
    //public EventNumberClickListener(MainActivity activity, TextInputEditText view, EventType eventType) {
        this.activity = activity;
        this.eventType = eventType;
        this.dialogThemeID = dialogThemeID;
    }

    @Override
    public void onClick(View v)
    {
        Util.showNumberDialog(activity, eventType, dialogThemeID);
    }


}

package nu.ere.mooddiary;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MeasurementTextClickListener implements OnClickListener {
    public Activity activity;
    public MeasurementType measurementType;
    public int dialogThemeID;
    public TextView textView = null;

    //TextInputEditText view;

    public MeasurementTextClickListener(Activity activity,
                                        TextView textView,
                                        MeasurementType measurementType,
                                        int dialogThemeID) {
        this.activity = activity;
        this.textView = textView;
        this.measurementType = measurementType;
        this.dialogThemeID = dialogThemeID;
    }

    @Override
    public void onClick(View v)
    {
        CustomDialogs.showNumberDialog(activity, textView, measurementType, dialogThemeID);
    }


}

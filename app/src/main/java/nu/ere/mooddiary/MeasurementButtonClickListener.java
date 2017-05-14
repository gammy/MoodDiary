package nu.ere.mooddiary;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class MeasurementButtonClickListener implements OnClickListener {
    public Activity activity;
    public MeasurementType measurementType;
    public int dialogThemeID;

    //TextInputEditText view;

    public MeasurementButtonClickListener(Activity activity,
                                          MeasurementType measurementType,
                                          int dialogThemeID) {
        this.activity = activity;
        this.measurementType = measurementType;
        this.dialogThemeID = dialogThemeID;
    }

    @Override
    public void onClick(View v)
    {
        Util.showNumberDialog(activity, null, measurementType, dialogThemeID);
    }


}

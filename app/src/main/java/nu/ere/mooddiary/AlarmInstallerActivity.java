package nu.ere.mooddiary;

import android.os.Bundle;
import android.util.Log;

public class AlarmInstallerActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "AlarmInstallerActivity";
    private ORM orm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);
        Log.d(LOG_PREFIX, "Installing alarms");
        Alarms.installAlarms(this);
        this.finish();
    }
}

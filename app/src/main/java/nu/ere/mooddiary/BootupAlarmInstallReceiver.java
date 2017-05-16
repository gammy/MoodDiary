package nu.ere.mooddiary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootupAlarmInstallReceiver extends BroadcastReceiver {
    private static final String LOG_PREFIX = "BootupAlarmInst..";

    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_PREFIX, "Enter onReceive");

        //Intent aIntent = new Intent(context.getApplicationContext(), AlarmInstallerActivity.class);
        Intent aIntent = new Intent();

        aIntent.setClassName("nu.ere.mooddiary", "nu.ere.mooddiary.AlarmInstallerActivity");
        aIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        aIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
//                       | Intent.FLAG_ACTIVITY_CLEAR_TOP
//                       | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(aIntent);
    }
}

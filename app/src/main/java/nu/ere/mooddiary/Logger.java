package nu.ere.mooddiary;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {
    private static final String LOG_PREFIX = "Logger";

    public static final int LOGLEVEL_QUIET = 0;
    public static final int LOGLEVEL_1 = 1;
    public static final int LOGLEVEL_2 = 2;
    public static final int LOGLEVEL_3 = 3;

    private static final int CURRENT_LOGLEVEL = LOGLEVEL_3;

    /**
     * Set the calling activity, required by the permission handler
     *
     * @param activity the default activity to use
     */
    public static void requestAccess(Activity activity) {

        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1 /* Callback id */);

        // http://stackoverflow.com/a/6942735/417115
        String state = Environment.getExternalStorageState();
        Log.d(LOG_PREFIX, "sdcard state: " + state);

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(LOG_PREFIX, "sdcard mounted and writable");
        } else if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d(LOG_PREFIX, "sdcard mounted readonly");
        }

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            }
        } else {
            if(PermissionUtils.hasSelfPermissions(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // We're good; nothing more to do
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        1 /* Code */);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(LOG_PREFIX, "We have write permissions");
                } else {
                    Log.d(LOG_PREFIX, "NEIN!!!");
                }
            }
        }
    }

    /**
     * Add a message to the logfile
     * Based on https://stackoverflow.com/questions/1756296/android-writing-logs-to-text-file
     *
     * Example: log(LOGLEVEL_INFO, "Renderer", "Copying memory to framebuffer");
     *
     * @param logLevel The minimum log level required to store the message
     * @param logPrefix A string which is automatically prepended to the message, separated by ': '
     * @param text The message to save
     */
    public static void log(Integer logLevel, String logPrefix, String text) {
        Log.d(logPrefix, text);

        if(CURRENT_LOGLEVEL < logLevel) {
            return;
        }

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YY HH:mm: ");

        String targetPath = "Download" + "/MoodDiary.log";
        File sd = Environment.getExternalStorageDirectory();
        File logFile  = new File(sd,   targetPath);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(dateFormat.format(currentTime));
            buf.append(logPrefix + ": ");
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

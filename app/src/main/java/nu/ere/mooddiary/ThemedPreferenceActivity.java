package nu.ere.mooddiary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ThemedPreferenceActivity extends PreferenceActivity {

    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    private void setTheme() {
        Log.d("ThemedPrefActivity", "Enter setTheme");

        String theme = null;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs != null) {
            Log.d("ThemedPrefActivity", "Unexpected: There is no sharedPreference object");
            theme = sharedPrefs.getString("preference_select_theme", null);
        }

        if(theme == null) {
            theme = new String("default");
        }

        int themeID = 0;

        switch(theme) {
            default:
            case "AppThemeLight":
                themeID = R.style.AppThemeLight;
                break;
            case "AppThemeDark":
                themeID = R.style.AppThemeDark;
                break;
        }

        Log.d("ThemedPrefActivity", "Theme: " + theme + " (rID " + Integer.toString(themeID) +")");
        super.setTheme(themeID);
        Log.d("ThemedPrefActivity", "Theme set by Android: " + super.getTheme());
    }
}


package nu.ere.mooddiary;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ThemedActivity extends AppCompatActivity {

    public int themeID;
    public int dialogThemeID;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    private void setTheme() {
        Log.d("ThemedActivity", "Enter setTheme");

        String theme = null;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs != null) {
            Log.d("ThemedActivity", "Unexpected: There is no sharedPreference object");
            theme = sharedPrefs.getString("preference_select_theme", null);
        }

        if(theme == null) {
            theme = new String("default");
        }

        themeID = dialogThemeID = 0;

        switch(theme) {
            default:
            case "AppThemeLight":
                themeID = R.style.AppThemeLight;
                dialogThemeID = R.style.AppThemeLight_PopupOverlay;
                break;
            case "AppThemeDark":
                themeID = R.style.AppThemeDark;
                dialogThemeID = R.style.AppThemeDark_PopupOverlay;
                 //@android:style/Theme.Holo.Light.Dialog
                break;
        }

        Log.d("ThemedActivity", "Theme: " + theme + " (rID " + Integer.toString(themeID) +")");
        super.setTheme(themeID);
        Log.d("ThemedActivity", "Theme set by Android: " + super.getTheme());
    }
}



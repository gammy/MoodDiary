package nu.ere.mooddiary;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ThemedActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    private void setTheme() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPrefs.getString("preference_select_theme", null);

        //int themeID = R.style.getIdentifier("R.style.ThemeOverlay_AppCompat_Dark", null, null);
        //int themeID = R.style.ThemeOverlay_AppCompat_Dark;
        int styleID = 0;

        switch(theme) {
            default:
            case "AppThemeLight":
                styleID = R.style.AppThemeLight;
                break;
            case "AppThemeDark":
                styleID = R.style.AppThemeDark;
                break;
        }

        Log.d("setTheme", "Theme: " + theme + " (rID " + Integer.toString(styleID) +")");
        //super.setTheme(android.R.style.ThemeOverlay_Material_Dark);
        //super.setTheme(R.style.AppThemeDark);
        super.setTheme(styleID);
        Log.d("setTheme", "Theme set by Android: " + super.getTheme());
    }
}



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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPrefs.getString("preference_select_theme", null);

        //int themeID = R.style.getIdentifier("R.style.ThemeOverlay_AppCompat_Dark", null, null);
        //int themeID = R.style.ThemeOverlay_AppCompat_Dark;
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

        Log.d("setTheme", "Theme: " + theme + " (rID " + Integer.toString(themeID) +")");
        //super.setTheme(android.R.style.ThemeOverlay_Material_Dark);
        //super.setTheme(R.style.AppThemeDark);
        super.setTheme(themeID);
        Log.d("setTheme", "Theme set by Android: " + super.getTheme());
    }
}



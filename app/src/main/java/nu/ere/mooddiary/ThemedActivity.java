/* Mood Diary, a free Android mood tracker
 * Copyright (C) 2017 Kristian Gunstone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nu.ere.mooddiary;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ThemedActivity extends AppCompatActivity {
    private static final String LOG_PREFIX = "ThemedActivity";

    public int themeID;
    public int dialogThemeID;

    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
    }

    private void setTheme() {
        Log.d(LOG_PREFIX, "Enter setTheme");

        String theme = null;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPrefs == null) {
            Log.d(LOG_PREFIX, "Unexpected: There is no sharedPreference object");
        } else {
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

        Log.d(LOG_PREFIX, "Theme: " + theme + " (rID " + Integer.toString(themeID) +")");
        super.setTheme(themeID);
        Log.d(LOG_PREFIX, "Theme set by Android: " + super.getTheme());
    }
}



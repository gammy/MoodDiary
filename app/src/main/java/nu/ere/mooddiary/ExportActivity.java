package nu.ere.mooddiary;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class ExportActivity extends ThemedPreferenceActivity {
    PreferenceScreen prefEventTypes;
    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.export);
        }
    }
    */

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    PreferenceScreen prefCSV,
                     prefSQL;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        addPreferencesFromResource(R.xml.export);
        PreferenceManager.setDefaultValues(this, R.xml.export, false);

        prefSQL = (PreferenceScreen) findPreference("export_sql");
        prefCSV = (PreferenceScreen) findPreference("export_csv");

        createSQLPreferences(); // When user clicks Export -> Export SQL, this is shown
        createCSVPreferences();  // When user clicks Export -> Export CSV, this is shown
    }

    public void createSQLPreferences() {
        Log.d("ExportActivity", "Enter createSQLPreferences");
    }

    public void createCSVPreferences() {
        Log.d("ExportActivity", "Enter createCSVPreferences");

        PreferenceScreen prefEventTypes =
                (PreferenceScreen) findPreference("export_csv_event_types");
        createEventTypePreferences(prefEventTypes);

        //Preference dateFromButton = (Preference) findPreference("export_csv_date_from");
        //dateFromButton.setOnPreferenceClickListener
        //);

        // FIXME these TimePreferences should be DatePreferences... need to do it all custom-style
        TimePreference timePrefBeg = new TimePreference(this);
        timePrefBeg.setTitle("Start time");
        timePrefBeg.setSummary("<selected date>");
        timePrefBeg.setKey("csv_edit_time_beg");
        prefCSV.addPreference(timePrefBeg);

        TimePreference timePrefEnd = new TimePreference(this);
        timePrefEnd.setTitle("End time");
        timePrefEnd.setSummary("<selected date>");
        timePrefEnd.setKey("csv_edit_time_end");
        prefCSV.addPreference(timePrefEnd);

        // Add save button (hitting 'back' without a save should just cancel)
        Preference saveButton = new Preference(this);
        saveButton.setTitle(R.string.submit);
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO Produce the CSV
                ExportActivity.this.finish();
                return true;
            }
        });

        prefCSV.addPreference(saveButton);
    }

    // Function copied near-verbatim from PreferencesActivity :(
    public void createEventTypePreferences(PreferenceScreen screen) {
        Log.d("ExportActivity", "Enter createEventTypePreferences");

        EventTypes eventTypes = MainActivity.eventTypes;

        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("csv_type_" + Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            screen.addPreference(cb);
            Log.d("ExportActivity", "ITERATE EventType");
        }
    }
}
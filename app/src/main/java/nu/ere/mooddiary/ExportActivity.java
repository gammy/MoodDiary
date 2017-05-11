package nu.ere.mooddiary;
import org.bostonandroid.preference.DatePreference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
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

        // Event select
        PreferenceScreen prefEventTypes =
                (PreferenceScreen) findPreference("export_csv_event_types");
        createEventTypePreferences(prefEventTypes);

        // Date begin widget
        DatePreference datePrefBeg = new DatePreference(this, null);
        datePrefBeg.setKey("csv_edit_time_beg");
        datePrefBeg.setPositiveButtonText(R.string.submit);
        datePrefBeg.setNegativeButtonText(R.string.cancel);
        datePrefBeg.setTitle("Start time");

        prefCSV.addPreference(datePrefBeg);
        //datePrefBeg.defaultValue("2017.01.01");

        // Date end widget
        DatePreference datePrefEnd = new DatePreference(this, null);
        datePrefEnd.setKey("csv_edit_time_end");
        datePrefEnd.setPositiveButtonText(R.string.submit);
        datePrefEnd.setNegativeButtonText(R.string.cancel);
        datePrefEnd.setTitle("End time");
        prefCSV.addPreference(datePrefEnd);

        // Save button (hitting 'back' without a save should just cancel)
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
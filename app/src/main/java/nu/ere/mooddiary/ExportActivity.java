package nu.ere.mooddiary;
import org.bostonandroid.preference.DatePreference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
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

        // Create categories
        PreferenceCategory typesCategory = new PreferenceCategory(this);
        PreferenceCategory datesCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);

        typesCategory.setTitle("Event types"); // FIXME hardcoded
        datesCategory.setTitle("Date range"); // FIXME hardcoded
        saveCategory.setTitle("Save"); // FIXME hardcoded

        prefCSV.addPreference(datesCategory);
        prefCSV.addPreference(typesCategory);
        prefCSV.addPreference(saveCategory);

        // Date begin widget
        DatePreference datePrefBeg = new DatePreference(this, null);
        datePrefBeg.setKey("csv_edit_time_beg");
        datePrefBeg.setPositiveButtonText(R.string.submit);
        datePrefBeg.setNegativeButtonText(R.string.cancel);
        datePrefBeg.setTitle("From date"); // FIXME hardcoded
        datesCategory.addPreference(datePrefBeg);

        // Date end widget
        DatePreference datePrefEnd = new DatePreference(this, null);
        datePrefEnd.setKey("csv_edit_time_end");
        datePrefEnd.setPositiveButtonText(R.string.submit);
        datePrefEnd.setNegativeButtonText(R.string.cancel);
        datePrefEnd.setTitle("To date"); // FIXME hardcoded
        datesCategory.addPreference(datePrefEnd);

        // Event type list
        createEventTypePreferences(typesCategory);
        prefCSV.addPreference(typesCategory);

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
        saveCategory.addPreference(saveButton);

    }

    // Function copied near-verbatim from PreferencesActivity :(
    public void createEventTypePreferences(PreferenceCategory screen) {
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
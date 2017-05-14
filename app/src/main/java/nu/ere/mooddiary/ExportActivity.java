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
    private static final String LOG_PREFIX = "ExportActivity";

    private ORM orm;
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
        Log.d(LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

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
        Log.d(LOG_PREFIX, "Enter createSQLPreferences");
    }

    public void createCSVPreferences() {
        Log.d(LOG_PREFIX, "Enter createCSVPreferences");

        // Create categories
        PreferenceCategory typesCategory = new PreferenceCategory(this);
        PreferenceCategory datesCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);

        typesCategory.setTitle(getApplicationContext().getString(R.string.title_category_types));
        datesCategory.setTitle(getApplicationContext().getString(R.string.title_category_dates));
        saveCategory.setTitle(getApplicationContext().getString(R.string.title_category_save));

        prefCSV.addPreference(datesCategory);
        prefCSV.addPreference(typesCategory);
        prefCSV.addPreference(saveCategory);

        // Date begin widget
        DatePreference datePrefBeg = new DatePreference(this, null);
        datePrefBeg.setKey("csv_edit_time_beg");
        datePrefBeg.setPositiveButtonText(R.string.submit);
        datePrefBeg.setNegativeButtonText(R.string.cancel);
        datePrefBeg.setTitle(getApplicationContext().getString(R.string.title_date_from));
        datesCategory.addPreference(datePrefBeg);

        // Date end widget
        DatePreference datePrefEnd = new DatePreference(this, null);
        datePrefEnd.setKey("csv_edit_time_end");
        datePrefEnd.setPositiveButtonText(R.string.submit);
        datePrefEnd.setNegativeButtonText(R.string.cancel);
        datePrefEnd.setTitle(getApplicationContext().getString(R.string.title_date_to));
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
                // TODO Produce the CSV based on the "csv_*" preferences
                ExportActivity.this.finish();
                return true;
            }
        });
        saveCategory.addPreference(saveButton);

    }

    // Function copied near-verbatim from PreferencesActivity :(
    public void createEventTypePreferences(PreferenceCategory screen) {
        Log.d(LOG_PREFIX, "Enter createEventTypePreferences");

        EventTypes eventTypes = orm.getEventTypes();

        Log.d(LOG_PREFIX, "event type count: " + Integer.toString(eventTypes.types.size()));
        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType e = eventTypes.types.get(i);
            CheckBoxPreference cb = new CheckBoxPreference(this);
            cb.setKey("csv_type_" + Long.toString(e.id));
            cb.setTitle(e.name);
            cb.setChecked(e.enabled == 1);
            screen.addPreference(cb);
        }
    }
}
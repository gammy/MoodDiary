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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/* New Reminder menu, containing a list of measurement types, a time, and a save button */
package nu.ere.mooddiary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;
import org.bostonandroid.preference.TimePreference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReminderPreferencesActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "ReminderPref..Activity";
    private ORM orm;

    private int tempHour, tempMinute;
    private Calendar cal = null;
    private HashMap<Integer, CheckBoxPreference> tempTypeMap = null;
    private Bundle bundle;
    PreferenceScreen screen;
    private int oldID = -1;

    public void onCreate(Bundle savedInstanceState) {
        Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);
        orm = orm.getInstance(this);

        ReminderTime reminderTime = null;
        Intent intent = getIntent();
        int editMode = intent.getIntExtra(BundleExtraKey.REMINDER_MODE, -1);

        switch(editMode) {
            default:
                throw new NoSuchElementException(Integer.toString(editMode) + ": Invalid mode");

            case PreferenceEditMode.REMINDER_CREATE:
                Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Our mission: CREATE reminder");
                break;

            case PreferenceEditMode.REMINDER_CHANGE:
            case PreferenceEditMode.REMINDER_DELETE:
                Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Our mission: EDIT / DELETE reminder");
                oldID = intent.getIntExtra(BundleExtraKey.REMINDER_TIME_ID, -1);
                Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "oldID: " + Integer.toString(oldID));
                if(oldID == -1) {
                    throw new NoSuchElementException(Integer.toString(oldID) + ": An existing" +
                        "reminderTimeId needs to be passed to this intent in this mode");
                }
                reminderTime = orm.getReminderTimes().getByID(oldID);
                //Toast.makeText(this, "reminderTime: " + Integer.toString(oldID), Toast.LENGTH_SHORT).show();
                break;
        }

        bundle = new Bundle();

        // Create the main context
        screen = getPreferenceManager().createPreferenceScreen(this);

        // Create categories
        PreferenceCategory timeCategory = new PreferenceCategory(this);
        PreferenceCategory typeCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);
        PreferenceCategory delCategory  = new PreferenceCategory(this);

        timeCategory.setTitle(getApplicationContext().getString(R.string.title_category_time));
        typeCategory.setTitle(getApplicationContext().getString(R.string.title_category_types));
        saveCategory.setTitle(getApplicationContext().getString(R.string.title_category_save));
        delCategory.setTitle(getApplicationContext().getString(R.string.title_category_delete));

        screen.addPreference(timeCategory);
        screen.addPreference(typeCategory);
        if(editMode == PreferenceEditMode.REMINDER_CHANGE) {
            screen.addPreference(delCategory);
        }
        screen.addPreference(saveCategory);

        // Add a time view
        TimePreference timePref = new TimePreference(this, null);

        if(reminderTime != null) {
            tempHour   = reminderTime.hour;
            tempMinute = reminderTime.minute;
        } else {
            // Set a brand-new date to one hour from now
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            tempHour   = calendar.get(Calendar.HOUR_OF_DAY);
            tempMinute = calendar.get(Calendar.MINUTE);
            // FIXME set actual time also (in the dialog)
        }

        cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, tempHour);
        cal.set(Calendar.MINUTE, tempMinute);
        timePref.setCalendar(cal);
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(cal.getTimeZone());
        timePref.setTime(dateFormat.toString());
        timePref.setTitle(Util.toHumanTime(this, tempHour, tempMinute));
        timePref.setSummary("");

        timeCategory.addPreference(timePref);

        MeasurementTypes measurementTypes = orm.getMeasurementTypes();

        ArrayList<MeasurementType> enabledReminderMeasurementTypes = null;

        if(editMode == PreferenceEditMode.REMINDER_CHANGE) {
            enabledReminderMeasurementTypes =
                    orm.getReminderTimes().getTypesByReminderTimeID(oldID);
        }

        tempTypeMap = new HashMap<>();

        // Add checkboxes for all measurement types, and configure them appropriately if
        // we are editing an existing reminder
        for(MeasurementType type: measurementTypes.types) {
            CheckBoxPreference cb = new CheckBoxPreference(this);

            tempTypeMap.put(type.id, cb);

            cb.setTitle(type.name);
            cb.setChecked(false); // Default off

            // Improve this code please
            if(editMode == PreferenceEditMode.REMINDER_CHANGE) {
                cb.setChecked(false);
                for(MeasurementType chkType: enabledReminderMeasurementTypes) {
                    if(chkType.id == type.id) {
                        cb.setChecked(true);
                    }
                }
            }

            typeCategory.addPreference(cb);
        }

        // If we're editing an existing reminder, add the option to delete it with a button
        if(editMode == PreferenceEditMode.REMINDER_CHANGE) {
            Preference delButton = new Preference(this);
            delButton.setTitle(R.string.delete);
            delButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter onPreferenceClick pre-delete bundle");
                    Intent rIntent = getIntent();
                    bundle.putInt(BundleExtraKey.REMINDER_MODE, PreferenceEditMode.REMINDER_DELETE);
                    bundle.putInt(BundleExtraKey.REMINDER_TIME_ID, oldID);
                    rIntent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, rIntent);
                    finish();
                    return true;
                }
            });
            delCategory.addPreference(delButton);
        }

        // Add save button (hitting 'back' without a save should just cancel)
        Preference saveButton = new Preference(this);
        saveButton.setTitle(R.string.submit);
        saveButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Util.log(Util.LOGLEVEL_1, LOG_PREFIX, "Enter onPreferenceClick pre-bundle");
                Intent rIntent = getIntent();

                bundle.putInt(BundleExtraKey.REMINDER_TIME_ID, oldID);
                bundle.putInt(BundleExtraKey.REMINDER_HOUR  , cal.get(Calendar.HOUR_OF_DAY));
                bundle.putInt(BundleExtraKey.REMINDER_MINUTE, cal.get(Calendar.MINUTE));

                ArrayList<Integer> tempTypeList = new ArrayList<>();
                Iterator<Integer> iterator = tempTypeMap.keySet().iterator();
                while(iterator.hasNext()) {
                    Integer typeId = iterator.next();
                    CheckBoxPreference typePref = tempTypeMap.get(typeId);
                    if(typePref.isChecked()) {
                        tempTypeList.add(typeId);
                    }
                }

                bundle.putIntegerArrayList(BundleExtraKey.REMINDER_TYPES, tempTypeList);
                rIntent.putExtras(bundle);
                setResult(Activity.RESULT_OK, rIntent);
                finish();
                return true;
            }
        });

        saveCategory.addPreference(saveButton);
        setPreferenceScreen(screen);
    }
}
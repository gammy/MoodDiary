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
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

// FIXME this is poorly implemented! Augh
public class MeasurementPreferencesActivity extends ThemedPreferenceActivity {
    private static final String LOG_PREFIX = "MeasurePref..Activity";
    private ORM orm;

    int editMode;
    MeasurementType mType = null;
    private Bundle bundle;
    PreferenceScreen screen;

    private int oldID = -1;
    EditTextPreference prefMin, prefMax, prefDfl, prefOrder;
    String valName = null;
    int valType = -1;

    /* What we need to show:
     *          Name: Farts        (EditBox)
     *          Type: range_center (Popup radio buttons?)
     *         Order: 15           (numberbox?)
     * Minimum value: 0            (numberbox)
     * Maximum value: 100          (numberbox)
     * Default value: 0            (numberbox)
     */
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);
        orm = orm.getInstance(this);

        Intent intent = getIntent();
        editMode = intent.getIntExtra(BundleExtraKey.MEASUREMENT_TYPE_MODE, -1);

        switch(editMode) {
            default:
                throw new NoSuchElementException(Integer.toString(editMode) + ": Invalid mode");

            case PreferenceEditMode.MEASUREMENT_TYPE_CREATE:
                Log.d(LOG_PREFIX, "Our mission: CREATE measurement type");
                break;

            case PreferenceEditMode.MEASUREMENT_TYPE_CHANGE:
            case PreferenceEditMode.MEASUREMENT_TYPE_DELETE:
                Log.d(LOG_PREFIX, "Our mission: EDIT / DELETE measurement type");
                oldID = intent.getIntExtra(BundleExtraKey.MEASUREMENT_TYPE_ID, -1);
                Log.d(LOG_PREFIX, "oldID: " + Integer.toString(oldID));
                if(oldID == -1) {
                    throw new NoSuchElementException(Integer.toString(oldID) + ": An existing" +
                        "measurement type id needs to be passed to this intent in this mode");
                }
                mType = orm.getMeasurementTypes().getByID(oldID);
                Toast.makeText(this, "mType: " + Integer.toString(oldID), Toast.LENGTH_SHORT).show();
                break;
        }

        bundle = new Bundle();

        // Create the main context
        screen = getPreferenceManager().createPreferenceScreen(this);

        // Create categories
        PreferenceCategory nameCategory = new PreferenceCategory(this);
        PreferenceCategory typeCategory = new PreferenceCategory(this);
        final PreferenceCategory valuesCategory = new PreferenceCategory(this);
        PreferenceCategory saveCategory = new PreferenceCategory(this);
        PreferenceCategory delCategory  = new PreferenceCategory(this);

        nameCategory.setTitle(getApplicationContext().getString(R.string.title_category_name));
        typeCategory.setTitle(getApplicationContext().getString(R.string.title_category_type));
        valuesCategory.setTitle(getApplicationContext().getString(R.string.title_category_values));
        saveCategory.setTitle(getApplicationContext().getString(R.string.title_category_save));
        delCategory.setTitle(getApplicationContext().getString(R.string.title_category_delete));

        // Name
        screen.addPreference(nameCategory);
        EditTextPreference namePref = new EditTextPreference(this);
        namePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.toString());
                valName = newValue.toString();
                return false;
            }
        });

        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            namePref.setText(mType.name);
            namePref.setSummary(mType.name);
            valName = mType.name;
        }

        // Type
        screen.addPreference(typeCategory);
        typeCategory.addPreference(buildPrimitivePreference());
        valType = 1; // Default

        // Order
        prefOrder = buildValuePicker("List order", 0);
        prefOrder.setEnabled(true); // buildValuePicker disables it, but this is editable.
        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            prefOrder.setSummary(Integer.toString(mType.order));
            prefOrder.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(newValue.toString());
                    return false;
                }
            });
        }

        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            prefOrder.setDefaultValue(Integer.toString(mType.order));
        }

        typeCategory.addPreference(prefOrder);

        // Values
        screen.addPreference(valuesCategory);
        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CREATE) {
            buildMinMaxDfl(valuesCategory, "0", "10", "0");
        } else {
            // Load up previous values
            buildMinMaxDfl(valuesCategory,
                    Integer.toString(mType.min),
                    Integer.toString(mType.max),
                    Integer.toString(mType.dfl));
        }

        // Add a delete section if this is an existing measurement
        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            screen.addPreference(delCategory);
        }
        screen.addPreference(saveCategory);

        nameCategory.addPreference(namePref);

        // If we're editing an existing reminder, add the option to delete it with a button
        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            Preference delButton = new Preference(this);
            delButton.setTitle(R.string.delete);
            delButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(LOG_PREFIX, "Enter onPreferenceClick pre-delete bundle");
                    Intent rIntent = getIntent();
                    bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_MODE,
                            PreferenceEditMode.MEASUREMENT_TYPE_DELETE);
                    bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_ID, oldID);
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
                Log.d(LOG_PREFIX, "Enter onPreferenceClick pre-bundle");

                String strOrder = prefOrder.getSummary().toString();
                String strMinimum = prefMin.getSummary().toString();
                String strMaximum = prefMax.getSummary().toString();
                String strDefault = prefDfl.getSummary().toString();

                if(! testValues(valName, valType, strOrder, strMinimum, strMaximum, strDefault)) {
                    Toast.makeText(MeasurementPreferencesActivity.this,
                            "HrngGg Noo! Check and try again", Toast.LENGTH_LONG).show();
                    return false;
                }

                int valOrder = Integer.parseInt(strOrder);
                int valMinimum = Integer.parseInt(strMinimum);
                int valMaximum = Integer.parseInt(strMaximum);
                int valDefault = Integer.parseInt(strDefault);

                Intent rIntent = getIntent();
                bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_ID, oldID);
                bundle.putString(BundleExtraKey.MEASUREMENT_TYPE_NAME, valName);
                bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_ENTITY, valType);
                bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_ORDER, valOrder);
                bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_MINIMUM, valMinimum);
                bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_MAXIMUM, valMaximum);
                bundle.putInt(BundleExtraKey.MEASUREMENT_TYPE_DEFAULT, valDefault);

                rIntent.putExtras(bundle);
                setResult(Activity.RESULT_OK, rIntent);
                finish();
                return true;
            }
        });

        saveCategory.addPreference(saveButton);
        setPreferenceScreen(screen);
    }

    public ListPreference buildPrimitivePreference() {
        ListPreference typeList = new ListPreference(this);

        typeList.setDialogTitle(getString(R.string.measurement_type_select_type));

        List<String> entityListItems = new ArrayList<>();
        List<String> entityListValues = new ArrayList<>();

        for(EntityPrimitive primitive: orm.getPrimitives().entities) {
            entityListItems.add(primitive.name);
            entityListValues.add(Integer.toString(primitive.id));
        }

        CharSequence[] primitiveKeys =
                entityListItems.toArray(new CharSequence[entityListItems.size()]);
        CharSequence[] primitiveValues =
                entityListValues.toArray(new CharSequence[entityListValues.size()]);

        typeList.setEntries(primitiveKeys);
        typeList.setEntryValues(primitiveValues);

        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CREATE) {
            typeList.setTitle(orm.getPrimitives().entities.get(0).name);
            typeList.setDefaultValue("1");
            typeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ListPreference listPref = (ListPreference) preference;

                    String textValue = newValue.toString();
                    int index = listPref.findIndexOfValue(textValue);
                    CharSequence[] entries = listPref.getEntries();
                    if(index >= 0) {
                        listPref.setTitle(entries[index]);
                        valType = Integer.parseInt(textValue);
                        listPref.setSummary(textValue);
                        listPref.setValueIndex(index);
                    }

                    return false;
                }
            });
        } else {
            typeList.setEnabled(false);

            try {
                typeList.setTitle(orm.getPrimitives().getByID(mType.entity).name);
            } catch (NoSuchElementException e) {
                Toast.makeText(this, "PROGRAMMING BUG ON ENTITY: RESET TO 0", Toast.LENGTH_LONG).show();
                typeList.setTitle(orm.getPrimitives().getByID(1).name);
            }
            typeList.setDefaultValue(Integer.toString(mType.entity)); // FIXME no effect?
        }

        return typeList;
    }

    public void buildMinMaxDfl(PreferenceCategory category,  String min, String max, String dfl) {
        prefMin = buildValuePicker(getString(R.string.select_minimum), Integer.parseInt(min));
        prefMax = buildValuePicker(getString(R.string.select_maximum), Integer.parseInt(max));
        prefDfl = buildValuePicker(getString(R.string.select_default), Integer.parseInt(dfl));

        category.addPreference(prefMin);
        category.addPreference(prefMax);
        category.addPreference(prefDfl);
    }

    public EditTextPreference buildValuePicker(String text, int value) {
        // FIXME in the future we really need a NUMBER Picker, it'll fix lots of issues here
        EditTextPreference valuePicker = new EditTextPreference(this);

        valuePicker.setTitle(text);
        valuePicker.setSummary(Integer.toString(value));
        valuePicker.setText(Integer.toString(value));

        if(editMode == PreferenceEditMode.MEASUREMENT_TYPE_CHANGE) {
            valuePicker.setEnabled(false);
        } else {
            valuePicker.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(newValue.toString());
                    return false;
                }
            });
        }

        return valuePicker;
    }

    public boolean testValues(String name, int valType, String order, String min, String max, String dfl) {

        if(name == null || name == "") {
            Log.d(LOG_PREFIX, "Cannot be empty: Name");
            Toast.makeText(this, "Cannot be empty: Name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(valType == -1) {
            Log.d(LOG_PREFIX, "Programming error: entity: -1");
            Toast.makeText(this, "Programming error entity: -1", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(! isNumeric(order)) {
            Log.d(LOG_PREFIX, "Invalid number: Order");
            Toast.makeText(this, "Invalid number: Order", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(! isNumeric(min)) {
            Log.d(LOG_PREFIX, "Invalid number: Minimum");
            Toast.makeText(this, "Invalid number: Minimum", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(! isNumeric(max)) {
            Log.d(LOG_PREFIX, "Invalid number: Maximum");
            Toast.makeText(this, "Invalid number: Maximum", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(! isNumeric(dfl)) {
            Log.d(LOG_PREFIX, "Invalid number: Default");
            Toast.makeText(this, "Invalid number: Default", Toast.LENGTH_SHORT).show();
            return false;
        }

        // is min less than max?
        int valMin = Integer.parseInt(min);
        int valMax = Integer.parseInt(max);
        int valDfl = Integer.parseInt(dfl);

        if(valMax - valMin <= 0) {
            Log.d(LOG_PREFIX, "Maximum must be more than minimum!");
            Toast.makeText(this, "Maximum must be more than minimum!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Is the default within the range?
        if(valDfl < valMin || valDfl > valMax) {
            Log.d(LOG_PREFIX, "Default must be within minimum and maximum!");
            Toast.makeText(this,
                    "Default must be within minimum and maximum!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // From https://stackoverflow.com/a/1102916/417115, by CraigTP
    public static boolean isNumeric(String str) {
        try {
            int num = Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
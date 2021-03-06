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
package nu.ere.mooddiary;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TextViewCompat;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ReminderActivity extends ThemedDialogActivity {
    private static final String LOG_PREFIX = "ReminderActivity";
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor prefEditor;
    private ORM orm;
    private int reminderID = -1;
    private ArrayList<MeasurementType> measurementTypes = null;
    private boolean nosave = false; // Debug option
    private ImageView fantasticView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter Create");
        super.onCreate(savedInstanceState);
        orm = ORM.getInstance(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = sharedPrefs.edit();

        if (savedInstanceState == null) {
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "This is *not* a saved instance");
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if(extras == null) {
                Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "No state data from instance, and no extras passed?! :O");
                reminderID = -1;
            } else {
                Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Saved instance has extras");
                nosave = extras.getBoolean("nosave", false);
                reminderID = extras.getInt("reminder_id");
                prefEditor.putInt("reminder_id", reminderID);
                prefEditor.apply();
            }
        } else {
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "This *is* a saved instance");
            reminderID = sharedPrefs.getInt("reminder_id", -1);
        }

        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Reminder ID: " + Integer.toString(reminderID));

        if(reminderID == -1) {
            Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Error: Caller didn't provide a reminderID");
            throw new NoSuchElementException("Caller didn't provide a reminderID");
        }

        //Toast.makeText(this, "ID: " + Integer.toString(reminderID), Toast.LENGTH_LONG).show();
        ArrayList<MeasurementType> mTmp = orm.getReminderTimes().getTypesByReminderTimeID(reminderID);
        measurementTypes = new ArrayList<>();
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Starting measurement type walk");
        for(MeasurementType type: mTmp) {
            if(type.enabled == 1) {
                Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, String.format("    Added %s", type.name));
                measurementTypes.add(type);
            } else {
                Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, String.format("  Type %s disabled", type.name));
            }
        }

        Alarms.installAlarms(this); // Reinstall 'em
        Util.raiseNotification(this);
        initUI();
    }

    private void initUI() {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter initUI" );

        setContentView(R.layout.content_reminders);

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) this.findViewById(R.id.reminderLayout);

        // Ensure that no programmatically generated view within our ScrollView forces the
        // view to scroll down: We want the initial to view always to be at the top:
        // http://stackoverflow.com/a/35071620
        ScrollView view = (ScrollView) findViewById(R.id.reminderScrollView);
        view.setFocusableInTouchMode(true);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        buildViews(entryLayout);

        ScrollView l = (ScrollView) findViewById(R.id.reminderScrollView);
        fantasticView = (ImageView) findViewById(R.id.fantasticView); //;new ImageView(this);
        ViewCompat.setElevation(fantasticView, 0); // Above most items

        // FIXME this never shows outside of the viewport for some fucking reason.
        // Set up the save button, which, on click, saves the event and runs an animation
        Button saveButton = new Button(this);

        saveButton.setText(getString(R.string.submit));
        // The saveClickListener below will terminate this activity once it's done.
        if(nosave) {
            ArrayList<MeasurementType> nothing = new ArrayList<>();
            saveButton.setOnClickListener(
                    new SaveClickListener(this, nothing, fantasticView));
        } else {
            saveButton.setOnClickListener(
                    new SaveClickListener(this, measurementTypes, fantasticView));
        }
        entryLayout.addView(saveButton);
    }

    /**
     * Generate the entire page layout for a Reminder.
     * This involves creating the encasing widgets (tables, etc), instantiating Views based on
     * minute primitives, and rendering everything.
     *
     */
    private void buildViews(LinearLayout entryLayout) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter buildViews");
        Resources resources = getResources();

        // Create a table
        TableLayout table = new TableLayout(this);
        table.setColumnStretchable(1, true); // Stretch the rightmost column (holding sliders etc)

        TableRow rowTitle = new TableRow(this);
        rowTitle.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
        rowParams.span = 1;

        rowParams.topMargin    = (int) resources.getDimension(R.dimen.entry_padding_top);
        rowParams.bottomMargin = (int) resources.getDimension(R.dimen.entry_padding_bottom);

        // Walk our measurement types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.

        for(MeasurementType type: measurementTypes) {
            EntityPrimitive primitive = type.getPrimitive(orm.getPrimitives());
            Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Renderer: primitive to render: " + primitive.name);

            // Make a label
            TextView label = new TextView(this);
            TextViewCompat.setTextAppearance(label,
                    android.R.style.TextAppearance_DeviceDefault_Small);
            label.setGravity(Gravity.START);
            label.setText(type.name);

            TableRow row = new TableRow(this);
            //row.setBackgroundColor(Color.BLUE); // (debugging)
            row.addView(label, rowParams);

            // Make the appropriate widget
            switch(primitive.name) {

                case "range_center":
                case "range_normal":
                    row.addView(buildRange(type, primitive), rowParams);
                    break;

                case "number":
                    row.addView(buildNumber(type), rowParams);
                    break;

                case "text":
                    row.addView(buildText(type), rowParams);
                    break;

                case "toggle":
                    row.addView(buildToggle(type), rowParams);
                    break;

                default:
                    break;

            }

            table.addView(row);
        }

        entryLayout.addView(table);
        ViewCompat.setElevation(entryLayout, 0);
    }

    private SeekBar buildRange(MeasurementType type, EntityPrimitive primitive) {
        Resources resources = getResources();
        SeekBar seekBar = new SeekBar(this);
        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Renderer: Range: Original View    : " + seekBar.toString());
        type.setView(seekBar);
        // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
        // the database EntityPrimitive name.
        int styleID = resources.getIdentifier(primitive.name,
                "drawable", this.getPackageName());
        seekBar.setProgressDrawable(
                ResourcesCompat.getDrawable(resources, styleID, null));
        seekBar.setMax(type.totalValues);
        seekBar.setProgress(type.normalDefault);

        return seekBar;
    }

    private TextView buildNumber(MeasurementType type) {
        TextView number = new TextView(this);
        type.setView(number);
        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Renderer: Number: Original View    : " + number.toString());

        /* Can't find an easier way to do this - insane */
        int[] attrs = new int[] { R.attr.editTextBackground};
        TypedArray ta = this.obtainStyledAttributes(attrs);
        Drawable drawableFromTheme = ta.getDrawable(0);
        ta.recycle();
        number.setBackgroundDrawable(drawableFromTheme);

        number.setGravity(Gravity.CENTER_HORIZONTAL);
        TextViewCompat.setTextAppearance(
                number, android.R.style.TextAppearance_DeviceDefault_Medium);
        //number.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

        number.setText(Integer.toString(type.dfl));
        MeasurementTextClickListener listener =
                new MeasurementTextClickListener(this, number, type, themeID);
        number.setOnClickListener(listener);

        return number;
    }

    private TextView buildText(MeasurementType type) {
        TextInputEditText text = new TextInputEditText(this);
        type.setView(text);
        Logger.log(Logger.LOGLEVEL_3, LOG_PREFIX, "Renderer: Text: Original View    : " + text.toString());
        text.setText("");
        TextViewCompat.setTextAppearance(text,
                android.R.style.TextAppearance_DeviceDefault_Medium);
        return text;
    }

    private CheckBox buildToggle(MeasurementType type) {
        CheckBox checkBox = new CheckBox(this);
        type.setView(checkBox);
        checkBox.setChecked(type.dfl == 1);
        return checkBox;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter onDestroy");
        Logger.log(Logger.LOGLEVEL_2, LOG_PREFIX, "Cancelling this notification");
        // Sets an ID for the notification
        int mNotificationId = 1;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(mNotificationId);
    }
}

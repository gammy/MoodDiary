package nu.ere.mooddiary;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

// TODO: get database objects from MainActivity, perhaps with:
//http://stackoverflow.com/questions/2906925/how-do-i-pass-an-object-from-one-activity-to-another-on-android

public class ReminderActivity extends ThemedActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ReminderActivity", "Create");
        super.onCreate(savedInstanceState);

        /** Notification test ***********************/
        Intent resultIntent = new Intent(this, ReminderActivity.class);
        // ..
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!");

        mBuilder.setContentIntent(pendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

        renderEntryTypes();
    }

    public void renderEntryTypes() {
        Log.d("MainActivity", "Enter renderEntryTypes");
    /*
        // FIXME move some of this back into initUI; this function is only called ONCE.

        // Get our main (scrollable) view, where we are to programmatically add our EntryTypes
        LinearLayout entryLayout = (LinearLayout) findViewById(R.id.reminderLayout);

        // Create a table
        TableLayout table = new TableLayout(this);
        table.setColumnStretchable(1, true); // Stretch the rightmost column (holding sliders etc)

        TableRow rowTitle = new TableRow(this);
        rowTitle.setGravity(Gravity.CENTER_HORIZONTAL);

        TableRow.LayoutParams rowParams = new TableRow.LayoutParams();
        rowParams.span = 1;

        rowParams.topMargin    = (int) getResources().getDimension(R.dimen.entry_padding_top);
        rowParams.bottomMargin = (int) getResources().getDimension(R.dimen.entry_padding_bottom);

        // Walk our event types and create the appropriate text and entry widget (slider, etc).
        // Add them to the main layout.
        for(int i = 0; i < eventTypes.types.size(); i++) {
            EventType etype = eventTypes.types.get(i);
            EntityPrimitive primitive = etype.getPrimitive(entityPrimitives);

            // Make a label
            TextView label = new TextView(this);
            TextViewCompat.setTextAppearance(label,
                    android.R.style.TextAppearance_DeviceDefault_Small);
            label.setGravity(Gravity.START);
            label.setText(etype.name);

            TableRow row = new TableRow(this);
            // row.setBackgroundColor(Color.BLUE); // (debugging)
            row.addView(label, rowParams);

            // Make the appropriate widget
            switch(primitive.name) {

                case "range_center":
                case "range_normal":
                    SeekBar seekBar = new SeekBar(this);
                    etype.setView(seekBar);
                    // The drawable resource name (i.e 'res/drawable/range_center.xml') matches
                    // the database EntityPrimitive name.
                    int styleID = getResources().getIdentifier(primitive.name,
                            "drawable", this.getPackageName());
                    seekBar.setProgressDrawable(
                            ResourcesCompat.getDrawable(getResources(), styleID, null));
                    seekBar.setMax((int) etype.totalValues);
                    seekBar.setProgress((int) etype.normalDefault);
                    row.addView(seekBar, rowParams);
                    break;

                case "number":
                    TextView number = new TextView(this);
                    //number.setPaintFlags(number.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    //number.setBackground(android:background="?attr/editTextBackground"

                    // Can't find an easier way to do this - insane
                    int[] attrs = new int[] { R.attr.editTextBackground};
                    TypedArray ta = this.obtainStyledAttributes(attrs);
                    Drawable drawableFromTheme = ta.getDrawable(0);
                    ta.recycle();
                    number.setBackgroundDrawable(drawableFromTheme);

                    //TextInputEditText number = new TextInputEditText(this);
                    etype.setView(number);
                    number.setGravity(Gravity.CENTER_HORIZONTAL);
                    TextViewCompat.setTextAppearance(number,
                            android.R.style.TextAppearance_DeviceDefault_Medium);
                    //number.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Medium);

                    number.setText(Long.toString(etype.normalDefault));
                    EventNumberClickListener listener =
                            new EventNumberClickListener(MainActivity.this, number, etype);
                    number.setOnClickListener(listener);
                    row.addView(number, rowParams);
                    break;

                case "text":
                    TextInputEditText text = new TextInputEditText(this);
                    etype.setView(text);
                    row.addView(text, rowParams);
                    break;

                default:
                    break;

            }

            table.addView(row);
        }

        entryLayout.addView(table);
    */
    }
}

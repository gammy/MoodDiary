package nu.ere.mooddiary;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

public class OverviewActivity extends ThemedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Overview", "Create");
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setContentView(R.layout.overview_main);

        loadInfo();
    }

    private void loadInfo() {
        Log.d("Overview", "Enter loadInfo");

        TextView view = (TextView) findViewById(R.id.overviewText);
        SQLiteDatabase db = MainActivity.db;

        SQLiteStatement s;

        s = db.compileStatement("SELECT COUNT(*) FROM Events");
        long entryCount = s.simpleQueryForLong();
        s.releaseReference();

        s = db.compileStatement("SELECT COUNT(*) FROM EventTypes");
        long eventTypeCount = s.simpleQueryForLong();
        s.releaseReference();

        s = db.compileStatement("SELECT COUNT(*) FROM EntityPrimitives");
        long entityPrimitiveCount = s.simpleQueryForLong();
        s.releaseReference();

        s = db.compileStatement("SELECT COUNT(*) FROM ReminderTimes");
        long reminderCount = s.simpleQueryForLong();
        s.releaseReference();

        String text =
                Long.toString(entryCount) + " entries\n" +
                Long.toString(reminderCount) + " reminders\n" +
                Long.toString(eventTypeCount) + " event types\n" +
                Long.toString(entityPrimitiveCount) + " entity primitives\n\n";

        Cursor cursor =
                MainActivity.db.rawQuery("SELECT date, value FROM Events ORDER BY date DESC", null);
        cursor.moveToFirst();

        while(cursor.moveToNext()) {
            long unixTime = cursor.getLong(0);
            String value  = cursor.getString(1);
            java.util.Date time = new java.util.Date((long) unixTime * 1000);
            text += time.toString() + ": " + value + "\n";
        }

        view.setText(text);
    }
}
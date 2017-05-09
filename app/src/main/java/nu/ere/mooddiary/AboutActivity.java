package nu.ere.mooddiary;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class AboutActivity extends ThemedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("About", "Create");
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setContentView(R.layout.about_main);
    }
}
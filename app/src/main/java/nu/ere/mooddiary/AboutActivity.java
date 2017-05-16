package nu.ere.mooddiary;

import nu.ere.mooddiary.BuildConfig;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_PREFIX, "Create");
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.aboutToolbar);
        // FIXME if I try to use the toolbar, app crashes
        setSupportActionBar(toolbar);

        setContentView(R.layout.content_about);

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        String stupid = getString(R.string.text_about);
        stupid = "Mood Diary v" + versionName +
                " (" + Integer.toString(versionCode) +") " + stupid;
        TextView view = (TextView) findViewById(R.id.aboutText);
        view.setText(stupid);

        Button donateButton = (Button) findViewById(R.id.donateButton);

        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = getResources().getString(R.string.paypal_action_link);
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });
    }

}
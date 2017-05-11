package nu.ere.mooddiary;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AboutActivity extends ThemedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("About", "Create");
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setContentView(R.layout.about_main);
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
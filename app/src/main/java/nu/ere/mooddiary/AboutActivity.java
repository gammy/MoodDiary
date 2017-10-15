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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package nu.ere.mooddiary;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends ThemedActivity {
    private static final String LOG_PREFIX = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.log(Logger.LOGLEVEL_1, LOG_PREFIX, "Enter onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_about);

        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        TextView versionTextView = (TextView) findViewById(R.id.versionTextView);
        versionTextView.setText("v" + versionName + "-" + Integer.toString(versionCode));

        WebView webView = (WebView) findViewById(R.id.aboutWebView);
        webView.loadUrl("file:///android_asset/about.html");
        webView.setBackgroundColor(Color.TRANSPARENT);
    }

}
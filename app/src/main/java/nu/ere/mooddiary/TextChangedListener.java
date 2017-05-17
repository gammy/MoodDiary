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

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;

public class TextChangedListener implements TextWatcher{

    SharedPreferences.Editor prefEditor = null;
    String prefKey = null;

    public void setPreference(SharedPreferences.Editor editor,  String key) {
        this.prefEditor = editor;
        this.prefKey = key;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        prefEditor.putString(prefKey, s.toString());
        prefEditor.apply();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}

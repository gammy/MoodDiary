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

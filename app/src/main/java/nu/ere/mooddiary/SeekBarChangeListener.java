package nu.ere.mooddiary;

import android.content.SharedPreferences;
import android.widget.SeekBar;

class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

    SharedPreferences.Editor prefEditor = null;
    String prefKey = null;

    public void setPreference(SharedPreferences.Editor editor,  String key) {
        this.prefEditor = editor;
        this.prefKey = key;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        prefEditor.putInt(prefKey, progress);
        prefEditor.apply();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}


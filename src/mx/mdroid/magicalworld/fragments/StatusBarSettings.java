package mx.mdroid.magicalworld.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY_SAVER_COLOR = "status_bar_battery_saver_color";
    private ColorPickerPreference mBatterySaverColor;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.mdroid_settings_statusbar);

        PreferenceScreen prefSet = getPreferenceScreen();

        final ContentResolver resolver = getActivity().getContentResolver();

        int batterySaverColor = Settings.Secure.getInt(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_SAVER_COLOR, 0xfff4511e);
        mBatterySaverColor = (ColorPickerPreference) findPreference(STATUS_BAR_BATTERY_SAVER_COLOR);
        mBatterySaverColor.setNewPreviewColor(batterySaverColor);
        mBatterySaverColor.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mBatterySaverColor) {
            int color = ((Integer) objValue).intValue();
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.STATUS_BAR_BATTERY_SAVER_COLOR, color);
            return true;
        } 
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.MAGICAL_WORLD;
    }

}

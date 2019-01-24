/*
 * Copyright (C) 2017-2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mx.mdroid.magicalworld.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;

import com.android.settings.mdroid.CustomSeekBarPreference;
import com.android.settings.mdroid.SecureSettingSwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;


public class NetworkTrafficSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener  {

    private static final String TAG = "NetworkTrafficSettings";

    private DropDownPreference mNetTrafficMode;
    private CustomSeekBarPreference mNetTrafficAutohide;
    private DropDownPreference mNetTrafficUnits;
    private DropDownPreference mNetTrafficFrequency;
    private SecureSettingSwitchPreference mNetTrafficShowUnits;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.network_traffic_settings);
        final ContentResolver resolver = getActivity().getContentResolver();

        mNetTrafficMode = (DropDownPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_MODE);
        mNetTrafficMode.setOnPreferenceChangeListener(this);
        int mode = Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_MODE, 0);
        mNetTrafficMode.setValue(String.valueOf(mode));

        mNetTrafficFrequency = (DropDownPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_FREQUENCY);
        mNetTrafficFrequency.setOnPreferenceChangeListener(this);
        int frequency = Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_FREQUENCY, /* 1000 ms */ 1000);
        mNetTrafficFrequency.setValue(String.valueOf(frequency));

        mNetTrafficAutohide = (CustomSeekBarPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE);
        int mAutohideThreshold = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE, /* 10 kbps */ 10, UserHandle.USER_CURRENT);
        mNetTrafficAutohide.setValue(mAutohideThreshold);
        mNetTrafficAutohide.setOnPreferenceChangeListener(this);

        mNetTrafficUnits = (DropDownPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_UNITS);
        mNetTrafficUnits.setOnPreferenceChangeListener(this);
        int units = Settings.Secure.getInt(resolver,
                Settings.Secure.NETWORK_TRAFFIC_UNITS, /* Mbps */ 1);
        mNetTrafficUnits.setValue(String.valueOf(units));

        mNetTrafficShowUnits = (SecureSettingSwitchPreference)
                findPreference(Settings.Secure.NETWORK_TRAFFIC_SHOW_UNITS);
        mNetTrafficShowUnits.setOnPreferenceChangeListener(this);

        updateEnabledStates(mode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNetTrafficMode) {
            int mode = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_MODE, mode);
            updateEnabledStates(mode);
        } else if (preference == mNetTrafficFrequency) {
            int frequency = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_FREQUENCY, frequency);
        } else if (preference == mNetTrafficUnits) {
            int units = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_UNITS, units);
        } else if (preference == mNetTrafficAutohide) {
            int value = (Integer) newValue;
            Settings.Secure.putIntForUser(getActivity().getContentResolver(),
                    Settings.Secure.NETWORK_TRAFFIC_AUTOHIDE, value, UserHandle.USER_CURRENT);
        }
        return true;
    }

    private void updateEnabledStates(int mode) {
        final boolean enabled = mode != 0;
        mNetTrafficFrequency.setEnabled(enabled);
        mNetTrafficAutohide.setEnabled(enabled);
        mNetTrafficUnits.setEnabled(enabled);
        mNetTrafficShowUnits.setEnabled(enabled);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.MAGICAL_WORLD;
    }
}

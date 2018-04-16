/*
 * Copyright (C) 2018 The MiracleDROID Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mx.mdroid.magicalworld;

import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.development.DevelopmentSettings;

public class MagicalWorld extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final String KEY_DEVICE_PART = "device_part";
        final String KEY_DEVICE_PART_PACKAGE_NAME = "com.mdroid.settings.device";

        addPreferencesFromResource(R.xml.mdroid_settings_main);
        PreferenceScreen prefScreen = getPreferenceScreen();

        // DeviceParts
        if (!DevelopmentSettings.isPackageInstalled(getActivity(), KEY_DEVICE_PART_PACKAGE_NAME)) {
            getPreferenceScreen().removePreference(findPreference(KEY_DEVICE_PART));
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.MAGICAL_WORLD;
    }
}

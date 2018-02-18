/*
 * Copyright (C) 2016 CarbonROM
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

package mx.elixir.magicalworld.fragments;

import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.widget.Toast;

import mx.elixir.magicalworld.preferences.CustomSeekBarPreference;

import com.android.settings.R;
import mx.elixir.magicalworld.fragments.ActionFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;
import com.android.internal.util.elixir.ElixirUtils;
import com.android.internal.util.hwkeys.ActionConstants;
import com.android.internal.util.hwkeys.ActionUtils;

public class ButtonSettings extends ActionFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "Buttons";
    private static final String TORCH_POWER_BUTTON_GESTURE = "torch_power_button_gesture";

    private static final String HWKEY_DISABLE = "hardware_keys_disable";
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";

    //Keys
    private static final String KEY_BUTTON_BRIGHTNESS = "button_brightness";
    private static final String KEY_BUTTON_BRIGHTNESS_SW = "button_brightness_sw";
    private static final String KEY_BACKLIGHT_TIMEOUT = "backlight_timeout";
    private static final String KEY_BUTTON_BACKLIGHT_ON_TOUCH = "button_backlight_on_touch_only";

    // category keys
    private static final String CATEGORY_HWKEY = "hardware_keys";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    private ListPreference mBacklightTimeout;
    private CustomSeekBarPreference mButtonBrightness;
    private SwitchPreference mButtonBrightness_sw;
    private SwitchPreference mButtonBacklightOnTouch;

    private ListPreference mTorchPowerButton;
    private ListPreference mVolumeKeyCursorControl;

    private ContentResolver resolver;

    private SwitchPreference mHwKeyDisable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.elixir_settings_button);

        resolver = getActivity().getContentResolver();

        PreferenceScreen prefSet = getPreferenceScreen();

        int cursorControlAction = Settings.System.getInt(resolver,
                Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
        mVolumeKeyCursorControl = initActionList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                cursorControlAction);

        if (!ElixirUtils.deviceSupportsFlashLight(getContext())) {
            Preference toRemove = prefSet.findPreference(TORCH_POWER_BUTTON_GESTURE);
            if (toRemove != null) {
                prefSet.removePreference(toRemove);
            }
        } else {
            mTorchPowerButton = (ListPreference) findPreference(TORCH_POWER_BUTTON_GESTURE);
            int mTorchPowerButtonValue = Settings.Secure.getInt(resolver,
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0);
            mTorchPowerButton.setValue(Integer.toString(mTorchPowerButtonValue));
            mTorchPowerButton.setSummary(mTorchPowerButton.getEntry());
            mTorchPowerButton.setOnPreferenceChangeListener(this);
        }

        final PreferenceCategory hwkeyCat = (PreferenceCategory) prefSet.findPreference(CATEGORY_HWKEY);
        final boolean needsNavbar = ActionUtils.hasNavbarByDefault(getActivity());
        mHwKeyDisable = (SwitchPreference) findPreference(HWKEY_DISABLE);

        int keysDisabled = 0;

        if (needsNavbar){
            prefSet.removePreference(hwkeyCat);
        }else{
            keysDisabled = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.HARDWARE_KEYS_DISABLE, 0,
                    UserHandle.USER_CURRENT);
            mHwKeyDisable.setChecked(keysDisabled != 0);
            mHwKeyDisable.setOnPreferenceChangeListener(this);

            final boolean variableBrightness = getResources().getBoolean(
                    com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);

            mBacklightTimeout =
                    (ListPreference) findPreference(KEY_BACKLIGHT_TIMEOUT);

            mButtonBrightness =
                    (CustomSeekBarPreference) findPreference(KEY_BUTTON_BRIGHTNESS);

            mButtonBrightness_sw =
                    (SwitchPreference) findPreference(KEY_BUTTON_BRIGHTNESS_SW);

            mButtonBacklightOnTouch =
                    (SwitchPreference) findPreference(KEY_BUTTON_BACKLIGHT_ON_TOUCH);

            if (mBacklightTimeout != null) {
                mBacklightTimeout.setOnPreferenceChangeListener(this);
                int BacklightTimeout = Settings.System.getInt(getContentResolver(),
                        Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 5000);
                mBacklightTimeout.setValue(Integer.toString(BacklightTimeout));
                mBacklightTimeout.setSummary(mBacklightTimeout.getEntry());
            }

            if (variableBrightness) {
                hwkeyCat.removePreference(mButtonBrightness_sw);
                if (mButtonBrightness != null) {
                    int ButtonBrightness = Settings.System.getInt(getContentResolver(),
                            Settings.System.BUTTON_BRIGHTNESS, 255);
                    mButtonBrightness.setValue(ButtonBrightness / 1);
                    mButtonBrightness.setOnPreferenceChangeListener(this);
                }
            } else {
                hwkeyCat.removePreference(mButtonBrightness);
                if (mButtonBrightness_sw != null) {
                    mButtonBrightness_sw.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.BUTTON_BRIGHTNESS, 1) == 1));
                    mButtonBrightness_sw.setOnPreferenceChangeListener(this);
                }
            }
        }

        // bits for hardware keys present on device
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        // read bits for present hardware keys
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        // load categories and init/remove preferences based on device
        // configuration
        final PreferenceCategory backCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_BACK);
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefSet.findPreference(CATEGORY_APPSWITCH);

        // back key
        if (!hasBackKey) {
            prefSet.removePreference(backCategory);
        }

        // home key
        if (!hasHomeKey) {
            prefSet.removePreference(homeCategory);
        }

        // App switch key (recents)
        if (!hasAppSwitchKey) {
            prefSet.removePreference(appSwitchCategory);
        }

        // menu key
        if (!hasMenuKey) {
            prefSet.removePreference(menuCategory);
        }

        // search/assist key
        if (!hasAssistKey) {
            prefSet.removePreference(assistCategory);
        }

        // let super know we can load ActionPreferences
        onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.HWKEYS));

        // load preferences first
        setActionPreferencesEnabled(keysDisabled == 0);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.MAGICAL_WORLD;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private ListPreference initActionList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleActionListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getActivity().getContentResolver(), setting, Integer.valueOf(value));
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean DoubleTapPowerGesture = Settings.Secure.getInt(resolver,
                    Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 1) == 0;

        if (preference == mTorchPowerButton) {
            int mTorchPowerButtonValue = Integer.valueOf((String) objValue);
            int index = mTorchPowerButton.findIndexOfValue((String) objValue);
            mTorchPowerButton.setSummary(
                    mTorchPowerButton.getEntries()[index]);
            Settings.Secure.putInt(resolver, Settings.Secure.TORCH_POWER_BUTTON_GESTURE,
                    mTorchPowerButtonValue);
            if (mTorchPowerButtonValue == 1 && DoubleTapPowerGesture) {
                //if doubletap for torch is enabled, switch off double tap for camera
                Settings.Secure.putInt(resolver, Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                        1);
                Toast.makeText(getActivity(),
                    (R.string.torch_power_button_gesture_dt_toast),
                    Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (preference == mHwKeyDisable) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.HARDWARE_KEYS_DISABLE,
                    value ? 1 : 0);
            setActionPreferencesEnabled(!value);
            if (mButtonBrightness_sw != null) {
                mButtonBrightness_sw.setEnabled(!value);
            }
            if (mButtonBrightness != null) {
                mButtonBrightness.setEnabled(!value);
            }
            if (mBacklightTimeout != null) {
                mBacklightTimeout.setEnabled(!value);
            }
            if (mButtonBacklightOnTouch != null) {
                mButtonBacklightOnTouch.setEnabled(!value);
            }
            return true;
        } else if (preference == mBacklightTimeout) {
            String BacklightTimeout = (String) objValue;
            int BacklightTimeoutValue = Integer.parseInt(BacklightTimeout);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, BacklightTimeoutValue);
            int BacklightTimeoutIndex = mBacklightTimeout
                    .findIndexOfValue(BacklightTimeout);
            mBacklightTimeout
                    .setSummary(mBacklightTimeout.getEntries()[BacklightTimeoutIndex]);
            return true;
        } else if (preference == mButtonBrightness) {
            int value = (Integer) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, value * 1);
            return true;
        } else if (preference == mButtonBrightness_sw) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, value ? 1 : 0);
            return true;
        } else if (preference == mVolumeKeyCursorControl) {
            handleActionListChange(mVolumeKeyCursorControl, objValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        }
        return false;
    }

    @Override
    protected boolean usesExtendedActionsList() {
        return true;
    }
}

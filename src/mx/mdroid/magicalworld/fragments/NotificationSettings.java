package mx.mdroid.magicalworld.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;

public class NotificationSettings extends SettingsPreferenceFragment
                        implements OnPreferenceChangeListener {

    private ListPreference mTickerMode;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.mdroid_settings_notifications);

        PreferenceScreen prefScreen = getPreferenceScreen();

        mTickerMode = (ListPreference) findPreference("ticker_mode");
        mTickerMode.setOnPreferenceChangeListener(this);
        int tickerMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_SHOW_TICKER,
                1, UserHandle.USER_CURRENT);
        mTickerMode.setValue(String.valueOf(tickerMode));
        mTickerMode.setSummary(mTickerMode.getEntry());
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference.equals(mTickerMode)) {
            int tickerMode = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TICKER, tickerMode, UserHandle.USER_CURRENT);
            int index = mTickerMode.findIndexOfValue((String) newValue);
            mTickerMode.setSummary(
                    mTickerMode.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.MAGICAL_WORLD;
    }
}

package org.mariotaku.twidere.extension.appwidget.activity;

import org.mariotaku.twidere.extension.appwidget.Constants;
import org.mariotaku.twidere.extension.appwidget.R;
import org.mariotaku.twidere.extension.appwidget.provider.StackWidgetProvider;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements Constants, OnSharedPreferenceChangeListener {

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		final AppWidgetManager manager = AppWidgetManager.getInstance(this);
		final int[] ids = manager.getAppWidgetIds(new ComponentName(this, StackWidgetProvider.class));
		manager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
		getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
		addPreferencesFromResource(R.xml.settings);
	}

}

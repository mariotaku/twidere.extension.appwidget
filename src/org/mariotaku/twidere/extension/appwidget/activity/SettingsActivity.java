package org.mariotaku.twidere.extension.appwidget.activity;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.appwidget.Constants;
import org.mariotaku.twidere.extension.appwidget.R;
import org.mariotaku.twidere.extension.appwidget.provider.StackWidgetProvider;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements Constants, OnSharedPreferenceChangeListener {

	private static final int REQUEST_REQUEST_PERMISSIONS = 101;
	
	@Override
	public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
		final AppWidgetManager manager = AppWidgetManager.getInstance(this);
		final int[] ids = manager.getAppWidgetIds(new ComponentName(this, StackWidgetProvider.class));
		manager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_REQUEST_PERMISSIONS: {
				if (resultCode != RESULT_OK) {
					finish();
					return;
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean granted;
		try {
			granted = Twidere.checkPermissionsSatisfied(this);
		} catch (SecurityException e) {
			//TODO show error
			finish();
			return;
		}
		if (!granted) {
			final Intent intent = new Intent(Twidere.INTENT_ACTION_REQUEST_PERMISSIONS);
			intent.setPackage("org.mariotaku.twidere");
			startActivityForResult(intent, REQUEST_REQUEST_PERMISSIONS);
		}
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
		getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
		addPreferencesFromResource(R.xml.settings);
	}

}

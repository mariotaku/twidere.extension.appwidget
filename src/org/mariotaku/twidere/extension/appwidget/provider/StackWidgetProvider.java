package org.mariotaku.twidere.extension.appwidget.provider;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.appwidget.Constants;
import org.mariotaku.twidere.extension.appwidget.R;
import org.mariotaku.twidere.extension.appwidget.service.StackWidgetHomeTimelineService;
import org.mariotaku.twidere.extension.appwidget.service.StackWidgetMentionsService;
import org.mariotaku.twidere.extension.appwidget.util.SetRemoteAdapterAccessor;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

public class StackWidgetProvider extends AppWidgetProvider implements Constants {

	@Override
	public void onDeleted(final Context context, final int[] appWidgetIds) {
		final SharedPreferences.Editor editor = context.getSharedPreferences(WIDGETS_PREFERENCES_NAME,
				Context.MODE_PRIVATE).edit();
		for (final int id : appWidgetIds) {
			editor.remove(String.valueOf(id));
		}
		editor.apply();
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final SharedPreferences preferences = context.getSharedPreferences(WIDGETS_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final String action = intent.getAction();
		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
		final int[] ids = manager.getAppWidgetIds(new ComponentName(context, getClass()));
		if (Twidere.BROADCAST_HOME_TIMELINE_DATABASE_UPDATED.equals(action)
				|| Twidere.BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
			for (final int id : ids) {
				final int ticker_widget_type = preferences.getInt(String.valueOf(id), WIDGET_TYPE_HOME_TIMELINE);
				switch (ticker_widget_type) {
					case WIDGET_TYPE_MENTIONS:
						if (Twidere.BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
							manager.notifyAppWidgetViewDataChanged(id, R.id.stack_view);
						}
						break;
					case WIDGET_TYPE_HOME_TIMELINE:
						if (Twidere.BROADCAST_HOME_TIMELINE_DATABASE_UPDATED.equals(action)) {
							manager.notifyAppWidgetViewDataChanged(id, R.id.stack_view);
						}
						break;
				}
			}

		} else if (Twidere.BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
			manager.notifyAppWidgetViewDataChanged(ids, R.id.stack_view);
		}
		super.onReceive(context, intent);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onUpdate(final Context context, final AppWidgetManager manager, final int[] ids) {
		final SharedPreferences preferences = context.getSharedPreferences(WIDGETS_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		for (final int id : ids) {
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stack_widget);
			final Intent intent;
			final int widget_type = preferences.getInt(String.valueOf(id), WIDGET_TYPE_HOME_TIMELINE);
			Log.d("Widget", "id = " + id + ", type = " + widget_type);
			switch (widget_type) {
				case WIDGET_TYPE_MENTIONS:
					intent = new Intent(context, StackWidgetMentionsService.class);
					break;
				default:
					intent = new Intent(context, StackWidgetHomeTimelineService.class);
					break;
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				SetRemoteAdapterAccessor.setRemoteAdapter(views, R.id.stack_view, intent);
			} else {
				views.setRemoteAdapter(id, R.id.stack_view, intent);
			}
			manager.updateAppWidget(id, views);
		}
	}

}

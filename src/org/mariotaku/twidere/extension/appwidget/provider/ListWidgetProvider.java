package org.mariotaku.twidere.extension.appwidget.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import java.util.Arrays;
import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.appwidget.Constants;
import org.mariotaku.twidere.extension.appwidget.R;
import org.mariotaku.twidere.extension.appwidget.service.ListWidgetHomeTimelineService;
import org.mariotaku.twidere.extension.appwidget.service.ListWidgetMentionsService;
import org.mariotaku.twidere.extension.appwidget.util.SetRemoteAdapterAccessor;

public class ListWidgetProvider extends AppWidgetProvider implements Constants {

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
				final int list_widget_type = preferences.getInt(String.valueOf(id), WIDGET_TYPE_HOME_TIMELINE);
				switch (list_widget_type) {
					case WIDGET_TYPE_MENTIONS:
						if (Twidere.BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
							manager.notifyAppWidgetViewDataChanged(id, R.id.list_view);
						}
						break;
					case WIDGET_TYPE_HOME_TIMELINE:
						if (Twidere.BROADCAST_HOME_TIMELINE_DATABASE_UPDATED.equals(action)) {
							manager.notifyAppWidgetViewDataChanged(id, R.id.list_view);
						}
						break;
				}
			}

		} else if (Twidere.BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
			manager.notifyAppWidgetViewDataChanged(ids, R.id.list_view);
		} else if (Twidere.BROADCAST_TASK_STATE_CHANGED.equals(action)) {
			onUpdate(context, manager, ids);
		} else if (BROADCAST_REFRESH_ALL.equals(action)) {
			// TODO
		} else if (BROADCAST_SET_LIST_WIDGET_TYPE.equals(action)) {
			final SharedPreferences.Editor editor = context.getSharedPreferences(WIDGETS_PREFERENCES_NAME,
					Context.MODE_PRIVATE).edit();
			final int id = intent.getIntExtra(INTENT_KEY_WIDGET_ID, -1);
			final int type = intent.getIntExtra(INTENT_KEY_WIDGET_TYPE, -1);
			editor.putInt(String.valueOf(id), type);
			editor.commit();
			onUpdate(context, manager, ids);			
		}
		super.onReceive(context, intent);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onUpdate(final Context context, final AppWidgetManager manager, final int[] ids) {
		final SharedPreferences preferences = context.getSharedPreferences(WIDGETS_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		// service.waitForService();
		final boolean is_mentions_refreshing = false;
		final boolean is_home_timeline_refreshing = false;
		for (final int id : ids) {
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.list_widget);
			views.setViewVisibility(R.id.top_bar, View.VISIBLE);
			final PendingIntent compose_intent = PendingIntent.getActivity(context, 0, new Intent(
					Twidere.INTENT_ACTION_COMPOSE), 0);
			final PendingIntent twidere_intent = PendingIntent.getActivity(context, 0, new Intent(
					Twidere.INTENT_ACTION_HOME), 0);
			final PendingIntent refresh_intent = PendingIntent.getBroadcast(context, 0, new Intent(
					BROADCAST_REFRESH_ALL), 0);
			views.setOnClickPendingIntent(R.id.compose, compose_intent);
			views.setOnClickPendingIntent(R.id.refresh, refresh_intent);
			views.setOnClickPendingIntent(R.id.top_bar, twidere_intent);
			
			final Intent set_home_intent = new Intent(BROADCAST_SET_LIST_WIDGET_TYPE);
			set_home_intent.putExtra(INTENT_KEY_WIDGET_TYPE, WIDGET_TYPE_HOME_TIMELINE);
			set_home_intent.putExtra(INTENT_KEY_WIDGET_ID, id);
			views.setOnClickPendingIntent(R.id.tab_home, PendingIntent.getBroadcast(context, getRequestId(id, WIDGET_TYPE_HOME_TIMELINE), set_home_intent, PendingIntent.FLAG_ONE_SHOT));
			final Intent set_mentions_intent = new Intent(BROADCAST_SET_LIST_WIDGET_TYPE);
			set_mentions_intent.putExtra(INTENT_KEY_WIDGET_TYPE, WIDGET_TYPE_MENTIONS);
			set_mentions_intent.putExtra(INTENT_KEY_WIDGET_ID, id);
			views.setOnClickPendingIntent(R.id.tab_mentions, PendingIntent.getBroadcast(context, getRequestId(id, WIDGET_TYPE_MENTIONS), set_mentions_intent, PendingIntent.FLAG_ONE_SHOT));

			final int widget_type = preferences.getInt(String.valueOf(id), WIDGET_TYPE_HOME_TIMELINE);
			final Intent adapter_intent = getAdapterIntent(context, widget_type);
			switch (widget_type) {
				case WIDGET_TYPE_MENTIONS:
					views.setViewVisibility(R.id.refresh_progress, is_mentions_refreshing ? View.VISIBLE : View.GONE);
					views.setViewVisibility(R.id.refresh, !is_mentions_refreshing ? View.VISIBLE : View.GONE);
					views.setViewVisibility(R.id.tab_indicator_home, View.GONE);
					views.setViewVisibility(R.id.tab_indicator_mentions, View.VISIBLE);
					break;
				default:
					views.setViewVisibility(R.id.refresh_progress, is_home_timeline_refreshing ? View.VISIBLE
							: View.GONE);
					views.setViewVisibility(R.id.refresh, !is_home_timeline_refreshing ? View.VISIBLE : View.GONE);
					views.setViewVisibility(R.id.tab_indicator_home, View.VISIBLE);
					views.setViewVisibility(R.id.tab_indicator_mentions, View.GONE);
					break;
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				SetRemoteAdapterAccessor.setRemoteAdapter(views, R.id.list_view, adapter_intent);
			} else {
				views.setRemoteAdapter(id, R.id.list_view, adapter_intent);
			}
			manager.updateAppWidget(id, views);
		}
	}

	private int getRequestId(final Object... values) {
		return Arrays.hashCode(values);
	}
	
	private Intent getAdapterIntent(final Context context, final int type) {
		switch (type) {
			case WIDGET_TYPE_MENTIONS:
				return new Intent(context, ListWidgetMentionsService.class);
			default:
				return new Intent(context, ListWidgetHomeTimelineService.class);
		}
	}
}

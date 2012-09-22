package org.mariotaku.twidere.extension.appwidget.adapter;

import static org.mariotaku.twidere.Constants.PREFERENCE_KEY_DISPLAY_NAME;
import static org.mariotaku.twidere.Constants.PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE;
import static org.mariotaku.twidere.Constants.PREFERENCE_KEY_ENABLE_FILTER;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.buildActivatedStatsWhereClause;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.buildFilterWhereClause;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getFilename;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getRoundedCornerBitmap;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getTableNameForContentUri;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getTwidereCacheDir;

import java.io.File;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.appwidget.Constants;
import org.mariotaku.twidere.extension.appwidget.R;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.HtmlEscapeHelper;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

public abstract class StatusesAdapter implements RemoteViewsFactory, Constants {

	private final int layout;

	private final Context context;
	private final ContentResolver resolver;
	private final Resources resources;
	private final SharedPreferences preferences;
	private Cursor cursor;
	private StatusCursorIndices indices;

	public StatusesAdapter(Context context, int layout) {
		this.layout = layout;
		this.context = context;
		resolver = context.getContentResolver();
		resources = context.getResources();
		preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	public abstract Uri getContentUri();

	@Override
	public int getCount() {
		return cursor != null ? cursor.getCount() : 0;
	}

	@Override
	public long getItemId(int position) {
		if (cursor == null) return -1;
		cursor.moveToPosition(position);
		return cursor.getLong(indices._id);
	}

	@Override
	public RemoteViews getLoadingView() {
		// return new RemoteViews(context.getPackageName(),
		// R.layout.status_item_loading);
		return null;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		final RemoteViews views = new RemoteViews(context.getPackageName(), layout);
		if (cursor == null || indices == null) return views;
		cursor.moveToPosition(position);
		if (!preferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true)) {
			views.setTextViewText(R.id.name, cursor.getString(indices.screen_name));
		} else {
			views.setTextViewText(R.id.name, cursor.getString(indices.name));
		}
		views.setTextViewText(R.id.text, HtmlEscapeHelper.unescape(cursor.getString(indices.text)));
		views.setTextViewText(R.id.time, DateUtils.getRelativeTimeSpanString(cursor.getLong(indices.status_timestamp)));
		final Uri.Builder uri_builder = new Uri.Builder();
		uri_builder.scheme(Twidere.SCHEME_TWIDERE);
		uri_builder.authority(Twidere.AUTHORITY_STATUS);
		uri_builder.appendQueryParameter(Twidere.QUERY_PARAM_ACCOUNT_ID,
				String.valueOf(cursor.getLong(indices.account_id)));
		uri_builder.appendQueryParameter(Twidere.QUERY_PARAM_STATUS_ID,
				String.valueOf(cursor.getLong(indices.status_id)));
		final Intent intent = new Intent(Intent.ACTION_VIEW, uri_builder.build());
		final PendingIntent pending_intent = PendingIntent.getActivity(context, 0, intent,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		views.setOnClickPendingIntent(R.id.item, pending_intent);
		if (!preferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true)) {
			views.setViewVisibility(R.id.profile_image, View.GONE);
		} else {
			views.setViewVisibility(R.id.profile_image, View.VISIBLE);
			final File cache_dir = getTwidereCacheDir();
			final String profile_image_url = cursor.getString(indices.profile_image_url);
			final String file_name = getFilename(resources.getBoolean(R.bool.hires_profile_image) ? getBiggerTwitterProfileImage(profile_image_url)
					: profile_image_url);
			final File profile_image_file = cache_dir != null && cache_dir.isDirectory() && file_name != null ? new File(
					cache_dir, file_name) : null;
			final Bitmap profile_image = profile_image_file != null && profile_image_file.isFile() ? BitmapFactory
					.decodeFile(profile_image_file.getPath()) : null;
			if (profile_image != null) {
				views.setImageViewBitmap(R.id.profile_image, getRoundedCornerBitmap(resources, profile_image));
			} else {
				views.setImageViewResource(R.id.profile_image, R.drawable.ic_profile_image_default);
			}
		}

		return views;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDataSetChanged() {
		final Uri uri = getContentUri();
		final String[] cols = new String[] { Statuses._ID, Statuses.ACCOUNT_ID, Statuses.STATUS_ID, Statuses.TEXT,
				Statuses.SCREEN_NAME, Statuses.NAME, Statuses.STATUS_TIMESTAMP, Statuses.PROFILE_IMAGE_URL };
		String where = buildActivatedStatsWhereClause(context, null);
		if (preferences.getBoolean(PREFERENCE_KEY_ENABLE_FILTER, false)) {
			final String table = getTableNameForContentUri(uri);
			where = buildFilterWhereClause(table, where);
		}
		cursor = resolver.query(uri, cols, where, null, Statuses.DEFAULT_SORT_ORDER);
		indices = new StatusCursorIndices(cursor);
	}

	@Override
	public void onDestroy() {
		if (cursor != null) {
			cursor.close();
		}
		cursor = null;
	}
}
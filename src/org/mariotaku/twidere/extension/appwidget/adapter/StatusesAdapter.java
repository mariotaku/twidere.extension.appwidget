package org.mariotaku.twidere.extension.appwidget.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.appwidget.Constants;
import org.mariotaku.twidere.extension.appwidget.R;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import static org.mariotaku.twidere.Constants.PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.buildActivatedStatsWhereClause;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.buildFilterWhereClause;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getAccountColor;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getTableId;
import static org.mariotaku.twidere.extension.appwidget.util.Utils.getTableNameById;

public abstract class StatusesAdapter implements RemoteViewsFactory, Constants {

	private final int layout;

	private final Context context;
	private final ContentResolver resolver;
	private final Resources resources;
	private final SharedPreferences preferences;
	private Cursor cursor;
	private StatusCursorIndices indices;

	private boolean should_show_account_color;

	public StatusesAdapter(final Context context, final int layout) {
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
	public long getItemId(final int position) {
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
	public RemoteViews getViewAt(final int position) {
		final RemoteViews views = new RemoteViews(context.getPackageName(), layout);
		if (cursor == null || indices == null) return views;
		cursor.moveToPosition(position);
		views.setTextViewText(R.id.screen_name, "@" + cursor.getString(indices.user_screen_name));
		views.setTextViewText(R.id.name, cursor.getString(indices.user_name));
		views.setTextViewText(R.id.text, cursor.getString(indices.text_unescaped));
		views.setTextViewText(R.id.time, DateUtils.getRelativeTimeSpanString(cursor.getLong(indices.status_timestamp)));
		final boolean is_gap = cursor.getInt(indices.is_gap) == 1;
		views.setViewVisibility(R.id.status_content, is_gap ? View.GONE : View.VISIBLE);
		views.setViewVisibility(R.id.account_color, is_gap ? View.GONE : View.VISIBLE);
		views.setViewVisibility(R.id.gap_indicator, is_gap ? View.VISIBLE : View.GONE);
		final Uri.Builder uri_builder = new Uri.Builder();
		uri_builder.scheme(Twidere.SCHEME_TWIDERE);
		uri_builder.authority(Twidere.AUTHORITY_STATUS);
		final Bundle extras = new Bundle();
		extras.putLong(Twidere.QUERY_PARAM_ACCOUNT_ID, cursor.getLong(indices.account_id));
		extras.putLong(Twidere.QUERY_PARAM_STATUS_ID, cursor.getLong(indices.status_id));
		final Intent intent = new Intent(Intent.ACTION_VIEW, uri_builder.build());
		intent.putExtras(extras);
		views.setOnClickFillInIntent(R.id.list_item, intent);
		if (!preferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true)) {
			views.setViewVisibility(R.id.profile_image, View.GONE);
		} else {
			views.setViewVisibility(R.id.profile_image, View.VISIBLE);
			final ParcelFileDescriptor fd = Twidere.getCachedImageFd(context, cursor.getString(indices.user_profile_image_url));
			final Bitmap profile_image = fd != null ? BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor()) : null;
			if (profile_image != null) {
				views.setImageViewBitmap(R.id.profile_image, profile_image);
			} else {
				views.setImageViewResource(R.id.profile_image, R.drawable.ic_profile_image_default);
			}
		}
		final long account_id = cursor.getLong(indices.account_id);
		views.setInt(R.id.account_color, "setBackgroundColor",
				should_show_account_color ? getAccountColor(context, account_id) : Color.TRANSPARENT);

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
		final String[] cols = new String[] { Statuses._ID, Statuses.ACCOUNT_ID, Statuses.STATUS_ID, Statuses.TEXT_UNESCAPED,
				Statuses.SCREEN_NAME, Statuses.NAME, Statuses.STATUS_TIMESTAMP, Statuses.PROFILE_IMAGE_URL, Statuses.IS_GAP };
		final String where = buildFilterWhereClause(getTableNameById(getTableId(uri)),
				buildActivatedStatsWhereClause(context, null));
		cursor = resolver.query(uri, cols, where, null, Statuses.DEFAULT_SORT_ORDER);
		indices = new StatusCursorIndices(cursor);
		should_show_account_color = getActivatedAccountIds(context).length > 1;
	}

	@Override
	public void onDestroy() {
		if (cursor != null) {
			cursor.close();
		}
		cursor = null;
	}
}

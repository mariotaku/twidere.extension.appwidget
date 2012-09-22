package org.mariotaku.twidere.extension.appwidget.util;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

public final class SetRemoteAdapterAccessor {

	@TargetApi(14)
	public static void setRemoteAdapter(RemoteViews views, int viewId, Intent intent) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			views.setRemoteAdapter(viewId, intent);
		}
	}
}

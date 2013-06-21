package org.mariotaku.twidere.extension.appwidget;

public interface Constants {

	public static final String WIDGETS_PREFERENCES_NAME = "widgets";
	public static final String SHARED_PREFERENCES_NAME = "settings";
	
	public static final String BROADCAST_REFRESH_ALL = "org.mariotaku.twidere.extension.appwidget.REFRESH_ALL";
	public static final String BROADCAST_SET_LIST_WIDGET_TYPE = "org.mariotaku.twidere.extension.appwidget.SET_LIST_WIDGET_TYPE";

	public static final String INTENT_KEY_WIDGET_ID = "widget_id";
	public static final String INTENT_KEY_WIDGET_TYPE = "widget_type";
	
	public static final int WIDGET_TYPE_HOME_TIMELINE = 1;
	public static final int WIDGET_TYPE_MENTIONS = 2;

}

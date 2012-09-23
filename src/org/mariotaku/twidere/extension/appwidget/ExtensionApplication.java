package org.mariotaku.twidere.extension.appwidget;

import org.mariotaku.twidere.extension.appwidget.util.ServiceInterface;

import android.app.Application;

public class ExtensionApplication extends Application {

	private ServiceInterface mService;

	public ServiceInterface getServiceInterface() {
		return mService;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mService = ServiceInterface.getInstance(this);
	}

}

package de.droidcachebox;

import CB_UI_Base.ProfilerBase;
import android.os.Debug;

public class AndroidProfiler extends ProfilerBase {

	@Override
	public void startMethodTracing() {
		Debug.startMethodTracing();
	}

	@Override
	public void stopMethodTracing() {
		Debug.stopMethodTracing();

	}

}

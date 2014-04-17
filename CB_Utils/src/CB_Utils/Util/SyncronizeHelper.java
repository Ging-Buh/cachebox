package CB_Utils.Util;

import CB_Utils.Log.Logger;

public class SyncronizeHelper
{

	private static String aktSyncBlock;

	private static boolean inSync = false;

	public static void sync(String methodeName)
	{
		if (inSync)
		{
			Logger.LogCat("Wait of Sync [" + methodeName + "] Bockt");
			return;
		}

		Logger.LogCat("Sync =>" + methodeName);
		aktSyncBlock = methodeName;
		inSync = true;
	}

	public static void endSync(String methodeName)
	{
		Logger.LogCat("Sync END=>" + methodeName);
		inSync = false;
	}

}

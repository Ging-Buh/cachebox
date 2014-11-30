package CB_Utils;

import com.badlogic.gdx.Application;

public enum LogLevel
{
	none, error, info, debug;

	public static int getLevelId(LogLevel level)
	{

		switch (level)
		{
		case debug:
			return Application.LOG_DEBUG;
		case error:
			return Application.LOG_ERROR;
		case info:
			return Application.LOG_INFO;
		case none:
			return Application.LOG_NONE;
		default:
			return Application.LOG_NONE;

		}

	}

}

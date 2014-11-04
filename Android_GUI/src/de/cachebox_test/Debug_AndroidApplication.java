package de.cachebox_test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import CB_UI_Base.Global;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.LogLevel;
import CB_Utils.Util.iChanged;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

public class Debug_AndroidApplication extends AndroidApplication
{
	private File LogFile;

	public Debug_AndroidApplication()
	{
		CB_UI_Base_Settings.AktLogLevel.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				logLevel = LogLevel.getLevelId((LogLevel) CB_UI_Base_Settings.AktLogLevel.getEnumValue());
			}
		});
		logLevel = LogLevel.getLevelId((LogLevel) CB_UI_Base_Settings.AktLogLevel.getEnumValue());
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

	}

	@Override
	public void debug(String tag, String message)
	{
		if (logLevel >= LOG_DEBUG)
		{
			tag = formatTag(tag);
			Log.d(tag, message);
			writeToLogFile(DEBUG, tag, message);
		}
	}

	@Override
	public void debug(String tag, String message, Throwable exception)
	{
		if (logLevel >= LOG_DEBUG)
		{
			tag = formatTag(tag);
			Log.d(tag, message, exception);
			writeToLogFile(DEBUG, tag, message, exception);
		}
	}

	@Override
	public void log(String tag, String message)
	{
		if (logLevel >= LOG_INFO)
		{
			tag = formatTag(tag);
			Log.i(tag, message);
			writeToLogFile(INFO, tag, message);
		}
	}

	@Override
	public void log(String tag, String message, Throwable exception)
	{
		if (logLevel >= LOG_INFO)
		{
			tag = formatTag(tag);
			Log.i(tag, message, exception);
			writeToLogFile(INFO, tag, message, exception);
		}
	}

	@Override
	public void error(String tag, String message)
	{
		if (logLevel >= LOG_ERROR)
		{
			tag = formatTag(tag);
			Log.e(tag, message);
			writeToLogFile(ERROR, tag, message);
		}
	}

	@Override
	public void error(String tag, String message, Throwable exception)
	{
		if (logLevel >= LOG_ERROR)
		{
			tag = formatTag(tag);
			Log.e(tag, message, exception);
			writeToLogFile(ERROR, tag, message, exception);
		}
	}

	private static final String INFO = "[INFO ] ";
	private static final String DEBUG = "[DEBUG] ";
	private static final String ERROR = "[ERROR] ";

	private void writeToLogFile(String Level, String tag, String message)
	{
		checkLogFile();
		try
		{
			tag = addTimeToTag(tag) + Level;
			FileOutputStream fos = new FileOutputStream(LogFile, true);
			PrintStream ps = new PrintStream(fos);
			ps.append(tag + ": " + message);
			ps.append(Global.br);
			ps.flush();
			ps.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	private void writeToLogFile(String Level, String tag, String message, Throwable exception)
	{
		checkLogFile();
		try
		{
			tag = addTimeToTag(tag) + Level;
			FileOutputStream fos = new FileOutputStream(LogFile, true);
			PrintStream ps = new PrintStream(fos);
			ps.append(tag + ": " + message);
			ps.append(Global.br);
			exception.printStackTrace(ps);
			ps.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	private void checkLogFile()
	{
		if (LogFile == null)
		{
			// create Log file
			String Path = Gdx.files.getLocalStoragePath() + "/Logs/";

			new File(Path).mkdirs();

			String logFileName = Path + "Log_" + new SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.GERMAN).format(new Date()) + ".txt";
			LogFile = new File(logFileName);
		}

	}

	private String formatTag(String tag)
	{
		int fill = 16 - tag.length();
		for (int i = 0; i < fill; i++)
			tag += " ";
		return tag;
	}

	private String addTimeToTag(String Tag)
	{
		return new SimpleDateFormat("HH:mm:ss.SSS", Locale.GERMAN).format(new Date()) + "";
	}
}

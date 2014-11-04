package de;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_UI_Base.Global;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.LogLevel;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * TODO Description
 * 
 * @author Longri 2014
 */
public class CB_LwjglApplication extends LwjglApplication
{

	private File LogFile;

	public CB_LwjglApplication(ApplicationListener listener, LwjglApplicationConfiguration config)
	{
		super(listener, config);

		CB_UI_Base_Settings.AktLogLevel.addChangedEventListner(new iChanged()
		{

			@Override
			public void isChanged()
			{
				logLevel = LogLevel.getLevelId((LogLevel) CB_UI_Base_Settings.AktLogLevel.getEnumValue());
			}
		});
	}

	public void setLogLevel(int level)
	{
		super.setLogLevel(level);
	}

	@Override
	public void debug(String tag, String message)
	{
		if (logLevel >= LOG_DEBUG)
		{
			tag = formatTag(tag);
			System.out.println(tag + ": " + message);
			writeToLogFile(DEBUG, tag, message);
		}
	}

	@Override
	public void debug(String tag, String message, Throwable exception)
	{
		if (logLevel >= LOG_DEBUG)
		{
			tag = formatTag(tag);
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
			writeToLogFile(DEBUG, tag, message, exception);
		}
	}

	@Override
	public void log(String tag, String message)
	{
		if (logLevel >= LOG_INFO)
		{
			tag = formatTag(tag);
			System.out.println(tag + ": " + message);
			writeToLogFile(INFO, tag, message);
		}
	}

	@Override
	public void log(String tag, String message, Throwable exception)
	{
		if (logLevel >= LOG_INFO)
		{
			tag = formatTag(tag);
			System.out.println(tag + ": " + message);
			exception.printStackTrace(System.out);
			writeToLogFile(INFO, tag, message, exception);

		}
	}

	@Override
	public void error(String tag, String message)
	{
		if (logLevel >= LOG_ERROR)
		{
			tag = formatTag(tag);
			System.err.println(tag + ": " + message);
			writeToLogFile(ERROR, tag, message);
		}
	}

	@Override
	public void error(String tag, String message, Throwable exception)
	{
		if (logLevel >= LOG_ERROR)
		{
			tag = formatTag(tag);
			System.err.println(tag + ": " + message);
			exception.printStackTrace(System.err);
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
			String logFileName = "Log_" + new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date()) + ".txt";
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
		return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + "";
	}

}

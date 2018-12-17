package cb_server;

import CB_UI_Base.Global;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.Clipboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class implements only the logger methods for logging on Gdx.app.debug, Gdx.app.info and Gdx.app.error!
 * @author Longri
 *
 */
public class GdxLogger implements com.badlogic.gdx.Application {

	private final Logger log = LoggerFactory.getLogger(CacheboxServer.class);
	private int logLevel;
	private File LogFile;

	@Override
	public void setLogLevel(int arg0) {
		logLevel = arg0;
	}

	@Override
	public void debug(String tag, String message) {
		if (logLevel >= LOG_DEBUG) {
			tag = formatTag(tag);
			log.debug(tag, message);
			writeToLogFile(DEBUG, tag, message);
		}
	}

	@Override
	public void debug(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_DEBUG) {
			tag = formatTag(tag);
			log.debug(tag, message, exception);
			writeToLogFile(DEBUG, tag, message, exception);
		}
	}

	@Override
	public void log(String tag, String message) {
		if (logLevel >= LOG_INFO) {
			tag = formatTag(tag);
			log.info(tag, message);
			writeToLogFile(INFO, tag, message);
		}
	}

	@Override
	public void log(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_INFO) {
			tag = formatTag(tag);
			log.info(tag, message, exception);
			writeToLogFile(INFO, tag, message, exception);
		}
	}

	@Override
	public void error(String tag, String message) {
		if (logLevel >= LOG_ERROR) {
			tag = formatTag(tag);
			log.error(tag, message);
			writeToLogFile(ERROR, tag, message);
		}
	}

	@Override
	public void error(String tag, String message, Throwable exception) {
		if (logLevel >= LOG_ERROR) {
			tag = formatTag(tag);
			log.error(tag, message, exception);
			writeToLogFile(ERROR, tag, message, exception);
		}
	}

	private static final String INFO = "[INFO ] ";
	private static final String DEBUG = "[DEBUG] ";
	private static final String ERROR = "[ERROR] ";

	private void writeToLogFile(String Level, String tag, String message) {
		checkLogFile();
		try {
			tag = addTimeToTag(tag) + Level;
			FileOutputStream fos = new FileOutputStream(LogFile, true);
			PrintStream ps = new PrintStream(fos);
			ps.append(tag + ": " + message);
			ps.append(Global.br);
			ps.flush();
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void writeToLogFile(String Level, String tag, String message, Throwable exception) {
		checkLogFile();
		try {
			tag = addTimeToTag(tag) + Level;
			FileOutputStream fos = new FileOutputStream(LogFile, true);
			PrintStream ps = new PrintStream(fos);
			ps.append(tag + ": " + message);
			ps.append(Global.br);
			exception.printStackTrace(ps);
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void checkLogFile() {
		if (LogFile == null) {
			// create Log file
			String logFileName = "Log_" + new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date()) + ".txt";
			LogFile = new File(logFileName);
		}

	}

	private String formatTag(String tag) {
		int fill = 16 - tag.length();
		for (int i = 0; i < fill; i++)
			tag += " ";
		return tag;
	}

	private String addTimeToTag(String Tag) {
		return new SimpleDateFormat("HH:mm:ss.SSS", Locale.GERMAN).format(new Date()) + "";
	}

	//#####################################################################
	// all other methods never used! return null or do nothing

	@Override
	public void exit() {
		// do nothing 

	}

	@Override
	public ApplicationListener getApplicationListener() {
		// do nothing 
		return null;
	}

	@Override
	public Audio getAudio() {
		// do nothing 
		return null;
	}

	@Override
	public Clipboard getClipboard() {
		// do nothing 
		return null;
	}

	@Override
	public Files getFiles() {
		// do nothing 
		return null;
	}

	@Override
	public Graphics getGraphics() {
		// do nothing 
		return null;
	}

	@Override
	public Input getInput() {
		// do nothing 
		return null;
	}

	@Override
	public long getJavaHeap() {
		// do nothing 
		return 0;
	}

	@Override
	public int getLogLevel() {
		// do nothing 
		return 0;
	}

	@Override
	public long getNativeHeap() {
		// do nothing 
		return 0;
	}

	@Override
	public Net getNet() {
		// do nothing 
		return null;
	}

	@Override
	public Preferences getPreferences(String arg0) {
		// do nothing 
		return null;
	}

	@Override
	public ApplicationType getType() {
		// do nothing 
		return null;
	}

	@Override
	public int getVersion() {
		// do nothing 
		return 0;
	}

	@Override
	public void postRunnable(Runnable arg0) {
		// do nothing 

	}

	@Override
	public void removeLifecycleListener(LifecycleListener arg0) {
		// do nothing 

	}

	@Override
	public void addLifecycleListener(LifecycleListener arg0) {
		// do nothing 

	}

}

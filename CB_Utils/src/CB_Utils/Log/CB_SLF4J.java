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
package CB_Utils.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;

import CB_Utils.Plattform;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Class for initialization of slf4j {@link #Logger} inside of all CB projects. <br>
 * Initial this class inside the main class with: <br>
 * <code>new CB_SLF4J(WorkPath);</code> <br>
 * <br>
 * Inside the given WorkPath will create a Folder <code>Logs</code>. <br>
 * On this folder the class will search the {@link #Logger} config file called <code>"logback.xml"</code>.<br>
 * <br>
 * If the config file exists, so the LoggerFactory will load this config file, otherwise the LoggerFactory <br>
 * will set to DEFAULT Logger.<br>
 * <br>
 * The config file will be changed the property value <code>property name="LOG_DIR"</code> to the path of the given<br>
 * WorkPath eG. <code> sdCard/cachebox/Logs</code> if the given WorkPath <code> cdCard/cachebox</code>
 * 
 * @author Longri 2014
 */
public class CB_SLF4J {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_SLF4J.class);
	public static final String br = System.getProperty("line.separator");
	private final String WORKPATH;
	private final String logFolder;
	private final String logBackXmlFile;
	private static CB_SLF4J that;

	/**
	 * Constructor for initialization of slf4j {@link #Logger} inside of all CB projects. <br>
	 * Initial this class inside the main class with: <br>
	 * <code>new CB_SLF4J(WorkPath);</code> <br>
	 * <br>
	 * Inside the given WorkPath will create a Folder <code>Logs</code>. <br>
	 * On this folder the class will search the {@link #Logger} config file called <code>"logback.xml"</code>.<br>
	 * <br>
	 * If the config file exists, so the LoggerFactory will load this config file, otherwise the LoggerFactory <br>
	 * will set to DEFAULT Logger.<br>
	 * <br>
	 * The config file will be changed the property value <code>property name="LOG_DIR"</code> to the path of the given<br>
	 * WorkPath eG. <code> sdCard/cachebox/Logs</code> if the given WorkPath <code> cdCard/cachebox</code>
	 */
	public CB_SLF4J(String workpath) {
		that = this;
		WORKPATH = workpath;

		logFolder = (WORKPATH + "/Logs").replace("\\", "/");
		logBackXmlFile = logFolder + "/logback.xml";

		File logFolderFiile = new File(logFolder);

		if (logFolderFiile.exists() && logFolderFiile.isDirectory()) {// delete all logs are not from today

			String fileNames[] = logFolderFiile.list();
			for (String fileName : fileNames) {
				if (!fileName.endsWith("logback.xml")) {
					File file = new File(logFolder + "/" + fileName);

					if (file.isFile() && file.lastModified() < System.currentTimeMillis() - (24 * 60 * 60 * 100)) {
						// file is older then 24h, so we delete
						file.delete();
					}
				}
			}
		} else {// create folder
			logFolderFiile.mkdirs();
		}

		if (new File(logBackXmlFile).exists() || LogLevel.isLogLevel(LogLevel.ERROR)) {
			Initial();
		}
	}

	private void Initial() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd._HH_mm_ss");
		String logfile = logFolder + "/log_" + simpleDateFormat.format(new Date()) + ".txt";

		boolean xmlLogbackInitial = false;

		if (new File(logBackXmlFile).exists()) {// if logback.xml exists then initial with this

			// first change <property name="LOG_DIR" inside logback.xml to workpath/Logs
			String xml = null;
			InputStream instream = null;
			try {

				StringBuilder sb = new StringBuilder();

				// open the file for reading
				instream = new FileInputStream(logBackXmlFile);

				// if file the available for reading
				if (instream != null) {
					// prepare the file for reading
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffreader = new BufferedReader(inputreader);

					String line;

					// read every line of the file into the line-variable, on line at the time
					do {
						line = buffreader.readLine();
						boolean red = false;
						if (red || line != null) {
							if (line.contains("<property name=\"LOG_DIR\"")) {
								int pos = line.indexOf("value=\"") + 7;
								int endpos = line.lastIndexOf("\"");
								line = line.substring(0, pos) + logFolder + line.substring(endpos, line.length());
								sb.append(line);
								sb.append(br);
								red = true;
							} else {
								sb.append(line);
								sb.append(br);
							}
						}

						// do something with the line
					} while (line != null);
					xml = sb.toString();
					buffreader.close();
				}
			} catch (Exception ex) {
				// print stack trace.
			} finally {
				// close the file.
				try {
					if (instream != null)
						instream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			FileWriter writer = null;
			try {
				writer = new FileWriter(logBackXmlFile, false);
				writer.write(xml);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (writer != null)
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

			// reset the default context (which may already have been initialized)
			// since we want to reconfigure it
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			lc.reset();

			JoranConfigurator config = new JoranConfigurator();
			config.setContext(lc);

			try {
				config.doConfigure(logBackXmlFile);
				xmlLogbackInitial = true;
			} catch (JoranException e) {
				e.printStackTrace();
				xmlLogbackInitial = false;
			}

		}

		if (!xmlLogbackInitial) {// initial with default
			initialDefaultLogBack(logfile);
		}

		// set LogLevel to all this can change with LogLevel.setLogLevel()
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ALL);

		log.info("logger initial");
	}

	private void initialDefaultLogBack(String logfile) {
		// reset the default context (which may already have been initialized)
		// since we want to reconfigure it
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		lc.reset();

		// add the newly created appenders to the root logger;
		// qualify Logger to disambiguate from org.slf4j.Logger
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		// setup FileAppender
		PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
		encoder1.setContext(lc);
		encoder1.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
		encoder1.start();

		FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
		fileAppender.setContext(lc);
		fileAppender.setFile(logfile);
		fileAppender.setEncoder(encoder1);
		fileAppender.start();
		root.addAppender(fileAppender);

		if (Plattform.used != Plattform.Android) {

			// setup ConsoleAppender
			PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
			encoder2.setContext(lc);
			encoder2.setPattern("%d{HH:mm:ss.SSS}[%-5level] %-36logger{36} - %msg%n");
			encoder2.start();

			ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
			consoleAppender.setContext(lc);
			consoleAppender.setEncoder(encoder2);
			consoleAppender.start();
			root.addAppender(consoleAppender);
		} else {
			// setup LogcatAppender
			PatternLayoutEncoder encoder3 = new PatternLayoutEncoder();
			encoder3.setContext(lc);
			encoder3.setPattern("[%thread] %msg%n");
			encoder3.start();

			LogcatAppender logcatAppender = new LogcatAppender();
			logcatAppender.setContext(lc);
			logcatAppender.setEncoder(encoder3);
			logcatAppender.start();
			root.addAppender(logcatAppender);
		}
	}

	public static void setLogLevel(LogLevel level) {
		LogLevel.setLogLevel(level);
		if (that != null && (new File(that.logBackXmlFile).exists() || LogLevel.isLogLevel(LogLevel.ERROR))) {
			that.Initial();
		}
	}
}

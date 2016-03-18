/* 
 * Copyright (C) 2016 team-cachebox.de
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

/** 
 * TODO document
 * @author Franz  2016
 *
 */
public class Log {
	public static void err(org.slf4j.Logger log, String logText) {
		LogLevel old = LogLevel.getLogLevel();
		LogLevel.setLogLevel(LogLevel.ERROR);
		log.error(logText);
		LogLevel.setLogLevel(old);
	}

	public static void err(org.slf4j.Logger log, String logText, Throwable t) {
		LogLevel old = LogLevel.getLogLevel();
		LogLevel.setLogLevel(LogLevel.ERROR);
		log.error(logText, t);
		LogLevel.setLogLevel(old);
	}

	public static void err(org.slf4j.Logger log, String logText, String logText1, Throwable t) {
		LogLevel old = LogLevel.getLogLevel();
		LogLevel.setLogLevel(LogLevel.ERROR);
		log.error(logText, logText1, t);
		LogLevel.setLogLevel(old);
	}

	public static void debug(org.slf4j.Logger log, String logText) {
		if (LogLevel.isLogLevel(LogLevel.DEBUG))
			log.debug(logText);
	}

	public static void info(org.slf4j.Logger log, String logText) {
		if (LogLevel.isLogLevel(LogLevel.INFO))
			log.info(logText);
	}

	public static void trace(org.slf4j.Logger log, String logText) {
		if (LogLevel.isLogLevel(LogLevel.TRACE))
			log.trace(logText);
	}

	public static void warn(org.slf4j.Logger log, String logText) {
		if (LogLevel.isLogLevel(LogLevel.WARN))
			log.warn(logText);
	}

}

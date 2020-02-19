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
package de.droidcachebox.utils.log;

import org.slf4j.LoggerFactory;

/**
 * TODO document
 *
 * @author Franz  2016
 */
public class Log {

    public static void err(String sKlasse, String logText) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sKlasse).error(logText);
        LogLevel.setLogLevel(old);
    }

    public static void err(String sKlasse, Throwable t) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sKlasse).error("", t);
        LogLevel.setLogLevel(old);
    }

    public static void err(String sKlasse, String logText, Throwable t) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sKlasse).error(logText, t);
        LogLevel.setLogLevel(old);
    }

    public static void err(String sKlasse, String logText, String logText1, Throwable t) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sKlasse).error(logText, logText1, t);
        LogLevel.setLogLevel(old);
    }

    public static void debug(String sKlasse, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.DEBUG))
            LoggerFactory.getLogger(sKlasse).debug(logText);
    }

    public static void info(String sKlasse, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.INFO))
            LoggerFactory.getLogger(sKlasse).info(logText);
    }

    public static void trace(String sKlasse, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.TRACE))
            LoggerFactory.getLogger(sKlasse).trace(logText);
    }

    public static void trace(String sKlasse, Throwable t) {
        if (LogLevel.shouldWriteLog(LogLevel.TRACE))
            LoggerFactory.getLogger(sKlasse).trace("", t);
    }

    public static void warn(String sKlasse, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.WARN))
            LoggerFactory.getLogger(sKlasse).warn(logText);
    }

}

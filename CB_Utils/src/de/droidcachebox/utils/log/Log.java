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

public class Log {

    public static void err(String sClass, String logText) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sClass).error(logText);
        LogLevel.setLogLevel(old);
    }

    public static void err(String sClass, Throwable t) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sClass).error("", t);
        LogLevel.setLogLevel(old);
    }

    public static void err(String sClass, String logText, Throwable t) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sClass).error(logText, t);
        LogLevel.setLogLevel(old);
    }

    public static void err(String sClass, String logText, String logText1, Throwable t) {
        LogLevel old = LogLevel.getLogLevel();
        LogLevel.setLogLevel(LogLevel.ERROR);
        LoggerFactory.getLogger(sClass).error(logText, logText1, t);
        LogLevel.setLogLevel(old);
    }

    public static void debug(String sClass, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.DEBUG))
            LoggerFactory.getLogger(sClass).debug(logText);
    }

    public static void info(String sClass, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.INFO))
            LoggerFactory.getLogger(sClass).info(logText);
    }

    public static void trace(String sClass, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.TRACE))
            LoggerFactory.getLogger(sClass).trace(logText);
    }

    public static void trace(String sClass, Throwable t) {
        if (LogLevel.shouldWriteLog(LogLevel.TRACE))
            LoggerFactory.getLogger(sClass).trace("", t);
    }

    public static void warn(String sClass, String logText) {
        if (LogLevel.shouldWriteLog(LogLevel.WARN))
            LoggerFactory.getLogger(sClass).warn(logText);
    }

}

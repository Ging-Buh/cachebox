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
package de.droidcachebox.database;

import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.utils.log.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LogDAO {
    private static final String log = "LogDAO";

    public void WriteToDatabase(LogEntry logEntry) {
        Parameters args = new Parameters();
        args.put("Id", logEntry.Id);
        args.put("Finder", logEntry.Finder);
        args.put("Type", logEntry.Type.ordinal());
        args.put("Comment", logEntry.Comment);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String stimestamp = iso8601Format.format(logEntry.Timestamp);
        args.put("Timestamp", stimestamp);
        args.put("CacheId", logEntry.CacheId);
        try {
            Database.Data.sql.insertWithConflictReplace("Logs", args);
        } catch (Exception exc) {
            Log.err(log, "Write Log", exc);
        }

    }

    /**
     * Delete all Logs without exist Cache
     */
    public void ClearOrphanedLogs() {
        String SQL = "DELETE  FROM  Logs WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Logs.CacheId = c.Id)";
        Database.Data.sql.execSQL(SQL);
    }

    /**
     * Delete all Logs for Cache
     */
    public void deleteLogs(long cacheId) {
        String SQL = "DELETE  FROM  Logs WHERE Logs.CacheId = " + cacheId;
        Database.Data.sql.execSQL(SQL);
    }

}

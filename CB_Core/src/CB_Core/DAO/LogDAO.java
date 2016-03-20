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
package CB_Core.DAO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.slf4j.LoggerFactory;

import CB_Core.Database;
import CB_Core.Import.ImporterProgress;
import CB_Core.Types.LogEntry;
import CB_Utils.Log.Log;
import de.cb.sqlite.Database_Core.Parameters;

public class LogDAO {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(ImageDAO.class);

	public void WriteToDatabase(LogEntry logEntry) {
		Parameters args = new Parameters();
		args.put("Id", logEntry.Id);
		args.put("Finder", logEntry.Finder);
		args.put("Type", logEntry.Type.ordinal());
		args.put("Comment", logEntry.Comment);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(logEntry.Timestamp);
		args.put("Timestamp", stimestamp);
		args.put("CacheId", logEntry.CacheId);
		try {
			Database.Data.insertWithConflictReplace("Logs", args);
		} catch (Exception exc) {
			Log.err(log, "Write Log", exc);
		}

	}

	// static HashMap<String, String> LogLookup = null;

	public void WriteImports(Iterator<LogEntry> logIterator) {
		WriteImports(logIterator, 0, null);
	}

	public void WriteImports(Iterator<LogEntry> logIterator, int logCount, ImporterProgress ip) {

		if (ip != null)
			ip.setJobMax("WriteLogsToDB", logCount);
		while (logIterator.hasNext()) {
			LogEntry log = logIterator.next();
			if (ip != null)
				ip.ProgressInkrement("WriteLogsToDB", String.valueOf(log.CacheId), false);
			try {
				WriteToDatabase(log);
			} catch (Exception e) {

				// Statt hier den Fehler abzufangen, sollte die LogTabelle
				// Indexiert werden
				// und nur die noch nicht vorhandenen Logs geschrieben werden.

				e.printStackTrace();
			}

		}

	}

	/**
	 * Delete all Logs without exist Cache
	 */
	public void ClearOrphanedLogs() {
		String SQL = "DELETE  FROM  Logs WHERE  NOT EXISTS (SELECT * FROM Caches c WHERE  Logs.CacheId = c.Id)";
		Database.Data.execSQL(SQL);
	}

}

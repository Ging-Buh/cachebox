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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.LoggerFactory;

import CB_Core.Database;
import CB_Core.Api.PocketQuery.PQ;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

public class PocketqueryDAO {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(PocketqueryDAO.class);

	public int writeToDatabase(PQ pq) {
		Parameters args = new Parameters();
		args.put("PQName", pq.Name);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(pq.DateLastGenerated);
		args.put("CreationTimeOfPQ", stimestamp);

		try {
			Database.Data.insertWithConflictReplace("PocketQueries", args);
		} catch (Exception exc) {
			log.error("Write Pocketquery to DB", pq.Name, exc);
			return -1;
		}

		return 0;
	}

	/**
	 * liefert das Datum wann die PQ mit dem gegebenen Namen das letzt mal erzeugt wurde Wenn eine PQ noch gar nicht in der Liste ist dann
	 * wird null zurück gegeben
	 * 
	 * @param pqName
	 * @return
	 */
	public Date getLastGeneratedDate(String pqName) {
		CoreCursor reader = Database.Data.rawQuery("select max(CreationTimeOfPQ) from PocketQueries where PQName=@PQName", new String[] { pqName });
		try {
			if (reader.getCount() > 0) {
				reader.moveToFirst();
				while (!reader.isAfterLast()) {
					String sDate = reader.getString(0);
					if (sDate == null) {
						// nicht gefunden!
						return null;
					}
					DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					try {
						return iso8601Format.parse(sDate);
					} catch (ParseException e) {
						// PQ ist in der DB, aber das Datum konnte nicht geparst werden
						e.printStackTrace();
						return new Date(0);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			reader.close();
		}
		return null;
	}
}

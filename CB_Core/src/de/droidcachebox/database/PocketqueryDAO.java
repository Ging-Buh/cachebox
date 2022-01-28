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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.droidcachebox.core.GroundspeakAPI.PQ;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.utils.log.Log;

public class PocketqueryDAO {
    private static final String sClass = "PocketqueryDAO";

    public int writeToDatabase(PQ pq) {
        Parameters args = new Parameters();
        args.put("PQName", pq.name);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        args.put("CreationTimeOfPQ", iso8601Format.format(pq.lastGenerated));
        try {
            CBDB.getInstance().insertWithConflictReplace("PocketQueries", args);
        } catch (Exception exc) {
            Log.err(sClass, "Write Pocketquery to DB", pq.name, exc);
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
        CoreCursor reader = CBDB.getInstance().rawQuery("select max(CreationTimeOfPQ) from PocketQueries where PQName=@PQName", new String[]{pqName});
        if (reader != null) {
            try {
                if (reader.getCount() > 0) {
                    reader.moveToFirst();
                    String sDate = reader.getString(0);
                    if (sDate == null) {
                        return null;
                    }
                    DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    try {
                        return iso8601Format.parse(sDate);
                    } catch (ParseException e) {
                        return new Date(0);
                    }
                }
            } catch (Exception ex) {
                return new Date(0);
            } finally {
                reader.close();
            }
        }
        return null;
    }
}

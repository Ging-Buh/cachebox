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

import CB_Core.Database;
import CB_Core.Types.Trackable;
import CB_Utils.Log.Log;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TrackableDAO {
    private static final String log = "TrackableDAO";

    private Trackable ReadFromCursor(CoreCursor reader) {
        try {
            Trackable trackable = new Trackable(reader);

            return trackable;
        } catch (Exception exc) {
            Log.err(log, "Read Trackable", "", exc);
            return null;
        }
    }

    public void WriteToDatabase(Trackable trackable) {
        try {
            Log.info(log, "Write Trackable insert");
            Database.FieldNotes.sql.insert("Trackable", createArgs(trackable));
            Log.info(log, "Write Trackable insert done");
        } catch (Exception exc) {
            Log.err(log, "Write Trackable error", exc);
        }
    }

    public void UpdateDatabase(Trackable trackable) {
        try {
            Log.info(log, "Write Trackable createArgs");
            Parameters args = createArgs(trackable);
            Log.info(log, "Write Trackable update");
            Database.FieldNotes.sql.update("Trackable", args, "GcCode='" + trackable.getTBCode() + "'", null);
        } catch (Exception exc) {
            Log.err(log, "Update Trackable error", exc);
        }

    }

    private Parameters createArgs(Trackable trackable) {
        String stimestampCreated = "";
        String stimestampLastVisit = "";

        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            stimestampCreated = iso8601Format.format(trackable.getDateCreated());
        } catch (Exception e) {
            Log.err(log, "stimestampCreated", e);
        }

        try {
            String lastVisit = trackable.getLastVisit();
            if (lastVisit.length() > 0)
                stimestampLastVisit = iso8601Format.format(lastVisit);
            else
                stimestampLastVisit = "";
        } catch (Exception e) {
            Log.err(log, "stimestampLastVisit", e);
        }

    /*
            cid      name                 type               notnull      dflt_value      pk
            -------  -------------------  -----------------  -----------  --------------  ---
            0        Id                   integer            1                            1
            1        Archived             bit                0                            0
            2        GcCode               nvarchar (15)      0                            0
            3        CacheId              bigint             0                            0
            4        CurrentGoal          ntext              0                            0
            5        CurrentOwnerName     nvarchar (255)     0                            0
            6        DateCreated          datetime           0                            0
            7        Description          ntext              0                            0
            8        IconUrl              nvarchar (255)     0                            0
            9        ImageUrl             nvarchar (255)     0                            0
            10       name                 nvarchar (255)     0                            0
            11       OwnerName            nvarchar (255)     0                            0
            12       Url                  nvarchar (255)     0                            0
            13       TypeName             ntext              0                            0
            14       LastVisit            datetime           0                            0
            15       Home                 ntext              0                            0
            16       TravelDistance       integer            0            0               0
    */

        Log.debug(log, "new Parameters()");
        Parameters args = new Parameters();
        try {
            args.put("Archived", trackable.getArchived() ? 1 : 0);
            putArgs(args, "GcCode", trackable.getTBCode());
            putArgs(args, "CurrentGoal", trackable.getCurrentGoal());
            putArgs(args, "CurrentOwnerName", trackable.getCurrentOwner());
            putArgs(args, "DateCreated", stimestampCreated);
            putArgs(args, "Description", trackable.getDescription());
            putArgs(args, "IconUrl", trackable.getIconUrl());
            putArgs(args, "ImageUrl", trackable.getImageUrl());
            putArgs(args, "name", trackable.getName());
            putArgs(args, "OwnerName", trackable.getOwner());
            putArgs(args, "Url", trackable.getUrl());
            putArgs(args, "TypeName", trackable.getTypeName());
            putArgs(args, "LastVisit", stimestampLastVisit);
            putArgs(args, "Home", trackable.getHome());
            putArgs(args, "TravelDistance", trackable.getTravelDistance());
            putArgs(args, "CacheId", trackable.getCurrentGeocacheCode());
        } catch (Exception e) {
            Log.err(log, "args", e);
        }
        return args;
    }

    private void putArgs(Parameters args, String Name, Object value) {
        Log.debug(log, Name + "=" + value);
        args.put(Name, value);
    }

    public Trackable getFromDbByGcCode(String GcCode) {
        String where = "GcCode = \"" + GcCode + "\"";
        String query = "select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url,TypeName, Home,TravelDistance   from Trackable WHERE " + where;
        CoreCursor reader = Database.FieldNotes.sql.rawQuery(query, null);

        try {
            if (reader != null && reader.getCount() > 0) {
                reader.moveToFirst();
                Trackable ret = ReadFromCursor(reader);

                reader.close();
                return ret;
            } else {
                if (reader != null)
                    reader.close();
                return null;
            }
        } catch (Exception e) {
            if (reader != null)
                reader.close();
            e.printStackTrace();
            return null;
        }

    }

}

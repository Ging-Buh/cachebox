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
package de.droidcachebox.dataclasses;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class Trackable implements Comparable<Trackable> {
    private static final String sClass = "Trackable";
    private final SimpleDateFormat postFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
    private boolean archived;
    private String tbCode;
    private String currentGoal;
    private String currentOwnerName;
    private Date dateCreated;
    private String description;
    private String iconUrl;
    private String imageUrl; // not in API 1.0
    private String name;
    private String ownerName;
    private String typeName;
    private String trackingCode;
    private int id;
    private long cacheId;
    private String url;
    private float travelDistance;
    private Date lastVisit;
    private String currentGeoCacheCode;

    /**
     * <img src="doc-files/1.png"/>
     */
    public Trackable() {
        imageUrl = "";
        id = -1;
        url = "";
        lastVisit = null;
        currentGeoCacheCode = "";
    }

    /**
     * Der Constructor, der ein Trackable über eine DB Abfrage erstellt! <img src="doc-files/1.png"/>
     * ,,,,,,,,,,,,,,Home,
     *
     * @param reader cursor from db
     */
    public Trackable(CoreCursor reader) {
        try {
            id = reader.getInt("Id");
            archived = reader.getInt("Archived") != 0;
            tbCode = reader.getString("GcCode").trim();
            cacheId = reader.getLong("CacheId");
            currentGoal = reader.getString("CurrentGoal").trim();
            currentOwnerName = reader.getString("CurrentOwnerName").trim();
            String sDate = reader.getString("DateCreated");
            dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(sDate);
            description = reader.getString("Description").trim();
            iconUrl = reader.getString("IconUrl").trim();
            imageUrl = reader.getString("ImageUrl").trim();
            name = reader.getString("name").trim();
            ownerName = reader.getString("OwnerName").trim();
            url = reader.getString("Url").trim();
            typeName = reader.getString("TypeName").trim();
            travelDistance = reader.getInt("TravelDistance");
            sDate = reader.getString("DateCreated");
            lastVisit = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(sDate);
            currentGeoCacheCode = "";
        } catch (Exception ex) {
            Log.err(sClass, "Read Trackable from DB", ex);
        }
    }

    public void writeToDatabase() {
        try {
            DraftsDatabase.getInstance().insert("Trackable", createArgs());
        } catch (Exception exc) {
            Log.err(sClass, "Write Trackable error", exc);
        }
    }

    private Database_Core.Parameters createArgs() {
        String sTimestampCreated = "";
        String sTimestampLastVisit = "";

        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            sTimestampCreated = iso8601Format.format(getDateCreated());
        } catch (Exception e) {
            Log.err(sClass, "sTimestampCreated", e);
        }

        try {
            String lastVisit = formatLastVisit();
            if (lastVisit.length() > 0)
                sTimestampLastVisit = iso8601Format.format(lastVisit);
            else
                sTimestampLastVisit = "";
        } catch (Exception e) {
            Log.err(sClass, "sTimestampLastVisit", e);
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

        Log.debug(sClass, "new Parameters()");
        Database_Core.Parameters args = new Database_Core.Parameters();
        try {
            args.put("Archived", archived ? 1 : 0);
            args.put("GcCode", tbCode);
            args.put("CurrentGoal", currentGoal);
            args.put("CurrentOwnerName", currentOwnerName);
            args.put("DateCreated", sTimestampCreated);
            args.put("Description", description);
            args.put("IconUrl", iconUrl);
            args.put("ImageUrl", imageUrl);
            args.put("name", name);
            args.put("OwnerName", ownerName);
            args.put("Url", url);
            args.put("TypeName", typeName);
            args.put("LastVisit", sTimestampLastVisit);
            args.put("Home", "");
            args.put("TravelDistance",  UnitFormatter.distanceString(travelDistance));
            args.put("CacheId", currentGeoCacheCode);
        } catch (Exception e) {
            Log.err(sClass, "args", e);
        }
        return args;
    }

    /*
     * Generiert eine Eindeutige ID aus den ASCII values des GcCodes. <br>
     * Damit lässt sich dieser TB schneller in der DB finden.
     *
     * @return long
    public static long GenerateTBId(String GcCode) {
        long result = 0;
        char[] dummy = GcCode.toCharArray();
        byte[] byteDummy = new byte[8];
        for (int i = 0; i < 8; i++) {
            if (i < GcCode.length())
                byteDummy[i] = (byte) dummy[i];
            else
                byteDummy[i] = 0;
        }
        for (int i = 7; i >= 0; i--) {
            result *= 256;
            result += byteDummy[i];
        }
        return result;
    }
     */

    public String getDateCreatedString() {
        if (dateCreated == null)
            return "";
        return postFormatter.format(dateCreated);
    }

    public String getCurrentGeoCacheCode() {
        return currentGeoCacheCode;
    }

    public void setCurrentGeoCacheCode(String currentGeoCacheCode) {
        this.currentGeoCacheCode = currentGeoCacheCode;
    }

    public String getHome() {
        return "";
    }

    public String formatLastVisit() {
        if (lastVisit == null)
            return "";
        return postFormatter.format(lastVisit);
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getOwner() {
        return ownerName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getId() {
        return id;
    }

    public boolean getArchived() {
        return archived;
    }

    public String getTbCode() {
        return tbCode;
    }

    public void setTbCode(String tbCode) {
        this.tbCode = tbCode;
    }

    public long CacheId() {
        return cacheId;
    }

    public String getCurrentGoal() {
        return currentGoal;
    }

    public void setCurrentGoal(String currentGoal) {
        this.currentGoal = currentGoal;
    }

    public String getCurrentOwner() {
        return currentOwnerName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public String getTrackingCode() {
        if (trackingCode == null)
            return "";
        else
            return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    @Override
    public int compareTo(Trackable trackable) {
        return name.compareToIgnoreCase(trackable.name);
    }

    /**
     * Returns True if a LogType possible <br>
     * <br>
     * Possible LogType for TB in Cache: <br>
     * 4 - Post Note <br>
     * 13 - Retrieve It from a Cache <br>
     * 14 - Place in a cache <br>
     * 16 - Mark as missing <br>
     * 48 - Discover <br>
     * <br>
     * Possible LogType for TB at other Person: <br>
     * 4 - Post Note <br>
     * 16 - Mark as missing <br>
     * 19 - Grab <br>
     * 48 - Discover <br>
     * 69 - Move to collection <br>
     * 70 - Move to inventory <br>
     * <br>
     * Possible LogType for TB at my inventory: <br>
     * 4 - Post Note <br>
     * 14 - Place in a cache <br>
     * 16 - Mark as missing<br>
     * 69 - Move to collection <br>
     * 70 - Move to inventory <br>
     * 75 - Visit<br>
     *
     * @param type     ?
     * @param userName Config.settings.GcLogin.getValue()
     * @return ?
     */
    public boolean isLogTypePossible(LogType type, String userName) {
        int ID = type.gsLogTypeId;

        if (ID == 4)
            return true; // Note

        if (currentGeoCacheCode != null && currentGeoCacheCode.length() > 0 && !currentGeoCacheCode.equalsIgnoreCase("null")) {
            // TB in Cache
            if (ID == 16)
                return true;

            // the next LogType only possible if User has entered the TrackingCode
            if (!(trackingCode != null && trackingCode.length() > 0))
                return false;
            // ist es Sinnvoll einen TB aus einem Cache in einen Cache zu packen?? ID 14 ist Laut GS erlaubt!
            return ID == 13 || /* ID == 14 || */ID == 48;
        }

        if (currentOwnerName.equalsIgnoreCase(userName)) {
            // TB in Inventory
            return ID == 14 || ID == 16 || ID == 69 || ID == 70 || ID == 75;
        }

        // TB at other Person

        // User entered TB-Code and not TrackingCode: he can not Grab or Discover
        if (trackingCode != null && trackingCode.length() > 0) {
            if (ID == 19 || ID == 48)
                return true;
        }
        return ID == 16 || ID == 69 || ID == 70;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setCurrentOwnerName(String currentOwnerName) {
        this.currentOwnerName = currentOwnerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}

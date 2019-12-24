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

import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Trackable implements Comparable<Trackable> {
    private static final String log = "Trackable";
    private final SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
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
     * DAO Constructor <br>
     * Der Constructor, der ein Trackable über eine DB Abfrage erstellt! <img src="doc-files/1.png"/>
     *
     * @param reader ?
     */
    public Trackable(CoreCursor reader) {
        try {
            id = reader.getInt(0);
            archived = reader.getInt(1) != 0;
            tbCode = reader.getString(2).trim();
            cacheId = reader.getLong(3);
            currentGoal = reader.getString(4).trim();
            currentOwnerName = reader.getString(5).trim();
            String sDate = reader.getString(6);
            dateCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(sDate);
            description = reader.getString(7).trim();
            iconUrl = reader.getString(8).trim();
            imageUrl = reader.getString(9).trim();
            name = reader.getString(10).trim();
            ownerName = reader.getString(11).trim();
            url = reader.getString(12).trim();
            typeName = reader.getString(13).trim();
            travelDistance = 0;
            lastVisit = null;
            currentGeoCacheCode = "";
        } catch (Exception ex) {
            Log.err(log, "Read Trackable from DB", ex);
        }
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

    public String formatTravelDistance() {
        return UnitFormatter.distanceString(travelDistance);
    }

    public String getBirth() {
        if (dateCreated == null)
            return "";
        return postFormater.format(dateCreated);
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
        return postFormater.format(lastVisit);
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
     * Possible GeoCacheLogType for TB in Cache: <br>
     * 4 - Post Note <br>
     * 13 - Retrieve It from a Cache <br>
     * 14 - Place in a cache <br>
     * 16 - Mark as missing <br>
     * 48 - Discover <br>
     * <br>
     * Possible GeoCacheLogType for TB at other Person: <br>
     * 4 - Post Note <br>
     * 16 - Mark as missing <br>
     * 19 - Grab <br>
     * 48 - Discover <br>
     * 69 - Move to collection <br>
     * 70 - Move to inventory <br>
     * <br>
     * Possible GeoCacheLogType for TB at my inventory: <br>
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
    public boolean isLogTypePossible(GeoCacheLogType type, String userName) {
        int ID = type.getGcLogTypeId();

        if (ID == 4)
            return true; // Note

        if (currentGeoCacheCode != null && currentGeoCacheCode.length() > 0 && !currentGeoCacheCode.equalsIgnoreCase("null")) {
            // TB in Cache
            if (ID == 16)
                return true;

            // the next GeoCacheLogType only possible if User has entered the TrackingCode
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

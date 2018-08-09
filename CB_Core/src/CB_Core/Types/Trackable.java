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
package CB_Core.Types;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;

import CB_Core.LogTypes;
import CB_Utils.Log.Log;
import CB_Utils.Util.UnitFormatter;
import de.cb.sqlite.CoreCursor;

public class Trackable implements Comparable<Trackable> {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Trackable.class);
    private int Id = -1;
    private boolean Archived;
    private String GcCode = "";
    private long CacheId;
    private String CurrentGoal = "";
    private String CurrentGeocacheCode = "";
    private String CurrentOwnerName = "";
    private Date DateCreated;
    private String Description;
    private String IconUrl = "";
    private String ImageUrl = "";
    private String Name = "";
    private String OwnerName = "";
    private String Url = "";
    private String TypeName = "";
    private String TrackingCode;

    // TODO must load info (the GS_API gives no info about this)
    private Date lastVisit;
    private String Home = "";
    private int TravelDistance;

    /**
     * <img src="doc-files/1.png"/>
     */
    public Trackable() {
    }

    /**
     * <img src="doc-files/1.png"/>
     *
     * @param Name
     * @param IconUrl
     * @param desc
     */
    public Trackable(String Name, String IconUrl, String desc) {
        this.Name = Name;
        this.IconUrl = IconUrl;
        this.Description = desc;
    }

    /**
     * DAO Constructor <br>
     * Der Constructor, der ein Trackable über eine DB Abfrage erstellt! <img src="doc-files/1.png"/>
     *
     * @param reader
     */
    public Trackable(CoreCursor reader) {
        try {
            Id = reader.getInt(0);
            Archived = reader.getInt(1) != 0;
            GcCode = reader.getString(2).trim();
            try {
                CacheId = reader.getLong(3);
            } catch (Exception e1) {

                e1.printStackTrace();
            }
            try {
                CurrentGoal = reader.getString(4).trim();
            } catch (Exception e1) {

                e1.printStackTrace();
            }
            try {
                CurrentOwnerName = reader.getString(5).trim();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String sDate = reader.getString(6);
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                DateCreated = iso8601Format.parse(sDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                Description = reader.getString(7).trim();
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                IconUrl = reader.getString(8).trim();
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                ImageUrl = reader.getString(9).trim();
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                Name = reader.getString(10).trim();
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                OwnerName = reader.getString(11).trim();
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                Url = reader.getString(12).trim();
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {
                TypeName = reader.getString(13).trim();
            } catch (Exception e1) {

                e1.printStackTrace();
            }

        } catch (Exception e) {

        }

    }

    /**
     * <img src="doc-files/1.png"/>
     */
    public Trackable(JSONObject JObj) {

        Archived = JObj.optBoolean("Archived");
        GcCode = JObj.optString("Code");
        CurrentGeocacheCode = JObj.optString("CurrentGeocacheCode");
        CurrentGoal = JObj.optString("CurrentGoal");
        JSONObject jOwner;
        try {
            jOwner = JObj.getJSONObject("CurrentOwner");
            CurrentOwnerName = jOwner.optString("UserName");
        } catch (JSONException e) {
            Log.err(log, "CurrentOwner", e);
        }
        try {
            String dateCreated = JObj.optString("DateCreated");
            int date1 = dateCreated.indexOf("/Date(");
            int date2 = dateCreated.indexOf("-");
            String date = (String) dateCreated.subSequence(date1 + 6, date2);
            DateCreated = new Date(Long.valueOf(date));
        } catch (Exception exc) {
            Log.err(log, "DateCreated", exc);
        }
        Description = JObj.optString("Description");
        IconUrl = JObj.optString("IconUrl");
        JSONArray jArray;
        try {
            jArray = JObj.getJSONArray("Images");
            if (jArray.length() > 0) {
                ImageUrl = jArray.getJSONObject(0).optString("Url");
            }
        } catch (JSONException e) {
            Log.err(log, "Images", e);
        }

        Name = JObj.optString("Name");
        try {
            jOwner = JObj.getJSONObject("OriginalOwner");
            OwnerName = jOwner.optString("UserName");
        } catch (JSONException e) {
            Log.err(log, "OriginalOwner", e);
        }
        TypeName = JObj.optString("TBTypeName");
    }

    final SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yyyy");

    public String getTravelDistance() {
        return UnitFormatter.DistanceString(TravelDistance);
    }

    public String getBirth() {
        if (DateCreated == null)
            return "";
        return postFormater.format(DateCreated);
    }

    public String getCurrentGeocacheCode() {
        return CurrentGeocacheCode;
    }

    public String getHome() {
        return Home;
    }

    public String getLastVisit() {
        if (lastVisit == null)
            return "";
        return postFormater.format(lastVisit);
    }

    public String getTypeName() {
        return TypeName;
    }

    public String getOwner() {
        return OwnerName;
    }

    public String getIconUrl() {
        return IconUrl;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public long getId() {
        return Id;
    }

    public boolean getArchived() {
        return Archived;
    }

    public String getGcCode() {
        return GcCode;
    }

    public long CacheId() {
        return CacheId;
    }

    public String getCurrentGoal() {
        // return CurrentGoal;
        return Jsoup.parse(CurrentGoal).text();
    }

    public String getCurrentOwner() {
        return CurrentOwnerName;
    }

    public Date getDateCreated() {
        return DateCreated;
    }

    public String getDescription() {
        // return Description;
        return Jsoup.parse(Description).text();
    }

    public String getName() {
        return Name;
    }

    public String getUrl() {
        return Url;
    }

    public String getTrackingNumber() {
        return this.TrackingCode;
    }

    /*
     * Setter
     */

    public void setTrackingCode(String trackingCode) {
        this.TrackingCode = trackingCode;
    }

    /*
     * Methods
     */

    /**
     * Generiert eine Eindeutige ID aus den ASCII values des GcCodes. <br>
     * Damit lässt sich dieser TB schneller in der DB finden.
     *
     * @return long
     */
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

    /*
     * Overrides
     */

    @Override
    public int compareTo(Trackable T2) {
        return Name.compareToIgnoreCase(T2.Name);
    }

    /**
     * Returns True if a LogType possible <br>
     * <br>
     * Possible LogTypes for TB in Cache: <br>
     * 4 - Post Note <br>
     * 13 - Retrieve It from a Cache <br>
     * 14 - Place in a cache <br>
     * 16 - Mark as missing <br>
     * 48 - Discover <br>
     * <br>
     * Possible LogTypes for TB at other Person: <br>
     * 4 - Post Note <br>
     * 16 - Mark as missing <br>
     * 19 - Grab <br>
     * 48 - Discover <br>
     * 69 - Move to collection <br>
     * 70 - Move to inventory <br>
     * <br>
     * Possible LogTypes for TB at my inventory: <br>
     * 4 - Post Note <br>
     * 14 - Place in a cache <br>
     * 16 - Mark as missing<br>
     * 69 - Move to collection <br>
     * 70 - Move to inventory <br>
     * 75 - Visit<br>
     *
     * @param type
     * @param userName Config.settings.GcLogin.getValue()
     * @return
     */
    public boolean isLogTypePosible(LogTypes type, String userName) {
        int ID = type.getGcLogTypeId();

        if (ID == 4)
            return true; // Note

        if (CurrentGeocacheCode != null && CurrentGeocacheCode.length() > 0 && !CurrentGeocacheCode.equalsIgnoreCase("null")) {
            // TB in Cache
            if (ID == 16)
                return true;

            // the next LogTypes only possible if User has entered the Trackingnumber
            if (!(TrackingCode != null && TrackingCode.length() > 0))
                return false;
            if (ID == 13 || /* ID == 14 || */ID == 48)
                return true; // TODO ist es Sinnvoll einen TB aus einem Cache in einen Cache zu
            // Packen?? ID 14 ist Laut GS erlaubt!
            return false;
        }

        if (CurrentOwnerName.equalsIgnoreCase(userName)) {
            // TB in Inventory
            if (ID == 14 || ID == 16 || ID == 69 || ID == 70 || ID == 75)
                return true;
            return false;
        }

        // TB at other Person

        // User entered TB-Code and not TrackingCode: he can not Grab or Discover
        if (TrackingCode != null && TrackingCode.length() > 0) {
            if (ID == 19 || ID == 48)
                return true;
        }
        if (ID == 16 || ID == 69 || ID == 70)
            return true;

        return false;
    }
}

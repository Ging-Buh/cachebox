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
package de.droidcachebox.core;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.droidcachebox.settings.CB_Core_Settings;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;

public class GCVote {
    private static final String sKlasse = "GCVote";

    public static ArrayList<RatingData> getRating(String user, String password, ArrayList<String> wayPoints) {
        ArrayList<RatingData> result = new ArrayList<>();

        StringBuilder data = new StringBuilder("userName=" + user + "&password=" + password + "&waypoints=");
        for (int i = 0; i < wayPoints.size(); i++) {
            data.append(wayPoints.get(i));
            if (i < (wayPoints.size() - 1))
                data.append(",");
        }

        try {
            InputStream is = Webb.create()
                    .get("http://gcvote.com/getVotes.php?" + data)
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asStream()
                    .getBody();

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(is);
            is.close();
            NodeList nodelist = doc.getElementsByTagName("vote");

            for (int i = 0; i < nodelist.getLength(); i++) {
                Node node = nodelist.item(i);
                RatingData ratingData = new RatingData();
                ratingData.rating = Float.valueOf(node.getAttributes().getNamedItem("voteAvg").getNodeValue());
                String userVote = node.getAttributes().getNamedItem("voteUser").getNodeValue();
                ratingData.vote = (userVote.length() == 0) ? 0 : Float.parseFloat(userVote);
                ratingData.wayPoint = node.getAttributes().getNamedItem("waypoint").getNodeValue();
                result.add(ratingData);
            }

        } catch (Exception e) {
            Log.err(sKlasse, "getRating", e);
            return null;
        }
        return result;

    }

    public static boolean sendVote(String User, String password, int vote, String url, String waypoint) {
        url = url.replace("http:", "https:"); // automatic redirect doesn't work from http to https
        int pos = url.indexOf("guid=");
        String guid = "";
        if (pos > -1) {
            guid = url.substring(pos + 5).trim();
        } else {
            // fetch guid from gc : works without login
            try {
                String page = Webb.create()
                        .get(url)
                        .ensureSuccess()
                        .asString()
                        .getBody();
                String toSearch = "cache_details.aspx?guid=";
                pos = page.indexOf(toSearch);
                if (pos > -1) {
                    int start = pos + toSearch.length();
                    int stop = page.indexOf("\"", start);
                    guid = page.substring(start, stop);
                }
            } catch (Exception e) {
                Log.err(sKlasse, "Send GCVotes: Can't get GUID for " + waypoint, e);
            }
        }
        if (guid.length() == 0) return false;

        String data = "userName=" + User + "&password=" + password + "&voteUser=" + vote / 100.0 + "&cacheId=" + guid + "&waypoint=" + waypoint;

        try {
            String responseString = Webb.create()
                    .get("http://gcvote.com/setVote.php?" + data)
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asString()
                    .getBody();
            return responseString.equals("OK");

        } catch (Exception ex) {
            return false;
        }

    }

}

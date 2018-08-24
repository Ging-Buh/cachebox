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
package CB_Core.GCVote;


import CB_Core.CB_Core_Settings;
import CB_Utils.Log.Log;
import CB_Utils.http.Webb;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;

import static CB_Utils.http.Webb.APP_FORM;
import static CB_Utils.http.Webb.HDR_CONTENT_TYPE;

public class GCVote {
    private static final String log = "GCVote";

    public static ArrayList<RatingData> GetRating(String User, String password, ArrayList<String> Waypoints) {
        ArrayList<RatingData> result = new ArrayList<RatingData>();

        String data = "userName=" + User + "&password=" + password + "&waypoints=";
        for (int i = 0; i < Waypoints.size(); i++) {
            data += Waypoints.get(i);
            if (i < (Waypoints.size() - 1))
                data += ",";
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

            for (Integer i = 0; i < nodelist.getLength(); i++) {
                Node node = nodelist.item(i);

                RatingData ratingData = new RatingData();
                ratingData.Rating = Float.valueOf(node.getAttributes().getNamedItem("voteAvg").getNodeValue());
                String userVote = node.getAttributes().getNamedItem("voteUser").getNodeValue();
                ratingData.Vote = (userVote == "") ? 0 : Float.valueOf(userVote);
                ratingData.Waypoint = node.getAttributes().getNamedItem("waypoint").getNodeValue();
                result.add(ratingData);

            }

        } catch (Exception e) {
            Log.err(log, "GetRating", e);
            return null;
        }
        return result;

    }

    // todo not tested cause no ui for changing vote
    public static Boolean SendVotes(String User, String password, int vote, String url, String waypoint) {
        String guid = url.substring(url.indexOf("guid=") + 5).trim();

        String data = "userName=" + User + "&password=" + password + "&voteUser=" + String.valueOf(vote / 100.0) + "&cacheId=" + guid + "&waypoint=" + waypoint;

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

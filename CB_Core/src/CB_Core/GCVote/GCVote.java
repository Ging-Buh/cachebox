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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import CB_Core.CB_Core_Settings;

public class GCVote {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(GCVote.class);

	public static RatingData GetRating(String User, String password, String Waypoint) {
		ArrayList<String> waypoint = new ArrayList<String>();
		waypoint.add(Waypoint);
		ArrayList<RatingData> result = GetRating(User, password, waypoint);

		if (result == null || result.size() == 0) {
			return new RatingData();
		} else {
			return result.get(0);
		}

	}

	public static ArrayList<RatingData> GetRating(String User, String password, ArrayList<String> Waypoints) {
		ArrayList<RatingData> result = new ArrayList<RatingData>();

		String data = "userName=" + User + "&password=" + password + "&waypoints=";
		for (int i = 0; i < Waypoints.size(); i++) {
			data += Waypoints.get(i);
			if (i < (Waypoints.size() - 1))
				data += ",";
		}

		try {
			HttpPost httppost = new HttpPost("http://gcvote.de/getVotes.php");

			httppost.setEntity(new ByteArrayEntity(data.getBytes("UTF8")));

			// log.info("GCVOTE-Post" + data);

			// Execute HTTP Post Request
			String responseString = Execute(httppost);

			// log.info("GCVOTE-Response" + responseString);

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseString));

			Document doc = db.parse(is);

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
			String Ex = "";
			if (e != null) {
				if (e != null && e.getMessage() != null)
					Ex = "Ex = [" + e.getMessage() + "]";
				else if (e != null && e.getLocalizedMessage() != null)
					Ex = "Ex = [" + e.getLocalizedMessage() + "]";
				else
					Ex = "Ex = [" + e.toString() + "]";
			}
			log.info("GcVote-Error" + Ex);
			return null;
		}
		return result;

	}

	private static String Execute(HttpRequestBase httprequest) throws IOException, ClientProtocolException {
		httprequest.setHeader("Content-type", "application/x-www-form-urlencoded");
		// httprequest.setHeader("UserAgent", "cachebox");

		int conectionTimeout = CB_Core_Settings.conection_timeout.getValue();
		int socketTimeout = CB_Core_Settings.socket_timeout.getValue();

		// Execute HTTP Post Request
		String result = "";

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.

		HttpConnectionParams.setConnectionTimeout(httpParameters, conectionTimeout);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.

		HttpConnectionParams.setSoTimeout(httpParameters, socketTimeout);

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

		HttpResponse response = httpClient.execute(httprequest);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null) {
			result += line + "\n";
		}
		return result;

	}

	public static Boolean SendVotes(String User, String password, int vote, String url, String waypoint) {
		String guid = url.substring(url.indexOf("guid=") + 5).trim();

		String data = "userName=" + User + "&password=" + password + "&voteUser=" + String.valueOf(vote / 100.0) + "&cacheId=" + guid + "&waypoint=" + waypoint;

		try {
			HttpPost httppost = new HttpPost("http://dosensuche.de/GCVote/setVote.php");

			httppost.setEntity(new ByteArrayEntity(data.getBytes("UTF8")));

			// Execute HTTP Post Request
			String responseString = Execute(httppost);

			return responseString.equals("OK\n");

		} catch (Exception ex) {
			return false;
		}

	}

}

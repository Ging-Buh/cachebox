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
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class GCVote
{

	public static RatingData GetRating(String User, String password, String Waypoint)
	{
		ArrayList<String> waypoint = new ArrayList<String>();
		waypoint.add(Waypoint);
		ArrayList<RatingData> result = GetRating(User, password, waypoint);

		if (result == null || result.size() == 0)
		{
			return new RatingData();
		}
		else
		{
			return result.get(0);
		}

	}

	public static ArrayList<RatingData> GetRating(String User, String password, ArrayList<String> Waypoints)
	{
		ArrayList<RatingData> result = new ArrayList<RatingData>();

		String data = "userName=" + User + "&password=" + password + "&waypoints=";
		for (int i = 0; i < Waypoints.size(); i++)
		{
			data += Waypoints.get(i);
			if (i < (Waypoints.size() - 1)) data += ",";
		}

		try
		{
			HttpPost httppost = new HttpPost("http://gcvote.de/getVotes.php");

			httppost.setEntity(new ByteArrayEntity(data.getBytes("UTF8")));

			// Execute HTTP Post Request
			String responseString = Execute(httppost);

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(responseString));

			Document doc = db.parse(is);

			NodeList nodelist = doc.getElementsByTagName("vote");

			for (Integer i = 0; i < nodelist.getLength(); i++)
			{
				Node node = nodelist.item(i);

				RatingData ratingData = new RatingData();
				ratingData.Rating = Float.valueOf(node.getAttributes().getNamedItem("voteAvg").getNodeValue());
				String userVote = node.getAttributes().getNamedItem("voteUser").getNodeValue();
				ratingData.Vote = (userVote == "") ? 0 : Float.valueOf(userVote);
				ratingData.Waypoint = node.getAttributes().getNamedItem("waypoint").getNodeValue();
				result.add(ratingData);

			}

		}
		catch (Exception ex)
		{
			return null;
		}
		return result;

	}

	private static String Execute(HttpRequestBase httprequest) throws IOException, ClientProtocolException
	{
		httprequest.setHeader("Content-type", "application/x-www-form-urlencoded");
		httprequest.setHeader("UserAgent", "cachebox");

		// Execute HTTP Post Request
		String result = "";
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(httprequest);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null)
		{
			result += line + "\n";
		}
		return result;
	}

	public static Boolean SendVotes(String User, String password, int vote, String url, String waypoint)
	{
		String guid = url.substring(url.indexOf("guid=") + 5).trim();

		String data = "userName=" + User + "&password=" + password + "&voteUser=" + String.valueOf(vote / 100.0) + "&cacheId=" + guid
				+ "&waypoint=" + waypoint;

		try
		{
			HttpPost httppost = new HttpPost("http://dosensuche.de/GCVote/setVote.php");

			httppost.setEntity(new ByteArrayEntity(data.getBytes("UTF8")));

			// Execute HTTP Post Request
			String responseString = Execute(httppost);

			return responseString.equals("OK\n");

		}
		catch (Exception ex)
		{
			return false;
		}

	}

}

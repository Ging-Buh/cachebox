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
package CB_Utils.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import CB_Utils.Plattform;
import CB_Utils.Interfaces.ICancel;

/**
 * @author Longri
 */
public class HttpUtils
{
	public static int conectionTimeout = 10000;
	public static int socketTimeout = 60000;

	/**
	 * Fürt ein Http Request aus und gibt die Antwort als String zurück. Da ein HttpRequestBase übergeben wird kann ein HttpGet oder
	 * HttpPost zum Ausführen übergeben werden.
	 * 
	 * @param httprequest
	 *            HttpGet oder HttpPost
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()
	 * @return Die Antwort als String.
	 * @throws IOException
	 * @throws ClientProtocolException
	 */

	/**
	 * Executes a HTTP request and returns the response as a string. As a HttpRequestBase is given, a HttpGet or HttpPost be passed for
	 * execution.<br>
	 * <br>
	 * Over the ICancel interface cycle is queried in 200 mSec, if the download should be canceled!<br>
	 * Can be NULL
	 * 
	 * @param httprequest
	 *            HttpRequestBase
	 * @param icancel
	 *            ICancel interface (maybe NULL)
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws ConnectTimeoutException
	 */
	public static String Execute(final HttpRequestBase httprequest, final ICancel icancel) throws IOException, ClientProtocolException, ConnectTimeoutException
	{

		httprequest.setHeader("Accept", "application/json");
		httprequest.setHeader("Content-type", "application/json");

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

		final AtomicBoolean ready = new AtomicBoolean(false);
		if (icancel != null)
		{
			Thread cancelChekThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					do
					{
						try
						{
							Thread.sleep(200);
						}
						catch (InterruptedException e)
						{
						}
						if (icancel.cancel()) httprequest.abort();
					}
					while (!ready.get());
				}
			});
			cancelChekThread.start();// start abort chk thread
		}
		HttpResponse response = httpClient.execute(httprequest);
		ready.set(true);// cancel abort chk thread

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null)
		{
			if (Plattform.used == Plattform.Server) line = new String(line.getBytes("ISO-8859-1"), "UTF-8");
			result += line + "\n";
		}
		return result;
	}
}

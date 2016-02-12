/* 
 * Copyright (C) 2015 team-cachebox.de
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
package CB_Utils;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import CB_Utils.Util.Downloader;
import CB_Utils.http.HttpUtils;

public class Downloader_test {
	@Test
	public void download() {
		// delete target temp file

		File target = new File("./testdata/download.jpg");
		if (target.exists()) {
			target.delete();
		}

		String testImage = "http://img.geocaching.com/cache/e96baf07-b869-4568-a1ef-8a69d27a3e43.jpg";

		// set time outs
		HttpUtils.conectionTimeout = 50000;
		HttpUtils.socketTimeout = 50000;

		try {
			Downloader dl = new Downloader(new URL(testImage), target);
			dl.run();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		assertTrue(target.exists());

	}

}

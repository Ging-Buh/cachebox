/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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
package cb_server;

import java.net.MalformedURLException;
import java.net.URL;

import CB_Core.Api.CB_Api;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * 
 * @author Longri
 *
 */
public class LogInWindow extends Window {

	private static final long serialVersionUID = -7023847473950264859L;
	private VerticalLayout content;
	final private static LogInWindow INSTANZ = new LogInWindow();

	public static LogInWindow getInstanz() {
		return INSTANZ;
	}

	private LogInWindow() {

		super("Get API Key"); // Set window caption

		this.setWidth(50, Unit.PERCENTAGE);
		this.setHeight(80, Unit.PERCENTAGE);

		center();

		URL url;
		try {

			String oAuthUrl = CB_Api.getGcAuthUrl();

			url = new URL(oAuthUrl);

			ExternalResource extRes = new ExternalResource(url);

			extRes.getURL();

			BrowserFrame browser = new BrowserFrame("", extRes);

			browser.setWidth(100, Unit.PERCENTAGE);
			browser.setHeight(100, Unit.PERCENTAGE);

			content = new VerticalLayout();
			content.setWidth(100, Unit.PERCENTAGE);
			content.setHeight(100, Unit.PERCENTAGE);
			setContent(content);

			//			browser.addListener(eventType, target, method);

			content.addComponent(browser);
		} catch (MalformedURLException e) {

			e.printStackTrace();
		}
	}
}

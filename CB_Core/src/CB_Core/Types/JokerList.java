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

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

public class JokerList extends ArrayList<JokerEntry> {

	final static org.slf4j.Logger log = LoggerFactory.getLogger(JokerList.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JokerList() {
	}

	public void AddJoker(String vorname, String name, String gclogin, String tage, String telefon, String bemerkung) { // Telefonjoker zur Liste hinzufügen
		try {
			long l = Long.parseLong(tage.trim());
			JokerEntry je = new JokerEntry(vorname, name, gclogin, telefon, l, bemerkung);
			this.add(je);
		} catch (NumberFormatException nfe) {
			log.error("DroidCachebox", "AddJoker", nfe);
		}
	}

	public void ClearList() { // Telefonjoker Liste löschen
		this.clear();

	}

}

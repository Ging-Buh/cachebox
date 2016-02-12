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

import CB_Core.Database;
import de.cb.sqlite.CoreCursor;

public class ExportList extends ArrayList<ExportEntry> {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(ExportList.class);
	private static final long serialVersionUID = -7774973724185994203L;

	public ExportList() {

	}

	public void loadExportList() {
		clear();
		String sql = "select Replication.Id, Replication.ChangeType, Replication.CacheId, Replication.WpGcCode, Replication.SolverCheckSum, Replication.NotesCheckSum, Replication.WpCoordCheckSum, Caches.Name from Replication INNER JOIN Caches ON Replication.CacheId = Caches.Id";

		CoreCursor reader = null;
		try {
			reader = Database.Data.rawQuery(sql, null);
		} catch (Exception exc) {
			log.error("ExportList", "LoadExportList", exc);
		}
		reader.moveToFirst();
		while (reader.isAfterLast() == false) {
			ExportEntry ee = new ExportEntry(reader);
			if (!this.contains(ee)) {
				this.add(ee);
			}

			reader.moveToNext();
		}
		reader.close();

	}
}

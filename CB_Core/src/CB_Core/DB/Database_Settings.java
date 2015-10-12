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
package CB_Core.DB;

import org.slf4j.LoggerFactory;

import de.cb.sqlite.Database_Core;
import de.cb.sqlite.SQLite;

public class Database_Settings extends Database_Core
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(Database_Fieldnotes.class);
	public static Database_Settings Settings;

	public Database_Settings(SQLite database)
	{
		super(database);
		Settings = this;
	}

	@Override
	public boolean StartUp()
	{
		boolean result = this.db.StartUp();
		if (!result) return false;
		return result;
	}

	@Override
	public void Initialize()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void Reset()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void Close()
	{
		// TODO Auto-generated method stub

	}
}

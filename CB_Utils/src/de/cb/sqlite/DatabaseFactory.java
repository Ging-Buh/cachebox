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
package de.cb.sqlite;

public abstract class DatabaseFactory
{
	private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseFactory.class);
	private static DatabaseFactory THAT;

	protected DatabaseFactory()
	{
		THAT = this;
	}

	public static SQLite getInstanz(String Path, AlternateDatabase alter)
	{
		return THAT.createInstanz(Path, alter);
	}

	public static boolean isInitial()
	{
		return THAT != null;
	}

	protected abstract SQLite createInstanz(String Path, AlternateDatabase alter);

}

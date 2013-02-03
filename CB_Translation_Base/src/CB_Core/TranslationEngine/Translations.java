/* 
 * Copyright (C) 2013 team-cachebox.de
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

package CB_Core.TranslationEngine;

/**
 * A Structure for ID as String and this Translation as String
 * 
 * @author Longri
 */
public class Translations
{
	/**
	 * Constructor
	 * 
	 * @param ID
	 *            as String
	 * @param Trans
	 *            as String
	 */
	public Translations(String ID, String Trans)
	{
		this.IdString = ID;
		this.Translation = Trans;
	}

	public String IdString;
	public String Translation;

}

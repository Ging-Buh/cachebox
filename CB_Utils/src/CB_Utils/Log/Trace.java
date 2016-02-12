/* 
 * Copyright (C) 2011 team-cachebox.de
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

package CB_Utils.Log;

/**
 * Der Logger basiert auf einem Interface als CallBack und kann damit auch von nicht GUI Klassen implementiert werden, damit sie einen
 * Fehler Melden können. Wenn keine GUI zur Darstellung diese Meldung empfängt, ist es halt so. Also benutzt so häufig wie möglich den
 * Logger in euren Klassen, dass wird uns helfen Fehler aus der debug.txt auszulesen.
 * 
 * @author Longri
 */
public class Trace {

	/**
	 * Get the Name of Class, Name of method and the linenumber of th Caller.
	 * 
	 * @return
	 */
	public static String getCallerName() {
		return getCallerName(1);
	}

	/**
	 * Get the Name of Class, Name of method and the linenumber of th Caller. For the given deep.
	 * 
	 * @param i
	 * @return
	 */
	public static String getCallerName(int i) {
		String ret = "NoInfo";

		try {
			StackTraceElement Caller = Thread.currentThread().getStackTrace()[3 + i];
			String Name = Caller.getClassName();
			String Methode = Caller.getMethodName();
			int Line = Caller.getLineNumber();
			ret = Name + "." + Methode + " [Line:" + Line + "]";
		} catch (Exception e) {

		}

		return ret;
	}
}

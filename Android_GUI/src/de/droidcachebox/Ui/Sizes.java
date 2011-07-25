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

package de.droidcachebox.Ui;



/**
 * Enthält die Größen einzelner Controls
 * @author Longri
 *
 */
public class Sizes 
{
	private static Size Button;
	private static Size QuickButtonList;
	
	public static int getButtonHeight()
	{
		return Button.height;
	}
	
	public static int getButtonWidth()
	{
		return Button.width;
	}
	
	public static int getQuickButtonListHeight()
	{
		return QuickButtonList.height;
	}
	
	public static int getQuickButtonListWidth()
	{
		return QuickButtonList.width;
	}
	
	
	public static void initial(boolean land)
	{
		//TODO berechne die Werte anhand der Auflösung.
		// jetzt eingesetzte Werte beziehen sich auf eine Auflösung von 460x800(HD2) Longri
		Button = new Size(96,88);
		QuickButtonList = new Size(460,90);
	}
}

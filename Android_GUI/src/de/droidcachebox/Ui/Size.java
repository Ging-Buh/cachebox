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
 *
 */

package de.droidcachebox.Ui;

import android.graphics.Rect;

/**
 * Die Size Structur enthält die Member width und height
 * 
 * 
 * @author Longri
 *
 */
public class Size 
{
	public int width;
	public int height;
	
	/**
	 * Constructor
	 * @param width
	 * @param height
	 */
	public Size(int width, int height)
	{
		this.width=width;
		this.height=height;
	}
	
	public Rect getBounds()
	{
		return getBounds(0,0);
	}
	
	public Rect getBounds(int x, int y)
	{
		return new Rect(x,y,width+x,height+y);
	}

	public Rect getBounds(int x, int y, int k, int l) 
	{
		return new Rect(x,y,width+x+k,height+y+l);
	}
}

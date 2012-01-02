/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

package de.cachebox_test;

import CB_Core.Log.Logger;

/**
 * Contains the static queries of the state of CacheBox, for the decision whether a job being processed has to do. Thus delivers
 * 'dontRender' the value True, if the display switched off and therefore of no Render jobs are necessary.
 * 
 * @author Longri
 */
public class Energy
{

	/**
	 * Explain of no Render jobs!
	 */
	public static boolean dontRender = false;

	/**
	 * Set dontRender to 'True'
	 */
	public static void setDontRender()
	{
		dontRender = true;
		Logger.DEBUG("ENERGY.set dontRender");
	}

	/**
	 * Set dontRender to 'False'
	 */
	public static void resetDontRender()
	{
		dontRender = false;
		Logger.DEBUG("ENERGY.reset dontRender");
	}

}

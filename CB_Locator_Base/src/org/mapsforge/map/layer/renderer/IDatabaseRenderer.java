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
package org.mapsforge.map.layer.renderer;

import CB_Locator.Map.TileGL;

/**
 * @author Longri
 */
public interface IDatabaseRenderer {

	/**
	 * Called when a job needs to be executed.
	 * 
	 * @param rendererJob
	 *            the job that should be executed.
	 */
	public abstract TileGL execute(RendererJob rendererJob);

}
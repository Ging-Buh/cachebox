/* 
 * Copyright (C) 2011, 2012, 2013 team-cachebox.de
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

package CB_UI_Base.GL_UI;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * This interface have a callback to render with LibGdx 3D Api. <br>
 * <br>
 * If you wont that an control render on 3D, implement this Interface to the control <br>
 * and register the Control at onShow() to GL. Gl.that.register3D(this)<br>
 * <br>
 * Unregister on onHide() with gl.that.unregister3D()
 * 
 * @author Longri
 */
public interface render3D
{
	public void render3d(ModelBatch modelBatch);

	/**
	 * return the modified 3Dcam if you wont or return null
	 * 
	 * @param cam3D
	 * @return
	 */
	public PerspectiveCamera get3DCamera(PerspectiveCamera cam3D);

	/**
	 * do you return false if not initial
	 * 
	 * @return
	 */
	public boolean is3D_Initial();

	/**
	 * Initial Model and set is Initial
	 */
	public void Initial3D();

}

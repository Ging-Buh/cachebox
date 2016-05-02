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
package CB_UI_Base.graphics;

/**
 * The possibilities as a Maptile is created. <br>
 * <br>
 * <br>
 * -MapsForge: Tile is created with the MapsForge rendering engine (AWT or Android) <br>
 * <br>
 * -OpenGL: Tile is rendered in a framebuffer OpenGL Texture. <br>
 * <br>
 * -Mixing: Tile is created with the MapsForge render engine but without icons and texts. These become separate created in OpenGL.<br>
 * <br>
 * 
 * @author Longri
 */
public enum GL_RenderType {
	Mapsforge, Mixing
}

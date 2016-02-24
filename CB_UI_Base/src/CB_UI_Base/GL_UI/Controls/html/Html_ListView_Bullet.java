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
package CB_UI_Base.GL_UI.Controls.html;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.Geometry.Circle;
import CB_UI_Base.graphics.Geometry.IGeometry;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_UI_Base.graphics.Geometry.Ring;

/**
 * 
 * @author Longri
 *
 */
public class Html_ListView_Bullet extends Box {

	private final int idx;
	private final Label orderLabel;
	private final PolygonDrawable bulletDrawable;

	public Html_ListView_Bullet(int idx, float width, float height, int bulletNumber) {
	super(width, height);
	this.idx = idx;

	if (bulletNumber == -1) {
		//ordet list (draw text)

		bulletDrawable = null;

		orderLabel = new Label(this.name + " orderLabel", this, String.valueOf(this.idx) + ".");
		orderLabel.setVAlignment(VAlignment.TOP);
		orderLabel.setY(-(HtmlView.margin / 2));
		this.addChild(orderLabel);
	} else {
		orderLabel = null;

		float pos = width / 2.6f;
		IGeometry geometry = null;

		switch (bulletNumber) {
		case 1:
		geometry = new Circle(this.getWidth() - pos, this.getHeight() - pos, pos / 2);
		break;
		case 2:
		geometry = new Ring(this.getWidth() - pos, this.getHeight() - pos, pos / 3f, pos / 2);
		break;
		case 3:
		float x = this.getWidth() - (pos);
		float y1 = this.getHeight() - pos;
		float y2 = this.getHeight();
		geometry = new Quadrangle(x, y1 - (pos / 2), x, y2 - (pos / 2), pos);
		break;
		default:
		//geometry = new Circle(this.getWidth() - pos, this.getHeight() - pos, pos / 2);
		break;
		}

		if (geometry != null) {
		GL_Paint paint = new GL_Paint();
		paint.setColor(Color.BLACK);
		bulletDrawable = new PolygonDrawable(geometry.getVertices(), geometry.getTriangles(), paint, this.getWidth(), this.getHeight());
		} else
		bulletDrawable = null;
	}

	}

	@Override
	public void render(Batch batch) {
	if (bulletDrawable != null) {
		bulletDrawable.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
	}
	}

}

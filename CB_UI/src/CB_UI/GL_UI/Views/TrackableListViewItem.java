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
package CB_UI.GL_UI.Views;

import com.badlogic.gdx.graphics.g2d.Batch;

import CB_Core.Types.Trackable;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class TrackableListViewItem extends ListViewItemBackground {
	private Image img;
	private Label lblName;

	public TrackableListViewItem(CB_RectF rec, int Index, Trackable TB) {
		super(rec, Index, TB.getName());

		float hw = this.getHeight() - this.getTopHeight() - this.getBottomHeight();

		img = new Image(leftBorder, this.getBottomHeight(), hw, hw, "IconImage", false);
		img.setImageURL(TB.getIconUrl());
		this.addChild(img);

		lblName = new Label(this.name + " lblName", img.getMaxX() + UI_Size_Base.that.getMargin(), img.getY(), this.getWidth() - img.getMaxX() - UI_Size_Base.that.getMargin(), img.getHeight());
		lblName.setWrappedText(TB.getName());
		this.addChild(lblName);
	}

	@Override
	protected void render(Batch batch) {
		super.render(batch);
	}

}

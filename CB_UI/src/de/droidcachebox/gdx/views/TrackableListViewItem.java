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
package de.droidcachebox.gdx.views;

import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.database.Trackable;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class TrackableListViewItem extends ListViewItemBackground {
    private Image img;
    private CB_Label lblName;

    public TrackableListViewItem(CB_RectF rec, int Index, Trackable TB) {
        super(rec, Index, TB.getName());

        float hw = this.getHeight() - this.getTopHeight() - this.getBottomHeight();
        this.topBorder = this.getTopHeight();
        this.bottomBorder = this.getBottomHeight();
        this.leftBorder = this.getLeftWidth();
        this.rightBorder = this.getRightWidth();

        img = new Image(0, 0, hw, hw, "img", false);
        img.setImageURL(TB.getIconUrl());
        addNext(img, FIXED);

        lblName = new CB_Label("lblName", 0, 0, this.getWidth() - img.getMaxX() - UiSizes.getInstance().getMargin(), img.getHeight());
        lblName.setWrappedText(TB.getName());
        addLast(lblName);
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
    }

}

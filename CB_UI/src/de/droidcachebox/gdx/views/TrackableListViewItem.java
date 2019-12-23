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

import de.droidcachebox.database.Trackable;
import de.droidcachebox.gdx.activities.TB_Details;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class TrackableListViewItem extends ListViewItemBackground {

    public TrackableListViewItem(CB_RectF rec, int Index, Trackable trackable) {
        super(rec, Index, trackable.getName());

        float hw = getHeight() - getTopHeight() - getBottomHeight();
        topBorder = getTopHeight();
        bottomBorder = getBottomHeight();
        leftBorder = getLeftWidth();
        rightBorder = getRightWidth();

        Image img = new Image(0, 0, hw, hw, "img", false);
        img.setImageURL(trackable.getIconUrl());
        addNext(img, FIXED);

        CB_Label lblName = new CB_Label("lblName", 0, 0, getWidth() - img.getMaxX() - UiSizes.getInstance().getMargin(), img.getHeight());
        lblName.setWrappedText(trackable.getName());
        addLast(lblName);
        setClickHandler((v1, x, y, pointer, button) -> {
            if (TB_Details.that == null)
                new TB_Details();
            TB_Details.that.show(trackable);
            return true;
        });

    }

}

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
package de.droidcachebox.gdx;

import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class ActivityBase extends CB_View_Base {
    protected float measuredLabelHeight;
    protected float measuredLabelHeightBig;
    protected float buttonHeight;
    protected float margin = 0;

    public ActivityBase(String Name) {
        this(new CB_RectF(0, 0, Math.min(UiSizes.getInstance().getSmallestWidth(), UiSizes.getInstance().getWindowHeight() * 0.66f), UiSizes.getInstance().getWindowHeight()), Name);
    }

    public ActivityBase(CB_RectF rec, String Name) {
        super(rec, Name);
        setBackground(Sprites.activityBackground);
        measuredLabelHeight = Fonts.measure("T").height * 1.5f;
        measuredLabelHeightBig = Fonts.measureForBigFont("T").height * 1.5f;
        buttonHeight = UiSizes.getInstance().getButtonHeight();
        registerSkinChangedEvent();
    }

    @Override
    protected void skinIsChanged() {
        setBackground(Sprites.activityBackground);
    }

    @Override
    public GL_View_Base addChild(GL_View_Base view) {
        addChildDirect(view);
        return view;
    }

    @Override
    public void removeChildren() {
        removeChildrenDirect();
    }

    @Override
    protected void renderInit() {
        // do not call super, it wants clear childs
    }

    public void finish() {
        GL.that.runOnGL(() -> GL.that.closeActivity());
    }

    public void show() {
        GL.that.showActivity(this);
    }

    public boolean canCloseWithBackKey() {
        return true;
    }

}

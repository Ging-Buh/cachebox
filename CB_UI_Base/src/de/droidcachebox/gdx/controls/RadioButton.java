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
package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class RadioButton extends CB_CheckBox {

    private final Image radioBack;
    private final Image radioSet;
    private RadioGroup group;

    public RadioButton() {
        super();
        radioBack = new Image(new CB_RectF(UiSizes.getInstance().getChkBoxSize()), name, false);
        radioBack.setDrawable(Sprites.radioBack);
        this.addChild(radioBack);

        radioSet = new Image(new CB_RectF(UiSizes.getInstance().getChkBoxSize()), name, false);
        radioSet.setDrawable(Sprites.radioOn);
        this.addChild(radioSet);
    }

    public void setRadioGroup(RadioGroup Group) {
        group = Group;
    }

    @Override
    protected void render(Batch batch) {
        if (lblTxt != null && lblTxt.getX() < radioBack.getMaxX()) {
            lblTxt.setX(radioBack.getMaxX() + UiSizes.getInstance().getMargin());
        }

        if (isChk && !radioSet.isVisible()) {
            radioSet.setVisible();
        } else if (!isChk && radioSet.isVisible()) {
            radioSet.setVisible(false);
        }

        super.render(batch);
    }

    @Override
    protected void initialize() {
        // do neither explicit nor implicit a super.Initial();
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        if (!isDisabled) {
            if (!isChk || group == null) {
                isChk = !isChk;
                if (changeListener != null)
                    changeListener.onCheckedChanged(this, isChk);
                if (group != null)
                    group.aktivate(this);

            }
        }
        return true;
    }

    @Override
    public void setText(String Text, Color color) {
        hAlignment = HAlignment.LEFT;
        setText(Text, null, color);
    }

    @Override
    public void setText(String Text) {
        hAlignment = HAlignment.LEFT;
        setText(Text, null, null);
    }

    public void setText(String Text, HAlignment alignment) {
        hAlignment = alignment;
        setText(Text, null, null);
    }
}

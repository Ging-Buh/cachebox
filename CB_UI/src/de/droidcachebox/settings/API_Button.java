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
package de.droidcachebox.settings;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;

public class API_Button extends CB_Button {

    Image image;
    OnClickListener click = (view, x, y, pointer, button) -> {
        PlatformUIBase.getApiKey();
        return true;
    };

    public API_Button(CB_RectF rec) {
        super(rec, "API-Button");

        setText();
        setClickHandler(click);

        CB_RectF rec1 = new CB_RectF(this);
        rec1.setWidth(getHeight());
        rec1.setX(getWidth() - getHeight());
        rec1 = rec1.scaleCenter(0.7f);

        image = new Image(rec1, "", false);

        addChild(image);
        setImage();

    }

    private void setText() {
        setText(Translation.get("getApiKey"));
    }

    public void setImage() {
        if (image != null) {
            if (GroundspeakAPI.hasGroundSpeakAccessToken()) {
                image.setDrawable(new SpriteDrawable(Sprites.getSprite("chk-icon")));
            } else {
                image.setDrawable(new SpriteDrawable(Sprites.getSprite("chk-icon-disable")));
            }
        }
    }

}

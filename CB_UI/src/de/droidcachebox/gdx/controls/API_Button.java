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

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.Config;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.translation.Translation;

public class API_Button extends CB_Button {

    Image apiChk;
    OnClickListener click = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            PlatformUIBase.getApiKey();
            return true;
        }
    };

    public API_Button(CB_RectF rec) {
        super(rec, "API-Button");

        setText();
        this.setClickHandler(click);

        CB_RectF rec1 = new CB_RectF(this);
        rec1.setWidth(this.getHeight());
        rec1.setX(this.getWidth() - this.getHeight());
        rec1 = rec1.ScaleCenter(0.7f);

        apiChk = new Image(rec1, "", false);

        this.addChild(apiChk);
        setImage();

    }

    private void setText() {
        this.setText(Translation.get("getApiKey"));
    }

    public void setImage() {
        if (apiChk != null) {
            Drawable drw;

            boolean Entry = false;

            if (Config.UseTestUrl.getValue()) {
                if (!Config.AccessTokenForTest.getValue().equals(""))
                    Entry = true;
            } else {
                if (!Config.AccessToken.getValue().equals(""))
                    Entry = true;
            }

            if (Entry) {
                drw = new SpriteDrawable(Sprites.getSprite("chk-icon"));
            } else {
                drw = new SpriteDrawable(Sprites.getSprite("chk-icon-disable"));
            }

            apiChk.setDrawable(drw);
        }

    }

}

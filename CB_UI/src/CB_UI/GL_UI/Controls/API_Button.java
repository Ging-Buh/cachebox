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
package CB_UI.GL_UI.Controls;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.CB_Button;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class API_Button extends CB_Button {

    Image apiChk;
    OnClickListener click = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            PlatformConnector.callGetApiKey();
            return true;
        }
    };

    public API_Button(CB_RectF rec) {
        super(rec, "API-Button");

        setText();
        this.setOnClickListener(click);

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

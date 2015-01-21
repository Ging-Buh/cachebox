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
import CB_UI.Api.GcApiLogin;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Plattform;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class API_Button extends Button {

    Image apiChk;

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
	this.setText(Translation.Get("getApiKey"));
    }

    @Override
    protected void Initial() {
	super.Initial();

    }

    public void setImage() {
	if (apiChk != null) {
	    Drawable drw;

	    boolean Entry = false;

	    if (Config.StagingAPI.getValue()) {
		if (!Config.GcAPIStaging.getValue().equals(""))
		    Entry = true;
	    } else {
		if (!Config.GcAPI.getValue().equals(""))
		    Entry = true;
	    }

	    if (Entry) {
		drw = new SpriteDrawable(SpriteCacheBase.getThemedSprite("chk-icon"));
	    } else {
		drw = new SpriteDrawable(SpriteCacheBase.getThemedSprite("chk-icon-disable"));
	    }

	    apiChk.setDrawable(drw);
	}

    }

    OnClickListener click = new OnClickListener() {

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
	    if (Plattform.used == Plattform.Desktop) {
		(new GcApiLogin()).RunRequest();
	    } else {
		platformConector.callGetApiKeyt();
	    }

	    return true;
	}
    };

}

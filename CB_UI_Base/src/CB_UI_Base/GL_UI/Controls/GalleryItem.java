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
package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;

/**
 * 
 * @author Longri
 *
 */
public class GalleryItem extends ListViewItemBackground {

    private final ImageLoader iloader;
    private final Image img;

    public GalleryItem(CB_RectF rec, int Index, ImageLoader loader) {
	super(rec, Index, "");
	iloader = loader;

	CB_RectF imgRec = rec.copy();
	imgRec.setPos(0, 0);

	img = new Image(iloader, imgRec.ScaleCenter(0.95f), "", false);
	img.setHAlignment(HAlignment.CENTER);
	this.addChild(img);
    }

    @Override
    public void onResized(CB_RectF rec) {
	super.onResized(rec);
	CB_RectF imgRec = rec.copy();
	imgRec.setPos(0, 0);
	img.setRec(imgRec.ScaleCenter(0.95f));
    }

    @Override
    protected void SkinIsChanged() {
    }

    public Image getImage() {
	return img;
    }

    private static NinePatch backSelect;

    protected static boolean mBackIsInitial = false;
    protected boolean isPressed = false;

    public static void ResetBackground() {
	mBackIsInitial = false;
    }

    @Override
    protected void Initial() {
	if (!mBackIsInitial) {
	    backSelect = new NinePatch(SpriteCacheBase.getThemedSprite("listrec-first"), 13, 13, 13, 13);
	    mBackIsInitial = true;
	}
    }

    @Override
    protected void render(Batch batch) {
	if (isPressed) {
	    isPressed = GL.getIsTouchDown();
	}

	if (this.isDisposed() || !this.isVisible())
	    return;
	super.render(batch);
	// Draw Background
	if (mBackIsInitial) {
	    if (isSelected) {
		backSelect.draw(batch, 0, 0, this.getWidth(), this.getHeight());
	    }

	} else {
	    Initial();
	}

    }

}

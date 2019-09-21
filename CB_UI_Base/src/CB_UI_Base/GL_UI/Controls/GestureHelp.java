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

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import static CB_UI_Base.Math.GL_UISizes.MainBtnSize;

public class GestureHelp extends CB_View_Base {

    /**
     * CacheID of the Cache showing Bubble
     */
    private final long mCacheId = -1;
    Image Button;
    Image UpIcon;
    Image Up;
    Image DownIcon;
    Image Down;
    Image LeftIcon;
    Image Left;
    Image RightIcon;
    Image Right;
    float h;
    float cX;
    float cY;
    float d;
    float ArrowH;
    float ArrowW;
    boolean UseLastBtnBackground = false;

    public GestureHelp(SizeF Size, String Name) {
        super(Size, Name);

    }

    public long getCacheId() {
        return mCacheId;
    }

    @Override
    protected void render(Batch batch) {

        Sprite sprite = Sprites.Bubble.get(UseLastBtnBackground ? 4 : 3);
        sprite.setPosition(0, 0);
        sprite.setSize(getWidth(), getHeight());
        sprite.draw(batch);
        super.render(batch);
    }

    @Override
    public void setPos(float x, float y) {
        if (x + getWidth() > UI_Size_Base.ui_size_base.getWindowWidth()) {
            UseLastBtnBackground = true;
            x = UI_Size_Base.ui_size_base.getWindowWidth() - getWidth();
        }

        super.setPos(x, y);
    }

    public void addBtnIcon(NinePatch icon) {
        h = MainBtnSize.getHeight() / 2.4f;
        d = h / 8;
        cX = (this.getHeight() / 2) - (h / 2);
        cY = cX + d + d;
        ArrowH = h / 3;
        ArrowW = h / 3;

        Button = new Image(cX, cY, h, h, "UpIcon", false);
        if (icon != null)
            Button.setDrawable(new NinePatchDrawable(icon));
        this.addChild(Button);
    }

    public void addUp(Sprite icon) {
        Up = new Image(cX, cY + h + d, h, ArrowH, "Up", false);
        if (icon != null)
            Up.setDrawable(new SpriteDrawable(Sprites.Arrows.get(7)));
        this.addChild(Up);

        UpIcon = new Image(cX, cY + h + d + ArrowH, h, h, "UpIcon", false);
        if (icon != null)
            UpIcon.setDrawable(new SpriteDrawable(icon));
        this.addChild(UpIcon);
    }

    public void addDown(Sprite icon) {
        Down = new Image(cX, cY - d - ArrowH, h, ArrowH, "Down", false);
        if (icon != null)
            Down.setDrawable(new SpriteDrawable(Sprites.Arrows.get(6)));
        this.addChild(Down);

        DownIcon = new Image(cX, cY - d - ArrowH - h, h, h, "DownIcon", false);
        if (icon != null)
            DownIcon.setDrawable(new SpriteDrawable(icon));
        this.addChild(DownIcon);
    }

    public void addLeft(Sprite icon) {
        Left = new Image(cX - d - ArrowW, cY, ArrowW, h, "Left", false);
        if (icon != null)
            Left.setDrawable(new SpriteDrawable(Sprites.Arrows.get(8)));
        this.addChild(Left);

        LeftIcon = new Image(cX - d - ArrowH - h, cY, h, h, "LeftIcon", false);
        if (icon != null)
            LeftIcon.setDrawable(new SpriteDrawable(icon));
        this.addChild(LeftIcon);
    }

    public void addRight(Sprite icon) {
        Right = new Image(cX + h + d, cY, ArrowW, h, "Up", false);
        if (icon != null)
            Right.setDrawable(new SpriteDrawable(Sprites.Arrows.get(9)));
        this.addChild(Right);

        RightIcon = new Image(cX + h + d + ArrowW, cY, h, h, "UpIcon", false);
        if (icon != null)
            RightIcon.setDrawable(new SpriteDrawable(icon));
        this.addChild(RightIcon);
    }

}

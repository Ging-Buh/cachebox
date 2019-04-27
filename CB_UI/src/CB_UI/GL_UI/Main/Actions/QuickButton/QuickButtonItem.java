/*
 * Copyright (C) 2011-2015 team-cachebox.de
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

package CB_UI.GL_UI.Main.Actions.QuickButton;

import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Stellt ein Item der Quick Button List dar
 *
 * @author Longri
 */
public class QuickButtonItem extends ListViewItemBase {
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

    private AbstractAction mAction;
    private Image mButtonIcon;
    private String mActionDesc;
    private Button mButton;
    private QuickActions quickActionsEnum;
    private int autoResortState = -1;
    private int spoilerState = -1;
    private int hintState = -1;
    private int torchState = -1;

    /**
     * Constructor
     *
     * @param action Action Enum
     * @param Icon   Action icon
     * @param Desc   Action Beschreibung
     */
    public QuickButtonItem(CB_RectF rec, int Index, AbstractAction action, String Desc, QuickActions type) {
        super(rec, Index, action.getTitleTranlationId());
        quickActionsEnum = type;
        mAction = action;
        mButtonIcon = new Image(rec.ScaleCenter(0.7f), "QuickListItemImage", false);
        mButtonIcon.setDrawable(new SpriteDrawable(action.getIcon()));
        mButtonIcon.setClickable(false);

        mActionDesc = Desc;

        mButton = new Button(rec, "QuickListItemButton");
        mButton.setButtonSprites(Sprites.QuickButton);
        mButton.setDraggable();
        this.addChild(mButton);
        this.addChild(mButtonIcon);

        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                mAction.Execute();
                return true;
            }
        });
    }

    /**
     * Gibt die Beschreibung dieses Items wieder
     *
     * @return String
     */
    public String getDesc() {
        return mActionDesc;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        return mButton.onTouchUp(x, y, pointer, button);
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        return mButton.click(x, y, pointer, button);
    }

    @Override
    protected void render(Batch batch) {
        if (childs.size() == 0) {
            this.addChild(mButton);
            this.addChild(mButtonIcon);
        }

        super.render(batch);

        if (mAction.getId() == MenuID.AID_AUTO_RESORT) {
            if (GlobalCore.getAutoResort() && autoResortState != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.autoSortOnIcon.name())));
                autoResortState = 1;
            } else if (!GlobalCore.getAutoResort() && autoResortState != 0) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.autoSortOffIcon.name())));
                autoResortState = 0;
            }
        } else if (mAction.getId() == MenuID.AID_SHOW_SPOILER) {
            boolean hasSpoiler = false;
            if (GlobalCore.isSetSelectedCache()) {
                hasSpoiler = GlobalCore.selectedCachehasSpoiler();
            }

            if (hasSpoiler && spoilerState != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.imagesIcon.name())));
                spoilerState = 1;
            } else if (!hasSpoiler && spoilerState != 0) {
                Sprite sprite = new Sprite(Sprites.getSprite(IconName.imagesIcon.name()));
                sprite.setColor(DISABLE_COLOR);
                mButtonIcon.setDrawable(new SpriteDrawable(sprite));
                spoilerState = 0;
            }
        } else if (mAction.getId() == MenuID.AID_TORCH) {

            if (PlatformConnector.isTorchOn() && torchState != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.TORCHON.name())));
                torchState = 1;
            } else if (!PlatformConnector.isTorchOn() && torchState != 0) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.TORCHOFF.name())));
                torchState = 0;
            }
        } else if (mAction.getId() == MenuID.AID_SHOW_HINT) {

            if (mAction.getEnabled() && hintState != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.hintIcon.name())));
                hintState = 1;
            } else if (!mAction.getEnabled() && hintState != 0) {
                Sprite sprite = new Sprite(Sprites.getSprite(IconName.hintIcon.name()));
                sprite.setColor(DISABLE_COLOR);
                mButtonIcon.setDrawable(new SpriteDrawable(sprite));
                hintState = 0;
            }
        }
    }

    public QuickActions getAction() {
        return quickActionsEnum;
    }

    @Override
    public void setY(float i) {
        super.setY(i);
    }

}

/*
 * Copyright (C) 2011-2022 team-cachebox.de
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

package de.droidcachebox.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.settings.Settings;

/**
 * represents one Item of Quick Button List
 *
 * @author Longri
 */
public class QuickButtonItem extends ListViewItemBase {
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

    private final Image mButtonIcon;
    private final CB_Button mButton;
    private final Action action;
    private int state;

    public QuickButtonItem(CB_RectF rec, int index, Action type) {
        super(rec, index, "");
        name = type.action.getTitleTranslationId();
        action = type;
        mButtonIcon = new Image(rec.scaleCenter(0.7f), "QuickListItemImage", false);
        mButtonIcon.setDrawable(new SpriteDrawable(type.action.getIcon()));
        mButtonIcon.setClickable(false);

        mButton = new CB_Button(rec, "QuickListItemButton");
        mButton.setDrawables(new SpriteDrawable(Sprites.getSprite("button")), new SpriteDrawable(Sprites.getSprite("btn-pressed")), null, new SpriteDrawable(Sprites.getSprite("btn-pressed")));
        mButton.setDraggable();
        this.addChild(mButton);
        this.addChild(mButtonIcon);

        state = -1;

        mButton.setClickHandler((v, x, y, pointer, button) -> {
            type.action.execute();
            return true;
        });
        if (type == Action.RememberGeoCache) {
            setLongClickHandler((view, x, y, pointer, button) -> {
                // forget remembered
                Settings.rememberedGeoCache.setValue("");
                Settings.getInstance().acceptChanges();
                return true;
            });
        }
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

        if (action == Action.SwitchAutoResort) {
            if (GlobalCore.getAutoResort() && state != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.autoSortOnIcon.name())));
                state = 1;
            } else if (!GlobalCore.getAutoResort() && state != 0) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.autoSortOffIcon.name())));
                state = 0;
            }
        } else if (action == Action.ShowSpoiler) {
            boolean hasSpoiler = false;
            if (GlobalCore.isSetSelectedCache()) {
                hasSpoiler = GlobalCore.selectedCacheHasSpoiler();
            }

            if (hasSpoiler && state != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.imagesIcon.name())));
                state = 1;
            } else if (!hasSpoiler && state != 0) {
                Sprite sprite = new Sprite(Sprites.getSprite(IconName.imagesIcon.name()));
                sprite.setColor(DISABLE_COLOR);
                mButtonIcon.setDrawable(new SpriteDrawable(sprite));
                state = 0;
            }
        } else if (action == Action.SwitchTorch) {
            if (Platform.isTorchOn() && state != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.TORCHON.name())));
                state = 1;
            } else if (!Platform.isTorchOn() && state != 0) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.TORCHOFF.name())));
                state = 0;
            }
        } else if (action == Action.ShowHint) {
            if (action.action.getEnabled() && state != 1) {
                mButtonIcon.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.hintIcon.name())));
                state = 1;
            } else if (!action.action.getEnabled() && state != 0) {
                Sprite sprite = new Sprite(Sprites.getSprite(IconName.hintIcon.name()));
                sprite.setColor(DISABLE_COLOR);
                mButtonIcon.setDrawable(new SpriteDrawable(sprite));
                state = 0;
            }
        } else if (action == Action.RememberGeoCache) {
            if (Settings.rememberedGeoCache.getValue().length() > 0) {
                mButtonIcon.setDrawable(new SpriteDrawable(action.action.getIcon()));
            } else {
                Sprite sprite = new Sprite(action.action.getIcon());
                sprite.setColor(DISABLE_COLOR);
                mButtonIcon.setDrawable(new SpriteDrawable(sprite));
            }
        }
    }

    public Action getQuickAction() {
        return action;
    }

    @Override
    public void setY(float i) {
        super.setY(i);
    }

}

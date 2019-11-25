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
package de.droidcachebox.gdx.main;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;

public class MenuItem extends ListViewItemBackground {

    protected Object data = null;
    boolean mIsCheckable = false;
    private boolean mIsChecked = false;
    protected boolean isPressed = false;
    private CB_Label mLabel;
    private Image checkImage;
    private Drawable mIcon;
    private String mTitle;
    private boolean mIsEnabled = true;
    private Image iconImage;

    public MenuItem(SizeF size, int Index, String Name) {
        super(new CB_RectF(size), Index, Name);
    }

    public MenuItem(int Index, String Name) {
        super(new CB_RectF(), Index, Name);
    }

    void toggleCheck() {
        if (mIsCheckable) {
            mIsChecked = !mIsChecked;

            Drawable drawable;
            if (mIsChecked) {
                drawable = new SpriteDrawable(Sprites.ChkIcons.get(1));
            } else {
                drawable = new SpriteDrawable(Sprites.ChkIcons.get(0));
            }

            checkImage.setDrawable(drawable);
        }
    }

    public boolean isCheckboxClicked(float x) {
        // true, if clicked right of the beginning of the image
        if (checkImage == null)
            return false;
        else return x > checkImage.getX();
    }

    @Override
    protected void initialize() {
        super.initialize();
        removeChilds();

        mLabel = new CB_Label(this);

        boolean hasIcon = (mIcon != null);
        if (hasIcon) {
            CB_RectF rec = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight()).ScaleCenter(0.75f);
            iconImage = new Image(rec, "MenuItemImage", false);
            iconImage.setDrawable(mIcon);
            if (!mIsEnabled) {
                iconImage.setColor(COLOR.getDisableFontColor());
            }
            addChild(iconImage);
        }

        if (mIsCheckable) {
            CB_RectF rec;
            if (hasIcon) {
                rec = new CB_RectF(getWidth() - 2 * getHeight(), 0, getHeight(), getHeight()).ScaleCenter(0.75f);
            } else {
                rec = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight()); // .ScaleCenter(0.75f);
            }

            rec.setHeight(rec.getWidth());

            checkImage = new Image(rec, "MenuItemCheckImage", false);

            Drawable drawable;
            if (mIsChecked) {
                drawable = new SpriteDrawable(Sprites.ChkIcons.get(1));
            } else {
                drawable = new SpriteDrawable(Sprites.ChkIcons.get(0));
            }

            checkImage.setDrawable(drawable);
            addChild(checkImage);
        }

        if (mTitle != null)
            mLabel.setText(mTitle);
        if (!mIsEnabled) {
            mLabel.setTextColor(COLOR.getDisableFontColor());
        }
        addChild(mLabel);
        setContentSize();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        setContentSize();
    }

    private void setContentSize() {
        boolean hasIcon = (mIcon != null);

        float left = getHeight() * 0.2f;
        float right = hasIcon ? getHeight() : 0;
        float labelWidth = getWidth() - right - left;
        mLabel.setWidth(labelWidth);

        if (hasIcon && iconImage != null) {
            iconImage.setPos(getWidth() - getHeight(), (getHeight() - iconImage.getHeight()) / 2);
        }

        if (mIsCheckable && checkImage != null) {
            CB_RectF rec;
            if (hasIcon) {
                rec = new CB_RectF(getWidth() - 2 * getHeight(), 0, getHeight(), getHeight()).ScaleCenter(0.75f);
            } else {
                rec = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight()); // .ScaleCenter(0.75f);
            }
            rec.setHeight(rec.getWidth());
            checkImage.setSize(rec);
        }

        mLabel.setPos(left, (getHeight() - mLabel.getHeight()) / 2);
    }

    /**
     * Retrieve the current title of the item.
     *
     * @return The title.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Change the title associated with this item.
     *
     * @param title The new text to be displayed.
     * @return This Item so additional setters can be called.
     */
    public MenuItem setTitle(String title) {
        mTitle = title;
        if (mLabel == null)
            mLabel = new CB_Label(name + " mLabel", this, title);
        else
            mLabel.setText(title);
        return this;
    }

    /**
     * Change the icon associated with this item. This icon will not always be shown, so the title should be sufficient in describing this
     * item. See {@link Menu} for the menu types that support icons.
     *
     * @param icon The new icon (as a Sprite) to be displayed.
     * @return This Item so additional setters can be called.
     */
    public MenuItem setIcon(Drawable icon) {
        mIcon = icon;
        resetInitial();
        return this;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

        isPressed = true;

        return false;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        isPressed = false;

        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        isPressed = false;

        return false;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
    }


    @Override
    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;

        if (!mIsEnabled) {
            // lösche ClickListener
            // these are the defaults, but they are never used
            // used is the menus onItemClickedListener,
            // who handles item clicks independant of isEnabled
            // what is not bad ex. for spoilers
            setClickHandler(null);
            setOnLongClickListener(null);
        }

        resetInitial();
    }

    public void setDisabled(boolean enabled) {
        // same as enabled but without deleting the click listeners
        mIsEnabled = enabled;
        resetInitial();
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
        resetInitial();
    }

    public void setCheckable(boolean isCheckable) {
        mIsCheckable = isCheckable;
        resetInitial();
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

}

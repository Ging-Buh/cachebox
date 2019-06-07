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
package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class MenuItem extends MenuItemBase {

    private final int mID;
    protected boolean mIsCheckable = false;
    protected boolean mIsChecked = false;
    protected boolean mLeft = false;
    protected boolean isPressed = false;
    private Label mLabel;
    private Image checkImage;
    private Drawable mIcon;
    private String mTitle;
    private boolean mIsEnabled = true;
    private Image iconImage;

    public MenuItem(SizeF size, int Index, int ID, String Name) {
        super(new CB_RectF(size), Index, Name);
        mID = ID;
    }

    public MenuItem(int Index, int ID, String Name) {
        super(new CB_RectF(), Index, Name);
        mID = ID;
    }

    public int getMenuItemId() {
        return mID;
    }

    public void toggleCheck() {
        if (mIsCheckable) {
            mIsChecked = !mIsChecked;

            Drawable drawable = null;
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
    protected void Initial() {
        super.Initial();
        this.removeChilds();

        mLabel = new Label(this);

        boolean hasIcon = (mIcon != null);
        if (hasIcon) {
            CB_RectF rec = new CB_RectF(this.getWidth() - this.getHeight(), 0, this.getHeight(), this.getHeight()).ScaleCenter(0.75f);
            iconImage = new Image(rec, "MenuItemImage", false);
            iconImage.setDrawable(mIcon);
            if (!mIsEnabled) {
                iconImage.setColor(COLOR.getDisableFontColor());
            }
            this.addChild(iconImage);
        }

        if (mIsCheckable) {
            CB_RectF rec;
            if (hasIcon) {
                rec = new CB_RectF(this.getWidth() - 2 * this.getHeight(), 0, this.getHeight(), this.getHeight()).ScaleCenter(0.75f);
            } else {
                rec = new CB_RectF(this.getWidth() - this.getHeight(), 0, this.getHeight(), this.getHeight()); // .ScaleCenter(0.75f);
            }

            rec.setHeight(rec.getWidth());

            checkImage = new Image(rec, "MenuItemCheckImage", false);

            Drawable drawable = null;
            if (mIsChecked) {
                drawable = new SpriteDrawable(Sprites.ChkIcons.get(1));
            } else {
                drawable = new SpriteDrawable(Sprites.ChkIcons.get(0));
            }

            checkImage.setDrawable(drawable);
            this.addChild(checkImage);
        }

        if (mTitle != null)
            mLabel.setText(mTitle);
        if (!mIsEnabled) {
            mLabel.setTextColor(COLOR.getDisableFontColor());
        }
        this.addChild(mLabel);
        setContentSize();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        setContentSize();
    }

    private void setContentSize() {
        boolean hasIcon = (mIcon != null);

        float left = this.getHeight() * 0.2f;
        float right = hasIcon ? this.getHeight() : 0;
        float labelWidth = this.getWidth() - right - left;
        mLabel.setWidth(labelWidth);

        if (hasIcon && iconImage != null) {
            iconImage.setPos(this.getWidth() - this.getHeight(), (this.getHeight() - iconImage.getHeight()) / 2);
        }

        if (mIsCheckable && checkImage != null) {
            CB_RectF rec;
            if (hasIcon) {
                rec = new CB_RectF(this.getWidth() - 2 * this.getHeight(), 0, this.getHeight(), this.getHeight()).ScaleCenter(0.75f);
            } else {
                rec = new CB_RectF(this.getWidth() - this.getHeight(), 0, this.getHeight(), this.getHeight()); // .ScaleCenter(0.75f);
            }
            rec.setHeight(rec.getWidth());
            checkImage.setSize(rec);
        }

        float x = left;
        float y = (this.getHeight() - mLabel.getHeight()) / 2;
        mLabel.setPos(x, y);
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
            mLabel = new Label(this.name + " mLabel", this, title);
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
        this.resetInitial();
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
            setOnClickListener(null);
            setOnLongClickListener(null);
        }

        this.resetInitial();
    }

    public void setDisabled(boolean enabled) {
        // same as enabled but without deleting the click listeners
        mIsEnabled = enabled;
        this.resetInitial();
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
        this.resetInitial();
    }

    public void setCheckable(boolean isCheckable) {
        mIsCheckable = isCheckable;
        this.resetInitial();
    }

    public void setLeft(boolean value) {
        mLeft = value;
        this.resetInitial();
    }

    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

}

/*
 * Copyright (C) 2011-2020 team-cachebox.de
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.ButtonSprites;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.MoveableList;

/**
 * @author Longri
 */
public class CB_Button extends CB_View_Base {
    BitmapFont mFont;

    protected Drawable drawableNormal;
    protected Drawable drawablePressed;
    protected Drawable drawableDisabled;
    protected Drawable drawableFocused;
    private MoveableList<Drawable> DrawableOverlayList = new MoveableList<>();

    protected boolean isFocused = false;
    protected boolean isPressed = false;
    boolean isDisabled = false;
    CB_Label lblTxt;
    boolean draggableButton = false;
    CB_Label.HAlignment hAlignment = CB_Label.HAlignment.CENTER;
    protected CB_Label.VAlignment vAlignment = CB_Label.VAlignment.CENTER;

    private Object tag = null; // sometimes also referred as data, for to attach an arbitrary object

    public CB_Button(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
        this.setClickable(true);
    }

    public CB_Button(CB_RectF rec, GL_View_Base parent, String name) {
        super(rec, parent, name);
        this.setClickable(true);
    }

    public CB_Button(GL_View_Base parent, String name) {
        super(UiSizes.getInstance().getButtonRectF(), parent, name);
        this.setClickable(true);
    }

    public CB_Button(String text) {
        super(UiSizes.getInstance().getButtonRectF(), text);
        this.setText(text);
        this.setClickable(true);
    }

    public CB_Button(CB_RectF rec, String name) {
        super(rec, name);
        this.setClickable(true);
    }

    public CB_Button(CB_RectF rec, OnClickListener onClick) {
        super(rec, "");
        this.setClickHandler(onClick);
    }

    public void setButtonSprites(ButtonSprites sprites) {
        if (sprites != null) {
            drawableNormal = sprites.getNormal();
            drawablePressed = sprites.getPressed();
            drawableDisabled = sprites.getDisabled();
            drawableFocused = sprites.getFocus();
        }
    }

    public void setFont(BitmapFont font) {
        mFont = font;
    }

    /**
     * render
     */
    @Override
    protected void render(Batch batch) {
        if (draggableButton) {
            if (isPressed && !GL_Input.that.getIsTouchDown()) {
                isPressed = false;
                GL.that.renderOnce();
            }
        }

        if (!isPressed && !isDisabled && !isFocused) {
            if (drawableNormal != null) {
                drawableNormal.draw(batch, 0, 0, getWidth(), getHeight());
            } else {
                initialize();
                GL.that.renderOnce();
            }
        } else if (isPressed) {
            if (drawablePressed != null) {
                drawablePressed.draw(batch, 0, 0, getWidth(), getHeight());
            } else {
                initialize();
                GL.that.renderOnce();
            }
        } else if (isFocused) {
            if (drawableFocused != null) {
                drawableFocused.draw(batch, 0, 0, getWidth(), getHeight());
            } else {
                initialize();
                GL.that.renderOnce();
            }
        } else {
            if (drawableDisabled != null) {
                drawableDisabled.draw(batch, 0, 0, getWidth(), getHeight());
            } else {
                initialize();
                GL.that.renderOnce();
            }
        }

        for (int i = 0, n = DrawableOverlayList.size(); i < n; i++) {
            Drawable drw = DrawableOverlayList.get(i);
            drw.draw(batch, 0, 0, getWidth(), getHeight());
        }

    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        if (!isDisabled) {
            isPressed = true;
            GL.that.renderOnce();
        }
        return !draggableButton;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        isPressed = false;
        GL.that.renderOnce();
        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {

        isPressed = false;
        GL.that.renderOnce();
        return !draggableButton;
    }

    public void enable() {
        isDisabled = false;
    }

    public void disable() {
        isDisabled = true;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        // wenn Button disabled ein Behandelt zurück schicken,
        // damit keine weiteren Abfragen durchgereicht werden.
        // Auch wenn dieser Button ein OnClickListener hat.
        if (isDisabled) {
            return true;
        } else
            return super.click(x, y, pointer, button);
    }

    public void setText(String text, Color color) {
        setText(text, null, color);
    }

    public void setText(String text, BitmapFont font, Color color) {
        if (text == null) return;
        // ? no change
        if (lblTxt != null)
            if (lblTxt.mText != null)
                if (lblTxt.mText.equals(text))
                    if (lblTxt.mFont.equals(font))
                        if (lblTxt.mColor.equals(color))
                            if (lblTxt.mHAlignment.equals(hAlignment))
                                if (lblTxt.mVAlignment.equals(vAlignment))
                                return;

        // no text -> remove label
        if (text.equals("")) {
            if (lblTxt != null) {
                this.removeChild(lblTxt);
                lblTxt.dispose();
            }
            lblTxt = null;
            GL.that.renderOnce();
            return;
        }

        // replace old label
        if (lblTxt != null) {
            this.removeChild(lblTxt);
        }

        if (font != null)
            mFont = font;
        if (mFont == null)
            mFont = Fonts.getBig();
        lblTxt = new CB_Label(text, mFont, color, WrapType.WRAPPED).setHAlignment(hAlignment).setVAlignment(vAlignment);
        this.initRow(BOTTOMUP);
        this.addLast(lblTxt);

        GL.that.renderOnce();
    }

    @Override
    protected void initialize() {
        if (drawableNormal == null) {
            drawableNormal = Sprites.btn;
        }
        if (drawablePressed == null) {
            drawablePressed = Sprites.btnPressed;
        }
        if (drawableDisabled == null) {
            drawableDisabled = Sprites.btnDisabled;
        }
        if (drawableFocused == null) {
            drawableFocused = Sprites.btnPressed;
        }
    }

    public void setDraggable() {
        setDraggable(true);
    }

    public void setDraggable(boolean value) {
        draggableButton = value;
    }

    @Override
    protected void skinIsChanged() {
        drawableNormal = null;

        drawablePressed = null;

        drawableDisabled = null;
        mFont = null;
        lblTxt = null;
        this.removeChilds();
    }

    public String getText() {
        if (lblTxt != null)
            return lblTxt.getText();
        return null;
    }

    public float getTextWidth() {
        return lblTxt.getTextWidth(); // + getWidth() - getInnerWidth();
    }

    public void setText(String Text) {
        setText(Text, null, null);
    }

    public void performClick() {
        click(0, 0, 0, 0);

    }

    public void setEnable(boolean value) {
        isDisabled = !value;
    }

    @Override
    public void setWidth(float Width) {
        super.setWidth(Width);
        setText(getText());
    }

    @Override
    public void setHeight(float Height) {
        super.setHeight(Height);
        setText(getText());
    }

    public void setFocus(boolean value) {
        isFocused = value;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

}

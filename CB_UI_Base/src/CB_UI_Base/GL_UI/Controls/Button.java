/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.ButtonSprites;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.MoveableList;

/**
 * 
 * @author Longri
 *
 */
public class Button extends CB_View_Base {
    protected BitmapFont mFont;

    protected Drawable drawableNormal;
    protected Drawable drawablePressed;
    protected Drawable drawableDisabled;
    protected Drawable drawableFocused;
    protected MoveableList<Drawable> DrawableOverlayList = new MoveableList<Drawable>();

    protected boolean isFocused = false;
    protected boolean isPressed = false;
    protected boolean isDisabled = false;
    protected Label lblTxt;
    protected boolean dragableButton = false;

    public Button(float X, float Y, float Width, float Height, String Name) {
	super(X, Y, Width, Height, Name);
	this.setClickable(true);
    }

    public Button(CB_RectF rec, GL_View_Base parent, String name) {
	super(rec, parent, name);
	this.setClickable(true);
    }

    public Button(GL_View_Base parent, String name) {
	super(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight(), parent, name);
	this.setClickable(true);
    }

    public Button(String text) {
	super(0, 0, UI_Size_Base.that.getButtonWidthWide(), UI_Size_Base.that.getButtonHeight(), "Button " + text);
	this.setText(text);
	this.setClickable(true);
    }

    public Button(CB_RectF rec, String name) {
	super(rec, name);
	this.setClickable(true);
    }

    public Button(CB_RectF cb_RectF, OnClickListener onClik) {
	this(cb_RectF, "");
	this.setOnClickListener(onClik);
    }

    public void setninePatch(Drawable drawable) {
	drawableNormal = drawable;
    }

    public void setninePatchPressed(Drawable drawable) {
	drawablePressed = drawable;
    }

    public void setninePatchDisabled(Drawable drawable) {
	drawableDisabled = drawable;
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

    public void addOverlayDrawable(Drawable drawable) {
	DrawableOverlayList.add(drawable);
    }

    public void removeOverlayDrawable(Drawable drawable) {
	DrawableOverlayList.remove(drawable);
    }

    /**
     * render
     */
    @Override
    protected void render(Batch batch) {
	if (dragableButton) {
	    if (isPressed && !GL.getIsTouchDown()) {
		isPressed = false;
		GL.that.renderOnce();
	    }
	}

	if (!isPressed && !isDisabled && !isFocused) {
	    if (drawableNormal != null) {
		drawableNormal.draw(batch, 0, 0, getWidth(), getHeight());
	    } else {
		Initial();
		GL.that.renderOnce();
	    }
	} else if (isPressed) {
	    if (drawablePressed != null) {
		drawablePressed.draw(batch, 0, 0, getWidth(), getHeight());
	    } else {
		Initial();
		GL.that.renderOnce();
	    }
	} else if (isFocused) {
	    if (drawableFocused != null) {
		drawableFocused.draw(batch, 0, 0, getWidth(), getHeight());
	    } else {
		Initial();
		GL.that.renderOnce();
	    }
	} else {
	    if (drawableDisabled != null) {
		drawableDisabled.draw(batch, 0, 0, getWidth(), getHeight());
	    } else {
		Initial();
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
	return dragableButton ? false : true;
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
	return dragableButton ? false : true;
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
	}

	else

	    // log.debug("Button " + this.name + " Clicked");
	    return super.click(x, y, pointer, button);
    }

    public void setText(String Text, Color color) {
	setText(Text, null, color);
    }

    public void setText(String Text) {
	setText(Text, null, null);
    }

    public void setText(String Text, BitmapFont font, Color color) {
	setText(Text, font, color, HAlignment.CENTER);
    }

    public void setText(String Text, BitmapFont font, Color color, HAlignment alignment) {

	// ? no change
	if (lblTxt != null)
	    if (lblTxt.mText.equals(Text))
		if (lblTxt.mFont.equals(font))
		    if (lblTxt.mColor.equals(color))
			if (lblTxt.mHAlignment.equals(alignment))
			    return;

	// no text -> remove label
	if (Text == null || Text.equals("")) {
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
	lblTxt = new Label(Text, mFont, color, WrapType.WRAPPED).setHAlignment(alignment);
	this.initRow(BOTTOMUP);
	this.addLast(lblTxt);

	GL.that.renderOnce();
    }

    @Override
    protected void Initial() {
	if (drawableNormal == null) {
	    drawableNormal = SpriteCacheBase.btn;
	}
	if (drawablePressed == null) {
	    drawablePressed = SpriteCacheBase.btnPressed;
	}
	if (drawableDisabled == null) {
	    drawableDisabled = SpriteCacheBase.btnDisabled;
	}
	if (drawableFocused == null) {
	    drawableFocused = SpriteCacheBase.btnPressed;
	}

    }

    public void setDraggable() {
	setDraggable(true);
    }

    public void setDraggable(boolean value) {
	dragableButton = value;
    }

    @Override
    protected void SkinIsChanged() {
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

    public boolean isFocused() {
	return isFocused;
    }

}

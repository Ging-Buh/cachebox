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
package de.droidcachebox.gdx.activities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.ColorPickerRec;
import de.droidcachebox.gdx.controls.GradiantFilledRectangle;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.graphics.ColorDrawable;
import de.droidcachebox.gdx.graphics.GradiantFill;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

public class ColorPicker extends ActivityBase {
    private HSV_Color initialColor;
    private HSV_Color currentColor;
    private CB_Button btnOK;

    private IColorCallBack colorCallBack;

    private ColorDrawable actColorDrawable;

    private Box lastColorBox;
    private Box actColorBox;
    private ColorPickerRec viewSatVal;

    private Image viewCursor;
    private Image viewTarget;

    private Image viewHue;
    private GradiantFill gradientWhite;
    private GradiantFill gradientBlack;

    public ColorPicker(Color color, IColorCallBack theColorCallBack) {
        super("ColorPicker");
        currentColor = new HSV_Color(color);
        initialColor = new HSV_Color(color);

        colorCallBack = theColorCallBack;
        setClickable(true);
        createOkCancelBtn();
        createColorPreviewLine();
        createViewHue();
        createTest();

        hueChanged();

        moveCursor();
        moveTarget();

    }

    private void createOkCancelBtn() {
        btnOK = new CB_Button(leftBorder, bottomBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");
        CB_Button btnCancel = new CB_Button(btnOK.getMaxX(), bottomBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        // Translations
        btnOK.setText(Translation.get("ok"));
        btnCancel.setText(Translation.get("cancel"));

        addChild(btnOK);
        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            activityBase.finish();
            if (colorCallBack != null)
                colorCallBack.returnColor(currentColor);
            return true;
        });

        addChild(btnCancel);
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (colorCallBack != null)
                colorCallBack.returnColor(null);
            finish();
            return true;
        });

    }

    @Override
    public void dispose() {
        gradientWhite.dispose();
        gradientBlack.dispose();
    }

    private void createColorPreviewLine() {
        CB_RectF rec = new CB_RectF(0, btnOK.getMaxY() + margin, UiSizes.getInstance().getButtonWidthWide(), UiSizes.getInstance().getButtonHeight());
        lastColorBox = new Box(rec, "LastColor");
        actColorBox = new Box(rec, "aktColor");

        rec.setWidth(rec.getHeight());
        Image arrow = new Image(rec, "arrowImage", false);
        arrow.setDrawable(new SpriteDrawable(Sprites.Arrows.get(11)));

        float lineWidth = lastColorBox.getWidth() + margin + arrow.getWidth() + margin + actColorBox.getWidth();
        float left = getHalfWidth() - (lineWidth / 2);
        lastColorBox.setX(left);
        arrow.setX(lastColorBox.getMaxX() + margin);
        actColorBox.setX(arrow.getMaxX() + margin);

        lastColorBox.setBackground(new ColorDrawable(initialColor));
        actColorBox.setBackground(new ColorDrawable(initialColor));

        addChild(lastColorBox);
        addChild(arrow);
        addChild(actColorBox);
    }

    private void createViewHue() {
        float vWidth = btnOK.getHeight();

        viewHue = new Image(getWidth() - rightBorder - margin - vWidth, actColorBox.getMaxY() + margin, vWidth, getHeight() - getTopHeight() - actColorBox.getMaxY() - margin * 2, "viewHue", false);
        viewHue.setDrawable(new SpriteDrawable(Sprites.ambilwarna_hue));
        addChild(viewHue);

        float cursorSize = Fonts.measure("T").height;

        viewCursor = new Image(0, 0, cursorSize, cursorSize, "", false);
        viewCursor.setDrawable(new SpriteDrawable(Sprites.ambilwarna_cursor));
        addChild(viewCursor);

    }

    private void createTest() {
        CB_RectF rec = new CB_RectF(leftBorder + margin, viewHue.getY(), viewHue.getX() - margin * 3 - leftBorder, viewHue.getHeight());

        viewSatVal = new ColorPickerRec(rec, "");
        addChild(viewSatVal);

        {
            // Gradiant Test

            // Color blackTransparent = new Color(1f, 1f, 0f, 0.2f);
            // gradiantBlack = new GradiantFill(Color.BLACK, blackTransparent, -30);
            // rectangle FillRecBlack = new rectangle(rec, gradiantBlack);
            // addChild(FillRecBlack);
        }

        Color whiteTransparent = new Color(1f, 1f, 1f, 0f);
        gradientWhite = new GradiantFill(Color.WHITE, whiteTransparent, 0);
        GradiantFilledRectangle FillRecWhite = new GradiantFilledRectangle(rec, gradientWhite);
        addChild(FillRecWhite);

        Color blackTransparent = new Color(0f, 0f, 0f, 0f);
        gradientBlack = new GradiantFill(Color.BLACK, blackTransparent, 90);
        GradiantFilledRectangle FillRecBlack = new GradiantFilledRectangle(rec, gradientBlack);
        addChild(FillRecBlack);

        float cursorSize = Fonts.measure("T").height;

        viewTarget = new Image(0, 0, cursorSize, cursorSize, "", false);
        viewTarget.setDrawable(new SpriteDrawable(Sprites.ambilwarna_target));
        addChild(viewTarget);

    }

    private void hueChanged() {
        if (viewSatVal != null)
            viewSatVal.setHue(currentColor.getHue());
    }

    protected void moveCursor() {
        float y = viewHue.getHeight() - (getHue() * viewHue.getHeight() / 360.f);
        if (y == viewHue.getHeight())
            y = 0.f;

        viewCursor.setX((float) (viewHue.getX() - Math.floor(viewCursor.getWidth() / 2)));

        viewCursor.setY((float) (viewHue.getMaxY() - y - Math.floor(viewCursor.getHeight() / 2)));

    }

    protected void moveTarget() {
        float x = getSat() * viewSatVal.getWidth();
        float y = getVal() * viewSatVal.getHeight();

        viewTarget.setX((float) (viewSatVal.getX() + x - Math.floor(viewTarget.getWidth() / 2)));
        viewTarget.setY((float) (viewSatVal.getY() + y - Math.floor(viewTarget.getHeight() / 2)));

    }

    private float getHue() {
        return currentColor.getHue();
    }

    private void setHue(float hue) {
        currentColor.setHue(hue);
    }

    private float getSat() {
        return currentColor.getSat();
    }

    private void setSat(float sat) {
        currentColor.setSat(sat);
    }

    private float getVal() {
        return currentColor.getVal();
    }

    private void setVal(float val) {
        currentColor.setVal(val);
    }

    private void onClick_DracgHueView(float y) {
        if (y < 0.f)
            y = 0.f;
        if (y > viewHue.getHeight())
            y = viewHue.getHeight() - 0.001f; // to avoid looping from end to start.
        float hue = 360.f / viewHue.getHeight() * y;
        if (hue == 360.f)
            hue = 0.f;
        setHue(hue);

        // update view
        viewSatVal.setHue(getHue());
        moveCursor();
        regenarateActColorBox();
    }

    private void onClickDragg_Sat(float x, float y) {
        if (x < 0.f)
            x = 0.f;
        if (x > viewSatVal.getWidth())
            x = viewSatVal.getWidth();
        if (y < 0.f)
            y = 0.f;
        if (y > viewSatVal.getHeight())
            y = viewSatVal.getHeight();

        setSat(1.f / viewSatVal.getWidth() * x);
        setVal(1.f / viewSatVal.getHeight() * y);

        // update view
        moveTarget();
        regenarateActColorBox();
    }

    private void regenarateActColorBox() {

        GL.that.runOnGL(() -> {
            if (actColorDrawable == null) {
                actColorDrawable = new ColorDrawable(currentColor);
            } else {
                actColorDrawable.setColor(currentColor);
            }

            actColorBox.setBackground(actColorDrawable);
        });

    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {

        if (viewHue.contains(x, y)) {
            onClick_DracgHueView(y - viewHue.getY());
            return true;
        }

        if (viewSatVal.contains(x, y)) {
            onClickDragg_Sat(x - viewSatVal.getX(), y - viewSatVal.getY());
            return true;
        }

        return super.click(x, y, pointer, button);
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

        if (!KineticPan && viewHue.contains(x, y)) {
            onClick_DracgHueView(y - viewHue.getY());
            return true;
        }

        if (!KineticPan && viewSatVal.contains(x, y)) {
            onClickDragg_Sat(x - viewSatVal.getX(), y - viewSatVal.getY());
            return true;
        }

        return false;
    }

    public void setColor(Color color) {
        currentColor = initialColor = new HSV_Color(color);
        hueChanged();

        moveCursor();
        moveTarget();
        lastColorBox.setBackground(new ColorDrawable(initialColor));
    }

    public interface IColorCallBack {
        void returnColor(Color color);
    }

}

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
package de.droidcachebox.gdx.controls.dialogs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.translation.Translation;

public class ButtonDialog extends Dialog {
    public static final int BTN_LEFT_POSITIVE = 1;
    public static final int BTN_MIDDLE_NEUTRAL = 2;
    public static final int BTN_RIGHT_NEGATIVE = 3;
    public ButtonClickHandler buttonClickHandler;
    protected CB_Button btnLeftPositive;
    protected CB_Button btnMiddleNeutral;
    protected CB_Button btnRightNegative;
    protected CB_Label msgLbl;
    protected Image iconImage;
    protected SettingBool rememberSetting = null;
    private MsgBoxIcon icon;
    private MsgBoxButton buttons;
    private String msg;
    private OnClickListener btnLeftPositiveClickListener;
    private OnClickListener btnMiddleNeutralClickListener;
    private OnClickListener btnRightNegativeClickListener;

    public ButtonDialog(String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon) {
        this(calcMsgBoxSize(msg, (title != null && title.length() > 0), (buttons != MsgBoxButton.NOTHING), false).getBounds().asFloat(), msg, title, buttons, icon, null);
    }

    public ButtonDialog(String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon, ButtonClickHandler listener, SettingBool remember) {
        super(calcMsgBoxSize(msg, (title != null && title.length() > 0), (buttons != MsgBoxButton.NOTHING), false).getBounds().asFloat(), "ButtonDialog");
        this.title = title;
        this.msg = msg;
        this.buttons = buttons;
        this.icon = icon;
        rememberSetting = remember;
        buttonClickHandler = listener;
        construct();
    }

    public ButtonDialog(CB_RectF cbRectF, String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon, ButtonClickHandler listener) {
        super(cbRectF, "ButtonDialog");
        this.title = title;
        this.msg = msg;
        this.buttons = buttons;
        this.icon = icon;
        buttonClickHandler = listener;
        construct();
    }

    /**
     * for Menu Extension
     *
     * @param rec  .
     * @param Name .
     */
    public ButtonDialog(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    private void construct() {
        setTitle(title);
        setButtonCaptions(buttons);
        SizeF contentSize = getContentSize();

        float labelXPos = 0; // or margin
        CB_RectF imageRec = new CB_RectF(0, contentSize.getHeight() - margin - UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
        if (icon != MsgBoxIcon.None && icon != null) {
            iconImage = new Image(imageRec, "MsgBoxIcon", false);
            iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
            addChild(iconImage);
            labelXPos = imageRec.getMaxX() + margin;
        }

        ScrollBox scrollBox = new ScrollBox(contentSize.getBounds());
        scrollBox.setX(labelXPos);
        scrollBox.setY(0);
        scrollBox.setWidth(scrollBox.getWidth() - labelXPos); // or - rightBorder to use full width of contentBox
        scrollBox.initRow(BOTTOMUp);

        msgLbl = new CB_Label(contentSize.getBounds());
        scrollBox.addLast(msgLbl);
        msgLbl.setWrappedText(msg);
        float labelHeight = msgLbl.getTextHeight();
        msgLbl.setHeight(labelHeight);

        scrollBox.setVirtualHeight(labelHeight);
        addChild(scrollBox);

    }

    public void show() {
        if (rememberSetting != null) {
            if (rememberSetting.getValue()) {
                // because of isRemembered we don't show the MsgBox.
                // but we call the OnMsgBoxClickListener.onClick for positive Button
                if (buttonClickHandler != null) {
                    buttonClickHandler.onClick(BTN_LEFT_POSITIVE, null);
                }
                return;
            }
        }
        GL.that.runOnGLWithThreadCheck(() -> GL.that.showDialog(this));
    }

    public void close() {
        GL.that.runOnGLWithThreadCheck(() -> GL.that.closeDialog(this));
    }

    public void setMessage(String text) {
        msgLbl.setWrappedText(text);
    }

    /**
     * without translation
     * @param number .number of Button (BTN_LEFT_POSITIVE, BTN_MIDDLE_NEUTRAL, BTN_RIGHT_NEGATIVE)
     * @param text .
     */
    public void setButtonText(int number, String text) {
        switch (number) {
            case BTN_LEFT_POSITIVE:
                btnLeftPositive.setText(text);
                break;
            case BTN_MIDDLE_NEUTRAL:
                btnMiddleNeutral.setText(text);
                break;
            case BTN_RIGHT_NEGATIVE:
                btnRightNegative.setText(text);
        }
    }

    public void setButtonText(String left, String middle, String right) {
        if (left != null)
            btnLeftPositive.setText(Translation.get(left));
        if (middle != null)
            btnMiddleNeutral.setText(Translation.get(middle));
        if (right != null)
            btnRightNegative.setText(Translation.get(right));
    }

    public void setButtonCaptions(MsgBoxButton buttons) {

        btnLeftPositiveClickListener = (v, x, y, pointer, button) -> ButtonClick(1);
        btnMiddleNeutralClickListener = (v, x, y, pointer, button) -> ButtonClick(2);
        btnRightNegativeClickListener = (v, x, y, pointer, button) -> ButtonClick(3);

        if (buttons == null)
            buttons = MsgBoxButton.NOTHING;

        if (buttons == MsgBoxButton.AbortRetryIgnore) {
            createButtons(3, Translation.get("abort"), Translation.get("retry"), Translation.get("ignore"));
        } else if (buttons == MsgBoxButton.OK) {
            createButtons(1, Translation.get("ok"), "", "");
        } else if (buttons == MsgBoxButton.OKCancel) {
            createButtons(2, Translation.get("ok"), "", Translation.get("cancel"));
        } else if (buttons == MsgBoxButton.RetryCancel) {
            createButtons(2, Translation.get("retry"), "", Translation.get("cancel"));
        } else if (buttons == MsgBoxButton.YesNo) {
            createButtons(2, Translation.get("yes"), "", Translation.get("no"));
        } else if (buttons == MsgBoxButton.YesNoCancel) {
            createButtons(3, Translation.get("yes"), Translation.get("no"), Translation.get("cancel"));
        } else if (buttons == MsgBoxButton.Cancel) {
            createButtons(3, "", "", Translation.get("cancel"));
            btnLeftPositive.setInvisible();
            btnMiddleNeutral.setInvisible();
        } else {
            // no Buttons
            setFooterHeight(calcFooterHeight(false));
        }
    }

    protected void createButtons(int nrOfButtons, String t1, String t2, String t3) {

        this.setBorders(margin, margin);
        this.setMargins(margin, margin);
        this.initRow(BOTTOMUp);

        switch (nrOfButtons) {
            case 1:
                btnLeftPositive = new CB_Button(t1);
                this.addLast(btnLeftPositive);
                btnLeftPositive.setClickHandler(btnLeftPositiveClickListener);
                break;
            case 2:
                btnLeftPositive = new CB_Button(t1);
                btnRightNegative = new CB_Button(t3);
                this.addNext(btnLeftPositive);
                this.addLast(btnRightNegative);
                btnLeftPositive.setClickHandler(btnLeftPositiveClickListener);
                btnRightNegative.setClickHandler(btnRightNegativeClickListener);
                break;
            case 3:
                btnLeftPositive = new CB_Button(t1);
                btnMiddleNeutral = new CB_Button(t2);
                btnRightNegative = new CB_Button(t3);
                this.addNext(btnLeftPositive);
                this.addNext(btnMiddleNeutral);
                this.addLast(btnRightNegative);
                btnLeftPositive.setClickHandler(btnLeftPositiveClickListener);
                btnMiddleNeutral.setClickHandler(btnMiddleNeutralClickListener);
                btnRightNegative.setClickHandler(btnRightNegativeClickListener);
                break;
        }

        if (rememberSetting != null) {
            Box rememberBox = new Box(this, "rememberBox");
            CB_CheckBox chkRemember = new CB_CheckBox();
            rememberBox.setBorders(chkRemember.getHeight() / 2f, 0);
            rememberBox.setMargins(chkRemember.getHeight() / 2f, 0);
            chkRemember.setChecked(rememberSetting.getValue());
            chkRemember.setClickHandler((view, x, y, pointer, button) -> {
                rememberSetting.setValue(chkRemember.isChecked());
                return true;
            });
            rememberBox.addNext(chkRemember, chkRemember.getHeight() * 2f / getWidth());
            CB_Label lbl = new CB_Label(Translation.get("remember"));
            rememberBox.addLast(lbl);
            rememberBox.adjustHeight();
            addLast(rememberBox);
        }

        setFooterHeight(this.getHeightFromBottom());

    }

    private boolean ButtonClick(int button) {
        Object _data = this.getData();
        boolean ret = false;
        if (buttonClickHandler != null) {
            ret = buttonClickHandler.onClick(button, _data);
        }
        GL.that.closeDialog(this);
        return ret;
    }

    private Sprite getIcon(MsgBoxIcon msgIcon) {
        if (msgIcon == null)
            return null;
        Sprite icon;
        switch (msgIcon.ordinal()) {
            case 0:
            case 4:
                icon = Sprites.getSprite(IconName.infoIcon.name());
                break;
            case 1:
            case 3:
            case 7:
                icon = Sprites.getSprite(IconName.closeIcon.name());
                break;
            case 2:
            case 8:
                icon = Sprites.getSprite(IconName.warningIcon.name());
                break;
            case 6:
                icon = Sprites.getSprite(IconName.helpIcon.name());
                break;
            case 9:
            case 10:
                icon = Sprites.getSprite(IconName.dayGcLiveIcon.name());
                break;
            default:
                icon = null;
        }
        return icon;
    }

    public void setButtonClickHandler(ButtonClickHandler buttonClickHandler) {
        this.buttonClickHandler = buttonClickHandler;
    }

    public interface ButtonClickHandler {
        /**
         * This method will be invoked when a button is clicked.
         *
         * @param btnNumber The button that was clicked (BUTTON_POSITIVE, ...)
         */
        boolean onClick(int btnNumber, Object data);
    }
}

/*
 * Copyright (C) 2014-2015 team-cachebox.de
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
package de.droidcachebox.gdx.controls.messagebox;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.settings.SettingBool;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.StringH;

import java.util.ArrayList;
import java.util.Iterator;

public class MessageBox extends Dialog {
    public static final int BTN_LEFT_POSITIVE = 1;
    public static final int BTN_MIDDLE_NEUTRAL = 2;
    public static final int BTN_RIGHT_NEGATIVE = 3;
    protected CB_Label label;
    protected CB_Button btnLeftPositive;
    protected CB_Button btnMiddleNeutral;
    protected CB_Button btnRightNegative;
    protected SettingBool rememberSetting = null;
    protected CB_CheckBox chkRemember;
    private OnClickListener btnLeftPositiveClickListener;
    private OnClickListener btnMiddleNeutralClickListener;
    private OnClickListener btnRightNegativeClickListener;
    private OnMsgBoxClickListener mMsgBoxClickListener;
    private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

    public MessageBox(Size size, String name) {
        super(size.getBounds().asFloat(), name);
    }

    public static MessageBox show(String msg) {
        MessageBox msgBox = new MessageBox(calcMsgBoxSize(msg, false, true, false), "MsgBox");
        msgBox.addButtons(MessageBoxButtons.OK);
        msgBox.label = new CB_Label(msgBox.getContentSize().getBounds());
        msgBox.label.setZeroPos(); // .getTextHeight()
        msgBox.label.setWrappedText(msg);
        msgBox.addChild(msgBox.label);
        GL.that.RunOnGL(() -> GL.that.showDialog(msgBox));
        return msgBox;
    }

    public static MessageBox show(String msg, OnMsgBoxClickListener Listener) {
        return show(msg, "", Listener);
    }

    public static MessageBox show(String msg, String title, OnMsgBoxClickListener Listener) {
        return show(msg, title, MessageBoxButtons.OK, Listener, null);
    }

    public static MessageBox show(String msg, String title, MessageBoxIcon icon) {
        return show(msg, title, MessageBoxButtons.OK, icon, null, null);
    }

    public static MessageBox show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {
        return show(msg, title, buttons, icon, Listener, null);
    }

    public static MessageBox show(String msg, String title, MessageBoxButtons buttons, OnMsgBoxClickListener Listener, SettingBool remember) {

        if (remember != null && remember.getValue()) {
            // wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
            // Wir liefern nur ein On Click auf den OK Button zur�ck!
            if (Listener != null) {
                Listener.onClick(BTN_LEFT_POSITIVE, null);
            }
            return null;
        }

        MessageBox msgBox = new MessageBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), false, (remember != null)), "MsgBox" + title);
        msgBox.rememberSetting = remember;
        msgBox.mMsgBoxClickListener = Listener;
        msgBox.addButtons(buttons);
        msgBox.setTitle(title);

        msgBox.label = new CB_Label();
        msgBox.label.setWrappedText(msg);
        float labelHeight = msgBox.label.getTextHeight();
        msgBox.label.setHeight(labelHeight);

        ScrollBox scrollBox = new ScrollBox(msgBox.getContentSize().getBounds());
        scrollBox.initRow(BOTTOMUP);
        scrollBox.setVirtualHeight(labelHeight);
        scrollBox.addLast(msgBox.label);

        msgBox.addChild(scrollBox);

        GL.that.RunOnGL(() -> GL.that.showDialog(msgBox));

        return msgBox;
    }

    public static MessageBox show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener, SettingBool remember) {

        if (remember != null && remember.getValue()) {
            // wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
            // Wir liefern nur ein On Click auf den OK Button zurück!
            if (Listener != null) {
                Listener.onClick(BTN_LEFT_POSITIVE, null);
            }
            return null;
        }

        // nur damit bei mir die Box maximiert kommt und damit der Text nicht skaliert.
        // !!! gilt für alle Dialoge, da statisch definiert. Könnte es auch dort ändern.
        Dialog.margin = 5;
        final MessageBox msgBox = new MessageBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), true, (remember != null)), "MsgBox" + title);

        msgBox.rememberSetting = remember;
        msgBox.mMsgBoxClickListener = Listener;
        msgBox.setTitle(title);

        msgBox.addButtons(buttons);

        SizeF contentSize = msgBox.getContentSize();

        CB_RectF imageRec = new CB_RectF(0, contentSize.getHeight() - margin - UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());

        Image iconImage = new Image(imageRec, "MsgBoxIcon", false);
        if (icon != MessageBoxIcon.None)
            iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
        msgBox.addChild(iconImage);

        msgBox.label = new CB_Label(contentSize.getBounds());
        msgBox.label.setWidth(contentSize.getBounds().getWidth() - 5 - UiSizes.getInstance().getButtonHeight());
        msgBox.label.setPos(imageRec.getMaxX() + 5, 0);
        msgBox.label.setWrappedText(msg);
        msgBox.addChild(msgBox.label);

        GL.that.RunOnGL(() -> GL.that.showDialog(msgBox));

        return msgBox;
    }

    public static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon) {
        return calcMsgBoxSize(Text, hasTitle, hasButtons, hasIcon, false);
    }

    private static Sprite getIcon(MessageBoxIcon msgIcon) {

        Sprite icon = null;

        try {
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
        } catch (Exception ignored) {
        }

        return icon;
    }

    protected void addButtons(MessageBoxButtons buttons) {
        switch (buttons) {
            case OK:
                createButtons(1, "ok", null, null);
                break;
            case YesNo:
                createButtons(2, "yes", null, "no");
                break;
            case OKCancel:
                createButtons(2, "ok", null, "cancel");
                break;
            case Cancel:
                createButtons(2, "", null, "cancel");
                btnLeftPositive.setInvisible();
                break;
            case NOTHING:
                this.setFooterHeight(calcFooterHeight(false));
                break;
            case YesNoCancel:
                createButtons(3, "yes", "no", "cancel");
                break;
            case YesNoRetry:
                createButtons(3, "yes", "no", "retry");
                break;
            case AbortRetryIgnore:
                createButtons(3, "abort", "retry", "ignore");
                break;
            case RetryCancel:
                createButtons(2, "retry", null, "cancel");
                break;
        }
    }

    protected void addButtons(String left, String middle, String right) {
        int anzahl;
        if (middle == null) {
            if (right == null) {
                anzahl = -1;
            } else {
                anzahl = -2;
            }
        } else {
            anzahl = -3;
        }
        createButtons(anzahl, left, middle, right);
    }

    private void createButtons(int anzahl, String left, String middle, String right) {
        initRow(BOTTOMUP, margin);
        setBorders(margin, margin);
        if (anzahl > 0) {
            left = Translation.get(left);
            if (anzahl > 1 && !StringH.isEmpty(right)) right = Translation.get(right);
            if (anzahl > 2) middle = Translation.get(middle);
        } else {
            anzahl = -1 * anzahl;
        }
        switch (anzahl) {
            case 1:
                btnLeftPositive = new CB_Button(left);
                addLast(btnLeftPositive);
                break;
            case 2:
                btnLeftPositive = new CB_Button(left);
                btnRightNegative = new CB_Button(right);
                addNext(btnLeftPositive);
                addLast(btnRightNegative);
                break;
            case 3:
                btnLeftPositive = new CB_Button(left);
                btnMiddleNeutral = new CB_Button(middle);
                btnRightNegative = new CB_Button(right);
                addNext(btnLeftPositive);
                addNext(btnMiddleNeutral);
                addLast(btnRightNegative);
                break;
        }

        setButtonListener();

        if (rememberSetting != null) {
            chkRemember = new CB_CheckBox("remember");
            setBorders(chkRemember.getHeight() / 2f, 0);
            setMargins(chkRemember.getHeight() / 2f, 0);
            addNext(chkRemember, chkRemember.getHeight() * 2f / getWidth());
            CB_Label lbl = new CB_Label("lbl");
            addLast(lbl);

            chkRemember.setChecked(rememberSetting.getValue());
            lbl.setText(Translation.get("remember"));
        }

        setFooterHeight(getHeightFromBottom());
    }

    private void setButtonListener() {
        if (btnLeftPositive != null) {
            if (btnLeftPositiveClickListener == null)
                btnLeftPositiveClickListener = (v, x, y, pointer, button) -> handleButtonClick(MessageBox.BTN_LEFT_POSITIVE);
            btnLeftPositive.setClickHandler(btnLeftPositiveClickListener);
        }
        if (btnMiddleNeutral != null) {
            if (btnMiddleNeutralClickListener == null)
                btnMiddleNeutralClickListener = (v, x, y, pointer, button) -> handleButtonClick(MessageBox.BTN_MIDDLE_NEUTRAL);
            btnMiddleNeutral.setClickHandler(btnMiddleNeutralClickListener);
        }
        if (btnRightNegative != null) {
            if (btnRightNegativeClickListener == null)
                btnRightNegativeClickListener = (v, x, y, pointer, button) -> handleButtonClick(MessageBox.BTN_RIGHT_NEGATIVE);
            btnRightNegative.setClickHandler(btnRightNegativeClickListener);
        }
    }

    private boolean handleButtonClick(int button) {
        if (rememberSetting != null) {
            if (chkRemember.isChecked()) {
                rememberSetting.setValue(true);
            }
        }

        boolean retValue = false;
        if (mMsgBoxClickListener != null) {
            retValue = mMsgBoxClickListener.onClick(button, data);
        }
        GL.that.closeDialog(this);
        return retValue;
    }

    public void setButtonText(int number, String text) {
        switch (number) {
            case 1:
                btnLeftPositive.setText(text);
                break;
            case 2:
                btnMiddleNeutral.setText(text);
                break;
            case 3:
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

    public CB_Button getButton(int number) {
        switch (number) {
            case 1:
                return btnLeftPositive;
            case 2:
                return btnMiddleNeutral;
        }
        return btnRightNegative;
    }

    public void setMessage(String message) {
        label.setWrappedText(message);
    }

    public void close() {
        GL.that.closeDialog(this);
    }

    public boolean finish() {
        GL.that.closeDialog(this);
        return true;
    }

    @Override
    protected void initialize() {
        if (isDisposed())
            return;
        super.initialize();
        synchronized (childs) {
            for (Iterator<CB_View_Base> iterator = FooterItems.iterator(); iterator.hasNext(); ) {
                childs.add(iterator.next());
            }
        }
    }

    @Override
    public void dispose() {
        //Log.debug(log, "Dispose MessageBox=> " + name);

        if (FooterItems != null) {
            for (CB_View_Base t : FooterItems) {
                t.dispose();
                t = null;
            }
            FooterItems = null;
        }

        btnLeftPositive = null;
        btnMiddleNeutral = null;
        btnRightNegative = null;
        mMsgBoxClickListener = null;
        btnLeftPositiveClickListener = null;
        btnMiddleNeutralClickListener = null;
        btnRightNegativeClickListener = null;

        label = null;

        rememberSetting = null;
        chkRemember = null;

        super.dispose();
    }

    public void setMsgBoxClickListener(OnMsgBoxClickListener listener) {
        mMsgBoxClickListener = listener;
    }

    public void setPositiveClickListener(OnClickListener listener) {
        btnLeftPositiveClickListener = listener;
        btnLeftPositive.setClickHandler(listener);
    }

    public void setMiddleNeutralClickListener(OnClickListener listener) {
        btnMiddleNeutralClickListener = listener;
        btnMiddleNeutral.setClickHandler(listener);
    }

    public void setRightNegativeClickListener(OnClickListener listener) {
        btnRightNegativeClickListener = listener;
        btnRightNegative.setClickHandler(listener);
    }

    public interface OnMsgBoxClickListener {
        /**
         * This method will be invoked when a button is clicked.
         *
         * @param btnNumber The button that was clicked (BUTTON_POSITIVE, ...)
         */
        boolean onClick(int btnNumber, Object data);
    }

}

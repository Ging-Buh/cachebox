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
package CB_UI_Base.GL_UI.Controls.MessageBox;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.*;
import CB_Utils.Settings.SettingBool;
import CB_Utils.StringH;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;
import java.util.Iterator;

public class MessageBox extends Dialog {
    public static final int BUTTON_POSITIVE = 1;
    public static final int BUTTON_NEUTRAL = 2;
    public static final int BUTTON_NEGATIVE = 3;
    public static MessageBox that;
    private OnMsgBoxClickListener mMsgBoxClickListener;
    public OnClickListener positiveButtonClickListener;
    public OnClickListener neutralButtonClickListener;
    public OnClickListener negativeButtonClickListener;
    protected Label label;
    protected Button button1;
    protected Button button2;
    protected Button button3;
    protected SettingBool rememberSetting = null;
    protected ChkBox chkRemember;
    private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

    public MessageBox(Size size, String name) {
        super(size.getBounds().asFloat(), name);
        that = this;
    }

    protected void addButtons(MessageBoxButtons buttons) {
        if (buttons == MessageBoxButtons.OK) {
            createButtons(1,"ok",null, null);
        } else if (buttons == MessageBoxButtons.YesNo) {
            createButtons(2,"yes", null,"no");
        } else if (buttons == MessageBoxButtons.OKCancel) {
            createButtons(2,"ok", null,"cancel");
        } else if (buttons == MessageBoxButtons.Cancel) {
            createButtons(2,"", null,"cancel");
            button1.setInvisible();
        } else if (buttons == MessageBoxButtons.NOTHING) {
            this.setFooterHeight(calcFooterHeight(false));
        } else if (buttons == MessageBoxButtons.YesNoCancel) {
            createButtons(3,"yes","no","cancel");
        } else if (buttons == MessageBoxButtons.YesNoRetry) {
            createButtons(3,"yes","no","retry");
        } else if (buttons == MessageBoxButtons.AbortRetryIgnore) {
            createButtons(3,"abort","retry","ignore");
        } else if (buttons == MessageBoxButtons.RetryCancel) {
            createButtons(2,"retry", null,"cancel");
        }
    }

    public void addButtons(String left, String middle, String right) {
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
        setButtonListener();
        initRow(BOTTOMUP, margin);
        setBorders(margin, margin);
        if (anzahl > 0 ) {
             left = Translation.get(left);
             if (anzahl > 1 && !StringH.isEmpty(right)) right = Translation.get(right);
             if (anzahl > 2) middle = Translation.get(middle);
        }
        else {
            anzahl = -1  * anzahl;
        }
        switch (anzahl) {
            case 1:
                button1 = new Button(left);
                button1.setOnClickListener(positiveButtonClickListener);
                addLast(button1);
                break;
            case 2:
                button1 = new Button(left);
                button1.setOnClickListener(positiveButtonClickListener);
                button3 = new Button(right);
                button3.setOnClickListener(negativeButtonClickListener);
                addNext(button1);
                addLast(button3);
                break;
            case 3:
                button1 = new Button(left);
                button1.setOnClickListener(positiveButtonClickListener);
                button2 = new Button(middle);
                button2.setOnClickListener(neutralButtonClickListener);
                button3 = new Button(right);
                button3.setOnClickListener(negativeButtonClickListener);
                addNext(button1);
                addNext(button2);
                addLast(button3);
                break;
        }

        if (rememberSetting != null) {
            chkRemember = new ChkBox("remember");
            setBorders(chkRemember.getHeight() / 2f, 0);
            setMargins(chkRemember.getHeight() / 2f, 0);
            addNext(chkRemember, chkRemember.getHeight() * 2f / getWidth());
            Label lbl = new Label("lbl");
            addLast(lbl);

            chkRemember.setChecked(rememberSetting.getValue());
            lbl.setText(Translation.get("remember"));
        }

        setFooterHeight(getHeightFromBottom());
    }

    private void setButtonListener() {
        positiveButtonClickListener = (v, x, y, pointer, button) -> handleButtonClick(1);
        neutralButtonClickListener = (v, x, y, pointer, button) -> handleButtonClick(2);
        negativeButtonClickListener = (v, x, y, pointer, button) -> handleButtonClick(3);
    }

    private boolean handleButtonClick(int button) {
        // check for remember
        if (rememberSetting != null) {
            if (chkRemember.isChecked()) {
                rememberSetting.setValue(true);
                // todo automated save
            }
        }

        boolean retValue = false;
        if (mMsgBoxClickListener != null) {
            retValue = mMsgBoxClickListener.onClick(button, data);
        }
        GL.that.closeDialog(that);
        return retValue;
    }

    public void setButtonText(int number, String text) {
        switch (number) {
            case 1:
                button1.setText(text);
                break;
            case 2:
                button2.setText(text);
                break;
            case 3:
                button3.setText(text);
        }
    }

    public Button getButton(int number) {
        switch (number) {
            case 1:
                return button1;
            case 2:
                return button2;
        }
        return button3;
    }

    public void setMessage(String message) {
        label.setWrappedText(message);
    }

    public void close() {
        GL.that.closeDialog(that);
    }

    @Override
    protected void Initial() {
        if (isDisposed())
            return;
        super.Initial();
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

        button1 = null;
        button2 = null;
        button3 = null;
        mMsgBoxClickListener = null;
        positiveButtonClickListener = null;
        neutralButtonClickListener = null;
        negativeButtonClickListener = null;

        label = null;

        rememberSetting = null;
        chkRemember = null;

        super.dispose();
    }

    public void setMsgBoxClickListener(OnMsgBoxClickListener listener) {
        mMsgBoxClickListener = listener;
    }

    public interface OnMsgBoxClickListener {
        /**
         * This method will be invoked when a button is clicked.
         *
         * @param btnNumber The button that was clicked ( the position of the item clicked.
         * @return
         */
        boolean onClick(int btnNumber, Object data);
    }

    // the class ends here
    //==========================================================================================================================================================================

    public static MessageBox show(String msg) {
        MessageBox msgBox = new MessageBox(calcMsgBoxSize(msg, false, true, false), "MsgBox");
        msgBox.addButtons(MessageBoxButtons.OK);
        msgBox.label = new Label(msgBox.getContentSize().getBounds());
        msgBox.label.setZeroPos(); // .getTextHeight()
        msgBox.label.setWrappedText(msg);
        msgBox.addChild(msgBox.label);

        GL.that.showDialog(msgBox);
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
                Listener.onClick(BUTTON_POSITIVE, null);
            }
            return null;
        }

        MessageBox msgBox = new MessageBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), false, (remember != null)), "MsgBox" + title);
        msgBox.rememberSetting = remember;
        msgBox.mMsgBoxClickListener = Listener;
        msgBox.addButtons(buttons);
        msgBox.setTitle(title);

        msgBox.label = new Label();
        msgBox.label.setWrappedText(msg);
        float labelHeight = msgBox.label.getTextHeight();
        msgBox.label.setHeight(labelHeight);

        ScrollBox scrollBox = new ScrollBox(msgBox.getContentSize().getBounds());
        scrollBox.initRow(BOTTOMUP);
        scrollBox.setVirtualHeight(labelHeight);
        scrollBox.addLast(msgBox.label);

        msgBox.addChild(scrollBox);

        GL.that.showDialog(msgBox);
        return msgBox;
    }

    public static MessageBox show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener, SettingBool remember) {

        if (remember != null && remember.getValue()) {
            // wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
            // Wir liefern nur ein On Click auf den OK Button zurück!
            if (Listener != null) {
                Listener.onClick(BUTTON_NEGATIVE, null);
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

        CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

        Image iconImage = new Image(imageRec, "MsgBoxIcon", false);
        if (icon != MessageBoxIcon.None)
            iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
        msgBox.addChild(iconImage);

        msgBox.label = new Label(contentSize.getBounds());
        msgBox.label.setWidth(contentSize.getBounds().getWidth() - 5 - UI_Size_Base.that.getButtonHeight());
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
                    icon = Sprites.getSprite(IconName.infoIcon.name());
                    break;
                case 1:
                    icon = Sprites.getSprite(IconName.closeIcon.name());
                    break;
                case 2:
                    icon = Sprites.getSprite(IconName.warningIcon.name());
                    break;
                case 3:
                    icon = Sprites.getSprite(IconName.closeIcon.name());
                    break;
                case 4:
                    icon = Sprites.getSprite(IconName.infoIcon.name());
                    break;
                case 5:
                    icon = null;
                    break;
                case 6:
                    icon = Sprites.getSprite(IconName.helpIcon.name());
                    break;
                case 7:
                    icon = Sprites.getSprite(IconName.closeIcon.name());
                    break;
                case 8:
                    icon = Sprites.getSprite(IconName.warningIcon.name());
                    break;
                case 9:
                    icon = Sprites.getSprite(IconName.dayGcLiveIcon.name());
                    break;
                case 10:
                    icon = Sprites.getSprite(IconName.dayGcLiveIcon.name());
                    break;
                default:
                    icon = null;

            }
        } catch (Exception e) {
        }

        return icon;
    }
}

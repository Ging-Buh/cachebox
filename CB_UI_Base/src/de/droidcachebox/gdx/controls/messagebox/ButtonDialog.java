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
package de.droidcachebox.gdx.controls.messagebox;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.Dialog;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;
import java.util.Iterator;

public class ButtonDialog extends Dialog {

    private final ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

    public CB_Button button1;
    public CB_Button button2;
    public CB_Button button3;
    public MessageBox.OnMsgBoxClickListener mMsgBoxClickListener;
    protected CB_Label label;
    protected Object data;
    protected OnClickListener positiveButtonClickListener;
    protected OnClickListener neutralButtonClickListener;
    protected OnClickListener negativeButtonClickListener;

    public ButtonDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, MessageBox.OnMsgBoxClickListener Listener) {
        this(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), (icon != MessageBoxIcon.None), false).getBounds().asFloat(), Name, msg, title, buttons, icon, Listener);
    }

    public ButtonDialog(CB_RectF rec, String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, MessageBox.OnMsgBoxClickListener Listener) {
        super(rec, Name);
        setTitle(title);
        setButtonCaptions(buttons);
        SizeF contentSize = getContentSize();

        CB_RectF imageRec = new CB_RectF(0, contentSize.getHeight() - margin - UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());

        if (icon != MessageBoxIcon.None && icon != null) {
            Image iconImage = new Image(imageRec, "MsgBoxIcon", false);
            iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
            addChild(iconImage);
        }

        label = new CB_Label(contentSize.getBounds());
        label.setWidth(contentSize.getBounds().getWidth() - 5 - UiSizes.getInstance().getButtonHeight());
        label.setX(imageRec.getMaxX() + 5);
        label.setY(-margin);
        label.setWrappedText(msg);
        addChild(label);

        mMsgBoxClickListener = Listener;

        // setFooterHeight(80);
    }

    public ButtonDialog(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public static ButtonDialog show(String msg) {
        ButtonDialog msgBox = new ButtonDialog("MsgBox", msg, "Title", MessageBoxButtons.NOTHING, MessageBoxIcon.None, null);
        GL.that.showDialog(msgBox);
        return msgBox;
    }

    @Override
    protected void initialize() {
        super.initialize();
        synchronized (childs) {
            for (Iterator<CB_View_Base> iterator = FooterItems.iterator(); iterator.hasNext(); ) {
                childs.add(iterator.next());
            }
        }
    }

    public void setText(String text) {
        label.setWrappedText(text);

    }

    public void close() {
        GL.that.RunOnGL(() -> GL.that.closeDialog(this));
    }

    public void setButtonCaptions(MessageBoxButtons buttons) {
        if (buttons == null)
            buttons = MessageBoxButtons.NOTHING;

        if (buttons == MessageBoxButtons.AbortRetryIgnore) {
            createButtons(3, Translation.get("abort"), Translation.get("retry"), Translation.get("ignore"));
        } else if (buttons == MessageBoxButtons.OK) {
            createButtons(1, Translation.get("ok"), "", "");
        } else if (buttons == MessageBoxButtons.OKCancel) {
            createButtons(2, Translation.get("ok"), "", Translation.get("cancel"));
        } else if (buttons == MessageBoxButtons.RetryCancel) {
            createButtons(2, Translation.get("retry"), "", Translation.get("cancel"));
        } else if (buttons == MessageBoxButtons.YesNo) {
            createButtons(2, Translation.get("yes"), "", Translation.get("no"));
        } else if (buttons == MessageBoxButtons.YesNoCancel) {
            createButtons(3, Translation.get("yes"), Translation.get("no"), Translation.get("cancel"));
        } else if (buttons == MessageBoxButtons.Cancel) {
            createButtons(3, "", "", Translation.get("cancel"));
            button1.setInvisible();
            button2.setInvisible();
        } else {
            // no Buttons
            setFooterHeight(calcFooterHeight(false));
        }
    }

    protected void createButtons(int anzahl, String t1, String t2, String t3) {
        setButtonListener();

        this.setBorders(margin, margin);
        this.setMargins(margin, margin);
        this.initRow(BOTTOMUP);

        switch (anzahl) {
            case 1:
                button1 = new CB_Button(t1);
                this.addLast(button1);
                button1.setClickHandler(positiveButtonClickListener);
                // addFooterChild(button1);
                break;
            case 2:
                button1 = new CB_Button(t1);
                button3 = new CB_Button(t3);
                this.addNext(button1);
                this.addLast(button3);
                button1.setClickHandler(positiveButtonClickListener);
                button3.setClickHandler(negativeButtonClickListener);
                // addFooterChild(button1);
                // addFooterChild(button3);
                break;
            case 3:
                button1 = new CB_Button(t1);
                button2 = new CB_Button(t2);
                button3 = new CB_Button(t3);
                this.addNext(button1);
                this.addNext(button2);
                this.addLast(button3);
                button1.setClickHandler(positiveButtonClickListener);
                button2.setClickHandler(neutralButtonClickListener);
                button3.setClickHandler(negativeButtonClickListener);
                // addFooterChild(button1);
                // addFooterChild(button2);
                // addFooterChild(button3);
                break;
        }
        setFooterHeight(this.getHeightFromBottom());
    }

    public void addFooterChild(CB_View_Base view) {
        FooterItems.add(view);
    }

    private void setButtonListener() {
        positiveButtonClickListener = (v, x, y, pointer, button) -> ButtonClick(1);
        neutralButtonClickListener = (v, x, y, pointer, button) -> ButtonClick(2);
        negativeButtonClickListener = (v, x, y, pointer, button) -> ButtonClick(3);
    }

    private boolean ButtonClick(int button) {
		/*
		Object _data=null;
		switch (button) {
		case 1:
			button1.getData();
			break;
		case 2:
			button2.getData();
			break;
		case 3:
			button3.getData();
			break;
		}
		*/
        Object _data = this.getData();
        boolean ret = false;
        if (mMsgBoxClickListener != null) {
            ret = mMsgBoxClickListener.onClick(button, _data);
        }
        GL.that.closeDialog(this);
        return ret;
    }

    public void show() {
        GL.that.RunOnGL(() -> {
            try {
                GL.that.showDialog(this);
            } catch (Exception ex) {
                Log.err("ButtonDialog", "show", ex);
            }
        });
    }

    private Sprite getIcon(MessageBoxIcon msgIcon) {
        if (msgIcon == null)
            return null;

        Sprite icon;

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

        return icon;
    }

}

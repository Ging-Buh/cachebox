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

import java.util.ArrayList;
import java.util.Iterator;

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

public class ButtonDialog extends Dialog {

    private final ArrayList<CB_View_Base> FooterItems = new ArrayList<>();

    public CB_Button btnLeftPositive;
    public CB_Button btnMiddleNeutral;
    public CB_Button btnRightNegative;
    public MsgBox.OnMsgBoxClickListener mMsgBoxClickListener;
    protected CB_Label label;
    protected Object data;
    protected OnClickListener btnLeftPositiveClickListener;
    protected OnClickListener btnMiddleNeutralClickListener;
    protected OnClickListener btnRightNegativeClickListener;

    public ButtonDialog(String Name, String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon, MsgBox.OnMsgBoxClickListener Listener) {
        this(calcMsgBoxSize(msg, true, (buttons != MsgBoxButton.NOTHING), (icon != MsgBoxIcon.None), false).getBounds().asFloat(), Name, msg, title, buttons, icon, Listener);
    }

    public ButtonDialog(CB_RectF rec, String Name, String msg, String title, MsgBoxButton buttons, MsgBoxIcon icon, MsgBox.OnMsgBoxClickListener Listener) {
        super(rec, Name);
        setTitle(title);
        setButtonCaptions(buttons);
        SizeF contentSize = getContentSize();

        CB_RectF imageRec = new CB_RectF(0, contentSize.getHeight() - margin - UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());

        if (icon != MsgBoxIcon.None && icon != null) {
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
        ButtonDialog msgBox = new ButtonDialog("MsgBox", msg, "Title", MsgBoxButton.NOTHING, MsgBoxIcon.None, null);
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

    public void setButtonCaptions(MsgBoxButton buttons) {
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

    protected void createButtons(int anzahl, String t1, String t2, String t3) {
        setButtonListener();

        this.setBorders(margin, margin);
        this.setMargins(margin, margin);
        this.initRow(BOTTOMUp);

        switch (anzahl) {
            case 1:
                btnLeftPositive = new CB_Button(t1);
                this.addLast(btnLeftPositive);
                btnLeftPositive.setClickHandler(btnLeftPositiveClickListener);
                // addFooterChild(button1);
                break;
            case 2:
                btnLeftPositive = new CB_Button(t1);
                btnRightNegative = new CB_Button(t3);
                this.addNext(btnLeftPositive);
                this.addLast(btnRightNegative);
                btnLeftPositive.setClickHandler(btnLeftPositiveClickListener);
                btnRightNegative.setClickHandler(btnRightNegativeClickListener);
                // addFooterChild(button1);
                // addFooterChild(button3);
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
        btnLeftPositiveClickListener = (v, x, y, pointer, button) -> ButtonClick(1);
        btnMiddleNeutralClickListener = (v, x, y, pointer, button) -> ButtonClick(2);
        btnRightNegativeClickListener = (v, x, y, pointer, button) -> ButtonClick(3);
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

    private Sprite getIcon(MsgBoxIcon msgIcon) {
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

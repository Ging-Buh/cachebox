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
package CB_UI_Base.GL_UI.Controls.MessageBox;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Dialog;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;
import java.util.Iterator;

public class ButtonDialog extends Dialog {

    public final int BUTTON_POSITIVE = 1;
    public final int BUTTON_NEUTRAL = 2;
    public final int BUTTON_NEGATIVE = 3;

    private final ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

    public Button button1;
    public Button button2;
    public Button button3;
    public OnMsgBoxClickListener mMsgBoxClickListener;
    protected Label label;
    protected Object data;
    protected OnClickListener positiveButtonClickListener;
    protected OnClickListener neutralButtonClickListener;
    protected OnClickListener negativeButtonClickListener;

    public ButtonDialog(String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {
        this(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), (icon != MessageBoxIcon.None), false).getBounds().asFloat(), Name, msg, title, buttons, icon, Listener);
    }

    public ButtonDialog(CB_RectF rec, String Name, String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {
        super(rec, Name);
        setTitle(title);
        setButtonCaptions(buttons);
        SizeF contentSize = getContentSize();

        CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

        if (icon != MessageBoxIcon.None && icon != null) {
            Image iconImage = new Image(imageRec, "MsgBoxIcon", false);
            iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
            addChild(iconImage);
        }

        label = new Label(contentSize.getBounds());
        label.setWidth(contentSize.getBounds().getWidth() - 5 - UI_Size_Base.that.getButtonHeight());
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

    public static ButtonDialog Show(String msg) {
        ButtonDialog msgBox = new ButtonDialog("MsgBox", msg, "Title", MessageBoxButtons.NOTHING, MessageBoxIcon.None, null);
        GL.that.showDialog(msgBox);
        return msgBox;
    }

    @Override
    protected void Initial() {
        super.Initial();
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
        GL.that.RunOnGL(() -> GL.that.closeDialog(ButtonDialog.this));

    }

    public void setButtonCaptions(MessageBoxButtons buttons) {
        if (buttons == null)
            buttons = MessageBoxButtons.NOTHING;

        if (buttons == MessageBoxButtons.AbortRetryIgnore) {
            createButtons(3, Translation.Get("abort"), Translation.Get("retry"), Translation.Get("ignore"));
        } else if (buttons == MessageBoxButtons.OK) {
            createButtons(1, Translation.Get("ok"), "", "");
        } else if (buttons == MessageBoxButtons.OKCancel) {
            createButtons(2, Translation.Get("ok"), "", Translation.Get("cancel"));
        } else if (buttons == MessageBoxButtons.RetryCancel) {
            createButtons(2, Translation.Get("retry"), "", Translation.Get("cancel"));
        } else if (buttons == MessageBoxButtons.YesNo) {
            createButtons(2, Translation.Get("yes"), "", Translation.Get("no"));
        } else if (buttons == MessageBoxButtons.YesNoCancel) {
            createButtons(3, Translation.Get("yes"), Translation.Get("no"), Translation.Get("cancel"));
        } else if (buttons == MessageBoxButtons.Cancel) {
            createButtons(3, "", "", Translation.Get("cancel"));
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
                button1 = new Button(t1);
                this.addLast(button1);
                button1.setOnClickListener(positiveButtonClickListener);
                // addFooterChild(button1);
                break;
            case 2:
                button1 = new Button(t1);
                button3 = new Button(t3);
                this.addNext(button1);
                this.addLast(button3);
                button1.setOnClickListener(positiveButtonClickListener);
                button3.setOnClickListener(negativeButtonClickListener);
                // addFooterChild(button1);
                // addFooterChild(button3);
                break;
            case 3:
                button1 = new Button(t1);
                button2 = new Button(t2);
                button3 = new Button(t3);
                this.addNext(button1);
                this.addNext(button2);
                this.addLast(button3);
                button1.setOnClickListener(positiveButtonClickListener);
                button2.setOnClickListener(neutralButtonClickListener);
                button3.setOnClickListener(negativeButtonClickListener);
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
        positiveButtonClickListener = new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                return ButtonClick(1);
            }
        };

        neutralButtonClickListener = new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                return ButtonClick(2);
            }
        };

        negativeButtonClickListener = new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                return ButtonClick(3);
            }
        };
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
        GL.that.closeDialog(this);
        if (mMsgBoxClickListener != null)
            return mMsgBoxClickListener.onClick(button, _data);
        return false;
    }

    public void Show() {
        GL.that.RunOnGL(() -> {
            try {
                GL.that.showDialog(ButtonDialog.this);
            } catch (Exception e) {

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

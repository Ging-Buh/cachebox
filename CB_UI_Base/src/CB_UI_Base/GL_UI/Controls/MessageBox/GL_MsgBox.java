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

import java.util.ArrayList;
import java.util.Iterator;

import CB_Utils.Log.Log; import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Dialog;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Config_Core;
import CB_Utils.Settings.SettingBool;

public class GL_MsgBox extends Dialog {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(GL_MsgBox.class);
	static GL_MsgBox that;
	public static final int BUTTON_POSITIVE = 1;
	public static final int BUTTON_NEUTRAL = 2;
	public static final int BUTTON_NEGATIVE = 3;

	private ArrayList<CB_View_Base> FooterItems = new ArrayList<CB_View_Base>();

	// TODO make private with getter and setter *********
	public Button button1;
	public Button button2;
	public Button button3;
	public OnMsgBoxClickListener mMsgBoxClickListener;
	public OnClickListener positiveButtonClickListener;
	public OnClickListener neutralButtonClickListener;
	public OnClickListener negativeButtonClickListener;

	public Label label;

	protected SettingBool rememberSetting = null;
	protected chkBox chkRemember;

	// **************************************************

	public static GL_MsgBox Show(String msg, OnMsgBoxClickListener Listener) {
		return Show(msg, "", Listener);
	}

	public static GL_MsgBox Show(String msg, String title, OnMsgBoxClickListener Listener) {
		return Show(msg, title, MessageBoxButtons.OK, Listener, null);
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxIcon icon) {
		return Show(msg, title, MessageBoxButtons.OK, icon, null, null);
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener) {
		return Show(msg, title, buttons, icon, Listener, null);
	}

	public static GL_MsgBox Show(String msg) {
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, false, true, false), "MsgBox" + msg.substring(0, Math.max(5, msg.length())));
		msgBox.setButtonCaptions(MessageBoxButtons.OK);
		msgBox.label = new Label("msgBox" + " label", msgBox.getContentSize().getBounds());
		msgBox.label.setZeroPos();
		msgBox.label.setWrappedText(msg);
		msgBox.addChild(msgBox.label);

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, OnMsgBoxClickListener Listener, SettingBool remember) {

		if (remember != null && remember.getValue()) {
			// wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
			// Wir liefern nur ein On Click auf den OK Button zur�ck!
			if (Listener != null) {
				Listener.onClick(BUTTON_POSITIVE, null);
			}
			return null;
		}

		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), false, (remember != null)), "MsgBox" + title);
		msgBox.rememberSetting = remember;
		msgBox.mMsgBoxClickListener = Listener;
		msgBox.setButtonCaptions(buttons);
		msgBox.setTitle(title);

		msgBox.label=new Label();
		msgBox.label.setWrappedText(msg);
		float labelHeight =  msgBox.label.getTextHeight();
		msgBox.label.setHeight(labelHeight);

		ScrollBox scrollBox = new ScrollBox(msgBox.getContentSize().getBounds());
		scrollBox.initRow(BOTTOMUP);
		scrollBox.setVirtualHeight(labelHeight);
		scrollBox.addLast(msgBox.label);

		msgBox.addChild(scrollBox);

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	public static GL_MsgBox Show(String msg, String title, MessageBoxButtons buttons, MessageBoxIcon icon, OnMsgBoxClickListener Listener, SettingBool remember) {

		if (remember != null && remember.getValue()) {
			// wir brauchen die MsgBox nicht anzeigen, da der User die Remember Funktion gesetzt hat!
			// Wir liefern nur ein On Click auf den OK Button zur�ck!
			if (Listener != null) {
				Listener.onClick(BUTTON_NEGATIVE, null);
			}
			return null;
		}

		// nur damit bei mir die Box maximiert kommt und damit der Text nicht skaliert.
		// !!! gilt f�r alle Dialoge, da statisch definiert. K�nnte es auch dort �ndern.
		Dialog.margin = 5;
		final GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(msg, true, (buttons != MessageBoxButtons.NOTHING), true, (remember != null)), "MsgBox" + title);

		msgBox.rememberSetting = remember;
		msgBox.mMsgBoxClickListener = Listener;
		msgBox.setTitle(title);

		msgBox.setButtonCaptions(buttons);

		SizeF contentSize = msgBox.getContentSize();

		CB_RectF imageRec = new CB_RectF(0, contentSize.height - margin - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

		Image iconImage = new Image(imageRec, "MsgBoxIcon", false);
		if (icon != MessageBoxIcon.None)
			iconImage.setDrawable(new SpriteDrawable(getIcon(icon)));
		msgBox.addChild(iconImage);

		msgBox.label = new Label("msgBox" + " label", contentSize.getBounds());
		msgBox.label.setWidth(contentSize.getBounds().getWidth() - 5 - UI_Size_Base.that.getButtonHeight());
		msgBox.label.setPos(imageRec.getMaxX() + 5, 0);
		msgBox.label.setWrappedText(msg);
		msgBox.addChild(msgBox.label);

		GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
				GL.that.showDialog(msgBox);
			}
		});

		return msgBox;
	}

	/**
	 * Interface used to allow the creator of a dialog to run some code when an item on the dialog is clicked..
	 */
	public interface OnMsgBoxClickListener {
		/**
		 * This method will be invoked when a button in the dialog is clicked.
		 * 
		 * @param which
		 *            The button that was clicked ( the position of the item clicked.
		 * @return
		 */
		public boolean onClick(int which, Object data);
	}

	public GL_MsgBox(CB_RectF rec, String Name) {
		super(rec, Name);
		setFooterHeight(80);
		that = this;
	}

	public GL_MsgBox(Size size, String name) {
		super(size.getBounds().asFloat(), name);
		that = this;
	}

	private boolean ButtonClick(int button) {

		// wenn Dies eine Remember MsgBox ist, �berpr�fen wir ob das remember gesetzt ist
		if (rememberSetting != null) {
			if (chkRemember.isChecked()) {
				// User hat Remember aktiviert, was hier abgespeichert wird!
				rememberSetting.setValue(true);
				Config_Core.AcceptChanges();
			}
		}

		boolean retValue = false;
		if (mMsgBoxClickListener != null) {
			retValue = mMsgBoxClickListener.onClick(button, data);
		}
		GL.that.closeDialog(that);
		return retValue;
	}

	public static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon) {
		return calcMsgBoxSize(Text, hasTitle, hasButtons, hasIcon, false);
	}

	public void setButtonCaptions(MessageBoxButtons buttons) {
		if (buttons == MessageBoxButtons.YesNoRetry) {
			createButtons(this, 3);
			button1.setText(Translation.Get("yes"));
			button2.setText(Translation.Get("no"));
			button3.setText(Translation.Get("retry"));
		} else if (buttons == MessageBoxButtons.AbortRetryIgnore) {
			createButtons(this, 3);
			button1.setText(Translation.Get("abort"));
			button2.setText(Translation.Get("retry"));
			button3.setText(Translation.Get("ignore"));
		} else if (buttons == MessageBoxButtons.OK) {
			createButtons(this, 1);
			button1.setText(Translation.Get("ok"));
		} else if (buttons == MessageBoxButtons.OKCancel) {
			createButtons(this, 2);
			button1.setText(Translation.Get("ok"));
			button3.setText(Translation.Get("cancel"));
		} else if (buttons == MessageBoxButtons.RetryCancel) {
			createButtons(this, 2);
			button1.setText(Translation.Get("retry"));
			button3.setText(Translation.Get("cancel"));
		} else if (buttons == MessageBoxButtons.YesNo) {
			createButtons(this, 2);
			button1.setText(Translation.Get("yes"));
			button3.setText(Translation.Get("no"));
		} else if (buttons == MessageBoxButtons.YesNoCancel) {
			createButtons(this, 3);
			button1.setText(Translation.Get("yes"));
			button2.setText(Translation.Get("no"));
			button3.setText(Translation.Get("cancel"));
		} else if (buttons == MessageBoxButtons.Cancel) {
			createButtons(this, 2);
			button1.setInvisible();
			button3.setText(Translation.Get("cancel"));
		} else {
			this.setFooterHeight(calcFooterHeight(false));
		}
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

	protected void createButtons(GL_MsgBox msgBox, int anzahl) {
		setButtonListener();
		msgBox.initRow(BOTTOMUP, margin);
		msgBox.setBorders(margin, margin);

		switch (anzahl) {
		case 1:
			button1 = new Button("positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			msgBox.addLast(button1);
			break;
		case 2:
			button1 = new Button("positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			button3 = new Button("negativeButton");
			button3.setOnClickListener(negativeButtonClickListener);
			msgBox.addNext(button1);
			msgBox.addLast(button3);
			break;
		case 3:
			button1 = new Button("positiveButton");
			button1.setOnClickListener(positiveButtonClickListener);
			button2 = new Button("negativeButton");
			button2.setOnClickListener(neutralButtonClickListener);
			button3 = new Button("neutralButton");
			button3.setOnClickListener(negativeButtonClickListener);
			msgBox.addNext(button1);
			msgBox.addNext(button2);
			msgBox.addLast(button3);
			break;
		}

		if (rememberSetting != null) {
			chkRemember = new chkBox("remember");
			msgBox.setBorders(chkRemember.getHeight() / 2f, 0);
			msgBox.setMargins(chkRemember.getHeight() / 2f, 0);
			msgBox.addNext(chkRemember, chkRemember.getHeight() * 2f / msgBox.getWidth());
			Label lbl = new Label("lbl");
			msgBox.addLast(lbl);

			chkRemember.setChecked(rememberSetting.getValue());
			lbl.setText(Translation.Get("remember"));
		}

		msgBox.setFooterHeight(msgBox.getHeightFromBottom());
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

	@Override
	protected void Initial() {
		if (isDisposed())
			return;
		super.Initial();
		synchronized (childs) {
			for (Iterator<CB_View_Base> iterator = FooterItems.iterator(); iterator.hasNext();) {
				childs.add(iterator.next());
			}
		}
	}

	public void setText(String text) {
		label.setWrappedText(text);
	}

	public void close() {
		GL.that.closeDialog(that);
	}

	@Override
	protected void SkinIsChanged() {
	}

	@Override
	public void dispose() {
		//Log.debug(log, "Dispose GL_MsgBox=> " + name);

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
}

package CB_UI_Base.GL_UI.Controls.PopUps;

import CB_Utils.Log.Log; import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Global;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.interfaces.ICopyPaste;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class CopyPastePopUp extends PopUp_Base {
	final static org.slf4j.Logger logger = LoggerFactory.getLogger(CopyPastePopUp.class);
	private ImageButton btnCopy;
	private ImageButton btnCut;
	private ImageButton btnPaste;

	public CopyPastePopUp(String Name, final ICopyPaste copyPasteControl) {
		super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth() * 3.2f, UI_Size_Base.that.getButtonHeight() * 1.5f), Name);

		int p = Sprites.patch;

		setBackground(new NinePatchDrawable(new NinePatch(Sprites.Bubble.get(3), p, p, p, (int) (p * 1.432))));

		this.setClickable(true);

		CB_RectF rec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

		btnPaste = new ImageButton(rec, "PasteButton");
		btnPaste.setFont(Fonts.getBubbleNormal());
		btnPaste.setImage(Sprites.paste);
		btnPaste.setY(rec.getHeight() * 0.4f);
		if (!copyPasteControl.isEditable())
			btnPaste.disable();
		btnPaste.setOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				logger.debug("Paste Button Clicked");
				close();
				String Msg = copyPasteControl.pasteFromClipboard();
				if (Msg != null)
					GL.that.Toast(Translation.Get("PasteFromClipboard") + Global.br + Msg);
				return false;
			}
		});

		this.addChild(btnPaste);

		btnCopy = new ImageButton(rec, "CopyButton");
		btnCopy.setFont(Fonts.getBubbleNormal());
		btnCopy.setImage(Sprites.copy);
		btnCopy.setY(rec.getHeight() * 0.4f);
		btnCopy.setOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				logger.debug("Copy Button Clicked");
				close();
				String Msg = copyPasteControl.copyToClipboard();
				if (Msg != null)
					GL.that.Toast(Translation.Get("CopyToClipboard") + Global.br + Msg);
				return false;
			}
		});

		this.addChild(btnCopy);

		btnCut = new ImageButton(rec, "CutButton");
		btnCut.setFont(Fonts.getBubbleNormal());
		btnCut.setImage(Sprites.cut);
		btnCut.setY(rec.getHeight() * 0.4f);
		if (!copyPasteControl.isEditable()) {
			btnCut.disable();
		}
		btnCut.setOnClickListener(new OnClickListener() {
			/**
			 * onClick
			 */
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				close();
				String Msg = copyPasteControl.cutToClipboard();
				if (Msg != null)
					GL.that.Toast(Translation.Get("CutToClipboard") + Global.br + Msg);
				return false;
			}
		});

		this.addChild(btnCut);

		float sollDivider = (this.getWidth() - p - (rec.getWidth() * 3)) / 4;

		btnCut.setX(sollDivider + (p / 2));
		btnCopy.setX(btnCut.getMaxX() + sollDivider);
		btnPaste.setX(btnCopy.getMaxX() + sollDivider);
	}

	/**
	 * does nothing
	 */
	@Override
	public void Initial() {
	}

	public void flipX() {
		int p = Sprites.patch;

		Drawable drawable = new NinePatchDrawable(new NinePatch(Sprites.Bubble.get(5), p, p, (int) (p * 1.432), p));
		setBackground(drawable);

		float yValue = this.getHeight() * 0.07f;

		btnPaste.setY(yValue);
		btnCopy.setY(yValue);
		btnCut.setY(yValue);
	}

	@Override
	protected void SkinIsChanged() {

	}

	public void setOnlyPaste() {
		btnCut.disable();
		btnCopy.disable();
	}

	public void setOnlyCopy() {
		btnCut.disable();
		btnCopy.enable();
		btnPaste.disable();
	}
}

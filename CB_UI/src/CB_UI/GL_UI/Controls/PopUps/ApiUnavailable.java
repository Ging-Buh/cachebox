package CB_UI.GL_UI.Controls.PopUps;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.QuickButtonList;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Dialog;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class ApiUnavailable extends Toast
{
	public static ApiUnavailable INSTANCE = new ApiUnavailable();

	private Image mIcon;

	private ApiUnavailable()
	{

		super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth() * 2.5f, UI_Size_Base.that.getButtonWidth() * 2f), "ConectionError");

		float wh = UI_Size_Base.that.getButtonWidth() * 2.5f;

		CB_RectF posRec = new CB_RectF((UI_Size_Base.that.getWindowWidth() / 2) - (wh / 2), UI_Size_Base.that.getWindowHeight() - wh
				- Dialog.margin, wh, wh);

		this.setRec(posRec);

		this.mTextField.setHeight(this.getHalfHeight());
		this.mTextField.setZeroPos();
		this.mTextField.setWidth(this.getWidth() * 0.8f);
		this.mTextField.setX(this.getHalfWidth() - mTextField.getHalfWidth());
		this.setWrappedText(Translation.Get("API-offline"));

		this.mIcon = new Image(0, 0, getWidth(), getHalfHeight(), "ImageIcon");
		this.mIcon.setSprite(SpriteCacheBase.getThemedSprite("api-offline"));
		float top = QuickButtonList.that.getY();
		this.setY(top - UI_Size_Base.that.getButtonHeight() - this.getHeight());
		super.addChildToOverlay(mIcon);
	}

	@Override
	protected void SkinIsChanged()
	{

	}

}

package CB_Core.GL_UI.Controls.PopUps;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.QuickButtonList;
import CB_Core.GL_UI.Controls.Dialogs.Toast;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

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

		this.mTextField.setHeight(this.halfHeight);
		this.mTextField.setZeroPos();
		this.mTextField.setWidth(this.width * 0.8f);
		this.mTextField.setX(this.halfWidth - mTextField.getHalfWidth());
		this.setWrappedText(Translation.Get("API-offline"));

		this.mIcon = new Image(0, 0, width, halfHeight, "ImageIcon");
		this.mIcon.setSprite(SpriteCache.getThemedSprite("api-offline"));
		float top = QuickButtonList.that.getY();
		this.setY(top - UI_Size_Base.that.getButtonHeight() - this.getHeight());
		super.addChildToOverlay(mIcon);
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}

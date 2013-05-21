package CB_Core.GL_UI.Controls.PopUps;

import CB_Core.GL_UI.Controls.Dialogs.Toast;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

public class ConnectionError extends Toast
{
	public static ConnectionError INSTANCE = new ConnectionError();

	private ConnectionError()
	{

		super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth() * 2.5f, UI_Size_Base.that.getButtonWidth() * 2f), "ConectionError");

		float wh = UI_Size_Base.that.getButtonWidth() * 2.5f;

		CB_RectF posRec = new CB_RectF((UI_Size_Base.that.getWindowWidth() / 2) - (wh / 2), UI_Size_Base.that.getWindowHeight() / 2, wh, wh);

		this.setRec(posRec);

		this.setWrappedText(Translation.Get("ConnectionError"));

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

}

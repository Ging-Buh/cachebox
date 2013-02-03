package CB_Core.GL_UI.Controls;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.Api.GcApiLogin;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class API_Button extends Button
{

	Image apiChk;

	public API_Button(CB_RectF rec)
	{
		super(rec, "API-Button");

		setText();
		this.setOnClickListener(click);

		CB_RectF rec1 = new CB_RectF(this);
		rec1.setWidth(this.height);
		rec1.setX(this.width - this.height);
		rec1 = rec1.ScaleCenter(0.7f);

		apiChk = new Image(rec1, "");

		this.addChild(apiChk);
		setImage();

	}

	private void setText()
	{
		this.setText(Translation.Get("getApiKey"));
	}

	@Override
	protected void Initial()
	{
		super.Initial();

	}

	public void setImage()
	{
		if (apiChk != null)
		{
			Drawable drw;

			if (Config.settings.GcAPI.getValue().equals(""))
			{
				drw = new SpriteDrawable(SpriteCache.getThemedSprite("chk-icon-disable"));
			}
			else
			{
				drw = new SpriteDrawable(SpriteCache.getThemedSprite("chk-icon"));
			}

			apiChk.setDrawable(drw);
		}

	}

	OnClickListener click = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			if (GlobalCore.platform == Plattform.Desktop)
			{
				(new GcApiLogin()).RunRequest();
			}
			else
			{
				platformConector.callGetApiKeyt();
			}

			return true;
		}
	};

}

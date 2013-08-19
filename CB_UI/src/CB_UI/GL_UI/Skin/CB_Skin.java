package CB_UI.GL_UI.Skin;

import CB_UI.Config;
import CB_UI.GlobalCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public abstract class CB_Skin extends SkinBase
{

	public static final CB_Skin INSTANCE = new CB_Skin()
	{
		protected void initial()
		{
			SkinSettings cfg = new SkinSettings();

			FileHandle fh;
			if (Config.settings.SkinFolder.getValue().contains("default"))
			{
				// fh = Gdx.files.internal("skins/default");
				fh = GlobalCore.getInternalFileHandle("skins/default");
			}
			else if (Config.settings.SkinFolder.getValue().contains("small"))
			{
				fh = GlobalCore.getInternalFileHandle("skins/small");
			}
			else
			{
				fh = Gdx.files.absolute(Config.settings.SkinFolder.getValue());
			}

			cfg.SkinFolder = fh;
			cfg.DefaultSkinFolder = GlobalCore.getInternalFileHandle("skins/default");

			cfg.SizeBiggest = Config.settings.FONT_SIZE_COMPASS_DISTANCE.getValue();
			cfg.SizeBig = Config.settings.FONT_SIZE_BIG.getValue();
			cfg.SizeNormal = Config.settings.FONT_SIZE_NORMAL.getValue();
			cfg.SizeNormalbubble = Config.settings.FONT_SIZE_NORMAL_BUBBLE.getValue();
			cfg.SizeSmall = Config.settings.FONT_SIZE_SMALL.getValue();
			cfg.SizeSmallBubble = Config.settings.FONT_SIZE_SMALL_BUBBLE.getValue();
			cfg.Nightmode = Config.settings.nightMode.getValue();

			settings = cfg;
		}

	};

	private CB_Skin()
	{
		super();
		initial();
		that = this;
	}

	protected abstract void initial();

}

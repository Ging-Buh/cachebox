package CB_UI_Base.GL_UI.Skin;

import CB_UI_Base.settings.CB_UI_Base_Settings;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Implements all infos for a Skin like Font-Path Ui_iconPack Path ....
 * 
 * @author Longri
 */
public abstract class SkinBase
{
	public static SkinBase that;
	private static Skin night_skin;
	private static Skin day_skin;

	protected static SkinSettings settings;

	public static Skin getDaySkin()
	{
		if (day_skin == null) initialSkin();
		return day_skin;
	}

	public static Skin getNightSkin()
	{
		if (night_skin == null) initialSkin();
		return night_skin;
	}

	public SkinSettings getSettings()
	{
		return settings;
	}

	protected SkinBase()
	{
	}

	public static Color getThemedColor(String Name)
	{
		if (night_skin == null || day_skin == null) initialSkin();
		if (CB_UI_Base_Settings.nightMode.getValue())
		{
			return night_skin.getColor(Name);
		}
		else
		{
			return day_skin.getColor(Name);
		}

	}

	private static void initialSkin()
	{
		if (day_skin == null)
		{
			String day_skinPath = settings.SkinFolder + "/day/skin.json";
			if (settings.SkinFolder.type() == FileType.Absolute)
			{
				day_skin = new Skin(Gdx.files.absolute(day_skinPath));
			}
			else
			{
				day_skin = new Skin(Gdx.files.internal(day_skinPath));
			}
		}

		if (night_skin == null)
		{
			String night_skinPath = settings.SkinFolder + "/night/skin.json";
			if (settings.SkinFolder.type() == FileType.Absolute)
			{
				night_skin = new Skin(Gdx.files.absolute(night_skinPath));
			}
			else
			{
				night_skin = new Skin(Gdx.files.internal(night_skinPath));
			}
		}
	}
}

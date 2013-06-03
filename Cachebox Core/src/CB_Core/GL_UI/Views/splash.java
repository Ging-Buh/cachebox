package CB_Core.GL_UI.Views;

import java.io.File;
import java.io.IOException;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.SelectDB;
import CB_Core.GL_UI.Activitys.SelectDB.ReturnListner;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.Log.Logger;
import CB_Core.Map.ManagerBase;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Settings.SettingString;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.Categories;
import CB_Core.Types.Waypoint;
import CB_Core.Util.FileList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class splash extends TabMainView
{
	public splash(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	TextureAtlas atlas;
	ProgressBar progress;
	Image CB_Logo, OSM_Logo, Route_Logo, Mapsforge_Logo, LibGdx_Logo, GC_Logo;

	Label descTextView;
	SelectDB selectDBDialog;

	int step = 0;
	boolean switcher = false;
	boolean breakForWait = false;

	@Override
	protected void Initial()
	{
		switcher = !switcher;
		if (switcher && !breakForWait)
		{
			// in jedem Render Vorgang einen Step ausführen
			switch (step)
			{
			case 0:
				atlas = new TextureAtlas(Gdx.files.absolute(Config.WorkPath + "/skins/default/day/SplashPack.spp"));
				setBackground(new SpriteDrawable(atlas.createSprite("splash-back")));
				break;
			case 1:
				ini_Progressbar();
				progress.setProgress(10, "Read Config");
				break;
			case 2:
				ini_Config();
				progress.setProgress(15, "Load Translations");
				break;
			case 3:
				ini_Translations();
				progress.setProgress(20, "Load Sprites");
				break;
			case 4:
				ini_Sprites();
				progress.setProgress(30, "check directoiries");
				break;
			case 5:
				ini_Dirs();
				progress.setProgress(40, "Select DB");
				break;
			case 6:
				ini_SelectDB();
				progress.setProgress(60, "Load Caches");
				break;
			case 7:
				ini_CacheDB();
				progress.setProgress(80, "initial Map layer");
				break;
			case 8:
				ini_MapPaks();
				progress.setProgress(100, "Run");
				break;
			case 100:
				ini_TabMainView();
				break;
			default:
				step = 99;
			}
			step++;
		}

		if (step <= 100) resetInitial();
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	/**
	 * Step 1 <br>
	 * add Progressbar
	 */
	private void ini_Progressbar()
	{

		float ref = UI_Size_Base.that.getWindowHeight() / 13;
		CB_RectF CB_LogoRec = new CB_RectF(this.halfWidth - (ref * 2.5f), this.height - ((ref * 5) / 4.11f) - ref, ref * 5,
				(ref * 5) / 4.11f);
		CB_Logo = new Image(CB_LogoRec, "CB_Logo");
		CB_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("cachebox-logo")));
		this.addChild(CB_Logo);

		String VersionString = GlobalCore.getVersionString();
		TextBounds bounds = Fonts.getNormal().getMultiLineBounds(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.splashMsg);
		descTextView = new Label(0, CB_Logo.getY() - ref - bounds.height, this.width, bounds.height + 10, "DescLabel");
		// HAlignment.CENTER funktioniert (hier) (noch) nicht, es kommt rechtsbündig raus
		descTextView.setWrappedText(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.splashMsg, HAlignment.CENTER);
		this.addChild(descTextView);

		Drawable ProgressBack = new NinePatchDrawable(atlas.createPatch("btn-normal"));
		Drawable ProgressFill = new NinePatchDrawable(atlas.createPatch("progress"));

		float ProgressHeight = Math.max(ProgressBack.getBottomHeight() + ProgressBack.getTopHeight(), ref / 1.5f);

		progress = new ProgressBar(new CB_RectF(0, 0, this.width, ProgressHeight), "Splash.ProgressBar");

		progress.setBackground(ProgressBack);
		progress.setProgressFill(ProgressFill);
		this.addChild(progress);

		float logoCalcRef = ref * 1.5f;

		CB_RectF rec_GC_Logo = new CB_RectF(20, 50, logoCalcRef, logoCalcRef);
		CB_RectF rec_Mapsforge_Logo = new CB_RectF(200, 50, logoCalcRef, logoCalcRef / 1.142f);
		CB_RectF rec_FX2_Logo = new CB_RectF(rec_Mapsforge_Logo);
		CB_RectF rec_LibGdx_Logo = new CB_RectF(20, 50, logoCalcRef * 4.17f * 0.8f, logoCalcRef * 0.8f);
		CB_RectF rec_OSM = new CB_RectF(rec_Mapsforge_Logo);
		CB_RectF rec_Route = new CB_RectF(rec_Mapsforge_Logo);

		rec_FX2_Logo.setX(400);

		GC_Logo = new Image(rec_GC_Logo, "GC_Logo");
		GC_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("gc_live")));

		Mapsforge_Logo = new Image(rec_Mapsforge_Logo, "Mapsforge_Logo");
		Mapsforge_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("mapsforge_logo")));

		LibGdx_Logo = new Image(rec_LibGdx_Logo, "LibGdx_Logo");
		LibGdx_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("libgdx")));

		Route_Logo = new Image(rec_OSM, "Route_Logo");
		Route_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("openrouteservice_logo")));

		OSM_Logo = new Image(rec_Route, "OSM_Logo");
		OSM_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("osm_logo")));

		float yPos = descTextView.getY() - GC_Logo.getHeight();
		float xPos = (this.width - ref - GC_Logo.getWidth() - Mapsforge_Logo.getWidth()) / 2;

		GC_Logo.setPos(xPos, yPos);
		xPos += GC_Logo.getWidth() + ref;

		Mapsforge_Logo.setPos(xPos, yPos);
		xPos += Mapsforge_Logo.getWidth() + ref;

		yPos -= GC_Logo.getHeight();// + refHeight;
		LibGdx_Logo.setPos(this.halfWidth - LibGdx_Logo.getHalfWidth(), yPos);

		yPos -= GC_Logo.getHeight();//
		xPos = (this.width - (ref) - Route_Logo.getWidth() - OSM_Logo.getWidth()) / 2;

		Route_Logo.setPos(xPos, yPos);

		xPos += Route_Logo.getWidth() + ref;
		OSM_Logo.setPos(xPos, yPos);

		this.addChild(GC_Logo);
		this.addChild(Mapsforge_Logo);
		this.addChild(LibGdx_Logo);
		this.addChild(Route_Logo);
		this.addChild(OSM_Logo);

	}

	/**
	 * Step 2 <br>
	 * Load Config DB3
	 */
	private void ini_Config()
	{
		Logger.DEBUG("ini_Config");
		Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");
		Config.settings.ReadFromDB();
		Config.AcceptChanges();
	}

	/**
	 * Step 3 <br>
	 * Load Translations
	 */
	private void ini_Translations()
	{
		Logger.DEBUG("ini_Translations");
		new Translation(Config.WorkPath, false);
		try
		{
			Translation.LoadTranslation(Config.settings.Sel_LanguagePath.getValue());
		}
		catch (Exception e)
		{
			try
			{
				Translation.LoadTranslation(Config.settings.Sel_LanguagePath.getDefaultValue());
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Step 4 <br>
	 * Load Sprites
	 */
	private void ini_Sprites()
	{
		Logger.DEBUG("ini_Sprites");
		SpriteCache.LoadSprites(false);
	}

	/**
	 * Step 5 <br>
	 * chk directories
	 */
	private void ini_Dirs()
	{
		Logger.DEBUG("ini_Dirs");
		ini_Dir(Config.settings.PocketQueryFolder.getValue());
		ini_Dir(Config.settings.TileCacheFolder.getValue());
		ini_Dir(Config.WorkPath + "/User");
		ini_Dir(Config.settings.TrackFolder.getValue());
		ini_Dir(Config.settings.UserImageFolder.getValue());
		ini_Dir(Config.WorkPath + "/repository");
		ini_Dir(Config.settings.DescriptionImageFolder.getValue());
		ini_Dir(Config.settings.MapPackFolder.getValue());
		ini_Dir(Config.settings.SpoilerFolder.getValue());

		// prevent mediascanner to parse all the images in the cachebox folder
		File nomedia = new File(Config.WorkPath, ".nomedia");
		if (!nomedia.exists())
		{
			try
			{
				nomedia.createNewFile();
			}
			catch (IOException e)
			{

				e.printStackTrace();
			}
		}
	}

	private void ini_Dir(String Folder)
	{
		File ff = new File(Folder);
		if (!ff.exists())
		{
			ff.mkdir();
		}
	}

	/**
	 * Step 5 <br>
	 * show select DB Dialog
	 */
	private void ini_SelectDB()
	{
		Logger.DEBUG("ini_SelectDB");
		// search number of DB3 files
		FileList fileList = null;
		try
		{
			fileList = new FileList(Config.WorkPath, "DB3");
		}
		catch (Exception ex)
		{
			Logger.Error("slpash.Initial()", "search number of DB3 files", ex);
		}
		if ((fileList.size() > 1) && Config.settings.MultiDBAsk.getValue() && !GlobalCore.restartAfterKill)
		{
			breakForWait = true;
			selectDBDialog = new SelectDB(this, "SelectDbDialog", true);
			selectDBDialog.setReturnListner(new ReturnListner()
			{
				@Override
				public void back()
				{
					returnFromSelectDB();
				}
			});
			selectDBDialog.show();
		}

	}

	private void returnFromSelectDB()
	{
		breakForWait = false;
		switcher = true;
	}

	/**
	 * Step 6<br>
	 * Load Cache DB3
	 */
	private void ini_CacheDB()
	{
		Logger.DEBUG("ini_CacheDB");
		// chk if exist filter preset splitter "#" and Replace
		String ConfigPreset = Config.settings.UserFilter.getValue();
		if (ConfigPreset.endsWith("#"))
		{
			// Preset implements old splitter, replaced!

			ConfigPreset = ConfigPreset.substring(0, ConfigPreset.length() - 1) + SettingString.STRING_SPLITTER;

			boolean replace = true;
			while (replace)
			{
				String newConfigPreset = ReplaceSplitter(ConfigPreset);
				if (newConfigPreset == null) replace = false;
				else
					ConfigPreset = newConfigPreset;
			}
			;
			Config.settings.UserFilter.setValue(ConfigPreset);
			Config.AcceptChanges();
		}

		Database.Data.StartUp(Config.settings.DatabasePath.getValue());

		Config.settings.ReadFromDB();

		// zuerst den FilterString im neuen JSON Format laden versuchen
		String FilterString = Config.settings.FilterNew.getValue();
		if (FilterString.length() > 0)
		{
			GlobalCore.LastFilter = new FilterProperties(FilterString);
		}
		else
		{
			// Falls kein Neuer gefunden wurde -> das alte Format versuchen
			FilterString = Config.settings.Filter.getValue();
			GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0].ToString())
					: new FilterProperties(FilterString);
		}
		String sqlWhere = GlobalCore.LastFilter.getSqlWhere();

		GlobalCore.Categories = new Categories();
		Database.Data.GPXFilenameUpdateCacheCount();

		synchronized (Database.Data.Query)
		{
			CacheListDAO cacheListDAO = new CacheListDAO();
			cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
			// Database.Data.Query.checkSelectedCacheValid(); // überflüssig, wird später überschrieben
		}

		CachListChangedEventList.Call();

		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

	}

	private String ReplaceSplitter(String ConfigPreset)
	{
		try
		{
			int pos = ConfigPreset.indexOf("#");
			int pos2 = ConfigPreset.indexOf(";", pos);

			String PresetName = (String) ConfigPreset.subSequence(pos + 1, pos2);
			if (!PresetName.contains(","))
			{
				String s1 = (String) ConfigPreset.subSequence(0, pos);
				String s2 = (String) ConfigPreset.subSequence(pos2, ConfigPreset.length());

				ConfigPreset = s1 + SettingString.STRING_SPLITTER + PresetName + s2;
				return ConfigPreset;
			}
		}
		catch (Exception e)
		{
			return null;
		}

		return null;
	}

	/**
	 * Step 7 <br>
	 * chk installed map packs/layers
	 */
	private void ini_MapPaks()
	{
		Logger.DEBUG("ini_MapPaks");
		ManagerBase.Manager.initialMapPacks();
	}

	/**
	 * Last Step <br>
	 * Show TabMainView
	 */
	private void ini_TabMainView()
	{
		Logger.DEBUG("ini_TabMainView");
		GL.that.removeRenderView(this);
		GL.that.switchToMainView();

		if (GlobalCore.restartCache != null)
		{
			synchronized (Database.Data.Query)
			{
				Cache c = Database.Data.Query.GetCacheByGcCode(GlobalCore.restartCache);
				if (GlobalCore.restartWaypoint != null && c != null && c.waypoints != null)
				{
					Waypoint w = null;

					for (Waypoint wp : c.waypoints)
					{
						if (wp.GcCode.equalsIgnoreCase(GlobalCore.restartWaypoint))
						{
							w = wp;
						}
					}
					Logger.DEBUG("ini_TabMainView: Set selectedCache to" + c.GcCode + " from restartCache + WP.");
					GlobalCore.setSelectedWaypoint(c, w);
				}
				else
				{
					Logger.DEBUG("ini_TabMainView: Set selectedCache to" + c.GcCode + " from restartCache.");
					GlobalCore.setSelectedCache(c);
				}
			}

		}
		GL.setIsInitial();
	}

	public void dispose()
	{
		this.removeChildsDirekt();

		if (selectDBDialog != null) selectDBDialog.dispose();
		if (descTextView != null) descTextView.dispose();
		if (GC_Logo != null) GC_Logo.dispose();
		if (LibGdx_Logo != null) LibGdx_Logo.dispose();
		if (Mapsforge_Logo != null) Mapsforge_Logo.dispose();
		if (CB_Logo != null) CB_Logo.dispose();
		if (progress != null) progress.dispose();
		if (atlas != null) atlas.dispose();

		selectDBDialog = null;
		descTextView = null;
		GC_Logo = null;
		LibGdx_Logo = null;
		Mapsforge_Logo = null;
		CB_Logo = null;
		progress = null;
		atlas = null;

	}

}

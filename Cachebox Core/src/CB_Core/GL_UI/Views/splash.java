package CB_Core.GL_UI.Views;

import java.io.File;
import java.io.IOException;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FileList;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.Controls.Dialogs.SelectDB;
import CB_Core.GL_UI.Controls.Dialogs.SelectDB.ReturnListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.GL_Listener.Tab_GL_Listner;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Categories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class splash extends TabMainView
{
	public splash(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		GL_Listener.glListener.addRenderView(this, GL_Listener.FRAME_RATE_IDLE);
		workPath = Config.WorkPath;
	}

	TextureAtlas atlas;
	ProgressBar progress;
	Image CB_Logo;
	Image Mapsforge_Logo;
	Image LibGdx_Logo;
	Image FX2_Logo;
	Image GC_Logo;
	Label versionTextView;
	Label descTextView;
	SelectDB selectDBDialog;

	int step = 0;
	boolean switcher = false;
	boolean breakForWait = false;
	String workPath;

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

				String defaultPath = Config.settings.SkinFolder.getValue();
				int pos = defaultPath.lastIndexOf("/");
				defaultPath = defaultPath.substring(0, pos) + "/default";

				String path = defaultPath + "/day/SplashPack.spp";
				atlas = new TextureAtlas(Gdx.files.absolute(path));
				setBackground(atlas.createSprite("splash_back"));
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
				ini_Dir();
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
		CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (UiSizes.getButtonWidth() * 2.5f), this.height
				- ((UiSizes.getButtonWidth() * 5) / 4.11f) - UiSizes.getButtonWidth(), UiSizes.getButtonWidth() * 5,
				(UiSizes.getButtonWidth() * 5) / 4.11f);
		CB_Logo = new Image(CB_LogoRec, "CB_Logo");
		CB_Logo.setSprite(atlas.createSprite("cachebox_logo"));
		this.addChild(CB_Logo);

		TextBounds bounds = Fonts.getNormal().getMultiLineBounds(GlobalCore.getVersionString());

		versionTextView = new Label(0, CB_Logo.getY() - UiSizes.getButtonHeight() - bounds.height, this.width, bounds.height + 10,
				"VesionLabel");
		versionTextView.setMultiLineText(GlobalCore.getVersionString());
		versionTextView.setHAlignment(HAlignment.CENTER);
		this.addChild(versionTextView);

		bounds = Fonts.getNormal().getMultiLineBounds(GlobalCore.splashMsg);

		descTextView = new Label(0, versionTextView.getY() - UiSizes.getButtonHeight() - bounds.height, this.width, bounds.height + 10,
				"DescLabel");
		descTextView.setMultiLineText(GlobalCore.splashMsg);
		descTextView.setHAlignment(HAlignment.CENTER);
		this.addChild(descTextView);

		progress = new ProgressBar(new CB_RectF(0, 0, this.width, UiSizes.getButtonHeight() / 1.5f), "Splash.ProgressBar");
		progress.setBackground(new NinePatch(atlas.createSprite("btn_normal"), 16, 16, 16, 16));
		progress.setProgressNinePatch(new NinePatch(atlas.createSprite("progress"), 15, 15, 15, 15));
		this.addChild(progress);

		float logoCalcRef = UiSizes.getButtonHeight() * 1.5f;

		CB_RectF rec_GC_Logo = new CB_RectF(20, 50, logoCalcRef, logoCalcRef);
		CB_RectF rec_Mapsforge_Logo = new CB_RectF(200, 50, logoCalcRef, logoCalcRef / 1.142f);
		CB_RectF rec_FX2_Logo = new CB_RectF(rec_Mapsforge_Logo);
		CB_RectF rec_LibGdx_Logo = new CB_RectF(20, 50, logoCalcRef * 4.17f * 0.8f, logoCalcRef * 0.8f);

		rec_FX2_Logo.setX(400);

		GC_Logo = new Image(rec_GC_Logo, "GC_Logo");
		GC_Logo.setSprite(atlas.createSprite("gc_live"));

		Mapsforge_Logo = new Image(rec_Mapsforge_Logo, "Mapsforge_Logo");
		Mapsforge_Logo.setSprite(atlas.createSprite("mapsforge_logo"));

		FX2_Logo = new Image(rec_FX2_Logo, "FX2_Logo");
		FX2_Logo.setSprite(atlas.createSprite("FXzwei"));

		LibGdx_Logo = new Image(rec_LibGdx_Logo, "LibGdx_Logo");
		LibGdx_Logo.setSprite(atlas.createSprite("libgdx"));

		float yPos = descTextView.getY() - GC_Logo.getHeight();
		float xPos = (this.width - (UiSizes.getButtonHeight() * 2) - GC_Logo.getWidth() - Mapsforge_Logo.getWidth() - FX2_Logo.getWidth()) / 2;

		GC_Logo.setPos(xPos, yPos);
		xPos += GC_Logo.getWidth() + UiSizes.getButtonHeight();

		Mapsforge_Logo.setPos(xPos, yPos);
		xPos += Mapsforge_Logo.getWidth() + UiSizes.getButtonHeight();

		FX2_Logo.setPos(xPos, yPos);

		yPos -= GC_Logo.getHeight();// + UiSizes.getButtonHeight();
		LibGdx_Logo.setPos(this.getHalfWidth() - LibGdx_Logo.getHalfWidth(), yPos);

		this.addChild(GC_Logo);
		this.addChild(Mapsforge_Logo);
		this.addChild(FX2_Logo);
		this.addChild(LibGdx_Logo);

	}

	/**
	 * Step 2 <br>
	 * Load Config DB3
	 */
	private void ini_Config()
	{
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
		try
		{
			GlobalCore.Translations.ReadTranslationsFile(Config.settings.Sel_LanguagePath.getValue());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Step 4 <br>
	 * Load Sprites
	 */
	private void ini_Sprites()
	{
		SpriteCache.LoadSprites(false);
	}

	/**
	 * Step 5 <br>
	 * chk directories
	 */
	private void ini_Dir()
	{
		String PocketQueryFolder = Config.settings.PocketQueryFolder.getValue();
		File directoryPocketQueryFolder = new File(PocketQueryFolder);
		if (!directoryPocketQueryFolder.exists())
		{
			directoryPocketQueryFolder.mkdir();
		}
		String TileCacheFolder = Config.settings.TileCacheFolder.getValue();
		File directoryTileCacheFolder = new File(TileCacheFolder);
		if (!directoryTileCacheFolder.exists())
		{
			directoryTileCacheFolder.mkdir();
		}
		String User = workPath + "/User";
		File directoryUser = new File(User);
		if (!directoryUser.exists())
		{
			directoryUser.mkdir();
		}
		String TrackFolder = Config.settings.TrackFolder.getValue();
		File directoryTrackFolder = new File(TrackFolder);
		if (!directoryTrackFolder.exists())
		{
			directoryTrackFolder.mkdir();
		}
		String UserImageFolder = Config.settings.UserImageFolder.getValue();
		File directoryUserImageFolder = new File(UserImageFolder);
		if (!directoryUserImageFolder.exists())
		{
			directoryUserImageFolder.mkdir();
		}

		String repository = workPath + "/repository";
		File directoryrepository = new File(repository);
		if (!directoryrepository.exists())
		{
			directoryrepository.mkdir();
		}
		String DescriptionImageFolder = Config.settings.DescriptionImageFolder.getValue();
		File directoryDescriptionImageFolder = new File(DescriptionImageFolder);
		if (!directoryDescriptionImageFolder.exists())
		{
			directoryDescriptionImageFolder.mkdir();
		}
		String MapPackFolder = Config.settings.MapPackFolder.getValue();
		File directoryMapPackFolder = new File(MapPackFolder);
		if (!directoryMapPackFolder.exists())
		{
			directoryMapPackFolder.mkdir();
		}
		String SpoilerFolder = Config.settings.SpoilerFolder.getValue();
		File directorySpoilerFolder = new File(SpoilerFolder);
		if (!directorySpoilerFolder.exists())
		{
			directorySpoilerFolder.mkdir();
		}

		// prevent mediascanner to parse all the images in the cachebox folder
		File nomedia = new File(workPath, ".nomedia");
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

	/**
	 * Step 5 <br>
	 * show select DB Dialog
	 */
	private void ini_SelectDB()
	{
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
		if ((fileList.size() > 1) && Config.settings.MultiDBAsk.getValue())
		{
			breakForWait = true;
			selectDBDialog = new SelectDB(this, "SelectDbDialog");
			selectDBDialog.setReturnListner(new ReturnListner()
			{
				@Override
				public void back()
				{
					returnFromSelectDB();
				}
			});
			this.addChild(selectDBDialog);
		}

	}

	private void returnFromSelectDB()
	{
		this.removeChild(selectDBDialog);
		breakForWait = false;
		switcher = true;
	}

	/**
	 * Step 6<br>
	 * Load Cache DB3
	 */
	private void ini_CacheDB()
	{
		String database = Config.settings.DatabasePath.getValue();
		Database.Data.StartUp(database);

		Config.settings.ReadFromDB();

		String FilterString = Config.settings.Filter.getValue();
		GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0]) : new FilterProperties(
				FilterString);
		String sqlWhere = GlobalCore.LastFilter.getSqlWhere();

		GlobalCore.Categories = new Categories();
		Database.Data.GPXFilenameUpdateCacheCount();

		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

		if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

	}

	/**
	 * Step 7 <br>
	 * chk installed map packs/layers
	 */
	private void ini_MapPaks()
	{
		File dir = new File(Config.settings.MapPackFolder.getValue());
		String[] files = dir.list();
		if (!(files == null))
		{
			if (files.length > 0)
			{
				for (String file : files)
				{
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("pack")) ManagerBase.Manager
							.LoadMapPack(Config.settings.MapPackFolder.getValue() + "/" + file);
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("map"))
					{
						Layer layer = new Layer(file, file, "");
						layer.isMapsForge = true;
						ManagerBase.Manager.Layers.add(layer);
					}
				}
			}
		}
		Descriptor.Init();
	}

	/**
	 * Last Step <br>
	 * Show TabMainView
	 */
	private void ini_TabMainView()
	{
		GL_Listener.glListener.removeRenderView(this);
		((Tab_GL_Listner) GL_Listener.glListener).switchToTabMainView();
	}
}

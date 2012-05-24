package CB_Core.GL_UI.Views;

import java.io.IOException;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.ProgressBar;
import CB_Core.GL_UI.Controls.Dialogs.SelectDB;
import CB_Core.GL_UI.Controls.Dialogs.SelectDB.ReturnListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.GL_Listener.Tab_GL_Listner;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Categories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class splash extends TabMainView
{
	public splash(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		GL_Listener.glListener.addRenderView(this, GL_Listener.FRAME_RATE_IDLE);
	}

	TextureAtlas atlas;
	ProgressBar progress;
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
				progress.setProgress(30, "Select DB");
				break;
			case 5:
				ini_SelectDB();
				progress.setProgress(40, "Load Caches");
				break;
			case 6:
				ini_CacheDB();
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
		progress = new ProgressBar(new CB_RectF(0, 0, this.width, UiSizes.getButtonHeight() / 1.5f), "Splash.ProgressBar");
		progress.setBackground(new NinePatch(atlas.createSprite("btn_normal"), 16, 16, 16, 16));
		progress.setProgressNinePatch(new NinePatch(atlas.createSprite("progress"), 15, 15, 15, 15));
		this.addChild(progress);
	}

	/**
	 * Step 2 <br>
	 * Load Config DB3
	 */
	private void ini_Config()
	{
		Database.Settings.StartUp(Config.WorkPath + "/Config.db3");
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
	 * show select DB Dialog
	 */
	private void ini_SelectDB()
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

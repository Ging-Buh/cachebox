package de.CB.TestBase.Views;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.CB.TestBase.Ex;
import de.CB.TestBase.Global;
import de.CB.TestBase.Res.ResourceCache;

public class splash extends MainViewBase
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(splash.class);
	
	private final long SPLASH_MIN_SHOW_TIME = 10;
	private boolean nextClicked = false;
	private long splashEndTime = 0;

	public splash(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		splashEndTime = System.currentTimeMillis() + SPLASH_MIN_SHOW_TIME;

	}

	TextureAtlas atlas;
	ProgressBar progress;
	Image Mapsforge_Logo, OSM_Logo, CB_Logo, CGeo_Logo;

	Label descTextView;

	int step = 0;
	boolean switcher = false;
	boolean breakForWait = false;

	@Override
	public void onShow()
	{
		GL.that.addRenderView(this, GL.FRAME_RATE_FAST_ACTION);

	}

	@Override
	protected void Initial()
	{
		switcher = !switcher;
		if (switcher && !breakForWait)
		{
			// in jedem Render Vorgang einen Step ausf�hren
			switch (step)
			{
			case 0:
				atlas = new TextureAtlas(Gdx.files.internal("skins/default/day/SplashPack.spp"));
				setBackground(new SpriteDrawable(atlas.createSprite("splash-back")));
				break;
			case 1:
				ini_Progressbar();
				progress.setProgress(10, "Read Config");
				break;
			case 2:

				// ini_Config();
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
				// ini_Dirs();
				progress.setProgress(40, "Select DB");
				break;
			// case 6:
			// ini_SelectDB();
			// progress.setProgress(60, "Load Caches");
			// break;
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

		if (step <= 101) resetInitial();
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

		float ref = UiSizes.that.getWindowHeight() / 13;
		CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.getHeight() - ((ref * 5) / 4.11f) - ref, ref * 5,
				(ref * 5) / 4.11f);

		String VersionString = Global.getVersionString()+ Global.br + Global.br + Global.splashMsg;
		GlyphLayout layout = new GlyphLayout(); 
		layout.setText( Fonts.getNormal(),VersionString);
		
		descTextView = new Label(0, CB_LogoRec.getY() - ref - layout.height, this.getWidth(), layout.height + 10, "DescLabel");

		descTextView.setWrappedText(VersionString );
		descTextView.setHAlignment(CB_UI_Base.GL_UI.Controls.Label.HAlignment.CENTER);
		this.addChild(descTextView);

		Drawable ProgressBack = new NinePatchDrawable(atlas.createPatch("btn-normal"));
		Drawable ProgressFill = new NinePatchDrawable(atlas.createPatch("progress"));

		float ProgressHeight = Math.max(ProgressBack.getBottomHeight() + ProgressBack.getTopHeight(), ref / 1.5f);

		progress = new ProgressBar(new CB_RectF(0, 0, this.getWidth(), ProgressHeight), "Splash.ProgressBar");

		progress.setBackground(ProgressBack);
		progress.setProgressFill(ProgressFill);
		this.addChild(progress);

		float logoCalcRef = ref * 1.5f;
		float w = logoCalcRef * 2.892655367231638f;
		CB_RectF rec_Mapsforge_Logo = new CB_RectF(w, descTextView.getMinY() - (50 + logoCalcRef / 1.142f), logoCalcRef,
				logoCalcRef / 1.142f);
		CB_RectF rec_FX2_Logo = new CB_RectF(rec_Mapsforge_Logo);

		float margin = UiSizes.that.getMargin() * 6;
		float xPos = (this.getWidth() - (rec_Mapsforge_Logo.getWidth() * 2) - (margin * 2)) / 2;
		final float fXPos = xPos;
		rec_Mapsforge_Logo.setX(xPos);

		boolean resizeHeight=false;
		
		Mapsforge_Logo = new Image(rec_Mapsforge_Logo, "mapsforge_logo",resizeHeight);
		Mapsforge_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("mapsforge_logo")));
		this.addChild(Mapsforge_Logo);

		rec_Mapsforge_Logo.setX(xPos + rec_Mapsforge_Logo.getWidth() + (margin * 2));

		OSM_Logo = new Image(rec_Mapsforge_Logo, "osm_logo",resizeHeight);
		OSM_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("osm_logo")));
		this.addChild(OSM_Logo);

		xPos = fXPos;
		rec_Mapsforge_Logo.setX(xPos);
		rec_Mapsforge_Logo.setY(rec_Mapsforge_Logo.getMinY() - rec_Mapsforge_Logo.getHeight() - margin);

		CGeo_Logo = new Image(rec_Mapsforge_Logo, "cgeo",resizeHeight);
		CGeo_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("cgeo")));
		this.addChild(CGeo_Logo);

		float div = rec_Mapsforge_Logo.getWidth() - rec_Mapsforge_Logo.getHeight();

		rec_Mapsforge_Logo.setWidth(rec_Mapsforge_Logo.getHeight());
		rec_Mapsforge_Logo.setX(xPos + rec_Mapsforge_Logo.getWidth() + (margin * 2) + div);
		rec_Mapsforge_Logo.setY(rec_Mapsforge_Logo.getY() + div);

		CB_Logo = new Image(rec_Mapsforge_Logo, "CB_Icon",resizeHeight);
		CB_Logo.setDrawable(new SpriteDrawable(atlas.createSprite("CB_Icon")));
		this.addChild(CB_Logo);

	}

	/**
	 * Step 3 <br>
	 * Load Translations
	 */
	private void ini_Translations()
	{
		log.debug("ini_Translations");
		new Translation("data", FileType.Internal);
		try
		{
			Translation.LoadTranslation("data/lang/en-GB/strings.ini");
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
		log.debug("ini_Sprites");
		ResourceCache.LoadSprites(false);
		GL_UISizes.initial(UI_Size_Base.that.getWindowWidth(), UI_Size_Base.that.getWindowHeight());
	}

	/**
	 * Step 5 <br>
	 * show select DB Dialog
	 */
	private void ini_SelectDB()
	{

	}

	/**
	 * Step 6<br>
	 * Load Cache DB3
	 */
	private void ini_CacheDB()
	{
		log.debug("ini_CacheDB");
		// chk if exist filter preset splitter "#" and Replace

	}

	/**
	 * Step 7 <br>
	 * chk installed map packs/layers
	 */
	private void ini_MapPaks()
	{
		log.debug("ini_MapPaks");
		ManagerBase.Manager.initialMapPacks();
	}

	/**
	 * Last Step <br>
	 * Show TabMainView
	 */
	private void ini_TabMainView()
	{

		if (splashEndTime > System.currentTimeMillis() && !nextClicked)
		{
			this.removeChild(progress);
			GL.that.renderOnce();
			return;
		}

		log.debug("ini_TabMainView");
		GL.that.removeRenderView(this);
		((Ex) GL.that).switchToMainView();

		GL.setIsInitial();
	}

	@Override
	public void dispose()
	{
		this.removeChildsDirekt();

		if (descTextView != null) descTextView.dispose();

		if (Mapsforge_Logo != null) Mapsforge_Logo.dispose();
		if (progress != null) progress.dispose();
		if (atlas != null) atlas.dispose();

		descTextView = null;

		Mapsforge_Logo = null;
		progress = null;
		atlas = null;

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		nextClicked = true;
		return super.onTouchDown(x, y, pointer, button);
	}

}

package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CategoryDAO;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CoordinateButton;
import CB_Core.GL_UI.Controls.CoordinateButton.CoordinateChangeListner;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class SearchOverPosition extends ActivityBase
{
	private Button bOK, bCancel, btnPlus, btnMinus;
	private Label lblTitle, lblRadius, lblRadiusEinheit, lblMarkerPos, lblExcludeFounds, lblOnlyAvible, lblExcludeHides;
	private Image gsLogo;
	private CoordinateButton coordBtn;
	private chkBox checkBoxExcludeFounds, checkBoxOnlyAvible, checkBoxExcludeHides;
	private EditWrapedTextField Radius;
	private float lineHeight;
	private MultiToggleButton tglBtnGPS, tglBtnMap;
	private Coordinate actSearchPos;
	private volatile Thread thread;
	private disable dis;
	private Box box;
	private boolean importRuns = false;

	private SearchOverPosition that;

	/**
	 * 0=GPS, 1= Map, 2= Manuell
	 */
	private int searcheState = 0;

	public SearchOverPosition()
	{
		super(ActivityRec(), "searchOverPosActivity");
		that = this;
		lineHeight = UiSizes.getButtonHeight();

		createOkCancelBtn();
		createBox();
		createTitleLine();
		createRadiusLine();
		createChkBoxLines();
		createToggleButtonLine();
		createCoordButton();

		initialContent();
	}

	private void createOkCancelBtn()
	{
		bOK = new Button(Left, Left, innerWidth / 2, UiSizes.getButtonHeight(), "OK Button");
		bCancel = new Button(bOK.getMaxX(), Left, innerWidth / 2, UiSizes.getButtonHeight(), "Cancel Button");

		// Translations
		bOK.setText(GlobalCore.Translations.Get("import"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				ImportNow();
				return true;
			}

		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (importRuns)
				{
					// breche den Import Thread ab
					if (thread != null) thread.interrupt();
				}
				else
				{
					finish();
				}
				return true;
			}
		});

	}

	private void createBox()
	{
		box = new Box(ActivityRec(), "ScrollBox");
		this.addChild(box);
		box.setHeight(this.height - lineHeight - bOK.getMaxY() - margin - margin);
		box.setY(bOK.getMaxY() + margin);
		box.setBackground(this.getBackground());
	}

	private void createTitleLine()
	{

		float lineHeight = UiSizes.getButtonHeight() * 0.75f;

		gsLogo = new Image(width - Left - Right - margin - lineHeight, this.height - Top - lineHeight - margin, lineHeight, lineHeight, "");
		gsLogo.setDrawable(new SpriteDrawable(SpriteCache.Icons.get(35)));
		this.addChild(gsLogo);

		lblTitle = new Label(Left + margin, this.height - Top - lineHeight - margin, width - Left - Right - margin - gsLogo.getWidth(),
				lineHeight, "TitleLabel");
		lblTitle.setFont(Fonts.getBig());
		lblTitle.setText(GlobalCore.Translations.Get("importCachesOverPosition"));
		this.addChild(lblTitle);

	}

	private void createRadiusLine()
	{
		String sRadius = GlobalCore.Translations.Get("Radius");
		String sEinheit = Config.settings.ImperialUnits.getValue() ? "mi" : "km";

		float wRadius = Fonts.Measure(sRadius).width;
		float wEinheit = Fonts.Measure(sEinheit).width;

		float y = box.getHeight() - margin - lineHeight;

		lblRadius = new Label(margin, y, wRadius, lineHeight, "");
		lblRadius.setText(sRadius);
		box.addChild(lblRadius);

		CB_RectF rec = new CB_RectF(lblRadius.getMaxX() + margin, y, UiSizes.getButtonWidthWide(), lineHeight);
		Radius = new EditWrapedTextField(this, rec, "");
		box.addChild(Radius);

		lblRadiusEinheit = new Label(Radius.getMaxX(), y, wEinheit, lineHeight, "");
		lblRadiusEinheit.setText(sEinheit);
		box.addChild(lblRadiusEinheit);

		btnMinus = new Button(lblRadiusEinheit.getMaxX() + (margin * 3), y, lineHeight, lineHeight, "");
		btnMinus.setText("-");
		box.addChild(btnMinus);

		btnPlus = new Button(btnMinus.getMaxX() + (margin * 2), y, lineHeight, lineHeight, "");
		btnPlus.setText("+");
		box.addChild(btnPlus);

	}

	private void createChkBoxLines()
	{
		checkBoxOnlyAvible = new chkBox("");
		checkBoxOnlyAvible.setPos(margin, Radius.getY() - margin - checkBoxOnlyAvible.getHeight());
		box.addChild(checkBoxOnlyAvible);

		checkBoxExcludeHides = new chkBox("");
		checkBoxExcludeHides.setPos(margin, checkBoxOnlyAvible.getY() - margin - checkBoxExcludeHides.getHeight());
		box.addChild(checkBoxExcludeHides);

		checkBoxExcludeFounds = new chkBox("");
		checkBoxExcludeFounds.setPos(margin, checkBoxExcludeHides.getY() - margin - checkBoxExcludeFounds.getHeight());
		box.addChild(checkBoxExcludeFounds);

		lblOnlyAvible = new Label(checkBoxOnlyAvible, "");
		lblOnlyAvible.setX(checkBoxOnlyAvible.getMaxX() + margin);
		lblOnlyAvible.setWidth(this.width - margin - checkBoxOnlyAvible.getMaxX() - margin);
		lblOnlyAvible.setText(GlobalCore.Translations.Get("SearchOnlyAvible"));
		box.addChild(lblOnlyAvible);

		lblExcludeHides = new Label(checkBoxExcludeHides, "");
		lblExcludeHides.setX(checkBoxOnlyAvible.getMaxX() + margin);
		lblExcludeHides.setWidth(this.width - margin - checkBoxExcludeHides.getMaxX() - margin);
		lblExcludeHides.setText(GlobalCore.Translations.Get("SearchWithoutOwns"));
		box.addChild(lblExcludeHides);

		lblExcludeFounds = new Label(checkBoxExcludeFounds, "");
		lblExcludeFounds.setX(checkBoxOnlyAvible.getMaxX() + margin);
		lblExcludeFounds.setWidth(this.width - margin - checkBoxExcludeFounds.getMaxX() - margin);
		lblExcludeFounds.setText(GlobalCore.Translations.Get("SearchWithoutFounds"));
		box.addChild(lblExcludeFounds);

	}

	private void createToggleButtonLine()
	{
		float y = lblExcludeFounds.getY() - margin - UiSizes.getButtonHeight();

		tglBtnGPS = new MultiToggleButton(Left, y, innerWidth / 2, UiSizes.getButtonHeight(), "");
		tglBtnMap = new MultiToggleButton(tglBtnGPS.getMaxX(), y, innerWidth / 2, UiSizes.getButtonHeight(), "");

		tglBtnGPS.setFont(Fonts.getSmall());
		tglBtnMap.setFont(Fonts.getSmall());

		MultiToggleButton.initialOn_Off_ToggleStates(tglBtnGPS, GlobalCore.Translations.Get("FromGps"),
				GlobalCore.Translations.Get("FromGps"));
		MultiToggleButton.initialOn_Off_ToggleStates(tglBtnMap, GlobalCore.Translations.Get("FromMap"),
				GlobalCore.Translations.Get("FromMap"));

		box.addChild(tglBtnGPS);
		box.addChild(tglBtnMap);

		if (MapView.that == null) tglBtnMap.disable();

	}

	private void createCoordButton()
	{
		CB_RectF rec = new CB_RectF(margin, tglBtnGPS.getY() - margin - lineHeight, this.width - (margin * 2), lineHeight);
		lblMarkerPos = new Label(rec, "");
		lblMarkerPos.setText(GlobalCore.Translations.Get("CurentMarkerPos"));
		box.addChild(lblMarkerPos);

		coordBtn = new CoordinateButton(rec, name, null);
		coordBtn.setY(lblMarkerPos.getY() - margin - lineHeight);
		box.addChild(coordBtn);

	}

	private void initialContent()
	{

		btnPlus.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				incrementRadius(1);
				return true;
			}

		});

		btnMinus.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				incrementRadius(-1);
				return true;
			}
		});

		tglBtnGPS.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				actSearchPos = GlobalCore.LastPosition;
				setToggleBtnState(0);
				return true;
			}
		});

		tglBtnMap.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (MapView.that == null)
				{
					actSearchPos = new Coordinate();
					actSearchPos.setLatitude(Config.settings.MapInitLatitude.getValue());
					actSearchPos.setLongitude(Config.settings.MapInitLongitude.getValue());
				}
				else
				{
					actSearchPos = MapView.that.center;
				}

				setToggleBtnState(1);
				return true;
			}
		});

		coordBtn.setCoordinateChangedListner(new CoordinateChangeListner()
		{

			@Override
			public void coordinateChanged(Coordinate coord)
			{
				if (coord != null)
				{
					actSearchPos = coord;
					setToggleBtnState(2);
				}
				that.show();
			}
		});

		if (MapView.that != null && MapView.that.isVisible())
		{
			actSearchPos = MapView.that.center;
			searcheState = 1;
		}
		else
		{
			actSearchPos = GlobalCore.LastPosition;
			searcheState = 0;
		}

		checkBoxExcludeFounds.setChecked(Config.settings.SearchWithoutFounds.getValue());
		checkBoxOnlyAvible.setChecked(Config.settings.SearchOnlyAvible.getValue());
		checkBoxExcludeHides.setChecked(Config.settings.SearchWithoutOwns.getValue());
		Radius.setText(String.valueOf(Config.settings.lastSearchRadius.getValue()));
		setToggleBtnState();

	}

	private void incrementRadius(int value)
	{
		try
		{
			int ist = Integer.parseInt(Radius.getText().toString());
			ist += value;

			if (ist > 100) ist = 100;
			if (ist < 1) ist = 1;

			Radius.setText(String.valueOf(ist));
		}
		catch (NumberFormatException e)
		{

		}
	}

	/**
	 * 0=GPS, 1= Map, 2= Manuell
	 */
	private void setToggleBtnState(int value)
	{
		searcheState = value;
		setToggleBtnState();
	}

	private void setToggleBtnState()
	{// 0=GPS, 1= Map, 2= Manuell
		switch (searcheState)
		{
		case 0:
			tglBtnGPS.setState(1);
			tglBtnMap.setState(0);
			break;
		case 1:
			tglBtnGPS.setState(0);
			tglBtnMap.setState(1);
			break;
		case 2:
			tglBtnGPS.setState(0);
			tglBtnMap.setState(0);
			break;

		}
		coordBtn.setCoordinate(actSearchPos);

	}

	private void ImportNow()
	{

		Config.settings.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
		Config.settings.SearchOnlyAvible.setValue(checkBoxOnlyAvible.isChecked());
		Config.settings.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

		int radius = 0;
		try
		{
			radius = Integer.parseInt(Radius.getText().toString());
		}
		catch (NumberFormatException e)
		{
			// Kein Integer
			e.printStackTrace();
		}

		if (radius != 0) Config.settings.lastSearchRadius.setValue(radius);

		Config.AcceptChanges();

		bOK.disable();

		// disable UI
		dis = new disable(box);
		dis.setBackground(getBackground());

		this.addChild(dis, false);

		importRuns = true;
		thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				boolean threadCanceld = false;

				try
				{
					if (actSearchPos != null)
					{

						String accessToken = Config.GetAccessToken();

						// alle per API importierten Caches landen in der Category und
						// GpxFilename
						// API-Import
						// Category suchen, die dazu gehört
						CategoryDAO categoryDAO = new CategoryDAO();
						Category category = categoryDAO.GetCategory(GlobalCore.Categories, "API-Import");
						if (category != null) // should not happen!!!
						{
							GpxFilename gpxFilename = categoryDAO.CreateNewGpxFilename(category, "API-Import");
							if (gpxFilename != null)
							{
								ArrayList<Cache> apiCaches = new ArrayList<Cache>();
								ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
								ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
								CB_Core.Api.SearchForGeocaches.SearchCoordinate searchC = new CB_Core.Api.SearchForGeocaches.SearchCoordinate();

								searchC.withoutFinds = Config.settings.SearchWithoutFounds.getValue();
								searchC.withoutOwn = Config.settings.SearchWithoutOwns.getValue();

								searchC.pos = actSearchPos;
								searchC.distanceInMeters = Config.settings.lastSearchRadius.getValue() * 1000;
								searchC.number = 50;
								CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, apiImages,
										gpxFilename.Id);
								if (apiCaches.size() > 0)
								{
									GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
								}

							}
						}
					}
				}
				catch (InterruptedException e)
				{
					// Thread abgebrochen!
					threadCanceld = true;
				}

				if (!threadCanceld)
				{
					CachListChangedEventList.Call();

					finish();
				}
				else
				{

					// Notify Map
					if (MapView.that != null) MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);

					bOK.enable();
				}
				importRuns = false;
			}

		});

		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();

	}

	private class disable extends Box
	{

		public disable(CB_RectF rec)
		{
			super(rec, "");

			float size = rec.getHalfWidth() / 2;
			float halfSize = rec.getHalfWidth() / 4;

			CB_RectF imageRec = new CB_RectF(this.halfWidth - halfSize, this.halfHeight - halfSize, size, size);

			iconImage = new Image(imageRec, "MsgBoxIcon");
			iconImage.setDrawable(new SpriteDrawable(SpriteCache.Icons.get(51)));
			iconImage.setOrigin(imageRec.getHalfWidth(), imageRec.getHalfHeight());

			this.addChild(iconImage);

			rotateAngle = 0;

			RotateTimer = new Timer();

			RotateTimer.schedule(rotateTimertask, 60, 60);

		}

		private Drawable back;
		private Image iconImage;

		Timer RotateTimer;
		float rotateAngle = 0;
		TimerTask rotateTimertask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (iconImage != null)
				{
					rotateAngle += 5;
					if (rotateAngle > 360) rotateAngle = 0;
					iconImage.setRotate(rotateAngle);
					GL.that.renderOnce("WaitRotateAni");
				}
			}
		};

		public void renderWithoutScissor(SpriteBatch batch)
		{
			if (drawableBackground != null)
			{
				back = drawableBackground;
				drawableBackground = null;
			}

			if (back != null)
			{
				Color c = batch.getColor();

				float a = c.a;
				float r = c.r;
				float g = c.g;
				float b = c.b;

				Color trans = new Color(0, 0.3f, 0, 0.25f);
				batch.setColor(trans);
				back.draw(batch, 0, 0, this.width, this.height);

				batch.setColor(new Color(r, g, b, a));

			}
		}

		@Override
		public void onHide()
		{
			RotateTimer.cancel();
			iconImage.dispose();
		}

		// alle Touch events abfangen

		@Override
		public boolean onTouchDown(int x, int y, int pointer, int button)
		{
			return true;
		}

		@Override
		public boolean onLongClick(int x, int y, int pointer, int button)
		{
			return true;
		}

		@Override
		public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
		{
			return true;
		}

		@Override
		public boolean onTouchUp(int x, int y, int pointer, int button)
		{
			return true;
		}

		@Override
		public boolean click(int x, int y, int pointer, int button)
		{
			return true;
		}

	}

}

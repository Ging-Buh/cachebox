package CB_Core.GL_UI.Views;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Events.GpsStateChangeEvent;
import CB_Core.Events.GpsStateChangeEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.SatBarChart;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Locator.GPS;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class AboutView extends CB_View_Base implements SelectedCacheEvent, GpsStateChangeEvent
{
	Label descTextView, CachesFoundLabel, WP, Coord, lblGPS, Gps, lblAccuracy, Accuracy, lblWP, lblCoord, lblCurrent, Current;
	Image CB_Logo;
	float margin;
	private SatBarChart chart;
	private int transFounds = -1;
	CancelWaitDialog pd;
	AboutView Me;

	public AboutView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		Me = this;
		registerSkinChangedEvent();
	}

	@Override
	public void onShow()
	{
		super.onShow();

		// add Event Handler
		SelectedCacheEventList.Add(this);
		GpsStateChangeEventList.Add(this);

		if (!this.isInitial) Initial();

		if (chart != null) chart.onShow();
		refreshText();

		platformConector.hideForDialog();
	}

	@Override
	public void onHide()
	{
		super.onHide();

		// remove Event Handler
		SelectedCacheEventList.Remove(this);
		GpsStateChangeEventList.Remove(this);

		if (chart != null) chart.onHide();
	}

	@Override
	protected void Initial()
	{
		this.removeChilds();

		this.setBackground(SpriteCache.AboutBack);
		float ref = UiSizes.getWindowHeight() / 13;
		margin = UiSizes.getMargin();
		CB_RectF CB_LogoRec = new CB_RectF(this.halfWidth - (ref * 2.5f), this.height - ((ref * 5) / 4.11f) - ref - margin, ref * 5,
				(ref * 5) / 4.11f);
		CB_Logo = new Image(CB_LogoRec, "CB_Logo");
		CB_Logo.setDrawable(new SpriteDrawable(SpriteCache.getSpriteDrawable("cachebox-logo")));
		this.addChild(CB_Logo);

		String VersionString = GlobalCore.getVersionString();
		TextBounds bounds = Fonts.getSmall().getMultiLineBounds(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.AboutMsg);
		descTextView = new Label(0, CB_Logo.getY() - margin - margin - margin - bounds.height, this.width, bounds.height + margin,
				"DescLabel");
		descTextView.setFont(Fonts.getSmall());

		descTextView.setWrappedText(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.AboutMsg, HAlignment.CENTER);
		this.addChild(descTextView);

		CachesFoundLabel = new Label("CachesFoundLabel");
		CachesFoundLabel.setWidth(this.width);
		CachesFoundLabel.setTextColor(Fonts.getLinkFontColor());
		CachesFoundLabel.setHAlignment(HAlignment.CENTER);

		CachesFoundLabel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				GL_MsgBox.Show(GlobalCore.Translations.Get("LoadFounds"), GlobalCore.Translations.Get("AdjustFinds"),
						MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new OnMsgBoxClickListener()
						{

							@Override
							public boolean onClick(int which)
							{
								// Behandle das ergebniss
								switch (which)
								{
								case 1:

									pd = CancelWaitDialog.ShowWait(GlobalCore.Translations.Get("LoadFounds"), new IcancelListner()
									{

										@Override
										public void isCanceld()
										{
											// TODO Auto-generated method stub

										}
									}, new Runnable()
									{

										@Override
										public void run()
										{
											transFounds = GroundspeakAPI.GetCachesFound(Config.GetAccessToken());
											pd.close();

											if (transFounds > -1)
											{
												String Text = GlobalCore.Translations.Get("FoundsSetTo", String.valueOf(transFounds));
												GL_MsgBox.Show(Text, GlobalCore.Translations.Get("LoadFinds!"), MessageBoxButtons.OK,
														MessageBoxIcon.GC_Live, null);

												Config.settings.FoundOffset.setValue(transFounds);
												Config.AcceptChanges();
												AboutView.this.refreshText();
											}
											else
											{
												GL_MsgBox.Show(GlobalCore.Translations.Get("LogInErrorLoadFinds"), "",
														MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);
											}
										}
									});

									break;
								case 3:
									NumerikInputBox.Show(GlobalCore.Translations.Get("TelMeFounds"),
											GlobalCore.Translations.Get("AdjustFinds"), CB_Core.Config.settings.FoundOffset.getValue(),
											DialogListner);
									break;

								}
								return true;
							}
						});

				return true;
			}
		});

		this.addChild(CachesFoundLabel);

		createTable();

		refreshText();
	}

	private void createTable()
	{
		float leftMaxWidth = 0;
		CB_RectF lblRec = new CB_RectF(0, 0, UiSizes.getButtonWidth(), UiSizes.getButtonHeight() / 2.5f);

		lblGPS = new Label(lblRec, "lblGPS");
		leftMaxWidth = Math.max(leftMaxWidth, lblGPS.setText(GlobalCore.Translations.Get("gps")).width);

		lblAccuracy = new Label(lblRec, "lblAccuracy");
		leftMaxWidth = Math.max(leftMaxWidth, lblAccuracy.setText(GlobalCore.Translations.Get("accuracy")).width);

		lblWP = new Label(lblRec, "lblWP");
		leftMaxWidth = Math.max(leftMaxWidth, lblWP.setText(GlobalCore.Translations.Get("waypoint")).width);

		lblCoord = new Label(lblRec, "lblCord");
		leftMaxWidth = Math.max(leftMaxWidth, lblCoord.setText(GlobalCore.Translations.Get("coordinate")).width);

		lblCurrent = new Label(lblRec, "lblCurrent");
		leftMaxWidth = Math.max(leftMaxWidth, lblCurrent.setText(GlobalCore.Translations.Get("current")).width);

		// set all lbl to the same max width + margin
		leftMaxWidth += margin;
		lblGPS.setWidth(leftMaxWidth);
		lblAccuracy.setWidth(leftMaxWidth);
		lblWP.setWidth(leftMaxWidth);
		lblCoord.setWidth(leftMaxWidth);
		lblCurrent.setWidth(leftMaxWidth);

		// set lbl position on Screen
		lblCurrent.setPos(margin, margin);
		lblCoord.setPos(margin, lblCurrent.getMaxY());
		lblWP.setPos(margin, lblCoord.getMaxY());
		lblAccuracy.setPos(margin, lblWP.getMaxY());
		lblGPS.setPos(margin, lblAccuracy.getMaxY());

		// add to Screen
		this.addChild(lblGPS);
		this.addChild(lblAccuracy);
		this.addChild(lblWP);
		this.addChild(lblCoord);
		this.addChild(lblCurrent);

		// ##############################
		// create Value Label
		lblRec.setX(lblGPS.getMaxX() + margin);
		lblRec.setWidth(this.width - margin - lblGPS.getMaxX());

		Gps = new Label(lblRec, "GPS");
		Accuracy = new Label(lblRec, "Accuracy");
		WP = new Label(lblRec, "WP");
		Coord = new Label(lblRec, "Cord");
		Current = new Label(lblRec, "Current");

		// set Y Pos
		Gps.setY(lblGPS.getY());
		Accuracy.setY(lblAccuracy.getY());
		WP.setY(lblWP.getY());
		Coord.setY(lblCoord.getY());
		Current.setY(lblCurrent.getY());

		// set LinkColor
		WP.setText("-", Fonts.getNormal(), Fonts.getLinkFontColor());

		WP.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (GlobalCore.getSelectedCache() == null) return true;
				platformConector.callUrl(GlobalCore.getSelectedCache().Url);
				return true;
			}
		});

		// add to Screen
		this.addChild(Gps);
		this.addChild(Accuracy);
		this.addChild(WP);
		this.addChild(Coord);
		this.addChild(Current);

		// create Sat Chart
		float l = margin * 2;
		chart = new SatBarChart(new CB_RectF(l, Gps.getMaxY() + l, this.width - l - l, CachesFoundLabel.getY() - Gps.getMaxY()),
				"Sat Chart");
		chart.setDrawWithAlpha(true);
		this.addChild(chart);

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		super.onRezised(rec);
		setYpositions();
	}

	@Override
	protected void SkinIsChanged()
	{
		Initial();
		setYpositions();
	}

	private void setYpositions()
	{
		CB_Logo.setY(this.height - (margin * 2) - CB_Logo.getHeight());
		descTextView.setY(CB_Logo.getY() - margin - margin - margin - descTextView.getHeight());
		CachesFoundLabel.setY(descTextView.getY() - CachesFoundLabel.getHeight() + margin);
		chart.setHeight(CachesFoundLabel.getY() - Gps.getMaxY());
	}

	public void refreshText()
	{
		CachesFoundLabel
				.setText(GlobalCore.Translations.Get("caches_found") + " " + String.valueOf(Config.settings.FoundOffset.getValue()));
		if (GlobalCore.getSelectedCache() != null) if (GlobalCore.getSelectedWaypoint() != null)
		{
			WP.setText(GlobalCore.getSelectedWaypoint().GcCode);
			Coord.setText(GlobalCore.FormatLatitudeDM(GlobalCore.getSelectedWaypoint().Pos.getLatitude()) + " "
					+ GlobalCore.FormatLongitudeDM(GlobalCore.getSelectedWaypoint().Pos.getLongitude()));
		}
		else
		{
			WP.setText(GlobalCore.getSelectedCache().GcCode);
			Coord.setText(GlobalCore.FormatLatitudeDM(GlobalCore.getSelectedCache().Pos.getLatitude()) + " "
					+ GlobalCore.FormatLongitudeDM(GlobalCore.getSelectedCache().Pos.getLongitude()));
		}

		GL.that.renderOnce("About refresh Text");
	}

	protected final returnValueListner DialogListner = new returnValueListner()
	{
		@Override
		public void returnValue(int value)
		{
			Config.settings.FoundOffset.setValue(value);
			Config.AcceptChanges();
			AboutView.this.refreshText();
		}

		@Override
		public void cancelClicked()
		{

		}

	};

	@Override
	public void GpsStateChanged()
	{
		if ((GlobalCore.Locator != null) && (GlobalCore.Locator.getLocation() != null) && (GlobalCore.Locator.getLocation().hasAccuracy()))
		{
			int radius = (int) GlobalCore.Locator.getLocation().getAccuracy();
			Accuracy.setText("+/- " + String.valueOf(radius) + "m (" + GlobalCore.Locator.ProviderString() + ")");
		}
		else
		{
			Accuracy.setText("");
		}
		if ((GlobalCore.Locator != null) && GlobalCore.Locator.getLocation() != null)
		{
			Current.setText(GlobalCore.FormatLatitudeDM(GlobalCore.Locator.getLocation().getLatitude()) + " "
					+ GlobalCore.FormatLongitudeDM(GlobalCore.Locator.getLocation().getLongitude()));
			Gps.setText(GPS.getSatAndFix() + "   " + GlobalCore.Translations.Get("alt") + " " + GlobalCore.Locator.getAltString());
		}

		if (GlobalCore.Locator == null)
		{
			Gps.setText(GlobalCore.Translations.Get("not_detected"));
			return;
		}
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		GL.that.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				refreshText();
			}
		});
	}

}

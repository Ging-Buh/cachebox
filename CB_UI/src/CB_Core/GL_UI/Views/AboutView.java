package CB_Core.GL_UI.Views;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Enums.WrapType;
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
import CB_Core.GL_UI.Controls.Animation.DownloadAnimation;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_Core.GL_UI.Controls.PopUps.ConnectionError;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Core.Util.UnitFormatter;
import CB_Locator.GPS;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Events.GpsStateChangeEvent;
import CB_Locator.Events.GpsStateChangeEventList;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class AboutView extends CB_View_Base implements SelectedCacheEvent, GpsStateChangeEvent, PositionChangedEvent
{
	Label descTextView, CachesFoundLabel, WP, Coord, lblGPS, Gps, lblAccuracy, Accuracy, lblWP, lblCoord, lblCurrent, Current;
	Image CB_Logo;
	float margin;
	private SatBarChart chart;
	private int result = -1;
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
		PositionChangedEventList.Add(this);

		PositionChanged();

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
		PositionChangedEventList.Remove(this);

		if (chart != null) chart.onHide();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);

		if (this.getBackground() == null) Initial();
	}

	@Override
	protected void Initial()
	{
		this.removeChilds();

		this.setBackground(SpriteCache.AboutBack);
		float ref = UI_Size_Base.that.getWindowHeight() / 13;
		margin = UI_Size_Base.that.getMargin();
		CB_RectF CB_LogoRec = new CB_RectF(this.halfWidth - (ref * 2.5f), this.height - ((ref * 5) / 4.11f) - ref - margin - margin,
				ref * 5, (ref * 5) / 4.11f);
		Logger.DEBUG("CB_Logo" + CB_LogoRec.toString());
		CB_Logo = new Image(CB_LogoRec, "CB_Logo");
		CB_Logo.setDrawable(new SpriteDrawable(SpriteCache.getSpriteDrawable("cachebox-logo")));
		this.addChild(CB_Logo);

		String VersionString = GlobalCore.getVersionString();
		TextBounds bounds = Fonts.getSmall().getMultiLineBounds(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.AboutMsg);
		descTextView = new Label(0, CB_Logo.getY() - margin - margin - margin - bounds.height, this.width, bounds.height + margin,
				"DescLabel");
		descTextView.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

		descTextView.setWrappedText(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.AboutMsg);
		this.addChild(descTextView);

		CachesFoundLabel = new Label("", Fonts.getNormal(), Fonts.getLinkFontColor(), WrapType.SINGLELINE).setHAlignment(HAlignment.CENTER);
		CachesFoundLabel.setWidth(width);

		CachesFoundLabel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				GL_MsgBox.Show(Translation.Get("LoadFounds"), Translation.Get("AdjustFinds"), MessageBoxButtons.YesNo,
						MessageBoxIcon.GC_Live, new OnMsgBoxClickListener()
						{

							@Override
							public boolean onClick(int which, Object data)
							{
								// Behandle das ergebniss
								switch (which)
								{
								case 1:

									pd = CancelWaitDialog.ShowWait(Translation.Get("LoadFounds"), DownloadAnimation.GetINSTANCE(),
											new IcancelListner()
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
													result = GroundspeakAPI.GetCachesFound();
													pd.close();

													if (result > -1)
													{
														String Text = Translation.Get("FoundsSetTo", String.valueOf(result));
														GL_MsgBox.Show(Text, Translation.Get("LoadFinds!"), MessageBoxButtons.OK,
																MessageBoxIcon.GC_Live, null);

														Config.settings.FoundOffset.setValue(result);
														Config.AcceptChanges();
														AboutView.this.refreshText();
													}
													if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
													{
														GL.that.Toast(ConnectionError.INSTANCE);
													}
													if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
													{
														GL.that.Toast(ApiUnavailable.INSTANCE);
													}
												}
											});

									break;
								case 3:
									NumerikInputBox.Show(Translation.Get("TelMeFounds"), Translation.Get("AdjustFinds"),
											CB_Core.Config.settings.FoundOffset.getValue(), DialogListner);
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
		CB_RectF lblRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight() / 2.5f);

		lblGPS = new Label(lblRec, "lblGPS");
		leftMaxWidth = Math.max(leftMaxWidth, lblGPS.setText(Translation.Get("gps")).getTextWidth());

		lblAccuracy = new Label(lblRec, "lblAccuracy");
		leftMaxWidth = Math.max(leftMaxWidth, lblAccuracy.setText(Translation.Get("accuracy")).getTextWidth());

		lblWP = new Label(lblRec, "lblWP");
		leftMaxWidth = Math.max(leftMaxWidth, lblWP.setText(Translation.Get("waypoint")).getTextWidth());

		lblCoord = new Label(lblRec, "lblCord");
		leftMaxWidth = Math.max(leftMaxWidth, lblCoord.setText(Translation.Get("coordinate")).getTextWidth());

		lblCurrent = new Label(lblRec, "lblCurrent");
		leftMaxWidth = Math.max(leftMaxWidth, lblCurrent.setText(Translation.Get("current")).getTextWidth());

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
		WP = new Label("-", Fonts.getNormal(), Fonts.getLinkFontColor(), WrapType.SINGLELINE);
		WP.setRec(lblRec);
		Coord = new Label(lblRec, "Cord");
		Current = new Label(lblRec, "Current");

		// set Y Pos
		Gps.setY(lblGPS.getY());
		Accuracy.setY(lblAccuracy.getY());
		WP.setY(lblWP.getY());
		Coord.setY(lblCoord.getY());
		Current.setY(lblCurrent.getY());

		// set LinkColor

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
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
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
		if (CB_Logo != null) CB_Logo.setY(this.height - (margin * 2) - CB_Logo.getHeight());
		if (descTextView != null) descTextView.setY(CB_Logo.getY() - margin - margin - margin - descTextView.getHeight());
		if (CachesFoundLabel != null) CachesFoundLabel.setY(descTextView.getY() - CachesFoundLabel.getHeight() + margin);
		if (chart != null) chart.setHeight(CachesFoundLabel.getY() - Gps.getMaxY());
	}

	public void refreshText()
	{
		if (WP == null || CachesFoundLabel == null) return;
		CachesFoundLabel.setText(Translation.Get("caches_found") + " " + String.valueOf(Config.settings.FoundOffset.getValue()));

		if (GlobalCore.getSelectedCache() != null) if (GlobalCore.getSelectedWaypoint() != null)
		{
			WP.setText(GlobalCore.getSelectedWaypoint().GcCode);
			Coord.setText(UnitFormatter.FormatLatitudeDM(GlobalCore.getSelectedWaypoint().Pos.getLatitude()) + " "
					+ UnitFormatter.FormatLongitudeDM(GlobalCore.getSelectedWaypoint().Pos.getLongitude()));
		}
		else
		{
			WP.setText(GlobalCore.getSelectedCache().GcCode);
			Coord.setText(UnitFormatter.FormatLatitudeDM(GlobalCore.getSelectedCache().Pos.getLatitude()) + " "
					+ UnitFormatter.FormatLongitudeDM(GlobalCore.getSelectedCache().Pos.getLongitude()));
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
		if (Locator.getCoordinate().hasAccuracy())
		{
			int radius = (int) Locator.getCoordinate().getAccuracy();
			if (Accuracy != null) Accuracy.setText("+/- " + String.valueOf(radius) + "m (" + Locator.getProvider().toString() + ")");
		}
		else
		{
			if (Accuracy != null) Accuracy.setText("?");
		}
		if (Locator.getProvider() == ProviderType.GPS || Locator.getProvider() == ProviderType.Network)
		{
			if (Current != null) Current.setText(UnitFormatter.FormatLatitudeDM(Locator.getLatitude()) + " "
					+ UnitFormatter.FormatLongitudeDM(Locator.getLongitude()));
			if (Gps != null) Gps.setText(GPS.getSatAndFix() + "   " + Translation.Get("alt") + " " + Locator.getAltStringWithCorection());
		}
		else
		{
			if (Gps != null) Gps.setText(Translation.Get("not_detected"));
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

	@Override
	public void PositionChanged()
	{
		GpsStateChanged();
	}

	@Override
	public void OrientationChanged()
	{
	}

	@Override
	public String getReceiverName()
	{
		return "AboutView";
	}

	@Override
	public Priority getPriority()
	{
		return Priority.Low;
	}

	@Override
	public void SpeedChanged()
	{
	}

}
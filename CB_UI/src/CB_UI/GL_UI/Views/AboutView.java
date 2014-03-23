package CB_UI.GL_UI.Views;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.GPS;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Events.GpsStateChangeEvent;
import CB_Locator.Events.GpsStateChangeEventList;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.GL_UI.Controls.SatBarChart;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Log.Logger;
import CB_Utils.Util.UnitFormatter;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class AboutView extends CB_View_Base implements SelectedCacheEvent, GpsStateChangeEvent, PositionChangedEvent
{
	Label descTextView, CachesFoundLabel, WaypointLabel, CoordLabel, lblGPS, Gps, lblAccuracy, Accuracy, lblWP, lblCoord, lblCurrent,
			Current;
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
		createControls();
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
	protected void render(Batch batch)
	{
		super.render(batch);

		if (this.getBackground() == null) Initial();
	}

	private void createControls()
	{
		this.removeChilds();

		this.setBackground(SpriteCacheBase.AboutBack);
		float ref = UI_Size_Base.that.getWindowHeight() / 13;
		margin = UI_Size_Base.that.getMargin();
		CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.getHeight() - ((ref * 5) / 4.11f) - ref - margin
				- margin, ref * 5, (ref * 5) / 4.11f);
		Logger.DEBUG("CB_Logo" + CB_LogoRec.toString());
		CB_Logo = new Image(CB_LogoRec, "CB_Logo");
		CB_Logo.setDrawable(new SpriteDrawable(SpriteCacheBase.getSpriteDrawable("cachebox-logo")));
		this.addChild(CB_Logo);

		String VersionString = GlobalCore.getVersionString();
		TextBounds bounds = Fonts.getSmall().getMultiLineBounds(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.AboutMsg);
		descTextView = new Label(0, CB_Logo.getY() - margin - margin - margin - bounds.height, this.getWidth(), bounds.height + margin,
				"DescLabel");
		descTextView.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

		descTextView.setWrappedText(VersionString + GlobalCore.br + GlobalCore.br + GlobalCore.AboutMsg);
		this.addChild(descTextView);

		CachesFoundLabel = new Label("", Fonts.getNormal(), COLOR.getLinkFontColor(), WrapType.SINGLELINE).setHAlignment(HAlignment.CENTER);
		CachesFoundLabel.setWidth(getWidth());

		CachesFoundLabel.setOnClickListener(new OnClickListener()
		{
			GL_MsgBox ms;

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				ms = GL_MsgBox.Show(Translation.Get("LoadFounds"), Translation.Get("AdjustFinds"), MessageBoxButtons.YesNo,
						MessageBoxIcon.GC_Live, new OnMsgBoxClickListener()
						{

							@Override
							public boolean onClick(int which, Object data)
							{
								// Behandle das ergebniss
								switch (which)
								{
								case 1:
									ms.close();
									pd = CancelWaitDialog.ShowWait(Translation.Get("LoadFounds"), DownloadAnimation.GetINSTANCE(),
											new IcancelListner()
											{

												@Override
												public void isCanceld()
												{

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

														Config.FoundOffset.setValue(result);
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
									ms.close();
									GL.that.RunOnGL(new IRunOnGL()
									{

										@Override
										public void run()
										{
											NumerikInputBox.Show(Translation.Get("TelMeFounds"), Translation.Get("AdjustFinds"),
													CB_UI.Config.FoundOffset.getValue(), DialogListner);
										}
									});

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
		lblRec.setWidth(this.getWidth() - margin - lblGPS.getMaxX());

		Gps = new Label(lblRec, "GPS");
		Accuracy = new Label(lblRec, "Accuracy");
		WaypointLabel = new Label("-", Fonts.getNormal(), COLOR.getLinkFontColor(), WrapType.SINGLELINE);
		WaypointLabel.setRec(lblRec);
		CoordLabel = new Label(lblRec, "Cord");
		Current = new Label(lblRec, "Current");

		// set Y Pos
		Gps.setY(lblGPS.getY());
		Accuracy.setY(lblAccuracy.getY());
		WaypointLabel.setY(lblWP.getY());
		CoordLabel.setY(lblCoord.getY());
		Current.setY(lblCurrent.getY());

		// set LinkColor

		WaypointLabel.setOnClickListener(new OnClickListener()
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
		this.addChild(WaypointLabel);
		this.addChild(CoordLabel);
		this.addChild(Current);

		// create Sat Chart
		float l = margin * 2;
		chart = new SatBarChart(new CB_RectF(l, Gps.getMaxY() + l, this.getWidth() - l - l, CachesFoundLabel.getY() - Gps.getMaxY()),
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
		createControls();
		setYpositions();
	}

	private void setYpositions()
	{
		if (CB_Logo != null) CB_Logo.setY(this.getHeight() - (margin * 2) - CB_Logo.getHeight());
		if (descTextView != null) descTextView.setY(CB_Logo.getY() - margin - margin - margin - descTextView.getHeight());
		if (CachesFoundLabel != null) CachesFoundLabel.setY(descTextView.getY() - CachesFoundLabel.getHeight() + margin);
		if (chart != null) chart.setHeight(CachesFoundLabel.getY() - Gps.getMaxY());
	}

	public void refreshText()
	{
		if (WaypointLabel == null || CachesFoundLabel == null || CoordLabel == null) return;
		CachesFoundLabel.setText(Translation.Get("caches_found") + " " + String.valueOf(Config.FoundOffset.getValue()));

		Cache selectedCache = GlobalCore.getSelectedCache();
		Waypoint selectedWaypoint = GlobalCore.getSelectedWaypoint();

		if (selectedCache != null)
		{
			if (selectedWaypoint != null)
			{
				WaypointLabel.setText(selectedWaypoint.GcCode);
				CoordLabel.setText(UnitFormatter.FormatLatitudeDM(selectedWaypoint.Pos.getLatitude()) + " "
						+ UnitFormatter.FormatLongitudeDM(selectedWaypoint.Pos.getLongitude()));
			}
			else
			{
				WaypointLabel.setText(selectedCache.GcCode);
				CoordLabel.setText(UnitFormatter.FormatLatitudeDM(selectedCache.Pos.getLatitude()) + " "
						+ UnitFormatter.FormatLongitudeDM(selectedCache.Pos.getLongitude()));
			}
		}
		GL.that.renderOnce("About refresh Text");
	}

	protected final returnValueListner DialogListner = new returnValueListner()
	{
		@Override
		public void returnValue(int value)
		{
			Config.FoundOffset.setValue(value);
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

			if (Accuracy != null) Accuracy.setText("+/- " + UnitFormatter.DistanceString(radius) + " (" + Locator.getProvider().toString()
					+ ")");
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
		GL.that.RunOnGL(new IRunOnGL()
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

	@Override
	protected void Initial()
	{

	}

}

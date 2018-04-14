/* 
 * Copyright (C) 2014-2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI.GL_UI.Views;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

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
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI.GL_UI.Controls.SatBarChart;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox.IReturnValueListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Util.UnitFormatter;

public class AboutView extends CB_View_Base implements SelectedCacheEvent, GpsStateChangeEvent, PositionChangedEvent {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(AboutView.class);
	Label descTextView, CachesFoundLabel, WaypointLabel, CoordLabel, lblGPS, Gps, lblAccuracy, Accuracy, lblWP, lblCoord, lblCurrent, Current;
	Image CB_Logo;
	float margin;
	private SatBarChart chart;
	private int result = -1;
	CancelWaitDialog pd;

	public AboutView(CB_RectF rec, String Name) {
		super(rec, Name);
		registerSkinChangedEvent();
		createControls();
	}

	@Override
	public void onShow() {
		super.onShow();

		// add Event Handler
		SelectedCacheEventList.Add(this);
		GpsStateChangeEventList.Add(this);
		PositionChangedEventList.Add(this);

		PositionChanged();

		if (!this.isInitial)
			Initial();

		if (chart != null)
			chart.onShow();
		refreshText();

		PlatformConnector.hideForDialog();
	}

	@Override
	public void onHide() {
		super.onHide();

		// remove Event Handler
		SelectedCacheEventList.Remove(this);
		GpsStateChangeEventList.Remove(this);
		PositionChangedEventList.Remove(this);

		if (chart != null)
			chart.onHide();
	}

	@Override
	protected void render(Batch batch) {
		super.render(batch);

		if (this.getBackground() == null)
			Initial();
	}

	private void createControls() {
		this.removeChilds();

		if (this.isDisposed())
			return;

		this.setBackground(Sprites.AboutBack);
		float ref = UI_Size_Base.that.getWindowHeight() / 13;
		margin = UI_Size_Base.that.getMargin();
		CB_RectF CB_LogoRec = new CB_RectF(this.getHalfWidth() - (ref * 2.5f), this.getHeight() - ((ref * 5) / 4.11f) - ref - margin - margin, ref * 5, (ref * 5) / 4.11f);
		//Log.debug(log, "CB_Logo" + CB_LogoRec.toString());
		CB_Logo = new Image(CB_LogoRec, "CB_Logo", false);
		CB_Logo.setDrawable(new SpriteDrawable(Sprites.getSpriteDrawable("cachebox-logo")));
		this.addChild(CB_Logo);

		String VersionString = GlobalCore.getVersionString() + GlobalCore.br + GlobalCore.br + GlobalCore.aboutMsg;

		GlyphLayout layout = new GlyphLayout();
		layout.setText(Fonts.getSmall(), VersionString);

		descTextView = new Label(this.name + " descTextView", 0, CB_Logo.getY() - margin - margin - margin - layout.height, this.getWidth(), layout.height + margin);
		descTextView.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

		descTextView.setWrappedText(VersionString);
		this.addChild(descTextView);

		CachesFoundLabel = new Label("", Fonts.getNormal(), COLOR.getLinkFontColor(), WrapType.SINGLELINE).setHAlignment(HAlignment.CENTER);
		CachesFoundLabel.setWidth(getWidth());

		CachesFoundLabel.setOnClickListener(new OnClickListener() {
			GL_MsgBox ms;

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

				ms = GL_MsgBox.Show(Translation.Get("LoadFounds"), Translation.Get("AdjustFinds"), MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new OnMsgBoxClickListener() {

					@Override
					public boolean onClick(int which, Object data) {
						// Behandle das ergebniss
						switch (which) {
						case 1:
							ms.close();
							pd = CancelWaitDialog.ShowWait(Translation.Get("LoadFounds"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

								@Override
								public void isCanceld() {

								}
							}, new cancelRunnable() {

								@Override
								public void run() {
									result = GroundspeakAPI.GetCachesFound(this);
									pd.close();

									if (result > -1) {
										String Text = Translation.Get("FoundsSetTo", String.valueOf(result));
										GL_MsgBox.Show(Text, Translation.Get("LoadFinds!"), MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);

										Config.FoundOffset.setValue(result);
										Config.AcceptChanges();
										AboutView.this.refreshText();
									}
									if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
										GL.that.Toast(ConnectionError.INSTANCE);
									}
									if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
										GL.that.Toast(ApiUnavailable.INSTANCE);
									}
								}

								@Override
								public boolean cancel() {
									// TODO handle cancel
									return false;
								}
							});

							break;
						case 3:
							ms.close();
							GL.that.RunOnGL(new IRunOnGL() {

								@Override
								public void run() {
									NumericInputBox.Show(Translation.Get("TelMeFounds"), Translation.Get("AdjustFinds"), Config.FoundOffset.getValue(), mDialogListener);
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

	private void createTable() {
		float leftMaxWidth = 0;
		CB_RectF lblRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight() / 2.5f);

		lblGPS = new Label(this.name + " lblGPS", lblRec);
		leftMaxWidth = Math.max(leftMaxWidth, lblGPS.setText(Translation.Get("gps")).getTextWidth());

		lblAccuracy = new Label(this.name + " lblAccuracy", lblRec);
		leftMaxWidth = Math.max(leftMaxWidth, lblAccuracy.setText(Translation.Get("accuracy")).getTextWidth());

		lblWP = new Label(this.name + " lblWP", lblRec);
		leftMaxWidth = Math.max(leftMaxWidth, lblWP.setText(Translation.Get("waypoint")).getTextWidth());

		lblCoord = new Label(this.name + " lblCoord", lblRec);
		leftMaxWidth = Math.max(leftMaxWidth, lblCoord.setText(Translation.Get("coordinate")).getTextWidth());

		lblCurrent = new Label(this.name + " lblCurrent", lblRec);
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

		Gps = new Label(this.name + " Gps", lblRec);
		Accuracy = new Label(this.name + " Accuracy", lblRec);
		WaypointLabel = new Label("-", Fonts.getNormal(), COLOR.getLinkFontColor(), WrapType.SINGLELINE);
		WaypointLabel.setRec(lblRec);
		CoordLabel = new Label(this.name + " CoordLabel", lblRec);
		Current = new Label(this.name + " Current", lblRec);

		// set Y Pos
		Gps.setY(lblGPS.getY());
		Accuracy.setY(lblAccuracy.getY());
		WaypointLabel.setY(lblWP.getY());
		CoordLabel.setY(lblCoord.getY());
		Current.setY(lblCurrent.getY());

		// set LinkColor

		WaypointLabel.setOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				if (GlobalCore.getSelectedCache() == null)
					return true;
				PlatformConnector.callUrl(GlobalCore.getSelectedCache().getUrl());
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
		chart = new SatBarChart(new CB_RectF(l, Gps.getMaxY() + l, this.getWidth() - l - l, CachesFoundLabel.getY() - Gps.getMaxY()), "Sat Chart");
		chart.setDrawWithAlpha(true);
		this.addChild(chart);
		setYpositions();
	}

	@Override
	public void onResized(CB_RectF rec) {
		super.onResized(rec);
		setYpositions();
	}

	@Override
	protected void SkinIsChanged() {
		createControls();
		setYpositions();
	}

	private void setYpositions() {
		if (CB_Logo != null)
			CB_Logo.setY(this.getHeight() - (margin * 2) - CB_Logo.getHeight());
		if (descTextView != null)
			descTextView.setY(CB_Logo.getY() - margin - margin - margin - descTextView.getHeight());
		if (CachesFoundLabel != null)
			CachesFoundLabel.setY(descTextView.getY() - CachesFoundLabel.getHeight() + margin);
		if (chart != null)
			chart.setHeight(CachesFoundLabel.getY() - Gps.getMaxY());
	}

	public void refreshText() {
		if (WaypointLabel == null || CachesFoundLabel == null || CoordLabel == null)
			return;
		try {
			CachesFoundLabel.setText(Translation.Get("caches_found") + " " + String.valueOf(Config.FoundOffset.getValue()));

			Cache selectedCache = GlobalCore.getSelectedCache();
			Waypoint selectedWaypoint = GlobalCore.getSelectedWaypoint();

			if (selectedCache != null) {
				try {
					if (selectedWaypoint != null) {
						WaypointLabel.setText(selectedWaypoint.getGcCode());
						CoordLabel.setText(UnitFormatter.FormatLatitudeDM(selectedWaypoint.Pos.getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(selectedWaypoint.Pos.getLongitude()));
					} else {
						WaypointLabel.setText(selectedCache.getGcCode());
						CoordLabel.setText(UnitFormatter.FormatLatitudeDM(selectedCache.Pos.getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(selectedCache.Pos.getLongitude()));
					}
				} catch (Exception e) {
					CoordLabel.setText(" - - - ");
				}
			}
			GL.that.renderOnce();
		}
		catch (Exception e){
		}
	}

	protected final IReturnValueListener mDialogListener = new IReturnValueListener() {
		@Override
		public void returnValue(int value) {
			Config.FoundOffset.setValue(value);
			Config.AcceptChanges();
			AboutView.this.refreshText();
		}

		@Override
		public void cancelClicked() {

		}

	};

	@Override
	public void GpsStateChanged() {
		if (Locator.getCoordinate().hasAccuracy()) {
			int radius = Locator.getCoordinate().getAccuracy();

			if (Accuracy != null)
				Accuracy.setText("+/- " + UnitFormatter.DistanceString(radius) + " (" + Locator.getProvider().toString() + ")");
		} else {
			if (Accuracy != null)
				Accuracy.setText("?");
		}
		if (Locator.getProvider() == ProviderType.GPS || Locator.getProvider() == ProviderType.Network) {
			if (Current != null)
				Current.setText(UnitFormatter.FormatLatitudeDM(Locator.getLatitude()) + " " + UnitFormatter.FormatLongitudeDM(Locator.getLongitude()));
			if (Gps != null)
				Gps.setText(GPS.getSatAndFix() + "   " + Translation.Get("alt") + " " + Locator.getAltStringWithCorection());
		} else {
			if (Gps != null)
				Gps.setText(Translation.Get("not_detected"));
		}
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
				refreshText();
			}
		});
	}

	@Override
	public void PositionChanged() {
		GpsStateChanged();
	}

	@Override
	public void OrientationChanged() {
	}

	@Override
	public String getReceiverName() {
		return "AboutView";
	}

	@Override
	public Priority getPriority() {
		return Priority.Low;
	}

	@Override
	public void SpeedChanged() {
	}

	@Override
	protected void Initial() {

	}

	@Override
	public void dispose() {

		if (descTextView != null)
			descTextView.dispose();
		descTextView = null;
		if (CachesFoundLabel != null)
			CachesFoundLabel.dispose();
		CachesFoundLabel = null;
		if (WaypointLabel != null)
			WaypointLabel.dispose();
		WaypointLabel = null;
		if (CoordLabel != null)
			CoordLabel.dispose();
		CoordLabel = null;
		if (lblGPS != null)
			lblGPS.dispose();
		lblGPS = null;
		if (Gps != null)
			Gps.dispose();
		Gps = null;
		if (lblAccuracy != null)
			lblAccuracy.dispose();
		lblAccuracy = null;
		if (Accuracy != null)
			Accuracy.dispose();
		Accuracy = null;
		if (lblWP != null)
			lblWP.dispose();
		lblWP = null;
		if (lblCoord != null)
			lblCoord.dispose();
		lblCoord = null;
		if (lblCurrent != null)
			lblCurrent.dispose();
		lblCurrent = null;
		if (Current != null)
			Current.dispose();
		Current = null;
		if (CB_Logo != null)
			CB_Logo.dispose();
		CB_Logo = null;
		if (chart != null)
			chart.dispose();
		chart = null;
		if (pd != null)
			pd.dispose();
		pd = null;

		SelectedCacheEventList.Remove(this);
		GpsStateChangeEventList.Remove(this);
		PositionChangedEventList.Remove(this);

		super.dispose();
	}
}

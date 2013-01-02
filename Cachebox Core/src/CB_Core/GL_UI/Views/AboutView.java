package CB_Core.GL_UI.Views;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class AboutView extends CB_View_Base
{
	Label descTextView, CachesFoundLabel, WP, Cord, lblGPS, GPS, lblAccuracy, Accuracy, lblWP, lblCord, lblCurrent, Current;
	Image CB_Logo;
	float margin;
	private int transFounds = -1;
	CancelWaitDialog pd;
	AboutView Me;

	public AboutView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		Me = this;

	}

	@Override
	public void onShow()
	{
		// Rufe ANDROID VIEW auf
		platformConector.showView(ViewConst.ABOUT_VIEW, this.Pos.x, this.Pos.y, this.width, this.height);
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.ABOUT_VIEW);
	}

	@Override
	protected void Initial()
	{
		this.setBackground(SpriteCache.AboutBack);
		float ref = UiSizes.getWindowHeight() / 13;
		margin = UiSizes.getMargin();
		CB_RectF CB_LogoRec = new CB_RectF(this.halfWidth - (ref * 2.5f), this.height - ((ref * 5) / 4.11f) - ref, ref * 5,
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

		refreshText();
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		CB_Logo.setY(this.height - (margin * 3) - CB_Logo.getHeight());
		descTextView.setY(CB_Logo.getY() - margin - margin - margin - descTextView.getHeight());
		CachesFoundLabel.setY(descTextView.getY() - (margin * 4) - CachesFoundLabel.getHeight());
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void refreshText()
	{
		CachesFoundLabel
				.setText(GlobalCore.Translations.Get("caches_found") + " " + String.valueOf(Config.settings.FoundOffset.getValue()));
		// if (GlobalCore.getSelectedCache() != null) if (GlobalCore.getSelectedWaypoint() != null)
		// {
		// WP.setText(GlobalCore.getSelectedWaypoint().GcCode);
		// Cord.setText(GlobalCore.FormatLatitudeDM(GlobalCore.getSelectedWaypoint().Pos.getLatitude()) + " "
		// + GlobalCore.FormatLongitudeDM(GlobalCore.getSelectedWaypoint().Pos.getLongitude()));
		// }
		// else
		// {
		// WP.setText(GlobalCore.getSelectedCache().GcCode);
		// Cord.setText(GlobalCore.FormatLatitudeDM(GlobalCore.getSelectedCache().Pos.getLatitude()) + " "
		// + GlobalCore.FormatLongitudeDM(GlobalCore.getSelectedCache().Pos.getLongitude()));
		// }

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

}

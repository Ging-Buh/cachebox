package CB_UI.GL_UI.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Types.Cache;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.Global;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_Utils.Plattform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class DescriptionView extends CB_View_Base
{
	final static String STRING_POWERD_BY = "Powerd by Geocaching Live";
	final static String BASIC = "Basic";
	final static String PREMIUM = "Premium";
	final static String BASIC_LIMIT = "3";
	final static String PREMIUM_LIMIT = "6000";

	CacheListViewItem cacheInfo;
	Button downloadButton;
	Label MessageLabel, PowerdBy;
	Image LiveIcon;
	private PolygonDrawable Line;
	private float margin;

	public DescriptionView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy DescriptionView");
		setBackground(SpriteCacheBase.ListBack);

		if (Plattform.used == Plattform.Desktop) this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		if (cacheInfo != null) this.removeChild(cacheInfo);
		Cache sel = GlobalCore.getSelectedCache();
		if (sel != null)
		{
			cacheInfo = new CacheListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), 0, sel);
			cacheInfo.setY(this.getHeight() - cacheInfo.getHeight());

			this.addChild(cacheInfo);
			resetUi();
			if (sel.isLive() || sel.getApiStatus() == 1)
			{
				showDownloadButton(sel);
			}
			else
			{
				showWebView();
			}
		}

	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		// onShow();
		if (cacheInfo != null) cacheInfo.setY(this.getHeight() - cacheInfo.getHeight());
		layout();
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.DESCRIPTION_VIEW);
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	private void showWebView()
	{
		// Rufe ANDROID VIEW auf
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				float infoHeight = 0;
				if (cacheInfo != null) infoHeight = cacheInfo.getHeight();
				platformConector.showView(ViewConst.DESCRIPTION_VIEW, DescriptionView.this.getX(), DescriptionView.this.getY(),
						DescriptionView.this.getWidth(), DescriptionView.this.getHeight(), 0, (infoHeight + GL_UISizes.margin), 0, 0);
			}
		};
		timer.schedule(task, 50);
	}

	private void resetUi()
	{
		if (MessageLabel != null)
		{
			this.removeChildsDirekt(MessageLabel);
			MessageLabel.dispose();
			MessageLabel = null;
		}
		if (downloadButton != null)
		{
			this.removeChildsDirekt(downloadButton);
			downloadButton.dispose();
			downloadButton = null;
		}
		if (LiveIcon != null)
		{
			this.removeChildsDirekt(LiveIcon);
			LiveIcon.dispose();
			LiveIcon = null;
		}
		if (PowerdBy != null)
		{
			this.removeChildsDirekt(PowerdBy);
			PowerdBy.dispose();
			PowerdBy = null;
		}
	}

	private void layout()
	{
		if (LiveIcon != null)
		{
			float IconX = this.getHalfWidth() - LiveIcon.getHalfWidth();
			float IconY = this.cacheInfo.getY() - (LiveIcon.getHeight() + margin);
			LiveIcon.setPos(IconX, IconY);

			if (PowerdBy != null)
			{
				PowerdBy.setY(LiveIcon.getY() - (PowerdBy.getHeight() + margin));

				if (MessageLabel != null)
				{
					MessageLabel.setY(this.PowerdBy.getY() - (MessageLabel.getHeight() + (margin * 3)));
					MessageLabel.setX(this.getHalfWidth() - MessageLabel.getHalfWidth());
				}
				downloadButton.setX(this.getHalfWidth() - downloadButton.getHalfWidth());
				downloadButton.setY(margin);
			}
		}
		Line = null;
	}

	private void showDownloadButton(Cache sel)
	{

		int result = CB_Core.Api.GroundspeakAPI.GetCacheLimits();

		if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
		{
			GL.that.Toast(ConnectionError.INSTANCE);
			return;
		}
		if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
		{
			GL.that.Toast(ApiUnavailable.INSTANCE);
			return;
		}

		float contentWidth = this.getWidth() * 0.95f;
		margin = GL_UISizes.margin;

		LiveIcon = new Image(0, 0, GL_UISizes.BottomButtonHeight, GL_UISizes.BottomButtonHeight, "LIVE-ICON");
		LiveIcon.setSprite(SpriteCacheBase.LiveBtn.get(0));

		this.addChild(LiveIcon);

		PowerdBy = new Label(this, "");
		PowerdBy.setHeight(Fonts.getNormal().getBounds(STRING_POWERD_BY).height + (margin * 2));
		PowerdBy.setFont(Fonts.getNormal()).setHAlignment(HAlignment.CENTER);

		PowerdBy.setWrappedText(STRING_POWERD_BY);
		this.addChild(PowerdBy);

		MessageLabel = new Label(this, "");
		MessageLabel.setWidth(contentWidth);
		MessageLabel.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
		MessageLabel.setHeight(this.getHalfHeight());

		MessageLabel.setWrappedText(getMessage());
		this.addChild(MessageLabel);

		downloadButton = new Button(Translation.Get("DownloadDetails"));
		downloadButton.setWidth(this.getWidth() * 0.8f);

		this.addChild(downloadButton);

		downloadButton.setOnClickListener(downloadClicked);

		if (CB_Core.Api.GroundspeakAPI.CachesLeft <= 0) downloadButton.disable();
		layout();
	}

	final static OnClickListener downloadClicked = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			GL.that.RunOnGL(new IRunOnGL()
			{
				@Override
				public void run()
				{
					TabMainView.actionShowDescriptionView.ReloadSelectedCache();
				}
			});
			return true;
		}
	};

	@Override
	public void render(Batch batch)
	{
		super.render(batch);

		if (PowerdBy != null)
		{
			if (Line == null)
			{
				float strokeWidth = 3 * UI_Size_Base.that.getScale();

				Line l1 = new Line(margin, PowerdBy.getY() - margin, this.getWidth() - margin, PowerdBy.getY() - margin);

				Quadrangle q1 = new Quadrangle(l1, strokeWidth);

				GL_Paint paint = new GL_Paint();
				paint.setGLColor(Color.DARK_GRAY);
				Line = new PolygonDrawable(q1.getVertices(), q1.getTriangles(), paint, this.getWidth(), this.getHeight());

				l1.dispose();

				q1.dispose();

			}

			Line.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
		}
	}

	private String getMessage()
	{
		StringBuilder sb = new StringBuilder();
		boolean basic = CB_Core.Api.GroundspeakAPI.GetMembershipType() == 1;
		String MemberType = basic ? BASIC : PREMIUM;
		String limit = basic ? BASIC_LIMIT : PREMIUM_LIMIT;
		String actLimit = Integer.toString(CB_Core.Api.GroundspeakAPI.CachesLeft - 1);

		sb.append(Translation.Get("LiveDescMessage", MemberType, limit));
		sb.append(Global.br);
		sb.append(Global.br);
		if (CB_Core.Api.GroundspeakAPI.CachesLeft > 0) sb.append(Translation.Get("LiveDescAfter", actLimit));

		if (CB_Core.Api.GroundspeakAPI.CachesLeft <= 0)
		{
			sb.append(Translation.Get("LiveDescLimit"));
			sb.append(Global.br);
			sb.append(Global.br);
			if (basic) sb.append(Translation.Get("LiveDescLimitBasic"));

		}

		return sb.toString();

	}
}

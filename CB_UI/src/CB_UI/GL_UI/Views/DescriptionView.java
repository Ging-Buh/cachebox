package CB_UI.GL_UI.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Types.Cache;
import CB_UI.GlobalCore;
import CB_UI_Base.Plattform;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;

public class DescriptionView extends CB_View_Base
{
	CacheListViewItem cacheInfo;

	public DescriptionView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy DescriptionView");
		setBackground(SpriteCacheBase.ListBack);

		if (GlobalCore.platform == Plattform.Desktop) this.addChild(lblDummy);

	}

	@Override
	public void onShow()
	{
		if (cacheInfo != null) this.removeChild(cacheInfo);
		Cache sel = GlobalCore.getSelectedCache();
		if (sel != null)
		{
			cacheInfo = new CacheListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), 0, sel);
			cacheInfo.setY(this.height - cacheInfo.getHeight());

			this.addChild(cacheInfo);

		}

		// Rufe ANDROID VIEW auf
		Timer timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				float infoHeight = 0;
				if (cacheInfo != null) infoHeight = cacheInfo.getHeight();
				platformConector.showView(ViewConst.DESCRIPTION_VIEW, 0, infoHeight, DescriptionView.this.width,
						DescriptionView.this.height - infoHeight);
			}
		};
		timer.schedule(task, 50);

	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		onShow();
		// cacheInfo.setY(this.height - cacheInfo.getHeight());
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

}

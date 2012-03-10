package CB_Core.GL_UI.Views;

import java.util.Date;

import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CacheInfo;
import CB_Core.GL_UI.Controls.InfoBubble;
import CB_Core.GL_UI.Controls.MapInfoPanel;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.ZoomButtons;
import CB_Core.GL_UI.Controls.ZoomScale;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class MapControlTest extends CB_View_Base
{

	private ZoomButtons btnZoom;
	private MultiToggleButton togBtn;
	private MapInfoPanel info;
	private ZoomScale zoomScale;
	private InfoBubble infoBubble;
	private InfoBubble infoBubbleZoom;
	private CacheInfo cacheInfo;

	public MapControlTest(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		// MapInfoPanel
		info = (MapInfoPanel) this.addChild(new MapInfoPanel(GL_UISizes.Info, "InfoPanel"));

		info.setBearing(aktBearing);
		info.setCoord(aktCoord);

		btnZoom = new ZoomButtons(GL_UISizes.ZoomBtn, this, "ZoomButtons");
		btnZoom.setClickable(true);

		btnZoom.setOnClickListenerDown(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				zoomScale.setZoom(btnZoom.getZoom());

				SizeF size = infoBubbleZoom.getSize();
				size.scale(0.9f);
				infoBubbleZoom.setSize(size);
				return true;
			}
		});

		btnZoom.setOnClickListenerUp(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				zoomScale.setZoom(btnZoom.getZoom());

				SizeF size = infoBubbleZoom.getSize();
				size.scale(1.11111111f);
				infoBubbleZoom.setSize(size);
				return true;
			}
		});

		this.addChild(btnZoom);

		togBtn = new MultiToggleButton(GL_UISizes.Toggle, this, "toggle");

		togBtn.addState("Free", Color.GRAY);
		togBtn.addState("GPS", Color.GREEN);
		togBtn.addState("Lock", Color.RED);
		togBtn.addState("Car", Color.YELLOW);
		togBtn.setLastStateWithLongClick(true);
		togBtn.setOnStateChangedListner(new OnStateChangeListener()
		{

			@Override
			public void onStateChange(GL_View_Base v, int State)
			{

			}
		});
		this.addChild(togBtn);

		final MultiToggleButton btnState = new MultiToggleButton(70, 510, 150, 65, this, "btnState");
		btnState.addState("Set Bearing", Color.GRAY);
		btnState.addState("Set Distance", Color.YELLOW);
		btnState.addState("Set Speed", Color.GREEN);
		btnState.addState("Set Lokation", Color.RED);

		this.addChild(btnState);

		Button btnPlus = new Button(230, 510, 65, 65, this, "btnSetCordNull");
		btnPlus.setText("+");
		btnPlus.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (btnState.getState())
				{
				case 0:
					aktBearing += 15;
					if (aktBearing > 360) aktBearing = 15;
					if (aktBearing < 0) aktBearing = 345;
					info.setBearing(aktBearing);
					break;

				case 1:
					aktDistance += 25;
					info.setDistance(aktDistance);
					break;
				case 2:
					aktSpeed += 25;
					info.setSpeed(aktSpeed + " km/h");
					break;
				case 3:
					aktCoord.Latitude += 0.001;
					aktCoord.Longitude += 0.001;
					info.setCoord(aktCoord);
					break;
				}
				return false;
			}
		});

		this.addChild(btnPlus);

		Button btnMinus = new Button(305, 510, 65, 65, this, "btnSetCordNull");
		btnMinus.setText("-");

		btnMinus.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (btnState.getState())
				{
				case 0:
					aktBearing -= 15;
					if (aktBearing > 360) aktBearing = 15;
					if (aktBearing < 0) aktBearing = 345;
					info.setBearing(aktBearing);
					break;

				case 1:
					aktDistance -= 25;
					info.setDistance(aktDistance);
					break;
				case 2:
					aktSpeed -= 25;
					info.setSpeed(aktSpeed + " km/h");
					break;
				case 3:
					aktCoord.Latitude -= 0.001;
					aktCoord.Longitude -= 0.001;
					info.setCoord(aktCoord);
					break;
				}
				return false;
			}
		});

		this.addChild(btnMinus);

		Button btnToggleDebugRec = new Button(10, 10, 150, 65, this, "btnSetCordNull");
		btnToggleDebugRec.setText("Toggle DebugRec");
		btnToggleDebugRec.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_View_Base.debug = !GL_View_Base.debug;
				return true;
			}
		});
		this.addChild(btnToggleDebugRec);

		zoomScale = new ZoomScale(GL_UISizes.ZoomScale, "zoomScale", 2, 21, 12);
		btnZoom.setMaxZoom(21);
		btnZoom.setMinZoom(2);
		btnZoom.setZoom(12);
		this.addChild(zoomScale);

		infoBubble = new InfoBubble(new SizeF(400, 150), "infoBubble");
		infoBubble.setX(70);
		infoBubble.setY(350);
		infoBubble.setCache(getTestCache());
		this.addChild(infoBubble);

		infoBubbleZoom = new InfoBubble(GL_UISizes.Bubble, "infoBubble");
		infoBubbleZoom.setX(70);
		infoBubbleZoom.setY(80);
		infoBubbleZoom.setCache(getTestCache());
		this.addChild(infoBubbleZoom);

		cacheInfo = new CacheInfo(UiSizes.getCacheListItemRec().asFloat(), "CacheInfo", getTestCache());
		cacheInfo.setY(10);
		cacheInfo.setX(200);
		// cacheInfo.setViewMode(CacheInfo.VIEW_MODE_DESCRIPTION);
		this.addChild(cacheInfo);

		requestLayout();
	}

	private Cache getTestCache()
	{
		Cache tmp = new Cache(0, 0, "Test Cache", CacheTypes.Traditional, "HOE258");
		tmp.DateHidden = new Date();
		tmp.Found = true;
		tmp.Difficulty = 2.5f;
		tmp.Terrain = 5f;
		tmp.Owner = "Ich";
		tmp.Size = CacheSizes.regular;
		tmp.Rating = 3;
		tmp.Pos = new Coordinate(13.548, 42.568);
		return tmp;
	}

	private float aktBearing = 45f;
	private float aktDistance = 455f;
	private float aktSpeed = 120f;
	private Coordinate aktCoord = new Coordinate(0.12, 1.3);

	private void requestLayout()
	{
		Logger.LogCat("MapControlTest requestLayout()");
		float margin = GL_UISizes.margin;
		info.setPos(new Vector2(margin, (float) (this.height - margin - info.getHeight())));
		togBtn.setPos(new Vector2((float) (this.width - margin - togBtn.getWidth()), this.height - margin - togBtn.getHeight()));

		GL_Listener.glListener.renderOnce(this);
	}

}

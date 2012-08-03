package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Map.RouteOverlay.Track;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class TrackListViewItem extends ListViewItemBackground
{
	private static Sprite chkOff;
	private static Sprite chkOn;

	private static CB_RectF lBounds;
	private static CB_RectF rBounds;
	private static CB_RectF rChkBounds;

	// private Member
	private Track mRoute;
	private float left;
	private float top;

	private Label EntryName;
	private Label EntryLength;
	private Sprite colorReck;
	private boolean Clicked = false;
	public Vector2 lastItemTouchPos;

	private RouteChangedListner mRouteChangedListner;

	public interface RouteChangedListner
	{
		public void RouteChanged(Track route);
	}

	public TrackListViewItem(CB_RectF rec, int Index, Track route, RouteChangedListner listner)
	{
		super(rec, Index, route.Name);
		mRoute = route;
		mRouteChangedListner = listner;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	protected void render(SpriteBatch batch)
	{

		super.render(batch);

		boolean rClick = false;
		boolean lClick = false;
		if (this.isPressed)
		{
			lClick = lBounds.contains(this.lastItemTouchPos);
			rClick = rBounds.contains(this.lastItemTouchPos);

			if (lClick || rClick) Clicked = true;
			isPressed = GL_Listener.isTouchDown();
		}
		else
		{
			if (Clicked)
			{
				Clicked = false;
				lClick = lBounds.contains(this.lastItemTouchPos);
				rClick = rBounds.contains(this.lastItemTouchPos);
				if (lClick) colorClick();
				if (rClick) chkClick();
			}
		}

		// initial
		left = LeftWidth;
		top = this.height - TopHight;

		drawColorRec(batch);

		// draw Name
		if (EntryName == null)
		{

			CB_RectF rec = new CB_RectF(left, this.height / 2, this.width - left - height - 10, this.height / 2);
			EntryName = new Label(rec, "");

			EntryName.setText(mRoute.Name);

			this.addChild(EntryName);
		}

		// draw Lenght
		if (EntryLength == null)
		{

			CB_RectF rec = new CB_RectF(left, 0, this.width - left - height - 10, this.height / 2);
			EntryLength = new Label(rec, "");
			EntryLength.setText(GlobalCore.Translations.Get("length") + ": " + UnitFormatter.DistanceString((float) mRoute.TrackLength));

			this.addChild(EntryLength);
		}

		drawRightChkBox(batch);

	}

	private void drawColorRec(SpriteBatch batch)
	{
		if (lBounds == null)
		{
			lBounds = new CB_RectF(0, 0, height, height);
			lBounds = lBounds.ScaleCenter(0.95f);
		}

		if (colorReck == null)
		{
			colorReck = SpriteCache.getThemedSprite("text-field-back");
			colorReck.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());
			colorReck.setColor(mRoute.getColor());
		}

		colorReck.draw(batch);

		left += lBounds.getWidth() + Dialog.margin;

	}

	private void drawRightChkBox(SpriteBatch batch)
	{
		if (rBounds == null || rChkBounds == null)
		{
			rBounds = new CB_RectF(width - height - 10, 5, height - 10, height - 10);// = right Button bounds

			rChkBounds = rBounds.ScaleCenter(0.8f);
		}

		if (chkOff == null)
		{
			chkOff = SpriteCache.getThemedSprite("check-off");
			chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
		}

		if (chkOn == null)
		{
			chkOn = SpriteCache.getThemedSprite("check-on");
			chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
		}

		if (mRoute.ShowRoute)
		{
			chkOn.draw(batch);
		}
		else
		{
			chkOff.draw(batch);
		}

	}

	private void chkClick()
	{
		mRoute.ShowRoute = !mRoute.ShowRoute;
		if (mRouteChangedListner != null) mRouteChangedListner.RouteChanged(mRoute);
	}

	private void colorClick()
	{
		// TODO Auto-generated method stub

	}

}

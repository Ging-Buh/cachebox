package CB_UI.GL_UI.Views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import CB_Locator.Map.Track;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Activitys.ColorPicker;
import CB_UI_Base.GL_UI.Activitys.ColorPicker.IReturnListner;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.UnitFormatter;

public class TrackListViewItem extends ListViewItemBackground {
    private static Sprite chkOff;
    private static Sprite chkOn;

    private static CB_RectF lBounds;
    private static CB_RectF rBounds;
    private static CB_RectF rChkBounds;

    // private Member
    private Track mRoute;
    private float left;

    private Label EntryName;
    private Label EntryLength;
    private Sprite colorReck;
    private boolean Clicked = false;
    public Vector2 lastItemTouchPos;

    private RouteChangedListner mRouteChangedListner;

    public interface RouteChangedListner {
	public void RouteChanged(Track route);
    }

    public TrackListViewItem(CB_RectF rec, int Index, Track route, RouteChangedListner listner) {
	super(rec, Index, route.Name);
	mRoute = route;
	mRouteChangedListner = listner;
    }

    @Override
    protected void SkinIsChanged() {

    }

    @Override
    protected void render(Batch batch) {

	super.render(batch);

	boolean rClick = false;
	boolean lClick = false;
	if (this.isPressed) {
	    // log.debug("TrackListViewItem => is Pressed");

	    lClick = lBounds.contains(this.lastItemTouchPos);
	    rClick = rBounds.contains(this.lastItemTouchPos);

	    if (lClick || rClick)
		Clicked = true;

	    try {
		Thread.sleep(50);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }

	    isPressed = GL.getIsTouchDown();
	} else {
	    if (Clicked) {
		// log.debug("TrackListViewItem => is Clicked");
		Clicked = false;
		lClick = lBounds.contains(this.lastItemTouchPos);
		rClick = rBounds.contains(this.lastItemTouchPos);
		if (lClick)
		    colorClick();
		if (rClick)
		    chkClick();
	    }
	}

	// initial
	left = getLeftWidth();

	drawColorRec(batch);

	// draw Name
	if (EntryName == null || EntryLength == null) {
	    createLabel();
	}

	drawRightChkBox(batch);

    }

    private void createLabel() {
	if (EntryName == null) {

	    CB_RectF rec = new CB_RectF(left, this.getHeight() / 2, this.getWidth() - left - getHeight() - 10, this.getHeight() / 2);
	    EntryName = new Label(this.name + " EntryName", rec);

	    EntryName.setText(mRoute.Name);

	    this.addChild(EntryName);
	}

	// draw Lenght
	if (EntryLength == null) {

	    CB_RectF rec = new CB_RectF(left, 0, this.getWidth() - left - getHeight() - 10, this.getHeight() / 2);
	    EntryLength = new Label(this.name + " EntryLength", rec, "");
	    EntryLength.setText(Translation.Get("length") + ": " + UnitFormatter.DistanceString((float) mRoute.TrackLength) + " / " + UnitFormatter.DistanceString((float) mRoute.AltitudeDifference));

	    this.addChild(EntryLength);
	}

	GL.that.renderOnce();
    }

    private void drawColorRec(Batch batch) {
	if (mRoute == null)
	    return;
	if (lBounds == null) {
	    lBounds = new CB_RectF(0, 0, getHeight(), getHeight());
	    lBounds = lBounds.ScaleCenter(0.95f);
	}

	if (colorReck == null) {
	    colorReck = SpriteCacheBase.getThemedSprite("text-field-back");
	    colorReck.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());
	    colorReck.setColor(mRoute.getColor());
	}

	colorReck.draw(batch);

	left += lBounds.getWidth() + UI_Size_Base.that.getMargin();

    }

    private void drawRightChkBox(Batch batch) {
	if (rBounds == null || rChkBounds == null) {
	    rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// = right Button bounds

	    rChkBounds = rBounds.ScaleCenter(0.8f);
	}

	if (chkOff == null) {
	    chkOff = SpriteCacheBase.getThemedSprite("check-off");
	    chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
	}

	if (chkOn == null) {
	    chkOn = SpriteCacheBase.getThemedSprite("check-on");
	    chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
	}

	if (mRoute.ShowRoute) {
	    chkOn.draw(batch);
	} else {
	    chkOff.draw(batch);
	}

    }

    private void chkClick() {
	// log.debug("TrackListViewItem => Chk Clicked");

	GL.that.RunOnGL(new IRunOnGL() {

	    @Override
	    public void run() {
		mRoute.ShowRoute = !mRoute.ShowRoute;
		if (mRouteChangedListner != null)
		    mRouteChangedListner.RouteChanged(mRoute);
	    }
	});
	GL.that.renderOnce();
    }

    private void colorClick() {
	// log.debug("TrackListViewItem => Color Clicked");

	GL.that.RunOnGL(new IRunOnGL() {

	    @Override
	    public void run() {
		ColorPicker clrPick = new ColorPicker(ActivityBase.ActivityRec(), mRoute.getColor(), new IReturnListner() {

		    @Override
		    public void returnColor(Color color) {
			if (color == null)// no changes
			{
			    return;
			}
			mRoute.setColor(color);
			colorReck = null;
		    }
		});
		clrPick.show();
	    }
	});
	GL.that.renderOnce();
    }

    public void notifyTrackChanged(Track route) {
	mRoute = route;
	if (EntryLength != null)
	    EntryLength.setText(Translation.Get("length") + ": " + UnitFormatter.DistanceString((float) mRoute.TrackLength) + " / " + UnitFormatter.DistanceString((float) mRoute.AltitudeDifference));

    }

    public Track getRoute() {
	return mRoute;
    }

}

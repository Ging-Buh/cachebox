package CB_UI.GL_UI.Views;

import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_UI.GL_UI.Controls.CacheInfo;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.UnitFormatter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

public class WaypointViewItem extends ListViewItemBackground implements PositionChangedEvent {
    private Cache mCache;
    private Waypoint mWaypoint;

    protected extendedCacheInfo info;
    protected boolean isPressed = false;

    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    private CB_RectF ArrowRec;
    private Sprite arrow = new Sprite(SpriteCacheBase.Arrows.get(0));
    private BitmapFontCache distance;
    private Sprite mIconSprite;
    private float mIconSize = 0;
    private float mMargin = 0;

    private BitmapFontCache mNameCache;
    private BitmapFontCache mDescCache;
    private BitmapFontCache mCoordCache;

    private int ViewMode = CacheInfo.VIEW_MODE_WAYPOINTS;

    /**
     * mit ausgeschaltener scissor berechnung
     * 
     * @author Longri
     */
    private class extendedCacheInfo extends CacheInfo {

	public extendedCacheInfo(CB_RectF rec, String Name, Cache value) {
	    super(rec, Name, value);
	}

	@Override
	public void renderChilds(final Batch batch, ParentInfo parentInfo) {
	    if (!disableScissor)
		Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

	    batch.flush();

	    this.render(batch);
	    batch.flush();

	    Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
	}

    }

    public WaypointViewItem(CB_RectF rec, int Index, Cache cache, Waypoint waypoint) {
	super(rec, Index, "");
	ViewMode = CacheInfo.VIEW_MODE_WAYPOINTS;
	initial(Index, cache, waypoint);
    }

    public WaypointViewItem(CB_RectF rec, int Index, Cache cache, Waypoint waypoint, int viewMode) {
	super(rec, Index, "");
	ViewMode = viewMode;
	initial(Index, cache, waypoint);
    }

    private void initial(int Index, Cache cache, Waypoint waypoint) {
	this.mCache = cache;
	this.mWaypoint = waypoint;

	distance = new BitmapFontCache(Fonts.getSmall());
	distance.setColor(COLOR.getFontColor());
	distance.setText("", 0, 0);

	if (waypoint == null) // this Item is the Cache
	{

	    if (cache == null) {
		arrow = null;
		distance = null;
		return;
	    }

	    info = new extendedCacheInfo(this, "CacheInfo " + Index + " @" + cache.getGcCode(), cache);
	    info.setZeroPos();
	    info.setViewMode(ViewMode);

	    this.addChild(info);
	}
	requestLayout();

	//register pos changed event
	if ((ViewMode & CacheInfo.SHOW_COMPASS) == CacheInfo.SHOW_COMPASS)
	    PositionChangedEventList.Add(this);

    }

    public Waypoint getWaypoint() {
	return mWaypoint;
    }

    private void setDistanceString(String txt) {
	GlyphLayout bounds = distance.setText(txt, ArrowRec.getX(), ArrowRec.getY());
	float x = ArrowRec.getHalfWidth() - (bounds.width / 2f);
	distance.setPosition(x, 0);

    }

    private void setActLocator() {
	if (Locator.Valid()) {

	    if (mWaypoint != null && mWaypoint.Pos.isZero()) {
		arrow = null;
		setDistanceString("???");
	    } else {
		double lat = (mWaypoint == null) ? mCache.Latitude() : mWaypoint.Pos.getLatitude();
		double lon = (mWaypoint == null) ? mCache.Longitude() : mWaypoint.Pos.getLongitude();
		float distance = (mWaypoint == null) ? mCache.Distance(CalculationType.FAST, true) : mWaypoint.Distance();

		Coordinate position = Locator.getCoordinate();
		double heading = Locator.getHeading();
		double bearing = CoordinateGPS.Bearing(CalculationType.FAST, position.getLatitude(), position.getLongitude(), lat, lon);
		double cacheBearing = -(bearing - heading);
		setDistanceString(UnitFormatter.DistanceString(distance));

		arrow.setRotation((float) cacheBearing);
		if (arrow.getColor().r == DISABLE_COLOR.r && arrow.getColor().g == DISABLE_COLOR.g && arrow.getColor().b == DISABLE_COLOR.b)// ignore
																	    // alpha
		{
		    float size = this.getHeight() / 2.3f;
		    arrow = new Sprite(SpriteCacheBase.Arrows.get(0));
		    arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
		    arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());
		}
	    }

	}
    }

    @Override
    protected void render(Batch batch) {
	if (mIndex != -1)
	    super.render(batch);

	if ((ViewMode & CacheInfo.SHOW_COMPASS) == CacheInfo.SHOW_COMPASS) {
	    if (arrow != null)
		arrow.draw(batch);
	    if (distance != null)
		distance.draw(batch);
	}

	if (mIconSprite != null)
	    mIconSprite.draw(batch);
	if (mIconSprite == null && mWaypoint != null)
	    requestLayout();

	if (mNameCache != null)
	    mNameCache.draw(batch);
	if (mDescCache != null)
	    mDescCache.draw(batch);
	if (mCoordCache != null)
	    mCoordCache.draw(batch);
    }

    @Override
    public void dispose() {
	PositionChangedEventList.Remove(this);
	if (info != null)
	    info.dispose();
	info = null;

	arrow = null;
	// if (distance != null) distance.dispose();
	distance = null;
	super.dispose();
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

	isPressed = true;

	return false;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
	isPressed = false;

	return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
	isPressed = false;

	return false;
    }

    @Override
    public void PositionChanged() {
	setActLocator();
    }

    @Override
    public void OrientationChanged() {
	if (mCache == null)
	    return;
	setActLocator();
    }

    @Override
    public String getReceiverName() {
	return "Core.WayPointViewItem";
    }

    @Override
    protected void SkinIsChanged() {

    }

    boolean inChange = false;

    public void requestLayout() {

	if (ViewMode != CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)// For Compass without own compass
	{
	    float size = UiSizes.that.getCacheListItemRec().asFloat().getHeight() / 2.3f;
	    ArrowRec = new CB_RectF(this.getWidth() - (size * 1.2f), this.getHeight() - (size * 1.6f), size, size);
	    arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
	    arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());

	    if (Locator.Valid()) {
		arrow.setColor(DISABLE_COLOR);
		setDistanceString("---");
	    } else {
		setActLocator();
	    }

	} else {
	    arrow = null;
	    distance = null;
	}

	if (mWaypoint != null) {
	    float scaleFactor = getWidth() / UiSizes.that.getCacheListItemRec().getWidth();
	    float mLeft = 3 * scaleFactor;
	    float mTop = 3 * scaleFactor;
	    mMargin = mLeft;

	    mIconSize = Fonts.Measure("T").height * 3.5f * scaleFactor;

	    Vector2 mSpriteCachePos = new Vector2(mLeft + mMargin, getHeight() - mTop - mIconSize);

	    { // Icon Sprite erstellen
	      // MultiStage Waypoint anders darstellen wenn dieser als Startpunkt definiert ist
		if ((mWaypoint.Type == CacheTypes.MultiStage) && mWaypoint.IsStart)
		    mIconSprite = new Sprite(SpriteCacheBase.BigIcons.get(23));
		else
		    mIconSprite = new Sprite(SpriteCacheBase.BigIcons.get(mWaypoint.Type.ordinal()));

		mIconSprite.setSize(mIconSize, mIconSize);
		mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);
	    }

	    mNameCache = new BitmapFontCache(Fonts.getNormal());
	    mDescCache = new BitmapFontCache(Fonts.getBubbleNormal());
	    mCoordCache = new BitmapFontCache(Fonts.getBubbleNormal());

	    mNameCache.setText("", 0, 0);
	    mDescCache.setText("", 0, 0);
	    mCoordCache.setText("", 0, 0);

	    mNameCache.setColor(COLOR.getFontColor());
	    mDescCache.setColor(COLOR.getFontColor());
	    mCoordCache.setColor(COLOR.getFontColor());

	    float textYPos = this.getHeight() - mMargin;

	    float allHeight = (mNameCache.setText(mWaypoint.getGcCode() + ": " + mWaypoint.getTitle(), mSpriteCachePos.x + mIconSize + mMargin, textYPos)).height + mMargin + mMargin;
	    textYPos -= allHeight;

	    if (ViewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK) {
		mDescCache = null;
	    } else {
		if (!mWaypoint.getDescription().equals("")) {

		    float textXPos = mSpriteCachePos.x + mIconSize + mMargin;
		    float descHeight = (mDescCache.setText(mWaypoint.getDescription(), textXPos, textYPos, this.getWidth() - (textXPos + mIconSize + mMargin), Align.left, true)).height + mMargin + mMargin;
		    allHeight += descHeight;

		    textYPos -= descHeight;
		}
	    }

	    String sCoord = "";

	    if (ViewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK) {
		sCoord = mWaypoint.Pos.FormatCoordinateLineBreake();
	    } else {
		sCoord = mWaypoint.Pos.FormatCoordinate();
	    }

	    float coordHeight = (mCoordCache.setText(sCoord, mSpriteCachePos.x + mIconSize + mMargin, textYPos)).height + mMargin + mMargin;
	    allHeight += coordHeight;
	    textYPos -= coordHeight;

	    if (allHeight > UiSizes.that.getCacheListItemRec().asFloat().getHeight()) {

		if (!inChange && member[3] != allHeight) {
		    inChange = true;
		    member[3] = allHeight;
		    calcCrossCorner();
		    CallRecChanged();
		    requestLayout();
		    inChange = false;
		}
	    }
	} else {
	    info.setSize(this);
	    info.requestLayout();
	}

    }

    @Override
    public Priority getPriority() {
	return Priority.Low;
    }

    @Override
    public void SpeedChanged() {
    }

    public float getAttributeHeight() {
	if (info == null)
	    return 0;
	return info.getAttributeHeight();
    }

    public float getTexteHeight() {
	if (info == null)
	    return 0;
	return info.getTextHeight();

    }
}

package de.droidcachebox.gdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.ParentInfo;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CacheInfo;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.*;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class WaypointViewItem extends ListViewItemBackground implements PositionChangedEvent {
    private static final String log = "WaypointViewItem";
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    protected ExtendedCacheInfo cacheInfo;
    protected boolean isPressed = false;
    boolean inChange = false;
    private Cache mCache;
    private Waypoint mWaypoint;
    private CB_RectF ArrowRec;
    private Sprite arrow = new Sprite(Sprites.Arrows.get(0));
    private BitmapFontCache distance;
    private Sprite mIconSprite;
    private BitmapFontCache mNameCache;
    private BitmapFontCache mDescCache;
    private BitmapFontCache mCoordCache;
    private int viewMode;

    public WaypointViewItem(CB_RectF rec, int Index, Cache cache, Waypoint waypoint) {
        // CB_UI.Slider, WaypointView (geoCache + waypoint) for the waypointentry
        super(rec, Index, "");
        viewMode = CacheInfo.VIEW_MODE_WAYPOINTS;
        initial(Index, cache, waypoint);
    }

    public WaypointViewItem(CB_RectF rec, int Index, Cache cache, Waypoint waypoint, int viewMode) {
        // CB_UI.Slider VIEW_MODE_SLIDER for the geoCache entry
        super(rec, Index, "");
        this.viewMode = viewMode;
        initial(Index, cache, waypoint);
    }

    private void initial(int Index, Cache cache, Waypoint waypoint) {
        mCache = cache;
        mWaypoint = waypoint;

        distance = new BitmapFontCache(Fonts.getSmall());
        distance.setColor(COLOR.getFontColor());

        if (waypoint == null) // this Item is the Cache
        {
            if (cache == null) {
                arrow = null;
                distance = null;
                return;
            }
            cacheInfo = new ExtendedCacheInfo(this, "CacheInfo " + Index + " @" + cache.getGcCode(), cache);
            cacheInfo.setZeroPos();
            cacheInfo.setViewMode(viewMode);
            addChild(cacheInfo);
        }
        requestLayout();

        //register pos changed event
        if ((viewMode & CacheInfo.SHOW_COMPASS) == CacheInfo.SHOW_COMPASS)
            PositionChangedListeners.addListener(this);

    }

    public Waypoint getWaypoint() {
        return mWaypoint;
    }

    private void setDistanceString(final String txt) {
        if (txt != null) {
            try {
                GlyphLayout bounds = distance.setText(txt, ArrowRec.getX(), ArrowRec.getY());
                float x = ArrowRec.getHalfWidth() - (bounds.width / 2f);
                distance.setPosition(x, 0);
            } catch (Exception e) {
                Log.err(log, "setDistanceString: '" + txt + "'" + e.getLocalizedMessage());
            }
        }
    }

    private void setActLocator() {
        if (mCache == null)
            return;

        if (Locator.getInstance().isValid()) {
            boolean isDefined = mWaypoint != null;
            if (isDefined) {
                if (mWaypoint.getCoordinate() == null) {
                    isDefined = false;
                } else {
                    if (mWaypoint.getCoordinate().isZero()) isDefined = false;
                }
            }

            if (isDefined) {
                double lat, lon;
                float distance;
                if (mWaypoint == null) {
                    lat = mCache.getLatitude();
                    lon = mCache.getLongitude();
                    distance = mCache.Distance(CalculationType.FAST, true);
                } else {
                    lat = mWaypoint.getLatitude();
                    lon = mWaypoint.getLongitude();
                    distance = mWaypoint.getDistance();
                }

                Coordinate position = Locator.getInstance().getMyPosition();
                double heading = Locator.getInstance().getHeading();
                double bearing = CoordinateGPS.Bearing(CalculationType.FAST, position.getLatitude(), position.getLongitude(), lat, lon);
                double cacheBearing = -(bearing - heading);
                setDistanceString(UnitFormatter.distanceString(distance));

                arrow.setRotation((float) cacheBearing);
                if (arrow.getColor().r == DISABLE_COLOR.r && arrow.getColor().g == DISABLE_COLOR.g && arrow.getColor().b == DISABLE_COLOR.b)// ignore
                // alpha
                {
                    float size = getHeight() / 2.3f;
                    arrow = new Sprite(Sprites.Arrows.get(0));
                    arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
                    arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());
                }
            } else {
                arrow = null;
                setDistanceString("???");
            }

        }
    }

    @Override
    protected void render(Batch batch) {
        if (mIndex != -1)
            super.render(batch);

        if ((viewMode & CacheInfo.SHOW_COMPASS) == CacheInfo.SHOW_COMPASS) {
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
        PositionChangedListeners.removeListener(this);
        if (cacheInfo != null)
            cacheInfo.dispose();
        cacheInfo = null;

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
    public void positionChanged() {
        setActLocator();
    }

    @Override
    public void orientationChanged() {
        setActLocator();
    }

    @Override
    public String getReceiverName() {
        return "Core.WayPointViewItem";
    }

    public void requestLayout() {

        if (viewMode != CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)// For Compass without own compass
        {
            float size = UiSizes.getInstance().getCacheListItemRec().asFloat().getHeight() / 2.3f;
            ArrowRec = new CB_RectF(getWidth() - (size * 1.2f), getHeight() - (size * 1.6f), size, size);
            arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
            arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());

            if (Locator.getInstance().isValid()) {
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
            float scaleFactor = getWidth() / UiSizes.getInstance().getCacheListItemRec().getWidth();
            float mLeft = 3 * scaleFactor;
            float mTop = 3 * scaleFactor;

            float mIconSize = Fonts.Measure("T").height * 3.5f * scaleFactor;

            Vector2 mSpriteCachePos = new Vector2(mLeft + mLeft, getHeight() - mTop - mIconSize);

            { // Icon Sprite erstellen
                // MultiStage Waypoint anders darstellen wenn dieser als Startpunkt definiert ist
                if ((mWaypoint.waypointType == GeoCacheType.MultiStage) && mWaypoint.isStartWaypoint)
                    mIconSprite = new Sprite(Sprites.getSprite("big" + GeoCacheType.MultiStage.name() + "StartP"));
                else
                    mIconSprite = new Sprite(Sprites.getSprite("big" + mWaypoint.waypointType.name()));

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

            float textYPos = getHeight() - mLeft;

            float allHeight = (mNameCache.setText(mWaypoint.getGcCode().substring(0, 2) + ": " + mWaypoint.getTitleForGui(), mSpriteCachePos.x + mIconSize + mLeft, textYPos)).height + mLeft + mLeft;
            textYPos -= allHeight;

            if (viewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK) {
                mDescCache = null;
            } else {
                String wpDescription = mWaypoint.getDescription();
                if (wpDescription.length() > 0) {
                    float textXPos = mSpriteCachePos.x + mIconSize + mLeft;
                    GlyphLayout gl = mDescCache.setText(wpDescription, textXPos, textYPos, getWidth() - (textXPos + mIconSize + mLeft), Align.left, true);
                    float descHeight = gl.height + mLeft + mLeft;
                    allHeight = allHeight + descHeight;
                    textYPos = textYPos - descHeight;
                }
            }

            String sCoord;

            if (viewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK) {
                sCoord = mWaypoint.getCoordinate().formatCoordinateLineBreak();
            } else {
                sCoord = mWaypoint.getCoordinate().formatCoordinate();
            }

            float coordHeight = (mCoordCache.setText(sCoord, mSpriteCachePos.x + mIconSize + mLeft, textYPos)).height + mLeft + mLeft;
            allHeight += coordHeight;

            if (allHeight > UiSizes.getInstance().getCacheListItemRec().asFloat().getHeight()) {

                if (!inChange && getHeight() != allHeight) {
                    inChange = true;
                    setHeight(allHeight);
                    requestLayout();
                    inChange = false;
                }
            }
        } else {
            cacheInfo.setSize(this);
            cacheInfo.requestLayout();
        }

    }

    @Override
    public Priority getPriority() {
        return Priority.Low;
    }

    @Override
    public void speedChanged() {
    }

    public float getAttributeHeight() {
        if (cacheInfo == null)
            return 0;
        return cacheInfo.getAttributeHeight();
    }

    public float getTexteHeight() {
        if (cacheInfo == null)
            return 0;
        return cacheInfo.getTextHeight();

    }

    public float getStarsHeight() {
        if (cacheInfo == null)
            return 0;
        return cacheInfo.getStarsHeight();

    }

    public ExtendedCacheInfo getCacheInfo() {
        return cacheInfo;
    }

    /**
     * mit ausgeschaltener scissor berechnung
     *
     * @author Longri
     */
    public static class ExtendedCacheInfo extends CacheInfo {

        public ExtendedCacheInfo(CB_RectF rec, String Name, Cache value) {
            super(rec, Name, value);
        }

        @Override
        public void renderChilds(final Batch batch, ParentInfo parentInfo) {
            if (!disableScissor)
                Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

            batch.flush();

            render(batch);
            batch.flush();

            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        }

    }
}

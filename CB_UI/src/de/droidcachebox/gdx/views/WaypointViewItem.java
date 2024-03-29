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

import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.ParentInfo;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CacheInfo;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class WaypointViewItem extends ListViewItemBackground implements PositionChangedEvent {
    private static final String sClass = "WaypointViewItem";
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    protected ExtendedCacheInfo cacheInfo;
    protected boolean isPressed = false;
    boolean inChange = false;
    private Cache mCache;
    private Waypoint wayPoint;
    private CB_RectF arrowRec;
    private Sprite arrow = new Sprite(Sprites.Arrows.get(0));
    private BitmapFontCache distance;
    private Sprite mIconSprite;
    private BitmapFontCache mNameCache;
    private BitmapFontCache mDescCache;
    private BitmapFontCache mCoordCache;
    private final int viewMode;

    public WaypointViewItem(CB_RectF rec, int index, Cache cache, Waypoint waypoint) {
        // CB_UI.Slider, WaypointView (geoCache + waypoint) for the waypointentry
        super(rec, index, "");
        viewMode = CacheInfo.VIEW_MODE_WAYPOINTS;
        initialize(index, cache, waypoint);
    }

    public WaypointViewItem(CB_RectF rec, int index, Cache cache, Waypoint waypoint, int viewMode) {
        // CB_UI.Slider VIEW_MODE_SLIDER for the geoCache entry
        super(rec, index, "");
        this.viewMode = viewMode;
        initialize(index, cache, waypoint);
    }

    private void initialize(int index, Cache cache, Waypoint waypoint) {
        mCache = cache;
        wayPoint = waypoint;

        distance = new BitmapFontCache(Fonts.getSmall());
        distance.setColor(COLOR.getFontColor());

        if (waypoint == null) // this Item is the Cache
        {
            if (cache == null) {
                arrow = null;
                distance = null;
                return;
            }
            cacheInfo = new ExtendedCacheInfo(this, "CacheInfo " + index + " @" + cache.getGeoCacheCode(), cache);
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
        return wayPoint;
    }

    private void setDistanceString(final String txt) {
        if (this.isDisposed)
            return;
        if (txt != null) {
            try {
                distance = new BitmapFontCache(Fonts.getSmall());
                distance.setColor(COLOR.getFontColor());
                float x = arrowRec.getHalfWidth();
                GlyphLayout bounds = distance.setText(txt, arrowRec.getX(), arrowRec.getY());
                distance.setPosition(x - (bounds.width / 2f), 0);
            } catch (Exception ex) {
                Log.err(sClass, "setDistanceString: '" + txt + "'");
            }
        }
    }

    private void setActLocator() {
        if (mCache == null)
            return;

        if (Locator.getInstance().isValid()) {
            boolean isDefined = wayPoint != null;
            if (isDefined) {
                if (wayPoint.getCoordinate() == null) {
                    isDefined = false;
                } else {
                    if (wayPoint.getCoordinate().isZero()) isDefined = false;
                }
            }

            if (isDefined) {
                double lat, lon;
                float calculatedDistance;
                if (wayPoint == null) {
                    lat = mCache.getLatitude();
                    lon = mCache.getLongitude();
                    calculatedDistance = mCache.recalculateAndGetDistance(CalculationType.FAST, true, Locator.getInstance().getMyPosition());
                } else {
                    lat = wayPoint.getLatitude();
                    lon = wayPoint.getLongitude();
                    calculatedDistance = wayPoint.recalculateAndGetDistance();
                }

                Coordinate position = Locator.getInstance().getMyPosition();
                double heading = Locator.getInstance().getHeading();
                double bearing = CoordinateGPS.Bearing(CalculationType.FAST, position.getLatitude(), position.getLongitude(), lat, lon);
                double cacheBearing = -(bearing - heading);
                setDistanceString(UnitFormatter.distanceString(calculatedDistance));

                arrow.setRotation((float) cacheBearing);
                if (arrow.getColor().r == DISABLE_COLOR.r && arrow.getColor().g == DISABLE_COLOR.g && arrow.getColor().b == DISABLE_COLOR.b)// ignore
                // alpha
                {
                    float size = getHeight() / 2.3f;
                    arrow = new Sprite(Sprites.Arrows.get(0));
                    arrow.setBounds(arrowRec.getX(), arrowRec.getY(), size, size);
                    arrow.setOrigin(arrowRec.getHalfWidth(), arrowRec.getHalfHeight());
                }
            } else {
                arrow = null;
                setDistanceString("?_?_?");
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
        if (mIconSprite == null && wayPoint != null)
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
        try {
            if (this.isDisposed) return;

            if (viewMode != CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)// For Compass without own compass
            {
                float size = UiSizes.getInstance().getCacheListItemRec().asFloat().getHeight() / 2.3f;
                arrowRec = new CB_RectF(getWidth() - (size * 1.2f), getHeight() - (size * 1.6f), size, size);
                arrow.setBounds(arrowRec.getX(), arrowRec.getY(), size, size);
                arrow.setOrigin(arrowRec.getHalfWidth(), arrowRec.getHalfHeight());

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

            if (wayPoint != null) {
                float scaleFactor = getWidth() / UiSizes.getInstance().getCacheListItemRec().getWidth();
                float mLeft = 3 * scaleFactor;
                float mTop = 3 * scaleFactor;

                float mIconSize = Fonts.measure("T").height * 3.5f * scaleFactor;

                Vector2 mSpriteCachePos = new Vector2(mLeft + mLeft, getHeight() - mTop - mIconSize);

                { // Icon Sprite erstellen
                    // MultiStage Waypoint anders darstellen wenn dieser als Startpunkt definiert ist
                    if ((wayPoint.waypointType == GeoCacheType.MultiStage) && wayPoint.isStartWaypoint)
                        mIconSprite = new Sprite(Sprites.getSprite("big" + GeoCacheType.MultiStage.name() + "StartP"));
                    else
                        mIconSprite = new Sprite(Sprites.getSprite("big" + wayPoint.waypointType.name()));

                    mIconSprite.setSize(mIconSize, mIconSize);
                    mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);
                }

                float allHeight;
                float textYPos = getHeight() - mLeft;

                mNameCache = new BitmapFontCache(Fonts.getNormal());
                String theName = wayPoint.getWaypointCode().substring(0, 2) + ": " + wayPoint.getTitleForGui();
                mNameCache.setColor(COLOR.getFontColor());
                GlyphLayout glName = mNameCache.setText(theName, mSpriteCachePos.x + mIconSize + mLeft, textYPos);
                allHeight = glName.height + mLeft + mLeft;
                textYPos = textYPos - allHeight;

                if (viewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK) {
                    mDescCache = null;
                } else {
                    String wpDescription = wayPoint.getDescription();
                    if (wpDescription.length() > 0) {
                        float textXPos = mSpriteCachePos.x + mIconSize + mLeft;
                        mDescCache = new BitmapFontCache(Fonts.getBubbleNormal());
                        mDescCache.setColor(COLOR.getFontColor());
                        GlyphLayout gl;
                        try {
                            gl = mDescCache.setText(wpDescription, textXPos, textYPos, getWidth() - (textXPos + mIconSize + mLeft), Align.left, true);
                        } catch (Exception e) {
                            gl = mDescCache.setText("", textXPos, textYPos, getWidth() - (textXPos + mIconSize + mLeft), Align.left, true);
                        }
                        float descHeight = gl.height + mLeft + mLeft;
                        allHeight = allHeight + descHeight;
                        textYPos = textYPos - descHeight;
                    }
                }

                String sCoord;
                if (viewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK) {
                    sCoord = wayPoint.getCoordinate().formatCoordinateLineBreak();
                } else {
                    sCoord = wayPoint.getCoordinate().formatCoordinate();
                }
                mCoordCache = new BitmapFontCache(Fonts.getBubbleNormal());
                mCoordCache.setColor(COLOR.getFontColor());
                GlyphLayout glCoords;
                try {
                    glCoords = mCoordCache.setText(sCoord, mSpriteCachePos.x + mIconSize + mLeft, textYPos);
                } catch (Exception e) {
                    glCoords = mCoordCache.setText("???", mSpriteCachePos.x + mIconSize + mLeft, textYPos);
                }
                allHeight = allHeight + glCoords.height + mLeft + mLeft;

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
        catch (Exception ex) {
            Log.err("WaypointViewItem", "requestLayout", ex);
            mNameCache = null;
            mDescCache = null;
            mCoordCache = null;
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

    public float getTextHeight() {
        if (cacheInfo == null)
            return 0;
        return cacheInfo.getTextHeight();

    }

    public float getStarsHeight() {
        if (cacheInfo == null)
            return 0;
        return cacheInfo.getStarsHeight();

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
        public void renderChildren(final Batch batch, ParentInfo parentInfo) {
            if (!disableScissor)
                Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);

            batch.flush();

            render(batch);
            batch.flush();

            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        }

    }
}

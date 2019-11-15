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
import de.droidcachebox.database.CacheTypes;
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
    protected ExtendedCacheInfo extendedCacheInfo;
    protected boolean isPressed = false;
    boolean inChange = false;
    private Cache mCache;
    private Waypoint mWaypoint;
    private CB_RectF ArrowRec;
    private Sprite arrow = new Sprite(Sprites.Arrows.get(0));
    private BitmapFontCache distance;
    private Sprite mIconSprite;
    private float mIconSize = 0;
    private float mMargin = 0;
    private BitmapFontCache mNameCache;
    private BitmapFontCache mDescCache;
    private BitmapFontCache mCoordCache;
    private int ViewMode;

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

        if (waypoint == null) // this Item is the Cache
        {
            if (cache == null) {
                arrow = null;
                distance = null;
                return;
            }
            extendedCacheInfo = new ExtendedCacheInfo(this, "CacheInfo " + Index + " @" + cache.getGcCode(), cache);
            extendedCacheInfo.setZeroPos();
            extendedCacheInfo.setViewMode(ViewMode);
            this.addChild(extendedCacheInfo);
        }
        requestLayout();

        //register pos changed event
        if ((ViewMode & CacheInfo.SHOW_COMPASS) == CacheInfo.SHOW_COMPASS)
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

            if (mWaypoint != null && mWaypoint.Pos != null && mWaypoint.Pos.isZero()) {
                arrow = null;
                setDistanceString("???");
            } else {
                double lat = (mWaypoint == null) ? mCache.getLatitude() : mWaypoint.Pos.getLatitude();
                double lon = (mWaypoint == null) ? mCache.getLongitude() : mWaypoint.Pos.getLongitude();
                float distance = (mWaypoint == null) ? mCache.Distance(CalculationType.FAST, true) : mWaypoint.getDistance();

                Coordinate position = Locator.getInstance().getMyPosition();
                double heading = Locator.getInstance().getHeading();
                double bearing = CoordinateGPS.Bearing(CalculationType.FAST, position.getLatitude(), position.getLongitude(), lat, lon);
                double cacheBearing = -(bearing - heading);
                setDistanceString(UnitFormatter.DistanceString(distance));

                arrow.setRotation((float) cacheBearing);
                if (arrow.getColor().r == DISABLE_COLOR.r && arrow.getColor().g == DISABLE_COLOR.g && arrow.getColor().b == DISABLE_COLOR.b)// ignore
                // alpha
                {
                    float size = this.getHeight() / 2.3f;
                    arrow = new Sprite(Sprites.Arrows.get(0));
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
        PositionChangedListeners.removeListener(this);
        if (extendedCacheInfo != null)
            extendedCacheInfo.dispose();
        extendedCacheInfo = null;

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

        if (ViewMode != CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)// For Compass without own compass
        {
            float size = UiSizes.getInstance().getCacheListItemRec().asFloat().getHeight() / 2.3f;
            ArrowRec = new CB_RectF(this.getWidth() - (size * 1.2f), this.getHeight() - (size * 1.6f), size, size);
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
            mMargin = mLeft;

            mIconSize = Fonts.Measure("T").height * 3.5f * scaleFactor;

            Vector2 mSpriteCachePos = new Vector2(mLeft + mMargin, getHeight() - mTop - mIconSize);

            { // Icon Sprite erstellen
                // MultiStage Waypoint anders darstellen wenn dieser als Startpunkt definiert ist
                if ((mWaypoint.Type == CacheTypes.MultiStage) && mWaypoint.IsStart)
                    mIconSprite = new Sprite(Sprites.getSprite("big" + CacheTypes.MultiStage.name() + "StartP"));
                else
                    mIconSprite = new Sprite(Sprites.getSprite("big" + mWaypoint.Type.name()));

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

            float allHeight = (mNameCache.setText(mWaypoint.getGcCode().substring(0, 2) + ": " + mWaypoint.getTitleForGui(), mSpriteCachePos.x + mIconSize + mMargin, textYPos)).height + mMargin + mMargin;
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

            String sCoord;

            if (ViewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK) {
                sCoord = mWaypoint.Pos.formatCoordinateLineBreak();
            } else {
                sCoord = mWaypoint.Pos.FormatCoordinate();
            }

            float coordHeight = (mCoordCache.setText(sCoord, mSpriteCachePos.x + mIconSize + mMargin, textYPos)).height + mMargin + mMargin;
            allHeight += coordHeight;
            textYPos -= coordHeight;

            if (allHeight > UiSizes.getInstance().getCacheListItemRec().asFloat().getHeight()) {

                if (!inChange && getHeight() != allHeight) {
                    inChange = true;
                    setHeight(allHeight);
                    requestLayout();
                    inChange = false;
                }
            }
        } else {
            extendedCacheInfo.setSize(this);
            extendedCacheInfo.requestLayout();
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
        if (extendedCacheInfo == null)
            return 0;
        return extendedCacheInfo.getAttributeHeight();
    }

    public float getTexteHeight() {
        if (extendedCacheInfo == null)
            return 0;
        return extendedCacheInfo.getTextHeight();

    }

    public float getStarsHeight() {
        if (extendedCacheInfo == null)
            return 0;
        return extendedCacheInfo.getStarsHeight();

    }

    /**
     * mit ausgeschaltener scissor berechnung
     *
     * @author Longri
     */
    private class ExtendedCacheInfo extends CacheInfo {

        public ExtendedCacheInfo(CB_RectF rec, String Name, Cache value) {
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
}

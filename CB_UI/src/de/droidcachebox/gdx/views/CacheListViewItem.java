package de.droidcachebox.gdx.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.ParentInfo;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CacheInfo;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;
import de.droidcachebox.utils.UnitFormatter;

public class CacheListViewItem extends ListViewItemBackground implements PositionChangedEvent {

    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    protected ExtendedCacheInfo info;
    protected boolean isPressed = false;
    double heading = 0;
    private Sprite liveCacheIcon;
    private Sprite arrow = new Sprite(Sprites.Arrows.get(0));
    private BitmapFontCache distance = new BitmapFontCache(Fonts.getSmall());

    // private BitmapFontCache debugIndex = new BitmapFontCache(Fonts.getSmall());

    private CB_RectF ArrowRec;

    private Cache mCache;
    private String lastString = "";

    public CacheListViewItem(CB_RectF rec, int Index, Cache cache) {
        super(rec, Index, cache.getName());
        mCache = cache;
        info = new ExtendedCacheInfo(UiSizes.getInstance().getCacheListItemRec().asFloat(), "CacheInfo " + Index + " @" + cache.getGcCode(), cache);
        info.setZeroPos();
        distance.setColor(COLOR.getFontColor());
        this.addChild(info);
        PositionChangedListeners.addListener(this);

        float size = this.getHeight() / 2.3f;
        ArrowRec = new CB_RectF(this.getWidth() - (size * 1.2f), this.getHeight() - (size * 1.6f), size, size);
        arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
        arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());

        if (!Locator.getInstance().Valid()) {
            arrow.setColor(DISABLE_COLOR);
            setDistanceString("---");
        } else {
            setActLocator();
        }

        if (mCache.isLive()) {
            liveCacheIcon = new Sprite(Sprites.LiveBtn.get(0));
            liveCacheIcon.setBounds(ArrowRec.getX() + (ArrowRec.getHalfWidth() / 2), ArrowRec.getMaxY(), ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());
        }

    }

    public Cache getCache() {
        return mCache;
    }

    private void setDistanceString(String txt) {
        if (this.isDisposed())
            return;

        if (txt == null)
            txt = "";

        if (txt.equals(lastString)) {
            return;
        }
        lastString = txt;
        synchronized (distance) {
            try {
                if (distance != null) {
                    GlyphLayout bounds = distance.setText(txt, ArrowRec.getX(), ArrowRec.getY());
                    float x = ArrowRec.getHalfWidth() - (bounds.width / 2f);
                    distance.setPosition(x, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void setActLocator() {

        // Log.debug(log, "CacheListItem set ActLocator");

        if (Locator.getInstance().Valid()) {

            if (mCache.Pos == null) {
                // mCache was disposed
                Cache c = Database.Data.cacheList.getCacheByIdFromCacheList(mCache.Id);
                if (c == null) {
                    return;
                }
                mCache = c;
            }

            Coordinate position = Locator.getInstance().getMyPosition();

            Waypoint FinalWp = mCache.getCorrectedFinal();

            Coordinate Final = FinalWp != null ? FinalWp.Pos : mCache.Pos;
            CalculationType calcType = CalculationType.FAST;
            Cache c = GlobalCore.getSelectedCache();
            if (c != null) {
                calcType = mCache.Id == GlobalCore.getSelectedCache().Id ? CalculationType.ACCURATE : CalculationType.FAST;
            }

            float result[] = new float[4];

            try {
                MathUtils.computeDistanceAndBearing(calcType, position.getLatitude(), position.getLongitude(), Final.getLatitude(), Final.getLongitude(), result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            double cacheBearing = -(result[2] - heading);
            mCache.cachedDistance = result[0];
            setDistanceString(UnitFormatter.DistanceString(mCache.cachedDistance));

            arrow.setRotation((float) cacheBearing);
            if (arrow.getColor() == DISABLE_COLOR) {
                float size = this.getHeight() / 2.3f;
                arrow = new Sprite(Sprites.Arrows.get(0));
                arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
                arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());
            }
        } else {
            if (mCache.cachedDistance >= 0) // (mCache.cachedDistance > 0)|| mCache == GlobalCore.getSelectedCache())
            {
                setDistanceString(UnitFormatter.DistanceString(mCache.cachedDistance));
            }
        }
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);

        if (liveCacheIcon != null)
            liveCacheIcon.draw(batch);
        if (arrow != null)
            arrow.draw(batch);
        if (distance != null) {
            synchronized (distance) {
                distance.draw(batch);
            }

        }

    }

    @Override
    public void dispose() {
        PositionChangedListeners.removeListener(this);
        if (info != null)
            info.dispose();
        info = null;

        arrow = null;
        // if (distance != null) distance.dispose();
        distance = null;
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
        this.heading = Locator.getInstance().getHeading();
        setActLocator();
    }

    @Override
    public String getReceiverName() {
        return "Core.CacheListViewItem";
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    @Override
    public void speedChanged() {
    }

    /**
     * mit ausgeschaltener scissor berechnung
     *
     * @author Longri
     */
    class ExtendedCacheInfo extends CacheInfo {

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
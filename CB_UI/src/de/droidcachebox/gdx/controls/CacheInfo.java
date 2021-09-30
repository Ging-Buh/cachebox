package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import de.droidcachebox.database.Attribute;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.GeoCacheSize;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.database.LogType;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class CacheInfo extends CB_View_Base {
    public static final Color gcVoteColor = new Color(0.5f, 0.5f, 1f, 1f);
    public static final int SHOW_COMPASS = 1;
    public static final int SHOW_NAME = 2;
    public static final int SHOW_OWNER = 4;
    public static final int SHOW_COORDS = 8;
    public static final int SHOW_GC = 16;
    public static final int SHOW_LAST_FOUND = 32;
    public static final int SHOW_ATTRIBUTES = 64;
    public static final int SHOW_CORRDS_WITH_LINEBRAKE = 128;
    public static final int SHOW_S_D_T = 256;
    public static final int SHOW_VOTE = 512;
    public static final int SHOW_ICON = 1024;
    public static final int SHOW_HIDDEN_DATE = 2048;
    /**
     * SHOW_GC, SHOW_NAME, SHOW_COMPASS
     */
    public static final int VIEW_MODE_CACHE_LIST = SHOW_GC + SHOW_NAME + SHOW_COMPASS + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 19;
    /**
     * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS, SHOW_GC, SHOW_LAST_FOUND, SHOW_ATTRIBUTES, SHOW_HIDDEN_DATE
     */
    public static final int VIEW_MODE_SLIDER = SHOW_ATTRIBUTES + SHOW_LAST_FOUND + SHOW_GC + SHOW_COORDS + SHOW_OWNER + SHOW_HIDDEN_DATE + SHOW_NAME + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 126
    /**
     * SHOW_COORDS, SHOW_COMPASS, SHOW_NAME
     */
    public static final int VIEW_MODE_WAYPOINTS = SHOW_COORDS + SHOW_NAME + SHOW_COMPASS + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T;
    /**
     * SHOW_COORDS, SHOW_COMPASS, SHOW_NAME
     */
    public static final int VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK = SHOW_COORDS + SHOW_NAME + SHOW_CORRDS_WITH_LINEBRAKE + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 138
    /**
     * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS
     */
    public static final int VIEW_MODE_BUBBLE = SHOW_COORDS + SHOW_OWNER + SHOW_NAME + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 30
    /**
     * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS
     */
    public static final int VIEW_MODE_BUBBLE_EVENT = SHOW_COORDS + SHOW_HIDDEN_DATE + SHOW_NAME + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T;
    private final int AttributesPerLine = 12;
    GlyphLayout layout;
    private int mViewMode = VIEW_MODE_CACHE_LIST;
    private Cache mCache;
    private float mIconSize;
    private float mMargin = 0;
    private Sprite mRatingSprite;
    private Sprite mIconSprite;
    private Sprite mFoundOwnerSprite;
    private Sprite mFavoriteSprite;
    private Sprite mAvailableSprite;
    private Sprite mSSprite;
    private Sprite mDSprite;
    private Sprite mTSprite;
    private Sprite mTBSprite;
    private Sprite mFPSprite;
    private Sprite[] mAttrSprites;
    private BitmapFont mBitmapFont = Fonts.getNormal();
    private BitmapFont mBitmapFontSmall = Fonts.getSmall();
    private BitmapFontCache mS_FontCache;
    private BitmapFontCache mD_FontCache;
    private BitmapFontCache mT_FontCache;
    private BitmapFontCache mTB_FontCache;
    private BitmapFontCache mFP_FontCache;
    private BitmapFontCache mInfo_FontCache;
    private boolean cacheIsInitial = false;
    private float mScaleFactor;

    public CacheInfo(SizeF size, String Name, Cache value) {
        super(size, Name);
        setCache(value);
        cacheIsInitial = false;
        initialMesure();
    }

    public CacheInfo(CB_RectF rec, String Name, Cache value) {
        super(rec, Name);
        setCache(value);
        cacheIsInitial = false;
        initialMesure();
    }

    /*
    public boolean needsMaintenance() {
        return mCache.getAttributes().contains(Attribute.Needs_maintenance);
        / *
        Date lastNeedsMaintenance = null;
        CB_List<LogEntry> logEntries = Database.getLogs(mCache);
        for (int i = 0; i < logEntries.size(); i++) {
            LogEntry logEntry = logEntries.get(i);
            if (logEntry.logType == LogType.owner_maintenance) {
                return false;
            }
            if (logEntry.logType == LogType.needs_maintenance && lastNeedsMaintenance == null) {
                return true;
            }
        }
        return false;
         * /
    }
     */

    /*
    public int numberOfDNFsAfterLastFound() {
        int dnfCount = 0;
        CB_List<LogEntry> logEntries = Database.getLogs(mCache);
        for (int i = 0; i < logEntries.size(); i++) {
            LogEntry logEntry = logEntries.get(i);
            if (logEntry.logType == LogType.found) {
                return dnfCount;
            }
            if (logEntry.logType == LogType.didnt_find) {
                dnfCount++;
            }
        }
        return dnfCount;
    }
     */

    private String getLastFoundLogDate() {
        CB_List<LogEntry> logEntries = CBDB.Data.getLogs(mCache);
        for (int i = 0; i < logEntries.size(); i++) {
            LogEntry logEntry = logEntries.get(i);
            if (logEntry.logType == LogType.found) {
                return new SimpleDateFormat("dd.MM.yy", Locale.US).format(logEntry.logDate);
            }
        }
        return "";
    }

    void initialMesure() {
        mScaleFactor = getWidth() / UiSizes.getInstance().getCacheListItemRec().getWidth();
        mIconSize = Fonts.measureForSmallFont("T").height * 3.5f * mScaleFactor;
        mMargin = 3 * mScaleFactor;
    }

    public void setFont(BitmapFont font) {
        mBitmapFont = font;
        requestLayout();
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);

        try {
            if (mIconSprite != null)
                mIconSprite.draw(batch);
            if (mFoundOwnerSprite != null)
                mFoundOwnerSprite.draw(batch);
            if (mRatingSprite != null)
                mRatingSprite.draw(batch);
            if (mS_FontCache != null)
                mS_FontCache.draw(batch);
            if (mD_FontCache != null)
                mD_FontCache.draw(batch);
            if (mT_FontCache != null)
                mT_FontCache.draw(batch);
            if (mTB_FontCache != null)
                mTB_FontCache.draw(batch);
            if (mSSprite != null)
                mSSprite.draw(batch);
            if (mDSprite != null)
                mDSprite.draw(batch);
            if (mTSprite != null)
                mTSprite.draw(batch);
            if (mTBSprite != null)
                mTBSprite.draw(batch);
            if (mFPSprite != null)
                mFPSprite.draw(batch);
            if (mFP_FontCache != null)
                mFP_FontCache.draw(batch);
            if (mInfo_FontCache != null)
                mInfo_FontCache.draw(batch);
            if (mFavoriteSprite != null)
                mFavoriteSprite.draw(batch);
            if (mAvailableSprite != null)
                mAvailableSprite.draw(batch);
            if (mAttrSprites != null) {
                for (Sprite s : mAttrSprites)
                    if (s != null)
                        s.draw(batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        mIconSprite = null;
        mFoundOwnerSprite = null;
        mRatingSprite = null;
        mS_FontCache = null;
        mD_FontCache = null;
        mT_FontCache = null;
        mTB_FontCache = null;
        mSSprite = null;
        mDSprite = null;
        mTSprite = null;
        mTBSprite = null;
        mInfo_FontCache = null;
        mFavoriteSprite = null;
        mAvailableSprite = null;
        if (mAttrSprites != null) {
            Arrays.fill(mAttrSprites, null);
            mAttrSprites = null;
        }
    }

    public void requestLayout() {
        try {
            if (mCache == null)
                return;
            if (mCache.geoCacheSize == null)
                return;

            this.removeChilds();

            float mLeft = mMargin;
            float mTop = mMargin;
            float mBottom = mMargin;

            // Size
            mS_FontCache = new BitmapFontCache(mBitmapFontSmall);
            mS_FontCache.setText("MSRLO", 0, 0);
            mS_FontCache.setColor(COLOR.getFontColor());
            // float starHeight = mS_FontCache.getLayouts().first().height * 1.1f;
            float starHeight = mIconSize / 3.5f;
            SizeF mStarSize = new SizeF(starHeight * 5, starHeight);

            if (ifModeFlag(SHOW_S_D_T)) {
                String CacheSize = GeoCacheSize.toShortString(mCache);
                mS_FontCache.setText(CacheSize, 0, 0);
                mBottom += mS_FontCache.getLayouts().first().height;
                float mSpriteBottom = mMargin;
                mS_FontCache.setPosition(mLeft, mBottom);
                mLeft += mS_FontCache.getLayouts().first().width + mMargin;

                // mStarSize.scale(mScaleFactor);
                mSSprite = new Sprite(Sprites.SizesIcons.get((mCache.geoCacheSize.ordinal())));
                mSSprite.setBounds(mLeft, mSpriteBottom, mStarSize.getWidth(), mStarSize.getHeight());
                // Difficulty
                mLeft += mSSprite.getWidth() + mMargin + mMargin;
                mD_FontCache = new BitmapFontCache(mBitmapFontSmall);
                mD_FontCache.setColor(COLOR.getFontColor());
                mD_FontCache.setText("D", mLeft, mBottom);
                mLeft += mD_FontCache.getLayouts().first().width + mMargin;
                mDSprite = new Sprite(Sprites.Stars.get((int) (mCache.getDifficulty() * 2)));
                mDSprite.setBounds(mLeft, mSpriteBottom, mStarSize.getWidth(), mStarSize.getHeight());
                mDSprite.setRotation(0);
                // Terrain
                mLeft += mDSprite.getWidth() + mMargin + mMargin;
                mT_FontCache = new BitmapFontCache(mBitmapFontSmall);
                mT_FontCache.setColor(COLOR.getFontColor());
                mT_FontCache.setText("T", mLeft, mBottom);
                mLeft += mT_FontCache.getLayouts().first().width + mMargin;
                mTSprite = new Sprite(Sprites.Stars.get((int) (mCache.getTerrain() * 2)));
                mTSprite.setBounds(mLeft, mSpriteBottom, mStarSize.getWidth(), mStarSize.getHeight());
                mTSprite.setRotation(0);
                // Draw TB
                mLeft += mTSprite.getWidth() + mMargin + mMargin + mMargin + mMargin;
                int numTb = mCache.numTravelbugs;
                if (numTb > 0) {
                    float sizes = mStarSize.getWidth() / 2.1f;

                    mTBSprite = new Sprite(Sprites.getSprite(IconName.tb.name()));
                    mTBSprite.setBounds(mLeft, mBottom - (sizes / 1.8f) - mMargin, sizes, sizes);
                    mTBSprite.setOrigin(sizes / 2, sizes / 2);
                    mTBSprite.setRotation(90);
                    mLeft += mTBSprite.getWidth() + mMargin;
                    if (numTb > 1) {
                        mTB_FontCache = new BitmapFontCache(mBitmapFontSmall);
                        mTB_FontCache.setColor(COLOR.getFontColor());
                        mTB_FontCache.setText("x" + numTb, mLeft, mBottom);
                        mLeft += mTB_FontCache.getLayouts().first().width + mMargin;
                    }
                    mLeft += mMargin;
                } else {
                    mTBSprite = null;
                    mTB_FontCache = null;
                }

                //Draw FavPoints
                int numFP = mCache.favPoints;
                if (numFP > 0) {
                    float sizes = mStarSize.getWidth() / 4f;

                    mFPSprite = new Sprite(Sprites.getSprite(IconName.FavPoi.name()));
                    mFPSprite.setBounds(mLeft, mBottom - (sizes / 1.8f) - mMargin, sizes, sizes);

                    mLeft += mFPSprite.getWidth() + mMargin;
                    mFP_FontCache = new BitmapFontCache(mBitmapFontSmall);
                    mFP_FontCache.setColor(COLOR.getFontColor());
                    mFP_FontCache.setText("x" + numFP, mLeft, mBottom);
                } else {
                    mFPSprite = null;
                }
            }

            Vector2 mSpriteCachePos = new Vector2(0, getHeight() - mTop - mIconSize);

            // Rating stars
            if (ifModeFlag(SHOW_VOTE)) {
                mLeft = -4 * mScaleFactor;

                mStarSize.scale(0.7f);
                mRatingSprite = new Sprite(Sprites.Stars.get((int) Math.min(mCache.gcVoteRating * 2, 5 * 2)));
                mRatingSprite.setBounds(mLeft + mStarSize.getHeight(), getHeight() - mTop - mStarSize.getWidth() - mMargin - mMargin - mMargin, mStarSize.getWidth(), mStarSize.getHeight());
                mRatingSprite.setOrigin(0, mStarSize.getHalfHeight());
                mRatingSprite.setRotation(90);
                mRatingSprite.setColor(gcVoteColor);
                //
                mLeft = mLeft + starHeight;
                mSpriteCachePos = new Vector2(mLeft + mMargin, getHeight() - mTop - mIconSize);
            }

            if (ifModeFlag(SHOW_NAME)
                    || ifModeFlag(SHOW_OWNER)
                    || ifModeFlag(SHOW_COORDS)
                    || ifModeFlag(SHOW_CORRDS_WITH_LINEBRAKE)
                    || ifModeFlag(SHOW_GC)
                    || ifModeFlag(SHOW_LAST_FOUND)
            ) {
                StringBuilder text = createText();

                mInfo_FontCache = new BitmapFontCache(mBitmapFont);

                if (mCache.isArchived() || !mCache.isAvailable()) {
                    mInfo_FontCache.setColor(Color.RED);
                } else {
                    mInfo_FontCache.setColor(COLOR.getFontColor());
                }
                mInfo_FontCache.setText(text.toString(), mSpriteCachePos.x + mIconSize + mMargin, this.getHeight() - mMargin);

            }

            if (ifModeFlag(SHOW_ICON)) { // Icon Sprite erstellen

                if (mCache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                    mIconSprite = new Sprite(Sprites.getSprite("big" + mCache.getGeoCacheType().name() + "Solved"));
                } else if ((mCache.getGeoCacheType() == GeoCacheType.Multi) && mCache.getStartWaypoint() != null) {
                    // Multi anders darstellen wenn dieser einen definierten Startpunkt hat
                    mIconSprite = new Sprite(Sprites.getSprite("big" + GeoCacheType.Multi.name() + "StartP"));
                } else if ((mCache.getGeoCacheType() == GeoCacheType.Mystery) && mCache.getStartWaypoint() != null) {
                    // Mystery anders darstellen wenn dieser keinen Final aber einen definierten Startpunkt hat
                    mIconSprite = new Sprite(Sprites.getSprite("big" + GeoCacheType.Mystery.name() + "StartP"));
                } else {
                    mIconSprite = new Sprite(Sprites.getSprite("big" + mCache.getGeoCacheType().name()));
                }
                mIconSprite.setSize(mIconSize, mIconSize);
                mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);

                // infoIcons erstellen

                float infoSize = mIconSize / 2;

                if (mCache.isFound()) {
                    mFoundOwnerSprite = new Sprite(Sprites.getSprite("log0icon"));
                } else if (mCache.iAmTheOwner()) {
                    mFoundOwnerSprite = new Sprite(Sprites.getSprite(IconName.star.name()));
                }
                if (mFoundOwnerSprite != null) {
                    mFoundOwnerSprite.setSize(infoSize, infoSize);
                    mFoundOwnerSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);
                }

                if (mCache.isFavorite()) {
                    mFavoriteSprite = new Sprite(Sprites.getSprite(IconName.favorit.name()));
                    mFavoriteSprite.setSize(infoSize, infoSize);
                    mFavoriteSprite.setPosition(mSpriteCachePos.x + infoSize, mSpriteCachePos.y + infoSize);
                }

                if (mCache.isArchived()) {
                    mAvailableSprite = new Sprite(Sprites.getSprite(IconName.log11icon.name()));
                } else if (!mCache.isAvailable()) {
                    mAvailableSprite = new Sprite(Sprites.getSprite(IconName.disabled.name()));
                }
                if (mAvailableSprite != null) {
                    mAvailableSprite.setSize(infoSize, infoSize);
                    mAvailableSprite.setPosition(mSpriteCachePos.x + infoSize, mSpriteCachePos.y);
                }
            }

            if (ifModeFlag(SHOW_ATTRIBUTES)) { // create Attribute Icons
                float attSize = getWidth() / AttributesPerLine - mMargin;
                int attCount = mCache.getAttributes().size();
                int lineCount = getLineCount(attCount, AttributesPerLine);

                float attX = mMargin;
                float attY = ifModeFlag(SHOW_S_D_T) ? mSSprite.getHeight() + (2 * mMargin) : mMargin;
                if (lineCount > 1) {
                    attY += ((attSize + mMargin) * (lineCount - 1));
                }

                ArrayList<Attribute> attributes = mCache.getAttributes();
                if (attributes != null) {
                    Iterator<Attribute> attributesIterator = attributes.iterator();
                    mAttrSprites = new Sprite[attCount];
                    int count = 0;
                    int actLineCount = 0;
                    if (attributesIterator.hasNext()) {
                        do {
                            Attribute attribute = attributesIterator.next();
                            mAttrSprites[count] = Sprites.getSprite(attribute.getImageName().replace("_", "-") + "Icon");
                            mAttrSprites[count].setSize(attSize, attSize);
                            mAttrSprites[count].setPosition(attX, attY);

                            attX += mAttrSprites[count].getWidth() + mMargin;

                            if (AttributesPerLine == ++actLineCount) {
                                //next line
                                attY -= (attSize + mMargin);
                                actLineCount = 0;
                                attX = mMargin;
                            }

                            count++;
                        } while (attributesIterator.hasNext());
                    }
                }
            }
        } catch (Exception ex) {
            Log.err("CacheInfo", "requestLayout", ex);
        }
    }

    private StringBuilder createText() {
        String br = "\n";
        StringBuilder text = new StringBuilder();
        if (ifModeFlag(SHOW_NAME)) {
            if (mViewMode == VIEW_MODE_WAYPOINTS) {
                text.append(mCache.getGeoCacheCode()).append(": ");
            }
            text.append(mCache.getGeoCacheName());
            text.append(br);
        }
        if (mCache != null && (ifModeFlag(SHOW_OWNER) || ifModeFlag(SHOW_HIDDEN_DATE))) {
            if (ifModeFlag(SHOW_OWNER)) {
                text.append("by " + mCache.getOwner());
                if (ifModeFlag(SHOW_HIDDEN_DATE)) {
                    text.append(", ");
                }
            }

            if (ifModeFlag(SHOW_HIDDEN_DATE)) {
                text.append(UnitFormatter.getReadableDate(mCache.getDateHidden()));
            }

            text.append(br);
        }

        if (ifModeFlag(SHOW_COORDS)) {
            if (mCache.getCoordinate() == null) {
                text.append("????");
                text.append(br);
            } else {
                if (ifModeFlag(SHOW_CORRDS_WITH_LINEBRAKE)) {
                    text.append(mCache.getCoordinate().formatCoordinateLineBreak());
                    text.append(br);
                } else {
                    text.append(mCache.getCoordinate().formatCoordinate());
                    text.append(br);
                }
            }

        }

        if (ifModeFlag(SHOW_GC)) {
            text.append(mCache.getGeoCacheCode());
            text.append(br);
        }

        if (ifModeFlag(SHOW_LAST_FOUND)) {
            String lastFoundLogDate = getLastFoundLogDate();
            if (!lastFoundLogDate.equals("")) {
                text.append("last found: " + lastFoundLogDate);
            }
        }

        return text;
    }

    public float getAttributeHeight() {
        float attSize = getWidth() / AttributesPerLine - mMargin;
        int attCount = 0;
        if (mCache != null && mCache.getAttributes() != null)
            attCount = mCache.getAttributes().size();
        int lineCount = getLineCount(attCount, AttributesPerLine);
        return lineCount * (attSize + mMargin) + mMargin;
    }

    private int getLineCount(int attCount, final int countPerLine) {
        double d = attCount / countPerLine;
        int lineCount = (int) Math.rint(d);
        if (attCount % countPerLine > 0)
            lineCount++;
        return lineCount;
    }

    private boolean ifModeFlag(int flag) {
        return (mViewMode & flag) == flag;
    }

    @Override
    public void onResized(CB_RectF rec) {
        if (cacheIsInitial)
            requestLayout();
    }

    public void setViewMode(int viewMode) {
        mViewMode = viewMode;
        if (cacheIsInitial)
            requestLayout();
    }

    public void setCache(Cache cache) {
        if (mCache != null && cache != null && mCache.generatedId == cache.generatedId)
            return;
        mCache = cache;
        if (cacheIsInitial)
            requestLayout();
    }

    @Override
    protected void initialize() {
        cacheIsInitial = true;
        requestLayout();
    }

    public void setSmallFont(BitmapFont font) {
        mBitmapFontSmall = font;
        requestLayout();
    }

    @Override
    protected void skinIsChanged() {
        mBitmapFont = Fonts.getNormal();
        mBitmapFontSmall = Fonts.getSmall();
        requestLayout();
    }

    public float getTextHeight() {
        if (layout == null)
            layout = new GlyphLayout();
        layout.setText(mBitmapFont, createText().toString());
        return layout.height;
    }

    public float getStarsHeight() {
        if (ifModeFlag(SHOW_S_D_T)) {
            return mIconSize / 3.5f;
        } else
            return 0;
    }
}

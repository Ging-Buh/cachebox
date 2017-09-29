package CB_UI.GL_UI.Controls;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import CB_Core.Attributes;
import CB_Core.CacheSizes;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.UnitFormatter;

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
	 * SHOW_COMPASS, SHOW_OWNER, SHOW_CORRDS, SHOW_GC, SHOW_HIDDEN_DATE 
	 */
	public static final int VIEW_MODE_DESCRIPTION = SHOW_GC + SHOW_COORDS + SHOW_OWNER + SHOW_HIDDEN_DATE + SHOW_COMPASS + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 29;

	/**
	 * SHOW_OWNER, SHOW_CORRDS, SHOW_GC, SHOW_LAST_FOUND
	 */
	public static final int VIEW_MODE_COMPAS = 60;

	/**
	 * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS, SHOW_GC, SHOW_LAST_FOUND, SHOW_ATTRIBUTES, SHOW_HIDDEN_DATE 
	 */
	public static final int VIEW_MODE_SLIDER = SHOW_ATTRIBUTES + SHOW_LAST_FOUND + SHOW_GC + SHOW_COORDS + SHOW_OWNER + SHOW_HIDDEN_DATE + SHOW_NAME + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 126

	/**
	 * SHOW_COORDS, SHOW_COMPASS, SHOW_NAME
	 */
	public static final int VIEW_MODE_WAYPOINTS = SHOW_COORDS + SHOW_NAME + SHOW_COMPASS + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 11

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

	/**
	 * SHOW_S_D_T
	 */
	public static final int VIEW_MODE_SDT_ONLY = SHOW_S_D_T;

	/**
	 * SHOW_LAST_FOUND
	 */
	public static final int VIEW_MODE_LAST_FOUND_ONLY = SHOW_LAST_FOUND;

	/**
	 * SHOW_LAST_FOUND , SHOW_S_D_T
	 */
	public static final int VIEW_MODE_LAST_FOUND_AND_DTS = SHOW_LAST_FOUND + SHOW_S_D_T;

	private int mViewMode = VIEW_MODE_CACHE_LIST;

	private Cache mCache;
	private float mIconSize = 0;
	private SizeF mStarSize = new SizeF();
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
	private Sprite[] mAttrSprites;

	private BitmapFont mBitmapFont = Fonts.getNormal();
	private BitmapFont mBitmapFontSmall = Fonts.getSmall();

	private BitmapFontCache mS_FontCache;
	private BitmapFontCache mD_FontCache;
	private BitmapFontCache mT_FontCache;
	private BitmapFontCache mTB_FontCache;

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

	void initialMesure() {
		mScaleFactor = getWidth() / UiSizes.that.getCacheListItemRec().getWidth();
		mIconSize = Fonts.MeasureSmall("T").height * 3.5f * mScaleFactor;
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
			for (int i = 0; i < mAttrSprites.length; i++) {
				mAttrSprites[i] = null;
			}
			mAttrSprites = null;
		}
	}

	public void requestLayout() {
		try {
			if (mCache == null)
				return;
			if (mCache.Size == null)
				return;

			this.removeChilds();

			float mLeft = mMargin;
			float mTop = mMargin;
			float mBottom = mMargin;

			// Size
			mS_FontCache = new BitmapFontCache(mBitmapFontSmall);
			mS_FontCache.setText("MSRLO", 0, 0);
			mS_FontCache.setColor(COLOR.getFontColor());
			float starHeight = mS_FontCache.getLayouts().first().height * 1.1f;
			mStarSize = new SizeF(starHeight * 5, starHeight);

			if (ifModeFlag(SHOW_S_D_T)) {
				String CacheSize = CacheSizes.toShortString(mCache);
				mS_FontCache.setText(CacheSize, 0, 0);
				mBottom += mS_FontCache.getLayouts().first().height;
				float mSpriteBottom = mMargin;
				mS_FontCache.setPosition(mLeft, mBottom);
				mLeft += mS_FontCache.getLayouts().first().width + mMargin;

				mStarSize.scale(mScaleFactor);
				mSSprite = new Sprite(Sprites.SizesIcons.get((mCache.Size.ordinal())));
				mSSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
				// Difficulty
				mLeft += mSSprite.getWidth() + mMargin + mMargin;
				mD_FontCache = new BitmapFontCache(mBitmapFontSmall);
				mD_FontCache.setColor(COLOR.getFontColor());
				mD_FontCache.setText("D", mLeft, mBottom);
				mLeft += mD_FontCache.getLayouts().first().width + mMargin;
				mDSprite = new Sprite(Sprites.Stars.get((int) (mCache.getDifficulty() * 2)));
				mDSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
				mDSprite.setRotation(0);
				// Terrain
				mLeft += mDSprite.getWidth() + mMargin + mMargin;
				mT_FontCache = new BitmapFontCache(mBitmapFontSmall);
				mT_FontCache.setColor(COLOR.getFontColor());
				mT_FontCache.setText("T", mLeft, mBottom);
				mLeft += mT_FontCache.getLayouts().first().width + mMargin;
				mTSprite = new Sprite(Sprites.Stars.get((int) (mCache.getTerrain() * 2)));
				mTSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
				mTSprite.setRotation(0);
				// Draw TB
				mLeft += mTSprite.getWidth() + mMargin + mMargin + mMargin + mMargin;
				int numTb = mCache.NumTravelbugs;
				if (numTb > 0) {
					float sizes = mStarSize.width / 2.1f;

					mTBSprite = new Sprite(Sprites.getSprite(IconName.tb.name()));
					mTBSprite.setBounds(mLeft, mBottom - (sizes / 1.8f) - mMargin, sizes, sizes);
					mTBSprite.setOrigin(sizes / 2, sizes / 2);
					mTBSprite.setRotation(90);

					if (numTb > 1) {
						mLeft += mTBSprite.getWidth() + mMargin;
						mTB_FontCache = new BitmapFontCache(mBitmapFontSmall);
						mTB_FontCache.setColor(COLOR.getFontColor());
						mTB_FontCache.setText("x" + String.valueOf(numTb), mLeft, mBottom);
					}
				} else {
					mTBSprite = null;
					mTB_FontCache = null;
				}
			}

			Vector2 mSpriteCachePos = new Vector2(0, getHeight() - mTop - mIconSize);

			// Rating stars
			if (ifModeFlag(SHOW_VOTE)) {
				mLeft = -4 * mScaleFactor;

				mStarSize.scale(0.7f);
				mRatingSprite = new Sprite(Sprites.Stars.get((int) Math.min(mCache.Rating * 2, 5 * 2)));
				mRatingSprite.setBounds(mLeft + mStarSize.height, getHeight() - mTop - mStarSize.width - mMargin - mMargin - mMargin, mStarSize.width, mStarSize.height);
				mRatingSprite.setOrigin(0, mStarSize.halfHeight);
				mRatingSprite.setRotation(90);
				mRatingSprite.setColor(gcVoteColor);
				//
				mLeft += starHeight;
				mSpriteCachePos = new Vector2(mLeft + mMargin, getHeight() - mTop - mIconSize);
			}

			if (ifModeFlag(SHOW_NAME) || ifModeFlag(SHOW_OWNER) || ifModeFlag(SHOW_COORDS) || ifModeFlag(SHOW_CORRDS_WITH_LINEBRAKE) || ifModeFlag(SHOW_GC) || ifModeFlag(SHOW_LAST_FOUND)) {// Text zusammensetzen

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

				if (mCache.CorrectedCoordiantesOrMysterySolved()) {
					mIconSprite = new Sprite(Sprites.getSprite("big" + "Solved"));
				} else if ((mCache.Type == CacheTypes.Multi) && mCache.HasStartWaypoint()) {
					// Multi anders darstellen wenn dieser einen definierten Startpunkt hat
					mIconSprite = new Sprite(Sprites.getSprite("big" + CacheTypes.Multi.name() + "StartP"));
				} else if ((mCache.Type == CacheTypes.Mystery) && mCache.HasStartWaypoint()) {
					// Mystery anders darstellen wenn dieser keinen Final aber einen definierten Startpunkt hat
					mIconSprite = new Sprite(Sprites.getSprite("big" + CacheTypes.Mystery.name() + "StartP"));
				} else {
					mIconSprite = new Sprite(Sprites.getSprite("big" + mCache.Type.name()));
				}
				mIconSprite.setSize(mIconSize, mIconSize);
				mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);

				// infoIcons erstellen

				float infoSize = mIconSize / 2;

				if (mCache.isFound()) {
					mFoundOwnerSprite = new Sprite(Sprites.getSprite("log0icon"));
				} else if (mCache.ImTheOwner()) {
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
				float attSize = mIconSize;
				int attCount = mCache.getAttributes().size();
				final int countPerLine = (int) (this.getWidth() / (attSize + mMargin));
				int lineCount = getLineCount(attCount, countPerLine);

				float attX = mMargin;
				float attY = ifModeFlag(SHOW_S_D_T) ? mSSprite.getHeight() + (2 * mMargin) : mMargin;
				if (lineCount > 1) {
					attY += ((attSize + mMargin) * (lineCount - 1));
				}

				Iterator<Attributes> attrs = mCache.getAttributes().iterator();
				mAttrSprites = new Sprite[attCount];
				int count = 0;
				int actLineCount = 0;
				if (attrs != null && attrs.hasNext()) {
					do {
						Attributes attribute = attrs.next();
						mAttrSprites[count] = Sprites.getSprite(attribute.getImageName().replace("_", "-") + "Icon");
						mAttrSprites[count].setSize(attSize, attSize);
						mAttrSprites[count].setPosition(attX, attY);

						attX += mAttrSprites[count].getWidth() + mMargin;

						if (countPerLine == ++actLineCount) {
							//next line
							attY -= (attSize + mMargin);
							actLineCount = 0;
							attX = mMargin;
						}

						count++;
					} while (attrs.hasNext());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private StringBuilder createText() {
		String br = String.format("%n");
		StringBuilder text = new StringBuilder();
		if (ifModeFlag(SHOW_NAME)) {
			text.append(mCache.getName());
			text.append(br);
		}
		if (mCache instanceof Cache && (ifModeFlag(SHOW_OWNER) || ifModeFlag(SHOW_HIDDEN_DATE))) {
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
			if(mCache.Pos==null){
				text.append("????");
				text.append(br);
			}else{
				if (ifModeFlag(SHOW_CORRDS_WITH_LINEBRAKE)) {
					text.append(mCache.Pos.formatCoordinateLineBreak());
					text.append(br);
				} else {
					text.append(mCache.Pos.FormatCoordinate());
					text.append(br);
				}
			}

		}

		if (ifModeFlag(SHOW_GC)) {
			text.append(mCache.getGcCode());
			text.append(br);
		}
		if (ifModeFlag(SHOW_LAST_FOUND)) {
			String LastFound = getLastFoundLogDate(mCache);
			if (!LastFound.equals("")) {
				text.append("last found: " + LastFound);
			}
		}
		return text;
	}

	public float getAttributeHeight() {
		float attSize = mIconSize;//2
		int attCount = 0;
		if (mCache != null && mCache.getAttributes() != null)
			attCount = mCache.getAttributes().size();
		final int countPerLine = (int) (this.getWidth() / (attSize + mMargin));
		int lineCount = getLineCount(attCount, countPerLine);
		return lineCount * (attSize + mMargin) + mMargin;
	}

	private int getLineCount(int attCount, final int countPerLine) {
		double d = attCount / countPerLine;
		int lineCount = (int) Math.rint(d);
		if (attCount % countPerLine > 0)
			lineCount++;
		return lineCount;
	}

	private static String getLastFoundLogDate(Cache mCache) {
		String FoundDate = "";
		CB_List<LogEntry> logs = new CB_List<LogEntry>();
		logs = Database.Logs(mCache);

		for (int i = 0, n = logs.size(); i < n; i++) {
			LogEntry l = logs.get(i);
			if (l.Type == LogTypes.found) {
				SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
				FoundDate = postFormater.format(l.Timestamp);
				break;
			}
		}
		return FoundDate;
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
		if (mCache != null && cache != null && mCache.Id == cache.Id)
			return;
		mCache = cache;
		if (cacheIsInitial)
			requestLayout();
	}

	@Override
	protected void Initial() {
		cacheIsInitial = true;
		requestLayout();
	}

	public void setSmallFont(BitmapFont font) {
		mBitmapFontSmall = font;
		requestLayout();
	}

	@Override
	protected void SkinIsChanged() {
		mBitmapFont = Fonts.getNormal();
		mBitmapFontSmall = Fonts.getSmall();
		requestLayout();
	}

	GlyphLayout layout;

	public float getTextHeight() {
		if (layout == null)
			layout = new GlyphLayout();
		layout.setText(mBitmapFont, createText().toString());
		return layout.height;
	}

}

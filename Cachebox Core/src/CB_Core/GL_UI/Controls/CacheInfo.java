package CB_Core.GL_UI.Controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class CacheInfo extends CB_View_Base
{
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

	/**
	 * SHOW_GC, SHOW_NAME, SHOW_COMPASS
	 */
	public static final int VIEW_MODE_CACHE_LIST = SHOW_GC + SHOW_NAME + SHOW_COMPASS + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 19;

	/**
	 * SHOW_COMPASS, SHOW_OWNER, SHOW_CORRDS, SHOW_GC
	 */
	public static final int VIEW_MODE_DESCRIPTION = SHOW_GC + SHOW_COORDS + SHOW_OWNER + SHOW_COMPASS + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 29;

	/**
	 * SHOW_OWNER, SHOW_CORRDS, SHOW_GC, SHOW_LAST_FOUND
	 */
	public static final int VIEW_MODE_COMPAS = 60;

	/**
	 * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS, SHOW_GC, SHOW_LAST_FOUND, SHOW_ATTRIBUTES
	 */
	public static final int VIEW_MODE_SLIDER = SHOW_ATTRIBUTES + SHOW_LAST_FOUND + SHOW_GC + SHOW_COORDS + SHOW_OWNER + SHOW_NAME
			+ SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 126

	/**
	 * SHOW_COORDS, SHOW_COMPASS, SHOW_NAME
	 */
	public static final int VIEW_MODE_WAYPOINTS = SHOW_COORDS + SHOW_NAME + SHOW_COMPASS + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 11

	/**
	 * SHOW_COORDS, SHOW_COMPASS, SHOW_NAME
	 */
	public static final int VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK = SHOW_COORDS + SHOW_NAME + SHOW_CORRDS_WITH_LINEBRAKE + SHOW_VOTE
			+ SHOW_ICON + SHOW_S_D_T; // 138

	/**
	 * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS
	 */
	public static final int VIEW_MODE_BUBBLE = SHOW_COORDS + SHOW_OWNER + SHOW_NAME + SHOW_VOTE + SHOW_ICON + SHOW_S_D_T; // 30

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

	private static final SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");

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

	private BitmapFont mBitmapFont = Fonts.getNormal();
	private BitmapFont mBitmapFontSmall = Fonts.getSmall();

	private BitmapFontCache mS_FontCache;
	private BitmapFontCache mD_FontCache;
	private BitmapFontCache mT_FontCache;
	private BitmapFontCache mTB_FontCache;

	private BitmapFontCache mInfo_FontCache;

	private boolean cacheIsInitial = false;

	public CacheInfo(SizeF size, String Name, Cache value)
	{
		super(size, Name);
		mCache = value;
		cacheIsInitial = false;
	}

	public CacheInfo(CB_RectF rec, String Name, Cache value)
	{
		super(rec, Name);
		mCache = value;
		cacheIsInitial = false;
	}

	public void setFont(BitmapFont font)
	{
		mBitmapFont = font;
		requestLayout();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);

		try
		{
			if (mIconSprite != null) mIconSprite.draw(batch);
			if (mFoundOwnerSprite != null) mFoundOwnerSprite.draw(batch);
			if (mRatingSprite != null) mRatingSprite.draw(batch);
			if (mS_FontCache != null) mS_FontCache.draw(batch);
			if (mD_FontCache != null) mD_FontCache.draw(batch);
			if (mT_FontCache != null) mT_FontCache.draw(batch);
			if (mTB_FontCache != null) mTB_FontCache.draw(batch);
			if (mSSprite != null) mSSprite.draw(batch);
			if (mDSprite != null) mDSprite.draw(batch);
			if (mTSprite != null) mTSprite.draw(batch);
			if (mTBSprite != null) mTBSprite.draw(batch);
			if (mInfo_FontCache != null) mInfo_FontCache.draw(batch);
			if (mFavoriteSprite != null) mFavoriteSprite.draw(batch);
			if (mAvailableSprite != null) mAvailableSprite.draw(batch);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void dispose()
	{
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
	}

	private void requestLayout()
	{
		if (mCache == null) return;

		this.removeChilds();

		float scaleFactor = width / UiSizes.that.getCacheListItemRec().getWidth();
		mMargin = 3 * scaleFactor;

		float mLeft = mMargin;
		float mTop = mMargin;
		float mBottom = mMargin;

		// Size
		mS_FontCache = new BitmapFontCache(mBitmapFontSmall);
		mS_FontCache.setText("MSRLO", 0, 0);
		mS_FontCache.setColor(Fonts.getFontColor());
		float starHeight = mS_FontCache.getBounds().height * 1.1f;
		mStarSize = new SizeF(starHeight * 5, starHeight);

		if (ifModeFlag(SHOW_S_D_T))
		{
			String CacheSize = "";
			switch ((int) (mCache.Size.ordinal()))
			{
			case 1:
				CacheSize = "M"; // micro;
				break;
			case 2:
				CacheSize = "S"; // small;
				break;
			case 3:
				CacheSize = "R"; // regular;
				break;
			case 4:
				CacheSize = "L"; // large;
				break;
			default:
				CacheSize = "O"; // other;
				break;
			}
			mS_FontCache.setText(CacheSize, 0, 0);
			mBottom += mS_FontCache.getBounds().height;
			float mSpriteBottom = mMargin;
			mS_FontCache.setPosition(mLeft, mBottom);
			mLeft += mS_FontCache.getBounds().width + mMargin;

			mStarSize.scale(scaleFactor);
			mSSprite = new Sprite(SpriteCache.SizesIcons.get((int) (mCache.Size.ordinal())));
			mSSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
			// Difficulty
			mLeft += mSSprite.getWidth() + mMargin + mMargin;
			mD_FontCache = new BitmapFontCache(mBitmapFontSmall);
			mD_FontCache.setColor(Fonts.getFontColor());
			mD_FontCache.setText("D", mLeft, mBottom);
			mLeft += mD_FontCache.getBounds().width + mMargin;
			mDSprite = new Sprite(SpriteCache.Stars.get((int) (mCache.Difficulty * 2)));
			mDSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
			mDSprite.setRotation(0);
			// Terrain
			mLeft += mDSprite.getWidth() + mMargin + mMargin;
			mT_FontCache = new BitmapFontCache(mBitmapFontSmall);
			mT_FontCache.setColor(Fonts.getFontColor());
			mT_FontCache.setText("T", mLeft, mBottom);
			mLeft += mT_FontCache.getBounds().width + mMargin;
			mTSprite = new Sprite(SpriteCache.Stars.get((int) (mCache.Terrain * 2)));
			mTSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
			mTSprite.setRotation(0);
			// Draw TB
			mLeft += mTSprite.getWidth() + mMargin + mMargin + mMargin + mMargin;
			int numTb = mCache.NumTravelbugs;
			if (numTb > 0)
			{
				float sizes = mStarSize.width / 2.1f;

				mTBSprite = new Sprite(SpriteCache.Icons.get(36));
				mTBSprite.setBounds(mLeft, mBottom - (sizes / 1.8f) - mMargin, sizes, sizes);
				mTBSprite.setOrigin(sizes / 2, sizes / 2);
				mTBSprite.setRotation(90);

				if (numTb > 1)
				{
					mLeft += mTBSprite.getWidth() + mMargin;
					mTB_FontCache = new BitmapFontCache(mBitmapFontSmall);
					mTB_FontCache.setColor(Fonts.getFontColor());
					mTB_FontCache.setText("x" + String.valueOf(numTb), mLeft, mBottom);
				}
			}
			else
			{
				mTBSprite = null;
				mTB_FontCache = null;
			}
		}

		Vector2 mSpriteCachePos = new Vector2(0, height - mTop - mIconSize);

		// Rating stars
		if (ifModeFlag(SHOW_VOTE))
		{
			mLeft = -4 * scaleFactor;

			mIconSize = Fonts.MeasureSmall("T").height * 3.5f * scaleFactor;

			mStarSize.scale(0.7f);
			mRatingSprite = new Sprite(SpriteCache.Stars.get((int) Math.min(mCache.Rating * 2, 5 * 2)));
			mRatingSprite.setBounds(mLeft + mStarSize.height, height - mTop - mStarSize.width - mMargin - mMargin - mMargin,
					mStarSize.width, mStarSize.height);
			mRatingSprite.setOrigin(0, mStarSize.halfHeight);
			mRatingSprite.setRotation(90);
			mRatingSprite.setColor(gcVoteColor);
			//
			mLeft += starHeight;
			mSpriteCachePos = new Vector2(mLeft + mMargin, height - mTop - mIconSize);
		}

		if (ifModeFlag(SHOW_NAME) || ifModeFlag(SHOW_OWNER) || ifModeFlag(SHOW_COORDS) || ifModeFlag(SHOW_CORRDS_WITH_LINEBRAKE)
				|| ifModeFlag(SHOW_GC) || ifModeFlag(SHOW_LAST_FOUND))
		{// Text zusammensetzen

			String br = String.format("%n");
			StringBuilder text = new StringBuilder();
			if (ifModeFlag(SHOW_NAME)) text.append(mCache.Name + br);
			if (ifModeFlag(SHOW_OWNER)) text.append("by " + mCache.Owner + ", " + postFormater.format(mCache.DateHidden) + br);
			if (ifModeFlag(SHOW_COORDS))
			{
				if (ifModeFlag(SHOW_CORRDS_WITH_LINEBRAKE))
				{
					text.append(mCache.Pos.FormatCoordinateLineBreake() + br);
				}
				else
				{
					text.append(mCache.Pos.FormatCoordinate() + br);
				}
			}

			if (ifModeFlag(SHOW_GC)) text.append(mCache.GcCode + br);
			if (ifModeFlag(SHOW_LAST_FOUND))
			{
				String LastFound = getLastFoundLogDate(mCache);
				if (!LastFound.equals(""))
				{
					text.append("last found: " + LastFound);
				}
			}

			mInfo_FontCache = new BitmapFontCache(mBitmapFont);
			mInfo_FontCache.setMultiLineText(text.toString(), mSpriteCachePos.x + mIconSize + mMargin, this.height - mMargin);
			if (mCache.Archived || !mCache.Available)
			{
				mInfo_FontCache.setColor(Color.RED);
			}
			else
			{
				mInfo_FontCache.setColor(Fonts.getFontColor());
			}

		}

		if (ifModeFlag(SHOW_ICON))
		{ // Icon Sprite erstellen

			if (mCache.CorrectedCoordiantesOrMysterySolved())
			{
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(21));
			}
			else if ((mCache.Type == CacheTypes.Multi) && mCache.HasStartWaypoint())
			{
				// Multi anders darstellen wenn dieser einen definierten Startpunkt hat
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(22));
			}
			else if ((mCache.Type == CacheTypes.Mystery) && mCache.HasStartWaypoint())
			{
				// Mystery anders darstellen wenn dieser keinen Final aber einen definierten Startpunkt hat
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(24));
			}
			else if (mCache.Type == CacheTypes.Munzee)
			{
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(25));
			}
			else
			{
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(mCache.Type.ordinal()));
			}
			mIconSprite.setSize(mIconSize, mIconSize);
			mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);

			// infoIcons erstellen

			float infoSize = mIconSize / 2;

			if (mCache.Found)
			{
				mFoundOwnerSprite = new Sprite(SpriteCache.BigIcons.get(19));
			}
			else if (mCache.ImTheOwner())
			{
				mFoundOwnerSprite = new Sprite(SpriteCache.Icons.get(43));
			}
			if (mFoundOwnerSprite != null)
			{
				mFoundOwnerSprite.setSize(infoSize, infoSize);
				mFoundOwnerSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);
			}

			if (mCache.Favorit())
			{
				mFavoriteSprite = new Sprite(SpriteCache.Icons.get(42));
				mFavoriteSprite.setSize(infoSize, infoSize);
				mFavoriteSprite.setPosition(mSpriteCachePos.x + infoSize, mSpriteCachePos.y + infoSize);
			}

			if (mCache.Archived)
			{
				mAvailableSprite = new Sprite(SpriteCache.Icons.get(45));
			}
			else if (!mCache.Available)
			{
				mAvailableSprite = new Sprite(SpriteCache.Icons.get(44));
			}
			if (mAvailableSprite != null)
			{
				mAvailableSprite.setSize(infoSize, infoSize);
				mAvailableSprite.setPosition(mSpriteCachePos.x + infoSize, mSpriteCachePos.y);
			}
		}

	}

	private static String getLastFoundLogDate(Cache cache)
	{
		String FoundDate = "";
		ArrayList<LogEntry> logs = new ArrayList<LogEntry>();
		logs = Database.Logs(cache);// cache.Logs();
		for (LogEntry l : logs)
		{
			if (l.Type == LogTypes.found)
			{
				SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
				FoundDate = postFormater.format(l.Timestamp);
				break;
			}
		}
		return FoundDate;
	}

	private boolean ifModeFlag(int flag)
	{
		return (mViewMode & flag) == flag;
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		if (cacheIsInitial) requestLayout();
	}

	public void setViewMode(int viewMode)
	{
		mViewMode = viewMode;
		if (cacheIsInitial) requestLayout();
	}

	public void setCache(Cache cache)
	{
		if (mCache != null && cache != null && mCache.Id == cache.Id) return;
		mCache = cache;
		if (cacheIsInitial) requestLayout();
	}

	@Override
	protected void Initial()
	{
		cacheIsInitial = true;
		requestLayout();
	}

	public void setSmallFont(BitmapFont font)
	{
		mBitmapFontSmall = font;
		requestLayout();
	}

	@Override
	protected void SkinIsChanged()
	{
		mBitmapFont = Fonts.getNormal();
		mBitmapFontSmall = Fonts.getSmall();
		requestLayout();
	}

}

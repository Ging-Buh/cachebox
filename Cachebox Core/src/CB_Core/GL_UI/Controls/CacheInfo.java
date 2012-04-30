package CB_Core.GL_UI.Controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import CB_Core.DB.Database;
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
	public static final int SHOW_CORRDS = 8;
	public static final int SHOW_GC = 16;
	public static final int SHOW_LAST_FOUND = 32;
	public static final int SHOW_ATTRIBUTES = 64;

	/**
	 * SHOW_COMPASS, SHOW_NAME, SHOW_GC
	 */
	public static final int VIEW_MODE_CACHE_LIST = 19;

	/**
	 * SHOW_COMPASS, SHOW_OWNER, SHOW_CORRDS, SHOW_GC
	 */
	public static final int VIEW_MODE_DESCRIPTION = 29;

	/**
	 * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS, SHOW_GC, SHOW_LAST_FOUND, SHOW_ATTRIBUTES
	 */
	public static final int VIEW_MODE_SLIDER = 126;

	/**
	 * SHOW_COMPASS, SHOW_NAME
	 */
	public static final int VIEW_MODE_WAYPOINTS = 3;

	/**
	 * SHOW_NAME, SHOW_OWNER, SHOW_CORRDS, SHOW_GC
	 */
	public static final int VIEW_MODE_BUBBLE = 30;

	private static final SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");

	private int mViewMode = VIEW_MODE_CACHE_LIST;

	private Cache mCache;
	private float mIconSize = 0;
	private float mCompasswidth = 0;
	private SizeF mStarSize = new SizeF();
	private float mMargin = 0;
	private Sprite mRatingSprite;
	private Sprite mIconSprite;
	private Sprite mFoundOwnerSprite;
	private Sprite mFavoriteSprite;
	private Sprite mAvibleSprite;
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

	public CacheInfo(SizeF size, CharSequence Name, Cache value)
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
		if (mAvibleSprite != null) mAvibleSprite.draw(batch);
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
		mAvibleSprite = null;
	}

	private void requestLayout()
	{
		this.removeChilds();
		float scaleFactor = width / UiSizes.getCacheListItemRec().getWidth();
		float mLeft = 4 * scaleFactor;
		float mTop = 4 * scaleFactor;
		mMargin = mLeft;

		float mBottom = mMargin;

		mCompasswidth = ifModeFlag(SHOW_COMPASS) ? width / 6 : 0;

		mS_FontCache = new BitmapFontCache(mBitmapFontSmall);
		mS_FontCache.setText("S", 0, 0);

		mBottom += mS_FontCache.getBounds().height;
		float mSpriteBottom = mMargin;

		mS_FontCache.setPosition(mLeft, mBottom);

		mLeft += mS_FontCache.getBounds().width + mMargin;
		float starHeight = mS_FontCache.getBounds().height * 1.1f;
		mStarSize = new SizeF(starHeight * 5, starHeight);
		mStarSize.scale(scaleFactor);

		mSSprite = new Sprite(SpriteCache.SizesIcons.get((int) (mCache.Size.ordinal())));
		mSSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);

		mLeft += mSSprite.getWidth() + mMargin + mMargin;

		mD_FontCache = new BitmapFontCache(mBitmapFontSmall);
		mD_FontCache.setText("D", mLeft, mBottom);

		mLeft += mD_FontCache.getBounds().width + mMargin;

		mDSprite = new Sprite(SpriteCache.Stars.get((int) (mCache.Difficulty * 2)));
		mDSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
		mDSprite.setRotation(0);

		mLeft += mDSprite.getWidth() + mMargin + mMargin;

		mT_FontCache = new BitmapFontCache(mBitmapFontSmall);
		mT_FontCache.setText("T", mLeft, mBottom);

		mLeft += mT_FontCache.getBounds().width + mMargin;

		mTSprite = new Sprite(SpriteCache.Stars.get((int) (mCache.Terrain * 2)));
		mTSprite.setBounds(mLeft, mSpriteBottom, mStarSize.width, mStarSize.height);
		mTSprite.setRotation(0);

		mLeft += mTSprite.getWidth() + mMargin + mMargin + mMargin + mMargin;

		// Draw TB
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
				mTB_FontCache.setText("x" + String.valueOf(numTb), mLeft, mBottom);
			}
		}

		mLeft = 4 * scaleFactor;
		mLeft *= -1;
		mIconSize = mT_FontCache.getBounds().height * 3.5f * scaleFactor;
		mStarSize.scale(0.7f);
		mRatingSprite = new Sprite(SpriteCache.Stars.get((int) Math.min(mCache.Rating * 2, 5 * 2)));
		mRatingSprite.setBounds(mLeft + mStarSize.height, height - mTop - mStarSize.width - mMargin - mMargin - mMargin, mStarSize.width,
				mStarSize.height);
		mRatingSprite.setOrigin(0, mStarSize.halfHeight);
		mRatingSprite.setRotation(90);
		mRatingSprite.setColor(gcVoteColor);

		mLeft += starHeight;

		Vector2 mSpriteCachePos = new Vector2(mLeft + mMargin, height - mTop - mIconSize);

		{// Text zusammensetzen

			CharSequence br = String.format("%n");
			StringBuilder text = new StringBuilder();
			if (ifModeFlag(SHOW_NAME)) text.append(mCache.Name + br);
			if (ifModeFlag(SHOW_OWNER)) text.append("by " + mCache.Owner + ", " + postFormater.format(mCache.DateHidden) + br);
			if (ifModeFlag(SHOW_CORRDS)) text.append(mCache.Pos.FormatCoordinate() + br);
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

		}

		{ // Icon Sprite erstellen

			if (mCache.MysterySolved())
			{
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(21));
			}
			else
			{
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(mCache.Type.ordinal()));
			}
			mIconSprite.setSize(mIconSize, mIconSize);
			mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);
		}

		{// infoIcons erstellen

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
				mAvibleSprite = new Sprite(SpriteCache.Icons.get(45));
			}
			else if (!mCache.Available)
			{
				mAvibleSprite = new Sprite(SpriteCache.Icons.get(44));
			}
			if (mAvibleSprite != null)
			{
				mAvibleSprite.setSize(infoSize, infoSize);
				mAvibleSprite.setPosition(mSpriteCachePos.x + infoSize, mSpriteCachePos.y);
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
		requestLayout();
	}

	public void setSmallFont(BitmapFont font)
	{
		mBitmapFontSmall = font;
		requestLayout();
	}

}

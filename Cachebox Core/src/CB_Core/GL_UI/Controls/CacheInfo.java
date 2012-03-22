package CB_Core.GL_UI.Controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import CB_Core.DB.Database;
import CB_Core.Enums.LogTypes;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class CacheInfo extends CB_View_Base
{

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
	private BitmapFont mBitmapFont = Fonts.get16();
	private BitmapFontCache mS_FontCache;
	private BitmapFontCache mD_FontCache;
	private BitmapFontCache mT_FontCache;

	private Label lblTextInfo;

	public CacheInfo(SizeF size, CharSequence Name, Cache value)
	{
		super(size, Name);
		mCache = value;
		isInitial = false;
	}

	public CacheInfo(CB_RectF rec, String Name, Cache value)
	{
		super(rec, Name);
		mCache = value;
		isInitial = false;
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
		if (mRatingSprite != null) mRatingSprite.draw(batch);
		if (mS_FontCache != null) mS_FontCache.draw(batch);
		if (mD_FontCache != null) mD_FontCache.draw(batch);
		if (mT_FontCache != null) mT_FontCache.draw(batch);

	}

	private void requestLayout()
	{
		this.removeChilds();

		float scaleFactor = width / UiSizes.getCacheListItemRec().getWidth();

		float mLeft = 5 * scaleFactor;
		float mTop = 5 * scaleFactor;

		mStarSize = new SizeF(GL_UISizes.DT_Size);
		mStarSize.scale(scaleFactor);
		mMargin = mStarSize.height / 2;

		mRatingSprite = new Sprite(SpriteCache.MapStars.get((int) Math.min(mCache.Rating * 2, 5 * 2)));
		mRatingSprite.setBounds(mLeft + mMargin + mStarSize.height, height - mTop - mStarSize.width - mMargin, mStarSize.width,
				mStarSize.height);
		mRatingSprite.setOrigin(0, mStarSize.halfHeight);
		mRatingSprite.setRotation(90);

		mIconSize = (UiSizes.getScaledIconSize() / 1.5f) * scaleFactor;

		mCompasswidth = ifModeFlag(SHOW_COMPASS) ? width / 6 : 0;

		Vector2 mSpriteCachePos = new Vector2(mLeft + mMargin + mMargin + mStarSize.height, height - mTop - mIconSize);
		CB_RectF lblRec = new CB_RectF(mSpriteCachePos.x + mIconSize + mMargin, 0, this.width - mSpriteCachePos.x - mIconSize
				- mCompasswidth, height - mTop);
		lblTextInfo = new Label(lblRec, "CacheInfoText");
		lblTextInfo.setFont(mBitmapFont);
		lblTextInfo.setVAlignment(VAlignment.TOP);
		this.addChild(lblTextInfo);

		{// Text zusammensetzen

			CharSequence br = String.format("%n");
			StringBuilder text = new StringBuilder();
			if (ifModeFlag(SHOW_NAME)) text.append(mCache.Name + br);
			if (ifModeFlag(SHOW_OWNER)) text.append("by " + mCache.Owner + ", " + postFormater.format(mCache.DateHidden) + br);
			if (ifModeFlag(SHOW_CORRDS)) text.append(mCache.Pos.FormatCoordinate() + br);
			if (ifModeFlag(SHOW_GC)) text.append(mCache.GcCode + " " + scaleFactor + br);
			if (ifModeFlag(SHOW_LAST_FOUND))
			{
				String LastFound = getLastFoundLogDate(mCache);
				if (!LastFound.equals(""))
				{
					text.append("last found: " + LastFound);
				}
			}

			lblTextInfo.setMultiLineText(text.toString());
		}

		{ // Icon Sprite erstellen

			if (mIconSize <= 0) mIconSize = GL_UISizes.PosMarkerSize * 1.2f;
			if (mCache.MysterySolved())
			{
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(19));
			}
			else
			{
				mIconSprite = new Sprite(SpriteCache.BigIcons.get(mCache.Type.ordinal()));
			}
			mIconSprite.setSize(mIconSize, mIconSize);
			mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);
		}

		mS_FontCache = new BitmapFontCache(mBitmapFont);
		mS_FontCache.setText("S", 0, 0);
		mS_FontCache.setPosition(mLeft, mMargin + mS_FontCache.getBounds().height);

		mLeft += mS_FontCache.getBounds().width + mMargin;

		mD_FontCache = new BitmapFontCache(mBitmapFont);
		mD_FontCache.setText("D", 0, 0);
		mD_FontCache.setPosition(mLeft, mMargin + mD_FontCache.getBounds().height);

		mLeft += mD_FontCache.getBounds().width + mMargin;

		mT_FontCache = new BitmapFontCache(mBitmapFont);
		mT_FontCache.setText("T", 0, 0);
		mT_FontCache.setPosition(mLeft, mMargin + mT_FontCache.getBounds().height);

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
		if (isInitial) requestLayout();
	}

	public void setViewMode(int viewMode)
	{
		mViewMode = viewMode;
		if (isInitial) requestLayout();
	}

	public void setCache(Cache cache)
	{
		mCache = cache;
		if (isInitial) requestLayout();
	}

	@Override
	protected void Initial()
	{
		requestLayout();
	}

}

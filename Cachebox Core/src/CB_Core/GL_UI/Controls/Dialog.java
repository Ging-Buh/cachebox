package CB_Core.GL_UI.Controls;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class Dialog extends CB_View_Base
{

	private String mTitle;
	public Label titleLabel;
	private Box mContent;
	private ArrayList<GL_View_Base> contentChilds = new ArrayList<GL_View_Base>();

	/**
	 * enthällt die Controls, welche über allen anderen gezeichnet werden zB. Selection Marker des TextFields
	 */
	private ArrayList<GL_View_Base> overlayForTextMarker = new ArrayList<GL_View_Base>(); // TODO das Handling der Marker in den Dialogen
																							// überarbeiten!

	/**
	 * Overlay über alles wird als letztes Gerändert
	 */
	private ArrayList<GL_View_Base> overlay = new ArrayList<GL_View_Base>();

	protected boolean dontRenderDialogBackground = false;
	protected Object data;
	static protected NinePatch mTitle9patch;
	static protected NinePatch mHeader9patch;
	static protected NinePatch mCenter9patch;
	static protected NinePatch mFooter9patch;
	static protected float mTitleVersatz = 6;
	static private int pW = 0;

	protected float mTitleHeight = 0;
	protected float mTitleWidth = 100;

	protected boolean mHasTitle = false;

	protected float mHeaderHight = 10f;
	protected float mFooterHeight = 10f;

	public static float margin = 5f;

	public static boolean lastNightMode = false;

	public Dialog(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mHeaderHight = margin = calcHeaderHeight();
		setFooterHeight(calcFooterHeight(false));

		try
		{
			if (SpriteCache.Dialog.get(2) == null) return;// noch nicht initialisiert!
		}
		catch (Exception e)
		{
			return;
		} // noch nicht initialisiert!

		if (mTitle9patch == null || mHeader9patch == null || mCenter9patch == null || mFooter9patch == null
				|| lastNightMode != Config.settings.nightMode.getValue())
		{
			// calcBase
			pW = (int) (SpriteCache.Dialog.get(2).getWidth() / 8);

			mTitle9patch = new NinePatch(SpriteCache.Dialog.get(3), pW, (pW * 12 / 8), pW, pW);
			mHeader9patch = new NinePatch(SpriteCache.Dialog.get(0), pW, pW, pW, 3);
			mCenter9patch = new NinePatch(SpriteCache.Dialog.get(1), pW, pW, 1, 1);
			mFooter9patch = new NinePatch(SpriteCache.Dialog.get(2), pW, pW, 3, pW);

			mTitleVersatz = (float) pW;

			lastNightMode = Config.settings.nightMode.getValue();
		}

	}

	@Override
	public GL_View_Base addChild(GL_View_Base view)
	{
		// die Childs in die Box umleiten ausser TextMarker

		if (view instanceof SelectionMarker)
		{
			overlayForTextMarker.add(view);
			mContent.addChildDirekt(view);

			if (mContent != null)
			{
				mContent.addChildDirekt(view);
			}
			else
			{
				childs.add(view);
			}

		}
		else
		{
			if (mContent != null)
			{
				mContent.addChildDirekt(view);
			}
			else
			{
				contentChilds.add(view);
			}
		}

		return view;
	}

	@Override
	public void removeChild(GL_View_Base view)
	{
		if (view instanceof SelectionMarker)
		{
			overlayForTextMarker.remove(view);
			if (mContent != null)
			{
				mContent.removeChildsDirekt(view);
			}
			else
			{
				childs.remove(view);
			}

		}
		else
		{
			if (mContent != null)
			{
				mContent.removeChildsDirekt(view);
			}
			else
			{
				contentChilds.remove(view);
			}
		}

	}

	@Override
	public void removeChilds()
	{
		contentChilds.clear();
		if (mContent != null) mContent.removeChilds();
	}

	@Override
	protected void Initial()
	{
		initialDialog();

		super.isInitial = true;

	}

	protected void initialDialog()
	{
		if (mContent != null)
		{
			// InitialDialog wurde schon aufgerufen!!!
			return;
		}
		super.removeChildsDirekt();

		mContent = new Box(this.ScaleCenter(0.95f), "Dialog Content Box");

		// debug mContent.setBackground(new ColorDrawable(Color.RED));

		reziseContentBox();

		for (Iterator<GL_View_Base> iterator = contentChilds.iterator(); iterator.hasNext();)
		{
			mContent.addChildDirekt(iterator.next());
		}

		super.addChild(mContent);

		if (overlayForTextMarker.size() > 0)
		{
			for (GL_View_Base view : overlayForTextMarker)
			{
				mContent.addChildDirekt(view);
			}
		}
	}

	protected void reziseContentBox()
	{

		if (mContent == null)
		{
			this.initialDialog();
			return;
		}

		mTitleHeight = 0;

		if (mTitle != null && !mTitle.equals(""))
		{
			mHasTitle = true;

			TextBounds bounds = Fonts.Measure(mTitle);
			mTitleWidth = bounds.width + (6.666f * pW);
			if (mTitleWidth > this.width) mTitleWidth = this.width;// - (1.666f * pW);

			mTitleHeight = bounds.height * 3f;

			if (titleLabel != null && childs.contains(titleLabel)) childs.remove(titleLabel);

			titleLabel = new Label(new CB_RectF((1.666f * pW), this.height - bounds.height - (margin * 3), mTitleWidth - (4.1666f * pW),
					(5.16666f * pW)), "DialogTitleLabel");
			titleLabel.setFont(Fonts.getNormal());
			titleLabel.setText(mTitle);

			bounds = null;
			super.addChildDirekt(titleLabel);

		}

		mContent.setWidth(this.width * 0.95f);
		mContent.setHeight(this.height - mHeaderHight - getFooterHeight() - mTitleHeight - margin);
		float centerversatzX = this.halfWidth - mContent.getHalfWidth();
		float centerversatzY = getFooterHeight();// this.halfHeight - mContent.getHalfHeight();
		mContent.setPos(new Vector2(centerversatzX, centerversatzY));
	}

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{

		batch.flush();

		if (mHeader9patch != null && !dontRenderDialogBackground) mHeader9patch.draw(batch, 0, this.height - mTitleHeight - mHeaderHight,
				this.width, mHeaderHight);
		if (mFooter9patch != null && !dontRenderDialogBackground) mFooter9patch.draw(batch, 0, 0, this.width, getFooterHeight() + 2);

		if (mCenter9patch != null && !dontRenderDialogBackground) mCenter9patch.draw(batch, 0, getFooterHeight(), this.width, (this.height
				- getFooterHeight() - mHeaderHight - mTitleHeight) + 3.5f);

		if (mHasTitle)
		{
			if (mTitleWidth < this.width)
			{
				if (mTitle9patch != null && !dontRenderDialogBackground) mTitle9patch.draw(batch, 0, this.height - mTitleHeight
						- mTitleVersatz, mTitleWidth, mTitleHeight);
			}
			else
			{
				if (mHeader9patch != null && !dontRenderDialogBackground) mHeader9patch.draw(batch, 0, this.height - mTitleHeight
						- mTitleVersatz, mTitleWidth, mTitleHeight);
			}
		}

		batch.flush();

		super.renderChilds(batch, parentInfo);

		for (Iterator<GL_View_Base> iterator = overlay.iterator(); iterator.hasNext();)
		{
			// alle renderChilds() der in dieser GL_View_Base
			// enthaltenen Childs auf rufen.

			GL_View_Base view;
			try
			{
				view = iterator.next();

				// hier nicht view.render(batch) aufrufen, da sonnst die in der
				// view enthaldenen Childs nicht aufgerufen werden.
				if (view != null && view.isVisible())
				{

					if (childsInvalidate) view.invalidate();

					ParentInfo myInfoForChild = myParentInfo.cpy();
					myInfoForChild.setWorldDrawRec(intersectRec);

					myInfoForChild.add(view.getX(), view.getY());

					batch.setProjectionMatrix(myInfoForChild.Matrix());
					nDepthCounter++;

					view.renderChilds(batch, myInfoForChild);
					nDepthCounter--;
					batch.setProjectionMatrix(myParentInfo.Matrix());
				}

			}
			catch (java.util.ConcurrentModificationException e)
			{
				// da die Liste nicht mehr gültig ist, brechen wir hier den Iterator ab
				break;
			}
		}

	}

	public SizeF getContentSize()
	{
		reziseContentBox();
		return mContent.getSize();
	}

	public void setTitle(String title)
	{
		mTitle = title;
		reziseContentBox();
	}

	public static float calcHeaderHeight()
	{
		return (Fonts.Measure("T").height) / 2;
	}

	public static float calcFooterHeight(boolean hasButtons)
	{
		return hasButtons ? UiSizes.getButtonHeight() + margin + margin : calcHeaderHeight();
	}

	public void setWidth(float Width)
	{
		super.setWidth(Width);
		reziseContentBox();
	}

	public void setHeight(float Height)
	{
		super.setHeight(Height);
		reziseContentBox();
	}

	public boolean setSize(SizeF Size)
	{
		return setSize(Size.width, Size.height);
	}

	/**
	 * Setzt die Werte für Height und Width. Wenn sich einer der Werte geändert hat, wird ein True zurück gegeben, ansonsten False.
	 * 
	 * @param Width
	 * @param Height
	 * @return
	 */
	public boolean setSize(float Width, float Height)
	{
		boolean ret = super.setSize(Width, Height);
		reziseContentBox();
		return ret;
	}

	public boolean setSize(CB_RectF rec)
	{
		boolean ret = super.setSize(rec);
		reziseContentBox();
		return ret;
	}

	public void addChildToOverlay(GL_View_Base view)
	{
		overlay.add(view);
	}

	public void RemoveChildsFromOverlay()
	{
		overlay.clear();
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		super.onRezised(rec);
		reziseContentBox();
	}

	public float getFooterHeight()
	{
		return mFooterHeight;
	}

	public void setFooterHeight(float FooterHeight)
	{
		this.mFooterHeight = FooterHeight;
		reziseContentBox();
	}

}

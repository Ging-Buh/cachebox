package CB_UI_Base.GL_UI.Controls;

import java.util.ArrayList;
import java.util.Iterator;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.DialogElement;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.settings.CB_UI_Base_Settings;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class Dialog extends CB_View_Base
{
	private String mTitle;
	private Label titleLabel;
	private Box mContent;
	private final ArrayList<GL_View_Base> contentChilds = new ArrayList<GL_View_Base>();

	/**
	 * enth�llt die Controls, welche �ber allen anderen gezeichnet werden zB. Selection Marker des TextFields
	 */
	private final ArrayList<GL_View_Base> overlayForTextMarker = new ArrayList<GL_View_Base>();
	// TODO das Handling der Marker in den Dialogen �berarbeiten!

	/**
	 * Overlay �ber alles wird als letztes Gerendert
	 */
	private final ArrayList<GL_View_Base> overlay = new ArrayList<GL_View_Base>();

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

	protected float mHeaderHeight = 10f;
	protected float mFooterHeight = 10f;

	protected static float margin = -1;

	public static boolean lastNightMode = false;

	public Dialog(CB_RectF rec, String Name)
	{
		super(rec, Name);
		// ctor without title and footer
		mHeaderHeight = calcHeaderHeight();
		mFooterHeight = mHeaderHeight;

		if (margin <= 0) margin = UI_Size_Base.that.getMargin();

		try
		{
			if (SpriteCacheBase.Dialog.get(DialogElement.footer.ordinal()) == null) return;// noch nicht initialisiert!
		}
		catch (Exception e)
		{
			return;
		} // noch nicht initialisiert!

		if (mTitle9patch == null || mHeader9patch == null || mCenter9patch == null || mFooter9patch == null
				|| lastNightMode != CB_UI_Base_Settings.nightMode.getValue())
		{
			// calcBase
			pW = (int) (SpriteCacheBase.Dialog.get(DialogElement.footer.ordinal()).getWidth() / 8);
			mTitle9patch = new NinePatch(SpriteCacheBase.Dialog.get(DialogElement.title.ordinal()), pW, (pW * 12 / 8), pW, pW);
			mHeader9patch = new NinePatch(SpriteCacheBase.Dialog.get(DialogElement.header.ordinal()), pW, pW, pW, 3);
			mCenter9patch = new NinePatch(SpriteCacheBase.Dialog.get(DialogElement.center.ordinal()), pW, pW, 1, 1);
			mFooter9patch = new NinePatch(SpriteCacheBase.Dialog.get(DialogElement.footer.ordinal()), pW, pW, 3, pW);
			mTitleVersatz = pW;
			lastNightMode = CB_UI_Base_Settings.nightMode.getValue();
		}

		leftBorder = mCenter9patch.getLeftWidth();
		rightBorder = mCenter9patch.getRightWidth();
		topBorder = mHeader9patch.getTopHeight();
		bottomBorder = mFooter9patch.getBottomHeight();
		innerWidth = width - leftBorder - rightBorder;
		innerHeight = height - topBorder - bottomBorder;

		reziseContentBox();
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

	private void reziseContentBox()
	{

		if (margin <= 0) margin = UI_Size_Base.that.getMargin();

		if (mContent == null)
		{
			this.initialDialog();
			return;
		}

		mTitleHeight = 0;
		if (mTitle != null && !mTitle.equals(""))
		{
			mHasTitle = true;

			if (titleLabel == null)
			{
				titleLabel = new Label(mTitle);
			}
			else
			{
				if (!titleLabel.getText().equals(mTitle))
				{
					titleLabel.setText(mTitle);
				}
			}
			titleLabel.setWidth(titleLabel.getTextWidth() + leftBorder + rightBorder);
			this.initRow();
			this.addLast(titleLabel, FIXED);

			mTitleHeight = titleLabel.getHeight();
			mTitleWidth = titleLabel.getWidth();
			mTitleWidth += rightBorder; // sonst sieht es bl�d aus
		}

		mContent.setWidth(this.width * 0.95f);
		mContent.setHeight((this.height - mHeaderHeight - mFooterHeight - mTitleHeight - margin));
		float centerversatzX = this.halfWidth - mContent.getHalfWidth();
		float centerversatzY = mFooterHeight;// this.halfHeight - mContent.getHalfHeight();
		mContent.setPos(new Vector2(centerversatzX, centerversatzY));

	}

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{

		batch.flush();

		if (mHeader9patch != null && !dontRenderDialogBackground)
		{
			mHeader9patch.draw(batch, 0, this.height - mTitleHeight - mHeaderHeight, this.width, mHeaderHeight);
		}
		if (mFooter9patch != null && !dontRenderDialogBackground)
		{
			mFooter9patch.draw(batch, 0, 0, this.width, mFooterHeight + 2);
		}
		if (mCenter9patch != null && !dontRenderDialogBackground)
		{
			mCenter9patch.draw(batch, 0, mFooterHeight, this.width, (this.height - mFooterHeight - mHeaderHeight - mTitleHeight) + 3.5f);
		}

		if (mHasTitle)
		{
			if (mTitleWidth < this.width)
			{
				if (mTitle9patch != null && !dontRenderDialogBackground)
				{
					mTitle9patch.draw(batch, 0, this.height - mTitleHeight - mTitleVersatz, mTitleWidth, mTitleHeight);
				}
			}
			else
			{
				if (mHeader9patch != null && !dontRenderDialogBackground)
				{
					mHeader9patch.draw(batch, 0, this.height - mTitleHeight - mTitleVersatz, mTitleWidth, mTitleHeight);
				}
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
				// da die Liste nicht mehr g�ltig ist, brechen wir hier den Iterator ab
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
		if (margin <= 0) margin = UI_Size_Base.that.getMargin();

		return hasButtons ? UI_Size_Base.that.getButtonHeight() + margin : calcHeaderHeight();
	}

	public void addChildToOverlay(GL_View_Base view)
	{
		overlay.add(view);
	}

	public void RemoveChildsFromOverlay()
	{
		overlay.clear();
	}

	// always automaticly called on changing size
	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		reziseContentBox();
	}

	public void setFooterHeight(float FooterHeight)
	{
		this.mFooterHeight = FooterHeight;
		reziseContentBox();
	}

	public static Size calcMsgBoxSize(String Text, boolean hasTitle, boolean hasButtons, boolean hasIcon, boolean hasRemember)
	{
		if (margin <= 0) margin = UI_Size_Base.that.getMargin();

		float Width = (((UI_Size_Base.that.getButtonWidthWide() + margin) * 3) + margin);
		if (Width * 1.2 < UI_Size_Base.that.getWindowWidth()) Width *= 1.2f;

		float MsgWidth = (Width * 0.95f) - 5 - UI_Size_Base.that.getButtonHeight();

		float MeasuredTextHeight = Fonts.MeasureWrapped(Text, MsgWidth).height + (margin * 4);

		int Height = (int) (hasIcon ? Math.max(MeasuredTextHeight, UI_Size_Base.that.getButtonHeight() + (margin * 4))
				: (int) MeasuredTextHeight);

		if (hasTitle)
		{
			TextBounds titleBounds = Fonts.Measure("T");
			Height += (titleBounds.height * 3);
			Height += margin * 2;
		}
		Height += calcFooterHeight(hasButtons);
		if (hasRemember) Height += UI_Size_Base.that.getChkBoxSize().height;
		Height += calcHeaderHeight();

		// min Height festlegen
		Height = (int) Math.max(Height, UI_Size_Base.that.getButtonHeight() * 2.5f);

		// max Height festlegen
		Height = (int) Math.min(Height, UI_Size_Base.that.getWindowHeight() * 0.95f);

		Size ret = new Size((int) Width, Height);
		return ret;
	}

}
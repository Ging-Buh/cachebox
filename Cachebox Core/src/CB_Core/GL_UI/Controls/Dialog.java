package CB_Core.GL_UI.Controls;

import java.util.ArrayList;
import java.util.Iterator;

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
	private Box mContent;
	private ArrayList<GL_View_Base> contentChilds = new ArrayList<GL_View_Base>();

	protected NinePatch mTitle9patch;
	protected NinePatch mHeader9patch;
	protected NinePatch mCenter9patch;
	protected NinePatch mFooter9patch;

	protected float mTitleHeight = 0;
	protected float mTitleWidth = 100;
	protected float mTitleVersatz = 6;
	protected boolean mHasTitle = false;

	protected float mHeaderHight = 200;
	protected float mFooterHeight = 200;
	public float Left;
	public float Reight;
	public float Top;
	public float Bottom;

	public static float margin = 5f;

	int pW = 0;

	public Dialog(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mHeaderHight = margin = calcHeaderHeight();
		mFooterHeight = calcFooterHeight(false);
		// calcBase
		pW = (int) (SpriteCache.Dialog.get(2).getWidth() / 8);

		mTitle9patch = new NinePatch(SpriteCache.Dialog.get(3), pW, (pW * 12 / 8), pW, pW);
		mHeader9patch = new NinePatch(SpriteCache.Dialog.get(0), pW, pW, pW, 3);
		mCenter9patch = new NinePatch(SpriteCache.Dialog.get(1), pW, pW, 3, 3);
		mFooter9patch = new NinePatch(SpriteCache.Dialog.get(2), pW, pW, 3, pW);

		Left = mCenter9patch.getLeftWidth();
		Reight = mCenter9patch.getRightWidth();
		Top = mCenter9patch.getTopHeight();
		Bottom = mFooter9patch.getBottomHeight();
		mTitleVersatz = (float) pW;

	}

	@Override
	public GL_View_Base addChild(GL_View_Base view)
	{
		// die Childs in die Box umleiten
		contentChilds.add(view);

		return view;
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
		super.removeChildsDirekt();
		mTitleHeight = 0;

		if (mTitle != null && !mTitle.equals(""))
		{
			mHasTitle = true;

			TextBounds bounds = Fonts.Mesure(mTitle);
			mTitleWidth = bounds.width + (6.666f * pW);
			if (mTitleWidth > this.width) mTitleWidth = this.width - (1.666f * pW);

			mTitleHeight = bounds.height * 3f;

			Label titleLabel = new Label(new CB_RectF((1.666f * pW), this.height - bounds.height - (margin * 3), mTitleWidth
					- (4.1666f * pW), (5.16666f * pW)), "DialogTitleLabel");
			titleLabel.setFont(Fonts.getNormal());
			titleLabel.setText(mTitle);

			bounds = null;
			super.addChildDirekt(titleLabel);

		}

		mContent = new Box(this.ScaleCenter(0.95f), "Dialog Content Box");
		mContent.setHeight(this.height - mHeaderHight - mFooterHeight - mTitleHeight - margin);
		float centerversatzX = this.halfWidth - mContent.getHalfWidth();
		float centerversatzY = mFooterHeight;// this.halfHeight - mContent.getHalfHeight();
		mContent.setPos(new Vector2(centerversatzX, centerversatzY));

		for (Iterator<GL_View_Base> iterator = contentChilds.iterator(); iterator.hasNext();)
		{
			mContent.addChildDirekt(iterator.next());
		}

		super.addChild(mContent);

	}

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{

		batch.begin();

		if (mHeader9patch != null) mHeader9patch.draw(batch, 0, this.height - mTitleHeight - mHeaderHight, this.width, mHeaderHight);
		if (mFooter9patch != null) mFooter9patch.draw(batch, 0, 0, this.width, mFooterHeight + 2);

		if (mCenter9patch != null) mCenter9patch.draw(batch, 0, mFooterHeight, this.width,
				(this.height - mFooterHeight - mHeaderHight - mTitleHeight) + 3.5f);

		if (mHasTitle)
		{
			if (mTitle9patch != null) mTitle9patch.draw(batch, 0, this.height - mTitleHeight - mTitleVersatz, mTitleWidth, mTitleHeight);
		}

		batch.end();

		super.renderChilds(batch, parentInfo);
	}

	public SizeF getContentSize()
	{
		if (mContent == null) this.Initial();
		return mContent.getSize();
	}

	public void setTitle(String title)
	{
		mTitle = title;
	}

	public static float calcHeaderHeight()
	{
		return (Fonts.Mesure("T").height) / 2;
	}

	public static float calcFooterHeight(boolean hasButtons)
	{
		return hasButtons ? UiSizes.getButtonHeight() * 1.2f : calcHeaderHeight();
	}

}

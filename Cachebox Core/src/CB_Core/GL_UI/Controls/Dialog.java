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

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class Dialog extends CB_View_Base
{

	private String mTitle;
	private Box mContent;
	private ArrayList<GL_View_Base> contentChilds = new ArrayList<GL_View_Base>();

	private NinePatch mTitle9patch;
	private NinePatch mHeader9patch;
	private NinePatch mCenter9patch;
	private NinePatch mFooter9patch;

	protected float mTitleHeight = 0;
	protected float mTitleWidth = 100;
	protected float mTitleVersatz = 6;
	protected boolean mHasTitle = false;

	protected float mHeaderHight = 20;
	protected float mFooterHeight = 20;

	public Dialog(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);

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

		if (mTitle != null && !mTitle.equals(""))
		{
			mHasTitle = true;
			mTitleHeight = 40;

			BitmapFontCache mesure = new BitmapFontCache(Fonts.getNormal());
			TextBounds bounds = mesure.setText(mTitle, 0, 0);
			mTitleWidth = bounds.width + 40;
			if (mTitleWidth > this.width) mTitleWidth = this.width - 10;

			Label titleLabel = new Label(new CB_RectF(10, this.height - 30, mTitleWidth - 25, 20), "DialogTitleLabel");
			titleLabel.setFont(Fonts.getNormal());
			titleLabel.setText(mTitle);

			super.addChildDirekt(titleLabel);

		}

		mContent = new Box(this.ScaleCenter(0.95f), "Dialog Content Box");
		mContent.setHeight(this.height - mHeaderHight - mFooterHeight - mTitleHeight);
		float centerversatzX = this.halfWidth - mContent.getHalfWidth();
		float centerversatzY = this.halfHeight - mContent.getHalfHeight();
		mContent.setPos(new Vector2(centerversatzX, centerversatzY));

		for (Iterator<GL_View_Base> iterator = contentChilds.iterator(); iterator.hasNext();)
		{
			mContent.addChildDirekt(iterator.next());
		}

		super.addChild(mContent);

		mTitle9patch = new NinePatch(SpriteCache.Dialog.get(3), 6, 9, 6, 5);
		mHeader9patch = new NinePatch(SpriteCache.Dialog.get(0), 6, 6, 6, 1);
		mCenter9patch = new NinePatch(SpriteCache.Dialog.get(1), 6, 6, 1, 1);
		mFooter9patch = new NinePatch(SpriteCache.Dialog.get(2), 6, 6, 1, 6);

	}

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{

		batch.begin();

		if (mHeader9patch != null) mHeader9patch.draw(batch, 0, this.height - mTitleHeight - mHeaderHight, this.width, mHeaderHight);
		if (mCenter9patch != null) mCenter9patch.draw(batch, 0, mFooterHeight - 1, this.width, this.height - mFooterHeight - mHeaderHight
				- mTitleHeight + 2);
		if (mFooter9patch != null) mFooter9patch.draw(batch, 0, 0, this.width, mFooterHeight);

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

}

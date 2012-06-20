package CB_Core.GL_UI.Controls.List;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ListViewItemBase extends CB_View_Base
{

	/**
	 * Constructor
	 * 
	 * @param rec
	 * @param Index
	 *            Index in der List
	 * @param Name
	 */
	public ListViewItemBase(CB_RectF rec, int Index, String Name)
	{
		super(rec, Name);
		mIndex = Index;
	}

	private int mIndex;

	public int getIndex()
	{
		return mIndex;
	}

	public boolean isSelected = false;

	private static NinePatch backSelect;
	private static NinePatch back1;
	private static NinePatch back2;
	private static boolean mBackIsInitial = false;

	public static void ResetBackground()
	{
		mBackIsInitial = false;
	}

	@Override
	protected void Initial()
	{
		if (!mBackIsInitial)
		{
			backSelect = new NinePatch(SpriteCache.getThemedSprite("listrec_selected"), 8, 8, 8, 8);
			back1 = new NinePatch(SpriteCache.getThemedSprite("listrec_first"), 8, 8, 8, 8);
			back2 = new NinePatch(SpriteCache.getThemedSprite("listrec_secend"), 8, 8, 8, 8);
			mBackIsInitial = true;
		}
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);
		// Draw Background
		if (mBackIsInitial)
		{
			Boolean BackGroundChanger = ((this.getIndex() % 2) == 1);
			if (isSelected)
			{
				backSelect.draw(batch, 0, 0, this.width, this.height);
			}
			else if (BackGroundChanger)
			{
				back1.draw(batch, 0, 0, this.width, this.height);
			}
			else
			{
				back2.draw(batch, 0, 0, this.width, this.height);
			}
		}
	}

}

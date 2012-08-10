package CB_Core.GL_UI.Controls.List;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ListViewItemBackground extends ListViewItemBase
{

	/**
	 * Constructor
	 * 
	 * @param rec
	 * @param Index
	 *            Index in der List
	 * @param Name
	 */
	public ListViewItemBackground(CB_RectF rec, int Index, String Name)
	{
		super(rec, Index, Name);
	}

	private static NinePatch backSelect;
	private static NinePatch back1;
	private static NinePatch back2;
	protected static boolean mBackIsInitial = false;
	protected boolean isPressed = false;

	protected static float LeftWidth = 0;
	protected static float RightWidth = 0;
	protected static float TopHight = 0;
	protected static float BottomHeight = 0;

	public static void ResetBackground()
	{
		mBackIsInitial = false;
	}

	@Override
	protected void Initial()
	{
		if (!mBackIsInitial)
		{
			backSelect = new NinePatch(SpriteCache.getThemedSprite("listrec-selected"), 16, 16, 16, 16);
			back1 = new NinePatch(SpriteCache.getThemedSprite("listrec-first"), 16, 16, 16, 16);
			back2 = new NinePatch(SpriteCache.getThemedSprite("listrec-secend"), 16, 16, 16, 16);

			LeftWidth = back1.getLeftWidth();
			RightWidth = back1.getRightWidth();
			TopHight = back1.getTopHeight();
			BottomHeight = back1.getBottomHeight();

			mBackIsInitial = true;
		}
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (isPressed)
		{
			isPressed = GL_Listener.isTouchDown();
		}

		if (!this.isVisible()) return;
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
		else
		{
			Initial();
		}

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		isPressed = true;
		GL_Listener.glListener.renderOnce(this.getName() + " touchDown");

		return false;
	}

	public static float getLeftWidthStatic()
	{
		if (mBackIsInitial)
		{
			return backSelect.getLeftWidth();
		}
		return 0;
	}

	public static float getRightWidthStatic()
	{
		if (mBackIsInitial)
		{
			return backSelect.getRightWidth();
		}
		return 0;
	}
}

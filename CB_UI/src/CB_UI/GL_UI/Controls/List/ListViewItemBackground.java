package CB_UI.GL_UI.Controls.List;

import CB_UI.GL_UI.SpriteCache;
import CB_UI.GL_UI.GL_Listener.GL;
import CB_UI.Math.CB_RectF;

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

	public static void ResetBackground()
	{
		mBackIsInitial = false;
	}

	@Override
	protected void Initial()
	{
		if (!mBackIsInitial)
		{
			backSelect = new NinePatch(SpriteCache.getThemedSprite("listrec-selected"), 13, 13, 13, 13);
			back1 = new NinePatch(SpriteCache.getThemedSprite("listrec-first"), 13, 13, 13, 13);
			back2 = new NinePatch(SpriteCache.getThemedSprite("listrec-secend"), 13, 13, 13, 13);

			mBackIsInitial = true;
		}
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (isPressed)
		{
			isPressed = GL.getIsTouchDown();
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
		GL.that.renderOnce(this.getName() + " touchDown");

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		if (isPressed)
		{
			isPressed = false;
		}

		GL.that.renderOnce(this.getName() + " touchDown");

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

	@Override
	public float getLeftWidth()
	{

		if (!mBackIsInitial) Initial();

		if (isSelected)
		{
			return backSelect.getLeftWidth();
		}
		else if ((this.getIndex() % 2) == 1)
		{
			return back1.getLeftWidth();
		}
		else
		{
			return back2.getLeftWidth();
		}
	}

	@Override
	public float getBottomHeight()
	{
		if (!mBackIsInitial) Initial();

		if (isSelected)
		{
			return backSelect.getBottomHeight();
		}
		else if ((this.getIndex() % 2) == 1)
		{
			return back1.getBottomHeight();
		}
		else
		{
			return back2.getBottomHeight();
		}
	}

	@Override
	public float getRightWidth()
	{

		if (!mBackIsInitial) Initial();

		if (isSelected)
		{
			return backSelect.getRightWidth();
		}
		else if ((this.getIndex() % 2) == 1)
		{
			return back1.getRightWidth();
		}
		else
		{
			return back2.getRightWidth();
		}
	}

	@Override
	public float getTopHeight()
	{

		if (!mBackIsInitial) Initial();

		if (isSelected)
		{
			return backSelect.getTopHeight();
		}
		else if ((this.getIndex() % 2) == 1)
		{
			return back1.getTopHeight();
		}
		else
		{
			return back2.getTopHeight();
		}
	}

}

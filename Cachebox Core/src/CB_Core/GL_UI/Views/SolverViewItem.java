package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.Math.CB_RectF;
import CB_Core.Solver.SolverZeile;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SolverViewItem extends ListViewItemBase
{
	protected boolean isPressed = false;
	protected SolverZeile solverZeile;

	public SolverViewItem(CB_RectF rec, int Index, SolverZeile solverZeile)
	{
		super(rec, Index, "");
		this.solverZeile = solverZeile;
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

	private static NinePatch backSelect;
	private static NinePatch back1;
	private static NinePatch back2;
	private static boolean mBackIsInitial = false;

	private BitmapFont mBitmapFont = Fonts.getNormal();
	private BitmapFont mBitmapFontSmall = Fonts.getSmall();
	private BitmapFontCache mS_FontCache;
	private BitmapFontCache mS_FontCacheResult;

	@Override
	protected void render(SpriteBatch batch)
	{
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

		mS_FontCache = new BitmapFontCache(mBitmapFont);
		mS_FontCache.setText(solverZeile.getOrgText(), 0, 0);
		mS_FontCache.setPosition(10, 45);
		mS_FontCache.draw(batch);

		mS_FontCacheResult = new BitmapFontCache(mBitmapFont);
		mS_FontCacheResult.setText(solverZeile.Solution, 0, 0);
		mS_FontCacheResult.setPosition(30, 20);
		mS_FontCacheResult.draw(batch);

		super.render(batch);
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		isPressed = true;

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isPressed = false;

		return false;
	}

}

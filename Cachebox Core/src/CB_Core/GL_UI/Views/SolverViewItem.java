package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;
import CB_Core.Solver.SolverZeile;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SolverViewItem extends ListViewItemBackground
{
	protected boolean isPressed = false;
	protected SolverZeile solverZeile;

	public SolverViewItem(CB_RectF rec, int Index, SolverZeile solverZeile)
	{
		super(rec, Index, "");
		this.solverZeile = solverZeile;
	}

	private BitmapFont mBitmapFont = Fonts.getNormal();
	private BitmapFont mBitmapFontSmall = Fonts.getSmall();
	private BitmapFontCache mS_FontCache;
	private BitmapFontCache mS_FontCacheResult;

	@Override
	protected void Initial()
	{
		super.Initial();

		mS_FontCache = new BitmapFontCache(mBitmapFont);
		mS_FontCache.setColor(Fonts.getFontColor());
		mS_FontCache.setText(solverZeile.getOrgText(), getLeftWidth(), this.height - getTopHeight());

		mS_FontCacheResult = new BitmapFontCache(mBitmapFont);
		mS_FontCacheResult.setText(solverZeile.Solution, getLeftWidth() * 3, getBottomHeight() * 2);
		mS_FontCacheResult.setColor(Fonts.getFontColor());
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);
		if (mS_FontCache != null) mS_FontCache.draw(batch);
		if (mS_FontCacheResult != null) mS_FontCacheResult.draw(batch);
	}

	@Override
	public void dispose()
	{
		mS_FontCache = null;
		mS_FontCacheResult = null;
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

	@Override
	protected void SkinIsChanged()
	{

	}

}

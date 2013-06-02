package CB_Core.GL_UI.Views;

import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Solver.SolverZeile;

public class SolverViewItem extends ListViewItemBackground
{
	protected boolean isPressed = false;
	protected SolverZeile solverZeile;
	Label lblSolverZeile;

	public SolverViewItem(CB_RectF rec, int Index, SolverZeile solverZeile)
	{
		super(rec, Index, "");
		this.solverZeile = solverZeile;
	}

	@Override
	protected void Initial()
	{
		lblSolverZeile = new Label();
		lblSolverZeile.setHeight(this.height);
		lblSolverZeile.setMultiLineText(solverZeile.getOrgText() + "\n" + solverZeile.Solution, null, null, null, VAlignment.CENTER);
		this.setBorders(UI_Size_Base.that.getMargin(), UI_Size_Base.that.getMargin());
		this.addLast(lblSolverZeile);
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

	@Override
	protected void SkinIsChanged()
	{

	}

}

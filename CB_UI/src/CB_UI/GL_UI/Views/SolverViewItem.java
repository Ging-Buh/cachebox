package CB_UI.GL_UI.Views;

import CB_UI.Solver.SolverZeile;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

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
		lblSolverZeile = new Label(solverZeile.getOrgText() + "\n" + solverZeile.Solution, Fonts.getNormal(), Fonts.getFontColor(),
				WrapType.MULTILINE);
		lblSolverZeile.setHeight(this.getHeight()); // todo ob das immer passt?
		this.setBorders(UI_Size_Base.that.getMargin(), UI_Size_Base.that.getMargin());
		this.addLast(lblSolverZeile);
	}

	@Override
	public void dispose()
	{
		lblSolverZeile = null;
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

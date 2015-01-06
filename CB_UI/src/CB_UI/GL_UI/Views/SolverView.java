package CB_UI.GL_UI.Views;

import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.ViewConst;
import CB_UI_Base.Math.CB_RectF;

public class SolverView extends CB_View_Base
{

	public SolverView(CB_RectF rec, String Name)
	{
		super(rec, Name);

	}

	@Override
	public void onShow()
	{
		// Rufe ANDROID VIEW auf
		platformConector.showView(ViewConst.SOLVER_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.SOLVER_VIEW);
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

}

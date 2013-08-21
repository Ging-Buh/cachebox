package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.Math.CB_RectF;

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
		this.setClickable(true);
	}

	protected int mIndex;

	public int getIndex()
	{
		return mIndex;
	}

	public boolean isSelected = false;

	// @Override
	// public boolean onTouchDown(int x, int y, int pointer, int button)
	// {
	//
	// return true;
	// }
	//
	// @Override
	// public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	// {
	//
	// return true;
	// }
	//
	// @Override
	// public boolean onTouchUp(int x, int y, int pointer, int button)
	// {
	//
	// return true;
	// }

}

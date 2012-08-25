package CB_Core.GL_UI.Controls.List;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;

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

	protected int mIndex;

	public int getIndex()
	{
		return mIndex;
	}

	public boolean isSelected = false;

}

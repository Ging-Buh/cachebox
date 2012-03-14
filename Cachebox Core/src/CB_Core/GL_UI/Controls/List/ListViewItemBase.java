package CB_Core.GL_UI.Controls.List;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Math.CB_RectF;

public abstract class ListViewItemBase extends CB_View_Base
{

	public ListViewItemBase(CB_RectF rec, int Index, CharSequence Name)
	{
		super(rec, Name);
		mIndex = Index;
	}

	private int mIndex;

	public int getIndex()
	{
		return mIndex;
	}

}

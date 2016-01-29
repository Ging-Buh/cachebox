package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.CB_View_Base;

public interface IScrollbarParent
{
	public CB_View_Base getView();

	public boolean isDragable();

	public float getScrollPos();

	public float getAllListSize();

	public void setListPos(float value);

	public float getFirstItemSize();

	public float getLastItemSize();

	public void chkSlideBack();

}

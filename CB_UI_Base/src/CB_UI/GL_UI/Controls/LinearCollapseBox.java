package CB_UI.GL_UI.Controls;

import CB_UI.GL_UI.GL_View_Base;
import CB_UI.GL_UI.Controls.Linearlayout.LayoutChanged;
import CB_UI.Math.CB_RectF;
import CB_Utils.Util.MoveableList;

/**
 * Eine CollabsBox mit LinearLayout
 * 
 * @author Longri
 */
public class LinearCollapseBox extends CollapseBox
{
	private Linearlayout linearLayout;

	public LinearCollapseBox(CB_RectF rec, String Name)
	{
		super(rec, Name);
		linearLayout = new Linearlayout(rec.getWidth(), "LinearLayout-" + Name);
		this.childs.add(linearLayout);

		linearLayout.setLayoutChangedListner(new LayoutChanged()
		{

			@Override
			public void LayoutIsChanged(Linearlayout linearLayout, float newHeight)
			{
				layout();
			}
		});

	}

	public GL_View_Base addChild(final GL_View_Base view)
	{
		return linearLayout.addChild(view, false);
	}

	public GL_View_Base addChild(final GL_View_Base view, final boolean last)
	{
		GL_View_Base v = linearLayout.addChild(view, last);
		layout();

		return v;
	}

	public void removeChild(final GL_View_Base view)
	{

		linearLayout.removeChild(view);
		layout();
	}

	public void removeChilds()
	{
		linearLayout.removeChilds();
		layout();

	}

	public void removeChilds(final MoveableList<GL_View_Base> Childs)
	{
		linearLayout.removeChilds(Childs);
		layout();
	}

	public GL_View_Base addChildDirekt(final GL_View_Base view)
	{
		linearLayout.addChildDirekt(view);
		layout();
		return view;
	}

	public GL_View_Base addChildDirektLast(final GL_View_Base view)
	{
		linearLayout.addChildDirektLast(view);

		layout();
		return view;
	}

	public void removeChildsDirekt()
	{
		linearLayout.removeChildsDirekt();
		layout();

	}

	private void layout()
	{
		this.setHeight(linearLayout.getHeight());
	}

	/**
	 * Setzt dieses View Clicable mit der �bergabe von True. </br> Wenn Dieses View nicht Clickable ist, werden auch keine Click-Abfragen
	 * an die Childs weitergegeben.
	 * 
	 * @param value
	 */
	public void setClickable(boolean value)
	{
		linearLayout.setClickable(value);
		super.setClickable(value);
	}
}

package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.Math.CB_RectF;
import CB_Core.Util.MoveableList;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ScrollBox extends CB_View_Base
{
	private V_ListView lv;
	private float virtualHeight;
	private ListViewItemBase item;
	private CustomAdapter thisAdapter;

	public ScrollBox(CB_RectF rec)
	{
		super(rec, "ScrollBox");
		initScrollBox();
	}

	public ScrollBox(float Width, float Height)
	{
		super(0, 0, Width, Height, "ScrollBox");
		initScrollBox();
	}

	private void initScrollBox()
	{
		// todo: check to have no scroll(? - margin) oder rec.getHalfHeight()
		virtualHeight = this.height;

		lv = new V_ListView(this, "ListView-" + name);
		lv.setClickable(true);

		item = new ListViewItemBase(this, 0, "ListViewItem-" + name)
		{

			@Override
			protected void SkinIsChanged()
			{
			}

			@Override
			protected void Initial()
			{
				isInitial = true;
			}
		};

		item.setHeight(virtualHeight);
		item.setClickable(true);
		thisAdapter = new CustomAdapter();
		lv.setDisposeFlag(false);
		lv.setBaseAdapter(thisAdapter);
		Layout();
		this.childs.add(lv);
	}

	@Override
	public void setBorders(float l, float r)
	{
		super.setBorders(l, r);
		Layout();
	}

	private void Layout()
	{
		lv.setSize(innerWidth, innerHeight);
		lv.setPos(leftBorder, bottomBorder);
		item.setHeight(virtualHeight);
		lv.notifyDataSetChanged();
	}

	@Override
	public void setBackground(Drawable background)
	{
		super.setBackground(background);
		Layout();
	}

	/**
	 ** virtualHeight to take all placed objects (scrolls if > height)
	 **/
	public void setVirtualHeight(float virtualHeight)
	{
		this.virtualHeight = virtualHeight;
		Layout();
	}

	public float getVirtualHeight()
	{
		return virtualHeight;
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		lv.setSize(innerWidth, innerHeight);
		item.setWidth(innerWidth);
	}

	public class CustomAdapter implements Adapter
	{

		public CustomAdapter()
		{
		}

		@Override
		public int getCount()
		{
			return 1;
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			return item;
		}

		@Override
		public float getItemSize(int position)
		{
			return item.getHeight();
		}
	}

	// ################ add / remove overrides ############################################
	public int getCildCount()
	{
		return item.getCildCount();
	}

	public GL_View_Base addChildDirekt(final GL_View_Base view)
	{
		item.addChildDirekt(view);
		lv.notifyDataSetChanged();
		return view;
	}

	public GL_View_Base addChildDirektLast(final GL_View_Base view)
	{
		item.addChildDirektLast(view);
		lv.notifyDataSetChanged();
		return view;
	}

	public void removeChildsDirekt()
	{
		item.removeChildsDirekt();
		lv.notifyDataSetChanged();
	}

	public GL_View_Base getChild(int i)
	{
		return item.getChild(i);
	}

	public GL_View_Base addChild(final GL_View_Base view)
	{
		item.addChildDirekt(view);
		lv.notifyDataSetChanged();
		return view;
	}

	public GL_View_Base addChild(final GL_View_Base view, final boolean last)
	{
		if (last)
		{
			item.addChildDirektLast(view);
		}
		else
		{
			item.addChildDirekt(view);
		}
		lv.notifyDataSetChanged();
		return view;
	}

	public void removeChild(final GL_View_Base view)
	{
		item.removeChild(view);
		lv.notifyDataSetChanged();
	}

	public void removeChilds()
	{
		item.removeChilds();
		lv.notifyDataSetChanged();
	}

	public void removeChilds(final MoveableList<GL_View_Base> Childs)
	{
		item.removeChilds(Childs);
		lv.notifyDataSetChanged();
	}

	@Override
	protected void Initial()
	{
		super.isInitial = true;

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setDragable()
	{
		lv.setDragable();
	}

	public float getScrollY()
	{
		return lv.getScrollPos();
	}

	public void scrollTo(float scrollPos)
	{
		lv.scrollTo(scrollPos);
	}

	public void setClickable(boolean value)
	{
		lv.setClickable(value);
		super.setClickable(value);
	}

	public void setLongClickable(boolean value)
	{

		lv.setLongClickable(value);
		super.setLongClickable(value);
	}
}

package CB_UI_Base.GL_UI.Controls;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Util.MoveableList;

/**
 * the Width must be set in advance to place and size the elemets
 * the final Height must take all placed elements
 * scrolling works, if virtualHeight > Height
 *
 */
public class ScrollBox extends CB_View_Base {
	protected V_ListView lv;
	protected float virtualHeight;
	protected ListViewItemBase item;
	protected CustomAdapter thisAdapter;

	public ScrollBox(CB_RectF rec) {
		super(rec, "ScrollBox");
		initScrollBox();
	}

	/**
	 *
	 * @param Width
	 * @param Height
	 */
	public ScrollBox(float Width, float Height) {
		super(0, 0, Width, Height, "ScrollBox");
		initScrollBox();
	}

	protected void initScrollBox() {
		virtualHeight = this.getHeight();

		lv = new V_ListView(this, this, "ListView-" + name);
		lv.setClickable(true);

		item = new ListViewItemBase(this, 0, "ListViewItem-" + name) {

			@Override
			protected void SkinIsChanged() {
			}

			@Override
			protected void Initial() {
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
	public void setBorders(float l, float r) {
		super.setBorders(l, r);
		Layout();
	}

	protected void Layout() {

		//if this is disposed do nothing
		if (this.isDisposed())
			return;

		//if Listview NULL initial
		if (lv == null) {
			initScrollBox();
			return;
		}

		// if Listview disposed do nothing!
		// THIS will dispose soon!
		if (lv.isDisposed())
			return;

		lv.setSize(innerWidth, innerHeight);

		item.setHeight(virtualHeight);
		lv.calcDefaultPosList();

		lv.setPos(leftBorder, bottomBorder);

		lv.scrollTo(lv.getScrollPos());
	}

	@Override
	public void setBackground(Drawable background) {
		super.setBackground(background);
		Layout();
	}

	/**
	 ** virtualHeight to take all placed objects (scrolls if > height)
	 **/
	public void setVirtualHeight(float virtualHeight) {
		this.virtualHeight = virtualHeight;
		Layout();
	}

	public float getVirtualHeight() {
		return virtualHeight;
	}

	@Override
	public void onResized(CB_RectF rec) {
		lv.setSize(innerWidth, innerHeight);
		item.setWidth(innerWidth);
	}

	public class CustomAdapter implements Adapter {

		public CustomAdapter() {
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public ListViewItemBase getView(int position) {
			return item;
		}

		@Override
		public float getItemSize(int position) {
			return item.getHeight();
		}
	}

	// ################ add / remove overrides ############################################
	@Override
	public int getCildCount() {
		return item.getCildCount();
	}

	@Override
	public GL_View_Base addChildDirekt(final GL_View_Base view) {
		item.addChildDirekt(view);
		lv.notifyDataSetChanged();
		return view;
	}

	@Override
	public GL_View_Base addChildDirektLast(final GL_View_Base view) {
		item.addChildDirektLast(view);
		lv.notifyDataSetChanged();
		return view;
	}

	@Override
	public void removeChildsDirekt(final GL_View_Base view) {
		item.removeChildsDirekt(view);
		lv.notifyDataSetChanged();
	}
	
	@Override
	public void removeChildsDirekt() {
		item.removeChildsDirekt();
		lv.notifyDataSetChanged();
	}

	@Override
	public GL_View_Base getChild(int i) {
		return item.getChild(i);
	}

	@Override
	public GL_View_Base addChild(final GL_View_Base view) {
		item.addChildDirekt(view);
		lv.notifyDataSetChanged();
		return view;
	}

	@Override
	public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
		if (last) {
			item.addChildDirektLast(view);
		} else {
			item.addChildDirekt(view);
		}
		lv.notifyDataSetChanged();
		return view;
	}

	@Override
	public void removeChild(final GL_View_Base view) {
		item.removeChild(view);
		lv.notifyDataSetChanged();
	}

	@Override
	public void removeChilds() {
		item.removeChilds();
		lv.notifyDataSetChanged();
	}

	@Override
	public void removeChilds(final MoveableList<GL_View_Base> Childs) {
		item.removeChilds(Childs);
		lv.notifyDataSetChanged();
	}

	@Override
	protected void Initial() {
		super.isInitial = true;

	}

	@Override
	protected void SkinIsChanged() {

	}

	public void setDragable() {
		lv.setDraggable();
	}

	public void setUndragable() {
		lv.setUnDraggable();
	}

	public float getScrollY() {
		return lv.getScrollPos();
	}

	public void scrollTo(float scrollPos) {
		lv.scrollTo(scrollPos);
	}

	@Override
	public void setClickable(boolean value) {
		lv.setClickable(value);
		super.setClickable(value);
	}

	@Override
	public void setLongClickable(boolean value) {

		lv.setLongClickable(value);
		super.setLongClickable(value);
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button) {

		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgel�st wird.
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

		return true;
	}

}

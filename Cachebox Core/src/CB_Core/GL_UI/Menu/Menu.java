package CB_Core.GL_UI.Menu;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Menu extends Dialog
{
	public static float ItemHeight = -1f;

	private ArrayList<MenuItem> mItems = new ArrayList<MenuItem>();
	private V_ListView mListView;
	private Menu Me;

	private OnClickListener MenuItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			GL_Listener.glListener.closeDialog();
			if (mOnItemClickListner != null) mOnItemClickListner.onClick(v, x, y, pointer, button);

			return false;
		}
	};

	public Menu(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		Me = this;
		if (ItemHeight == -1f) ItemHeight = UiSizes.getButtonHeight();
	}

	@Override
	protected void Initial()
	{
		this.removeChilds();

		mListView = new V_ListView(this, "MenuList");

		mListView.setHeight(this.height - 50);

		mListView.setZeroPos();
		this.addChild(mListView);

		mListView.setBaseAdapter(new CustomAdapter());

		super.Initial();

	}

	public class CustomAdapter implements Adapter
	{

		public ListViewItemBase getView(int position)
		{
			return mItems.get(position);
		}

		@Override
		public int getCount()
		{
			return mItems.size();
		}

		@Override
		public float getItemSize(int position)
		{
			return mItems.get(position).getHeight();
		}
	}

	public void addItem(MenuItem menuItem)
	{
		menuItem.setOnClickListener(MenuItemClickListner);
		mItems.add(menuItem);
	}

	public MenuItem addItem(int ID, String StringId)
	{

		String trans = GlobalCore.Translations.Get(StringId);

		MenuItem item = new MenuItem(new SizeF(this.width * 0.95f, ItemHeight), mItems.size(), ID, "Menu Item@" + ID);

		float higherValue = this.height + ItemHeight + 2; // 2= Standard divider Height

		if (higherValue < UiSizes.getWindowHeight() * 0.8f)
		{
			this.setHeight((higherValue));
			this.resetInitial();
		}

		item.setTitle(trans);
		addItem(item);

		return item;
	}

	public void show()
	{

		// wenn irgent ein Item Chackable ist, dann alle Titles Einrücken.
		boolean oneIsChakable = false;
		for (Iterator<MenuItem> iterator = mItems.iterator(); iterator.hasNext();)
		{
			if (iterator.next().isCheckable())
			{
				oneIsChakable = true;
				break;
			}
		}
		if (oneIsChakable)
		{
			for (Iterator<MenuItem> iterator = mItems.iterator(); iterator.hasNext();)
			{
				iterator.next().setLeft(true);
			}
		}

		GL_Listener.glListener.showDialog(this);
	}

	public MenuItem addItem(int ID, String StringId, Sprite icon)
	{
		MenuItem item = addItem(ID, StringId);
		item.setIcon(icon);
		return item;
	}

	private OnClickListener mOnItemClickListner;

	public void setItemClickListner(OnClickListener onItemClickListner)
	{
		this.mOnItemClickListner = onItemClickListner;
	}
}

package CB_Core.GL_UI.Menu;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class Menu extends ButtonDialog
{
	private static CB_RectF MENU_REC = null;
	private static boolean sizeIsInitial = false;

	private static void initialSize()
	{
		float sollWidth = GL_UISizes.UI_Left.getWidth();

		if (!GlobalCore.isTab) sollWidth /= 1.2f;
		sollWidth *= 0.83;
		MENU_REC = new CB_RectF(new SizeF(sollWidth, 50));
		sizeIsInitial = true;
	}

	public static CB_RectF getMenuRec()
	{
		if (!sizeIsInitial) initialSize();
		return MENU_REC;
	}

	public float ItemHeight = -1f;

	private ArrayList<MenuItemBase> mItems = new ArrayList<MenuItemBase>();
	private V_ListView mListView;
	protected Menu that;

	protected OnClickListener MenuItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			GL.that.closeDialog(that);
			if (mOnItemClickListner != null)
			{
				for (OnClickListener tmp : mOnItemClickListner)
				{
					if (tmp.onClick(v, x, y, pointer, button)) break;
				}
			}

			return true;
		}
	};

	public Menu(String Name)
	{
		super(getMenuRec(), Name);
		that = this;

		if (ItemHeight == -1f) ItemHeight = UI_Size_Base.that.getButtonHeight();

		MENU_REC = new CB_RectF(new SizeF(
				(GlobalCore.isTab ? UI_Size_Base.that.getWindowHeight() : UI_Size_Base.that.getWindowWidth()) * 0.83f, mHeaderHight
						+ getFooterHeight() + (margin * 2)));

		this.setRec(MENU_REC);

		mListView = new V_ListView(this, "MenuList");
		mListView.setSize(this.getContentSize());
		// mListView.setWidth(this.width - Left - Right);
		// mListView.setHeight(this.height);

		mListView.setZeroPos();
		this.addChild(mListView);

	}

	@Override
	protected void Initial()
	{
		mListView.setSize(this.getContentSize());

		this.addChild(mListView);
		mListView.setBaseAdapter(new CustomAdapter());

		super.Initial();
		super.initialDialog();
	}

	public class CustomAdapter implements Adapter
	{

		public ListViewItemBase getView(int position)
		{
			ListViewItemBase v = mItems.get(position);

			v.setWidth(mListView.getWidth());

			v.resetInitial();
			return v;
		}

		@Override
		public int getCount()
		{
			return mItems.size();
		}

		@Override
		public float getItemSize(int position)
		{
			if (mItems == null || mItems.size() == 0 || mItems.size() < position) return 0;
			return mItems.get(position).getHeight();
		}
	}

	public void addItem(MenuItemBase menuItem)
	{
		menuItem.setOnClickListener(MenuItemClickListner);
		mItems.add(menuItem);
		mListView.notifyDataSetChanged();
	}

	public MenuItem addItem(int ID, String StringId)
	{
		return addItem(ID, StringId, "", false);
	}

	public MenuItem addItem(int ID, String StringId, boolean withoutTranslation)
	{
		return addItem(ID, StringId, "", withoutTranslation);
	}

	public MenuItem addItem(int ID, String StringId, String anhang, Sprite icon)
	{
		MenuItem item = addItem(ID, StringId, anhang);
		if (icon != null) item.setIcon(new SpriteDrawable(icon));
		return item;
	}

	public MenuItem addItem(int ID, String StringId, String anhang, Drawable icon)
	{
		MenuItem item = addItem(ID, StringId, anhang);
		if (icon != null) item.setIcon(icon);
		return item;
	}

	public MenuItem addItem(int ID, String StringId, String anhang)
	{
		return addItem(ID, StringId, anhang, false);
	}

	public MenuItem addItem(int index, String text, Drawable drawable, boolean withoutTranslation)
	{
		MenuItem item = addItem(index, text, "", withoutTranslation);
		if (drawable != null) item.setIcon(drawable);
		return item;
	}

	public MenuItem addItem(int ID, String StringId, String anhang, boolean withoutTranslation)
	{
		String trans;
		if (StringId == null || StringId.equals(""))
		{
			trans = anhang;
		}
		else
		{
			trans = Translation.Get(StringId) + anhang;
		}

		if (withoutTranslation) trans = StringId;

		// layout();
		MenuItem item = new MenuItem(new SizeF(mListView.getWidth(), ItemHeight), mItems.size(), ID, "Menu Item@" + ID);

		item.setTitle(trans);
		addItem(item);

		return item;
	}

	private void layout()
	{
		float higherValue = mTitleHeight + mHeaderHight + getFooterHeight() + (margin * 2);

		for (MenuItemBase item : mItems)
		{
			higherValue += item.getHeight() + mListView.getDividerHeight();
		}

		higherValue = Math.min(higherValue, UI_Size_Base.that.getWindowHeight() * 0.95f);

		if (higherValue > UI_Size_Base.that.getWindowHeight() * 0.95f)
		{
			higherValue = UI_Size_Base.that.getWindowHeight() * 0.95f;
		}

		float MenuWidth = GL_UISizes.UI_Left.getWidth();

		if (!GlobalCore.isTab) MenuWidth /= 1.2f;

		this.setSize(MenuWidth, higherValue);

		this.resetInitial();

		mListView.setSize(this.getContentSize());
		mListView.setZeroPos();

		// Alle Items in der Breite anpassen

		float w = mListView.getWidth();
		for (MenuItemBase item : mItems)
		{
			item.setWidth(w);
			item.resetInitial();
		}

		mListView.notifyDataSetChanged();
	}

	@Override
	public void Show()
	{
		layout();
		// wenn irgent ein Item Chackable ist, dann alle Titles Einrücken.
		/*
		 * boolean oneIsChakable = false; for (Iterator<MenuItemBase> iterator = mItems.iterator(); iterator.hasNext();) { MenuItemBase tmp
		 * = iterator.next(); if (tmp instanceof MenuItem) { if (((MenuItem) tmp).isCheckable()) { oneIsChakable = true; break; } } } if
		 * (oneIsChakable) { for (Iterator<MenuItemBase> iterator = mItems.iterator(); iterator.hasNext();) { MenuItemBase tmp =
		 * iterator.next(); if (tmp instanceof MenuItem) { ((MenuItem) tmp).setLeft(true); } } }
		 */
		super.Show();

	}

	public MenuItem addItem(int ID, String StringId, Sprite icon)
	{
		MenuItem item = addItem(ID, StringId);
		if (icon != null) item.setIcon(new SpriteDrawable(icon));
		return item;
	}

	protected ArrayList<OnClickListener> mOnItemClickListner;

	public void addItemClickListner(OnClickListener onItemClickListner)
	{
		if (this.mOnItemClickListner == null) this.mOnItemClickListner = new ArrayList<GL_View_Base.OnClickListener>();
		this.mOnItemClickListner.add(onItemClickListner);
	}

	public void addItemClickListner(ArrayList<OnClickListener> onItemClickListner)
	{
		if (this.mOnItemClickListner == null) this.mOnItemClickListner = new ArrayList<GL_View_Base.OnClickListener>();
		this.mOnItemClickListner.addAll(onItemClickListner);
	}

	public ArrayList<OnClickListener> getItemClickListner()
	{
		return this.mOnItemClickListner;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setPrompt(String Prompt)
	{
		// set Title with full width, add many blanks
		this.setTitle(Prompt + "                                                       ");
		layout();
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		if (mListView != null)
		{
			mListView.setSize(this.getContentSize());
			mListView.setZeroPos();
		}
	}

	public ArrayList<MenuItemBase> getItems()
	{
		return mItems;
	}

	public void addItems(ArrayList<MenuItemBase> items)
	{
		for (MenuItemBase menuItem : items)
		{
			menuItem.setOnClickListener(MenuItemClickListner);
			mItems.add(menuItem);
			mListView.notifyDataSetChanged();
		}
	}

	public void addDivider()
	{
		MenuItemDivider item = new MenuItemDivider(new SizeF(mListView.getWidth(), ItemHeight / 5), mItems.size(), "Menu Devider");

		item.setEnabled(false);

		addItem(item);
	}

	/**
	 * Die indexes der Items werden neu erstellt.
	 */
	public int reorganizeIndexes()
	{
		int Index = 0;
		for (MenuItemBase item : mItems)
		{
			item.setIndex(Index++);
		}
		return Index;
	}

}

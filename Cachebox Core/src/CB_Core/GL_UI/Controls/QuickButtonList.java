package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.H_ListView;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Main.CB_Button;
import CB_Core.GL_UI.Main.CB_ButtonList;
import CB_Core.GL_UI.Main.CB_ButtonListItem;
import CB_Core.Math.CB_RectF;

public class QuickButtonList extends H_ListView
{

	private float btnHeight;
	private float btnYPos;

	public QuickButtonList(CB_RectF rec, String Name)
	{
		super(rec, Name);

		btnHeight = rec.getHeight() * 0.93f;
		setBackground(SpriteCache.ButtonBack);

		CB_RectF btnRec = new CB_RectF(0, 0, btnHeight, btnHeight);

		btnYPos = this.halfHeight - btnRec.getHalfHeight();

		CB_Button btn1 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList).disableGester();
		CB_Button btn2 = new CB_Button(btnRec, "Button2", SpriteCache.Cache).disableGester();
		CB_Button btn3 = new CB_Button(btnRec, "Button3", SpriteCache.Nav).disableGester();
		CB_Button btn4 = new CB_Button(btnRec, "Button4", SpriteCache.Tool).disableGester();
		CB_Button btn5 = new CB_Button(btnRec, "Button5", SpriteCache.Misc).disableGester();
		CB_Button btn6 = new CB_Button(btnRec, "Button1", SpriteCache.CacheList).disableGester();
		CB_Button btn7 = new CB_Button(btnRec, "Button2", SpriteCache.Cache).disableGester();
		CB_Button btn8 = new CB_Button(btnRec, "Button3", SpriteCache.Nav).disableGester();
		CB_Button btn9 = new CB_Button(btnRec, "Button4", SpriteCache.Tool).disableGester();
		CB_Button btn10 = new CB_Button(btnRec, "Button5", SpriteCache.Misc).disableGester();

		CB_ButtonList btnList = new CB_ButtonList();
		btnList.addButton(btn1);
		btnList.addButton(btn2);
		btnList.addButton(btn3);
		btnList.addButton(btn4);
		btnList.addButton(btn5);
		btnList.addButton(btn6);
		btnList.addButton(btn7);
		btnList.addButton(btn8);
		btnList.addButton(btn9);
		btnList.addButton(btn10);

		mButtonList = btnList;
		this.setBaseAdapter(new CustomAdapter());
	}

	@Override
	public void Initial()
	{
		this.setDragable();
	}

	private CB_ButtonList mButtonList;

	public void addButtonList(CB_ButtonList ButtonList)
	{
		mButtonList = ButtonList;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		// send Event to Buttons
		for (GL_View_Base btn : this.childs)
		{
			btn.onTouchUp(x, y, pointer, button);
			if (btn.contains(x, y))
			{
				return btn.click(x, y, pointer, button);
			}
		}

		return super.click(x, y, pointer, button);
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		for (GL_View_Base btn : this.childs)
		{
			btn.onTouchUp(x, y, pointer, button);
		}

		return true;
	}

	public class CustomAdapter implements Adapter
	{

		public CustomAdapter()
		{

		}

		public long getItemId(int position)
		{
			return position;
		}

		public ListViewItemBase getView(int position)
		{

			if (mButtonList == null || mButtonList.Buttons == null) return null;

			CB_ButtonListItem v = new CB_ButtonListItem(position, mButtonList.Buttons.get(position), "Item " + position);
			v.setSize(btnHeight, btnHeight);
			v.setY(btnYPos);// center btn on y direction
			return v;
		}

		@Override
		public int getCount()
		{
			return mButtonList.Buttons.size();
		}

		@Override
		public float getItemSize(int position)
		{
			return btnHeight;
		}
	}

}

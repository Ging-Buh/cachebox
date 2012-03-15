package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.H_ListView;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

public class CB_TabView extends CB_View_Base
{

	private CB_ButtonList mButtonList;
	private H_ListView buttonListView;

	public void addButtonList(CB_ButtonList ButtonList)
	{
		mButtonList = ButtonList;
		AddButtonsAsChild();
	}

	private void AddButtonsAsChild()
	{
		if (mButtonList == null) return;
		buttonListView = new H_ListView(new CB_RectF(0, 0, this.width, GL_UISizes.BottomButtonHeight), "ButtonList von " + this.getName());
		buttonListView.setBaseAdapter(new CustomAdapter());
		this.addChild(buttonListView);
	}

	public CB_TabView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

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
			return v;
		}

		@Override
		public int getCount()
		{
			return mButtonList.Buttons.size();
		}
	}

}

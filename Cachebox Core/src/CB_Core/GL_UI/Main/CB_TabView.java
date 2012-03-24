package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.H_ListView;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;

import com.badlogic.gdx.math.Vector2;

public class CB_TabView extends CB_View_Base
{

	private CB_ButtonList mButtonList;
	private H_ListView buttonListView;
	private CB_View_Base aktView;

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
		buttonListView.setUndragable();
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

		@Override
		public float getItemSize(int position)
		{
			return GL_UISizes.BottomButtonHeight;
		}
	}

	public void ShowView(CB_View_Base view)
	{
		// delete all Views up to the ButtonList
		if (aktView != null)
		{
			this.removeChild(aktView);
			aktView.onStop();
			aktView.onHide();
		}

		// set View size and pos
		view.setSize(this.width, this.height - buttonListView.getHeight());
		view.setPos(new Vector2(0, buttonListView.getHeight()));

		aktView = view;
		this.addChild(aktView);

		aktView.onShow();
		GL_Listener.glListener.renderOnce(aktView);

	}

}

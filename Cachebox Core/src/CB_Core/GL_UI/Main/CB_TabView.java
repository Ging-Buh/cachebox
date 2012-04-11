package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
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

	private CB_RectF mContentRec;

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
		buttonListView.setBackground(SpriteCache.ButtonBack);
		this.addChild(buttonListView);
	}

	public CB_TabView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		mContentRec = rec.copy();
		mContentRec.setHeight(this.getHeight() - GL_UISizes.BottomButtonHeight);
		mContentRec.setPos(0, GL_UISizes.BottomButtonHeight);
	}

	@Override
	protected void Initial()
	{
		// Wenn die Anzahl der Buttons = der Anzahl der Möglichen Buttons ist, diese gleichmäßig verteilen
		if (mButtonList.Buttons.size() == buttonListView.getMaxItemCount())
		{
			float sollDivider = (buttonListView.getWidth() - (GL_UISizes.BottomButtonHeight * mButtonList.Buttons.size()))
					/ (mButtonList.Buttons.size() + 1);
			buttonListView.setDividerSize(sollDivider);
		}

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
			// aktView.onStop();
			aktView.onHide();
			aktView.setVisibility(INVISIBLE);
		}

		// set View size and pos
		view.setSize(this.width, this.height - buttonListView.getHeight());
		view.setPos(new Vector2(0, buttonListView.getHeight()));

		aktView = view;
		this.addChild(aktView);

		aktView.setVisibility(VISIBLE);
		aktView.onShow();
		GL_Listener.glListener.renderOnce(aktView);

	}

	public CB_RectF getContentRec()
	{
		return mContentRec;
	}

}

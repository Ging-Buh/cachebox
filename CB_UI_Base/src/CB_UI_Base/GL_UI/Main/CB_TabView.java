package CB_UI_Base.GL_UI.Main;

import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.H_ListView;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;

import com.badlogic.gdx.math.Vector2;

public class CB_TabView extends CB_View_Base
{

	private CB_ButtonList mButtonList;
	private H_ListView buttonListView;
	private CB_View_Base aktView;

	private final CB_RectF mContentRec;

	public void addButtonList(CB_ButtonList ButtonList)
	{
		mButtonList = ButtonList;
		AddButtonsAsChild();
	}

	private void AddButtonsAsChild()
	{
		if (mButtonList == null) return;
		buttonListView = new H_ListView(new CB_RectF(0, 0, this.getWidth(), GL_UISizes.BottomButtonHeight), "ButtonList von "
				+ this.getName());
		buttonListView.setBaseAdapter(new CustomAdapter());
		buttonListView.setUndragable();
		buttonListView.setBackground(SpriteCacheBase.ButtonBack);
		this.addChild(buttonListView);
	}

	public CB_TabView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mContentRec = rec.copy();
		layout();
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		layout();
	}

	private void layout()
	{
		mContentRec.setHeight(this.getHeight() - GL_UISizes.BottomButtonHeight);
		mContentRec.setPos(0, GL_UISizes.BottomButtonHeight);

		if (aktView != null)
		{
			// set View size and pos
			aktView.setSize(this.getWidth(), this.getHeight() - buttonListView.getHeight());
			aktView.setPos(new Vector2(0, buttonListView.getHeight()));

		}
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

		// Das Button Seitenverhältniss ist 88x76!
		// Höhe der Buttons einstellen und diese Zentrieren!
		float buttonHeight = GL_UISizes.BottomButtonHeight * 0.863f;
		for (CB_Button btn : mButtonList.Buttons)
		{
			btn.setHeight(buttonHeight);
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

		@Override
		public ListViewItemBase getView(int position)
		{

			if (mButtonList == null || mButtonList.Buttons == null) return null;

			CB_Button btn = mButtonList.Buttons.get(position);

			btn.setActView(aktView);

			CB_ButtonListItem v = new CB_ButtonListItem(position, btn, "Item " + position);
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

		GL.that.clearRenderViews();
		GL.that.closeAllDialogs();

		platformConector.hideAll();

		// delete all Views up to the ButtonList
		if (aktView != null && aktView != view)
		{
			this.removeChild(aktView);
			// aktView.onStop();
			aktView.onHide();
			aktView.setInvisible();
		}

		// set View size and pos
		view.setSize(this.getWidth(), this.getHeight() - buttonListView.getHeight());
		view.setPos(new Vector2(0, buttonListView.getHeight()));

		if (aktView == view) return;

		aktView = view;
		this.addChild(aktView);

		aktView.setVisible();
		sendOnShow2aktView();

		GL.that.renderOnce(aktView.getName() + " TabView=>ShowView()");

	}

	/**
	 * Beim Wechsel der View, kann es sein, dass noch nicht alle Childs der View geladen sind, da die meisten Childs erst in der initial()
	 * erstellt werden. Damit erhalten diese Childs dann kein onShow(). Als Abhilfe werden hier erst 150ms gewartet, bevor ein onShow()
	 * ausgeführt wird.
	 */
	private void sendOnShow2aktView()
	{
		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				GL.that.RunOnGL(new IRunOnGL()
				{

					@Override
					public void run()
					{
						if (aktView != null && aktView.isVisible()) aktView.onShow();
						buttonListView.notifyDataSetChanged();
					}
				});

			}
		});
	}

	public CB_RectF getContentRec()
	{
		return mContentRec;
	}

	@Override
	public void SkinIsChanged()
	{
		ShowView(aktView);
	}

}

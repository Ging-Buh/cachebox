package CB_Core.GL_UI.Views;

import CB_Core.Config;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.TrackableListDAO;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Activitys.TB_Details;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Animation.DownloadAnimation;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_Core.GL_UI.Controls.PopUps.ConnectionError;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Math.UiSizes;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Core.Util.ByRef;

public class TrackableListView extends CB_View_Base
{
	public static TrackableListView that;
	private V_ListView listView;
	private CustomAdapter lvAdapter;
	private TbList TB_List;
	private Box searchBox;
	private EditWrapedTextField txtSearch;
	private ImageButton btnSearch;
	private CancelWaitDialog wd;

	public TrackableListView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		setBackground(SpriteCache.ListBack);
		lvAdapter = new CustomAdapter();
	}

	@Override
	public void onShow()
	{
		if (TB_List == null) TB_List = TrackableListDAO.ReadTbList("");
		lvAdapter = new CustomAdapter();
		if (listView != null) listView.setBaseAdapter(lvAdapter);

	}

	public void reloadTB_List()
	{
		TB_List = TrackableListDAO.ReadTbList("");
		lvAdapter = new CustomAdapter();
		if (listView != null) listView.setBaseAdapter(lvAdapter);
	}

	@Override
	public void onHide()
	{
	}

	@Override
	protected void Initial()
	{
		this.removeChilds();
		// ##################################################################################################
		// Search Box
		// ##################################################################################################
		searchBox = new Box(this.width, 10, "TB_Search Box");
		btnSearch = new ImageButton("Search");
		searchBox.setBackground(SpriteCache.activityBackground);
		searchBox.setHeight(btnSearch.getHeight() + searchBox.getTopHeight() + searchBox.getBottomHeight());
		this.addChild(searchBox);

		searchBox.initRow();

		txtSearch = new EditWrapedTextField("SearchInput", TextFieldType.SingleLine);
		txtSearch.setMessageText(Translation.Get("SearchTB_Code"));
		searchBox.addNext(txtSearch);

		btnSearch.setImage(SpriteCache.Icons.get(IconName.lupe_27.ordinal()));
		btnSearch.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				final String TBCode = txtSearch.getText().trim();
				if (TBCode.length() > 0)
				{
					wd = CancelWaitDialog.ShowWait(Translation.Get("search"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
					{

						@Override
						public void isCanceld()
						{
							// TODO Auto-generated method stub

						}
					}, new Runnable()
					{

						@SuppressWarnings("unused")
						@Override
						public void run()
						{

							Trackable tb = null;
							ByRef<Trackable> ref = new ByRef<Trackable>(tb);
							int result = GroundspeakAPI.getTBbyTreckNumber(Config.GetAccessToken(true), TBCode, ref);

							if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
							{
								GL.that.Toast(ConnectionError.INSTANCE);
								wd.close();
								return;
							}
							if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
							{
								GL.that.Toast(ApiUnavailable.INSTANCE);
								wd.close();
								return;
							}

							result = GroundspeakAPI.getTBbyTbCode(Config.GetAccessToken(true), TBCode, new ByRef<Trackable>(tb));
							if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
							{
								GL.that.Toast(ConnectionError.INSTANCE);
								wd.close();
								return;
							}

							if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
							{
								GL.that.Toast(ApiUnavailable.INSTANCE);
								wd.close();
								return;
							}

							wd.close();

							// get RefValue
							tb = ref.get();

							if (tb != null)
							{
								TB_Details details = new TB_Details();
								details.Show(tb);
							}
							else
							{
								GL.that.Toast(Translation.Get("NoTbFound"));
							}

						}
					});

				}

				return true;
			}
		});
		searchBox.addLast(btnSearch, FIXED);

		listView = new V_ListView(new CB_RectF(0, 0, width, height - searchBox.getHeight()), "TB_LIstView");
		listView.setEmptyMsg(Translation.Get("TB_List_Empty"));
		listView.setBaseAdapter(lvAdapter);
		this.addChild(listView);

		Layout();
	}

	private void Layout()
	{
		searchBox.setY(this.height - searchBox.getHeight());
		listView.setHeight(this.height - searchBox.getHeight() - UI_Size_Base.that.getMargin());
		listView.notifyDataSetChanged();
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		Layout();
	}

	public void NotifyDataSetChanged()
	{
		listView.notifyDataSetChanged();
	}

	public void RefreshTbList()
	{
		wd = CancelWaitDialog.ShowWait(Translation.Get("RefreshInventory"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
		{

			@Override
			public void isCanceld()
			{
				// TODO Auto-generated method stub

			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				int result = -1;
				TbList searchList = new TbList();
				result = CB_Core.Api.GroundspeakAPI.getMyTbList(Config.GetAccessToken(), searchList);

				if (result == GroundspeakAPI.IO)
				{
					TrackableListDAO.clearDB();
					searchList.writeToDB();
					TrackableListView.that.reloadTB_List();
				}

				if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
				{
					GL.that.Toast(ConnectionError.INSTANCE);
				}

				if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
				{
					GL.that.Toast(ApiUnavailable.INSTANCE);
				}

				wd.close();
			}
		});
	}

	public class CustomAdapter implements Adapter
	{

		@Override
		public int getCount()
		{
			if (TB_List == null) return 0;
			return TB_List.size();
		}

		@Override
		public ListViewItemBase getView(final int position)
		{
			TrackableListViewItem v = new TrackableListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position,
					TB_List.get(position));
			v.setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					if (TB_Details.that == null) new TB_Details();
					TB_Details.that.Show(TB_List.get(position));
					return true;
				}
			});

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			return UiSizes.that.getCacheListItemRec().getHeight();
		}

	}

}

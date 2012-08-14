package CB_Core.GL_UI.Views;

import java.util.Iterator;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.Views.TrackListViewItem.RouteChangedListner;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.RouteOverlay.Track;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.math.Vector2;

public class TrackListView extends V_ListView
{
	public static CB_RectF ItemRec;
	BitmapFontCache emptyMsg;
	int selectedTrackItem;

	public static TrackListView that;

	public TrackListView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;

		ItemRec = new CB_RectF(0, 0, this.width, UiSizes.getButtonHeight() * 1.1f);

		this.setEmptyMsg(GlobalCore.Translations.Get("EmptyTrackList"));

		setBackground(SpriteCache.ListBack);

		this.setBaseAdapter(null);
		this.setBaseAdapter(new CustomAdapter());

	}

	@Override
	public void onShow()
	{
		this.notifyDataSetChanged();

		// platformConector.showView(ViewConst.TRACK_LIST_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public void onHide()
	{
		// platformConector.hideView(ViewConst.TRACK_LIST_VIEW);
	}

	@Override
	public void Initial()
	{
		super.Initial();
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	TrackListViewItem aktRouteItem;

	public class CustomAdapter implements Adapter
	{

		public CustomAdapter()
		{
		}

		@Override
		public int getCount()
		{

			if (GlobalCore.AktuelleRoute != null) return RouteOverlay.Routes.size() + 1;

			return RouteOverlay.Routes.size();
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			int index = position;
			if (GlobalCore.AktuelleRoute != null)
			{
				if (position == 0)
				{
					aktRouteItem = new TrackListViewItem(ItemRec, index, GlobalCore.AktuelleRoute, new RouteChangedListner()
					{

						@Override
						public void RouteChanged(Track route)
						{
							// Notify Map to Reload RouteOverlay
							RouteOverlay.RoutesChanged();
						}
					});
					aktRouteItem.setOnClickListener(onItemClickListner);
					aktRouteItem.setOnLongClickListener(mOnLongClickListener);

					return aktRouteItem;
				}
				position--;
			}

			TrackListViewItem v = new TrackListViewItem(ItemRec, index, RouteOverlay.Routes.get(position), new RouteChangedListner()
			{

				@Override
				public void RouteChanged(Track route)
				{
					// Notify Map to Reload RouteOverlay
					RouteOverlay.RoutesChanged();
				}
			});

			v.setOnClickListener(onItemClickListner);
			v.setOnLongClickListener(mOnLongClickListener);
			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			if (GlobalCore.AktuelleRoute != null && position == 1)
			{
				return ItemRec.getHeight() + ItemRec.getHalfHeight();
			}

			return ItemRec.getHeight();
		}

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		super.onTouchDown(x, y, pointer, button);

		// for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
		{
			// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
			GL_View_Base view = iterator.next();

			if (view instanceof TrackListViewItem)
			{
				if (view.contains(x, y))
				{
					((TrackListViewItem) view).lastItemTouchPos = new Vector2(x - view.getPos().x, y - view.getPos().y);
				}
			}
		}
		return true;
	}

	public void notifyActTrackChanged()
	{
		aktRouteItem.notifyTrackChanged(GlobalCore.AktuelleRoute);

		GL_Listener.glListener.renderOnce("ActTrackChanged");
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			selectedTrackItem = ((ListViewItemBase) v).getIndex();

			setSelection(selectedTrackItem);
			return true;
		}
	};

	private OnLongClickListener onItemLongClickListner = new OnLongClickListener()
	{

		@Override
		public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			// TODO Auto-generated method stub
			return false;
		}
	};

	public TrackListViewItem getSelectedItem()
	{
		return (TrackListViewItem) super.getSelectedItem();

	}

}

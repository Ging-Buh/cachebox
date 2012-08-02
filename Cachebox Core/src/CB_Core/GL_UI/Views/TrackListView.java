package CB_Core.GL_UI.Views;

import java.util.Iterator;

import CB_Core.GlobalCore;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Views.TrackListViewItem.RouteChangedListner;
import CB_Core.Map.RouteOverlay;
import CB_Core.Map.RouteOverlay.Track;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class TrackListView extends V_ListView
{
	public static CB_RectF ItemRec;
	BitmapFontCache emptyMsg;

	public TrackListView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		ItemRec = new CB_RectF(0, 0, this.width, UiSizes.getButtonHeight() * 1.1f);

		setBackground(SpriteCache.ListBack);

		this.setBaseAdapter(null);
		this.setBaseAdapter(new CustomAdapter());

	}

	@Override
	public void onShow()
	{
		platformConector.showView(ViewConst.TRACK_LIST_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.TRACK_LIST_VIEW);
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void render(SpriteBatch batch)
	{
		// if Track List empty, draw empty Msg
		if (RouteOverlay.Routes == null || RouteOverlay.Routes.size() == 0)
		{
			if (emptyMsg == null)
			{
				emptyMsg = new BitmapFontCache(Fonts.getBig());
				TextBounds bounds = emptyMsg.setText(GlobalCore.Translations.Get("EmptyTrackList"), 0, 0);
				emptyMsg.setPosition(this.halfWidth - (bounds.width / 2), this.halfHeight - (bounds.height / 2));
			}
			if (emptyMsg != null) emptyMsg.draw(batch, 0.5f);
		}
		else
		{
			super.render(batch);
		}
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	public class CustomAdapter implements Adapter
	{

		public CustomAdapter()
		{
		}

		@Override
		public int getCount()
		{
			return RouteOverlay.Routes.size();
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			TrackListViewItem v = new TrackListViewItem(ItemRec, position, RouteOverlay.Routes.get(position), new RouteChangedListner()
			{

				@Override
				public void RouteChanged(Track route)
				{
					// Notify Map to Reload RouteOverlay
					RouteOverlay.RoutesChanged();
				}
			});
			return v;
		}

		@Override
		public float getItemSize(int position)
		{
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

}

package CB_Core.GL_UI.Controls;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.H_ListView;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Main.Actions.QuickButton.QuickActions;
import CB_Core.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
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

		this.setBaseAdapter(new CustomAdapter());
		this.setDisposeFlag(false);

		registerSkinChangedEvent();
	}

	@Override
	public void Initial()
	{
		chkIsDrageble();
	}

	private void chkIsDrageble()
	{
		if (quickButtonList != null)
		{
			if (this.getMaxItemCount() < quickButtonList.size())
			{
				this.setDragable();
			}
			else
			{
				this.setUndragable();
			}
		}
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		// send Event to Buttons
		synchronized (childs)
		{
			for (GL_View_Base btn : this.childs)
			{
				btn.onTouchUp(x, y, pointer, button);
				if (btn.contains(x, y))
				{
					return btn.click(x, y, pointer, button);
				}
			}
		}
		return super.click(x, y, pointer, button);
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		synchronized (this.childs)
		{
			for (GL_View_Base btn : this.childs)
			{
				btn.onTouchDown(x, y, pointer, button);
			}
		}
		return super.onTouchDown(x, y, pointer, button);
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		synchronized (this.childs)
		{
			for (GL_View_Base btn : this.childs)
			{
				btn.onTouchUp(x, y, pointer, button);
			}
		}
		return super.onTouchUp(x, y, pointer, button);
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		synchronized (this.childs)
		{

			for (GL_View_Base btn : this.childs)
			{
				btn.onTouchDragged(x, y, pointer, KineticPan);
			}

		}
		return super.onTouchDragged(x, y, pointer, KineticPan);
	}

	ArrayList<QuickButtonItem> quickButtonList;

	public class CustomAdapter implements Adapter
	{

		public CustomAdapter()
		{
			readQuickButtonItemsList();

		}

		public long getItemId(int position)
		{
			return position;
		}

		public ListViewItemBase getView(int position)
		{

			if (quickButtonList == null) return null;

			QuickButtonItem v = quickButtonList.get(position);
			v.setSize(btnHeight, btnHeight);
			v.setY(btnYPos);// center btn on y direction
			return v;
		}

		@Override
		public int getCount()
		{
			return quickButtonList.size();
		}

		@Override
		public float getItemSize(int position)
		{
			return btnHeight;
		}
	}

	private void readQuickButtonItemsList()
	{
		if (quickButtonList == null)
		{
			String ConfigActionList = Config.settings.quickButtonList.getValue();
			String[] ConfigList = ConfigActionList.split(",");
			quickButtonList = QuickActions.getListFromConfig(ConfigList, btnHeight);
		}
		chkIsDrageble();
	}

	@Override
	protected void SkinIsChanged()
	{
		quickButtonList = null;
		readQuickButtonItemsList();
		setBackground(SpriteCache.ButtonBack);
		reloadItems();
		ListViewItemBackground.ResetBackground();
	}

}

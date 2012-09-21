package CB_Core.GL_UI.Main.Actions;

import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.FieldNotesView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_QuickFieldNote extends CB_ActionCommand
{

	public CB_Action_QuickFieldNote()
	{
		super("QuickFieldNote", AID_QUICK_FIELDNOTE);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(54);
	}

	private final int MI_FOUND = 0;
	private final int MI_NOT_FOUND = 1;

	@Override
	public void Execute()
	{
		Menu cm = new Menu("QuickFieldNote");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_FOUND:
					FieldNotesView.addNewFieldnote(1, true);
					if (FieldNotesView.that != null) FieldNotesView.that.notifyDataSetChanged();
					CachListChangedEventList.Call(); // damit der Status geändert wird
					return true;
				case MI_NOT_FOUND:
					FieldNotesView.addNewFieldnote(2, true);
					if (FieldNotesView.that != null) FieldNotesView.that.notifyDataSetChanged();
					CachListChangedEventList.Call(); // damit der Status geändert wird
					return true;
				}
				return false;
			}
		});

		cm.addItem(MI_FOUND, "found", SpriteCache.getThemedSprite("log0icon"));
		cm.addItem(MI_NOT_FOUND, "DNF", SpriteCache.getThemedSprite("log1icon"));

		cm.show();

	}

}

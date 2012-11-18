package CB_Core.GL_UI.Main.Actions;

import CB_Core.GlobalCore;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.PopUps.PopUp_Base;
import CB_Core.GL_UI.Controls.PopUps.QuickFieldNoteFeedbackPopUp;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.FieldNotesView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_QuickFieldNote extends CB_ActionCommand
{

	public CB_Action_QuickFieldNote()
	{
		super("QuickFieldNote", MenuID.AID_QUICK_FIELDNOTE);
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

	@Override
	public void Execute()
	{
		Menu cm = new Menu("QuickFieldNote");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_QUICK_FOUND:
					FieldNotesView.addNewFieldnote(1, true);
					if (FieldNotesView.that != null) FieldNotesView.that.notifyDataSetChanged();
					CachListChangedEventList.Call(); // damit der Status geändert wird
					// damit die Icons in der Map aktualisiert werden
					SelectedCacheEventList.Call(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());
					QuickFieldNoteFeedbackPopUp pop = new QuickFieldNoteFeedbackPopUp(true);
					pop.show(PopUp_Base.SHOW_TIME_SHORT);
					platformConector.vibrate();
					return true;
				case MenuID.MI_QUICK_NOT_FOUND:
					FieldNotesView.addNewFieldnote(2, true);
					if (FieldNotesView.that != null) FieldNotesView.that.notifyDataSetChanged();
					CachListChangedEventList.Call(); // damit der Status geändert wird
					// damit die Icons in der Map aktualisiert werden
					SelectedCacheEventList.Call(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());
					QuickFieldNoteFeedbackPopUp pop2 = new QuickFieldNoteFeedbackPopUp(false);
					pop2.show(PopUp_Base.SHOW_TIME_SHORT);
					platformConector.vibrate();
					return true;
				}
				return false;
			}
		});

		cm.addItem(MenuID.MI_QUICK_FOUND, "found", SpriteCache.getThemedSprite("log0icon"));
		cm.addItem(MenuID.MI_QUICK_NOT_FOUND, "DNF", SpriteCache.getThemedSprite("log1icon"));

		cm.show();

	}

}

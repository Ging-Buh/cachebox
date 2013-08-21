package CB_UI.GL_UI.Main.Actions;

import CB_Core.Enums.LogTypes;
import CB_Core.Events.CachListChangedEventList;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.GL_UI.Controls.PopUps.QuickFieldNoteFeedbackPopUp;
import CB_UI.GL_UI.Views.FieldNotesView;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;

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
		return SpriteCacheBase.Icons.get(IconName.fieldNote_54.ordinal());
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
					FieldNotesView.addNewFieldnote(LogTypes.found, true);
					if (FieldNotesView.that != null) FieldNotesView.that.notifyDataSetChanged();
					CachListChangedEventList.Call(); // damit der Status geändert wird
					// damit die Icons in der Map aktualisiert werden
					SelectedCacheEventList.Call(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
					QuickFieldNoteFeedbackPopUp pop = new QuickFieldNoteFeedbackPopUp(true);
					pop.show(PopUp_Base.SHOW_TIME_SHORT);
					platformConector.vibrate();
					return true;
				case MenuID.MI_QUICK_NOT_FOUND:
					FieldNotesView.addNewFieldnote(LogTypes.didnt_find, true);
					if (FieldNotesView.that != null) FieldNotesView.that.notifyDataSetChanged();
					CachListChangedEventList.Call(); // damit der Status geändert wird
					// damit die Icons in der Map aktualisiert werden
					SelectedCacheEventList.Call(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
					QuickFieldNoteFeedbackPopUp pop2 = new QuickFieldNoteFeedbackPopUp(false);
					pop2.show(PopUp_Base.SHOW_TIME_SHORT);
					platformConector.vibrate();
					return true;
				}
				return false;
			}
		});

		cm.addItem(MenuID.MI_QUICK_FOUND, "found", SpriteCacheBase.getThemedSprite("log0icon"));
		cm.addItem(MenuID.MI_QUICK_NOT_FOUND, "DNF", SpriteCacheBase.getThemedSprite("log1icon"));

		cm.Show();

	}

}

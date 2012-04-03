package CB_Core.GL_UI.Main;

import java.util.ArrayList;

import CB_Core.GL_UI.ButtonSprites;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.GL_View_Base.OnLongClickListener;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;

public class CB_Button extends Button implements OnClickListener, OnLongClickListener
{

	ArrayList<CB_ActionButton> mButtonActions;

	public CB_Button(CB_RectF rec, String Name, ArrayList<CB_ActionButton> ButtonActions)
	{
		super(rec, Name);
		mButtonActions = ButtonActions;
		this.setOnClickListener(this);
		this.setOnLongClickListener(this);
	}

	public CB_Button(CB_RectF rec, String Name)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		this.setOnClickListener(this);
		this.setOnLongClickListener(this);
	}

	public CB_Button(CB_RectF rec, String Name, ButtonSprites sprites)
	{
		super(rec, Name);
		mButtonActions = new ArrayList<CB_ActionButton>();
		this.setOnClickListener(this);
		this.setOnLongClickListener(this);
		this.setButtonSprites(sprites);
	}

	public void addAction(CB_ActionButton Action)
	{
		mButtonActions.add(Action);
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
	{
		// GL_MsgBox.Show("Button " + Me.getName() + " recivet a LongClick Event");
		// Wenn diesem Button mehrere Actions zugeordnet sind dann wird nach einem Lang-Click ein Menü angezeigt aus dem eine dieser
		// Actions gewählt werden kann

		if (mButtonActions.size() > 1)
		{
			Menu cm = new Menu("Name");
			cm.setItemClickListner(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					for (CB_ActionButton ba : mButtonActions)
					{
						CB_Action action = ba.getAction();
						if (action == null) continue;
						int mId = ((MenuItem) v).getMenuItemId();
						if (mId == action.getId())
						{
							// Action ausführen
							action.CallExecute();
							break;
						}
					}
					return true;
				}
			});
			for (CB_ActionButton ba : mButtonActions)
			{
				CB_Action action = ba.getAction();
				if (action == null) continue;
				MenuItem mi = cm.addItem(action.getId(), action.getName());
				mi.setEnabled(action.getEnabled());
				mi.setCheckable(action.getIsCheckable());
				mi.setChecked(action.getIsChecked());
				mi.setIcon(action.getIcon());
			}
			cm.show();
		}
		else if (mButtonActions.size() == 1)
		{
			// nur eine Action dem Button zugeordnet -> diese Action gleich ausführen
			CB_ActionButton ba = mButtonActions.get(0);
			CB_Action action = ba.getAction();
			if (action != null) action.CallExecute();
		}

		return true;
	}

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
	{
		// Einfacher Click -> Default Action starten
		for (CB_ActionButton ba : mButtonActions)
		{
			if (ba.isDefaultAction())
			{
				CB_Action action = ba.getAction();
				if (action != null)
				{
					action.CallExecute();
					break;
				}
			}
		}
		return true;
	}

}

package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Main.CB_TabView;
import CB_Core.GL_UI.Main.TabMainView;

public abstract class CB_Action_ShowView extends CB_Action
{
	protected CB_TabView tab;
	protected TabMainView tabMainView;

	public CB_Action_ShowView(String translationId, int id)
	{
		super(translationId, id);
		tab = null;
		tabMainView = null;
	}

	public abstract CB_View_Base getView();

	public void setTab(TabMainView tabMainView, CB_TabView tab)
	{
		this.tab = tab;
		this.tabMainView = tabMainView;
	}

	public boolean HasContextMenu()
	{
		return false;
	}

	// zeigt, falls vorhanden das Contectmenü dieser View an und gibt true zurück
	// oder gibt false zurück, falls kein Contextmenü vorhanden ist
	public boolean ShowContextMenu()
	{
		return false;
	}
}

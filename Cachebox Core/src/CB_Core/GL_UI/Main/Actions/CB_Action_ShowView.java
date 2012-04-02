package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.Main.CB_TabView;
import CB_Core.GL_UI.Main.TabMainView;

public class CB_Action_ShowView extends CB_Action
{
	protected CB_TabView tab;
	protected TabMainView tabMainView;

	public CB_Action_ShowView(String name, int id)
	{
		super(name, id);
		tab = null;
		tabMainView = null;
	}

	public void setTab(TabMainView tabMainView, CB_TabView tab)
	{
		this.tab = tab;
		this.tabMainView = tabMainView;
	}

}

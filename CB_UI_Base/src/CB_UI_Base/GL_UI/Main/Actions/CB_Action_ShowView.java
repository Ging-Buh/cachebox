package CB_UI_Base.GL_UI.Main.Actions;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.CB_TabView;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.GL_UI.Menu.Menu;

public abstract class CB_Action_ShowView extends CB_Action
{
	protected CB_TabView tab;
	protected MainViewBase tabMainView;

	public CB_Action_ShowView(String translationId, int id)
	{
		super(translationId, id);
		tab = null;
		tabMainView = null;
	}

	public CB_Action_ShowView(String translationId, String translationExtention, int id)
	{
		super(translationId, translationExtention, id);
		tab = null;
		tabMainView = null;
	}

	public abstract CB_View_Base getView();

	public void setTab(MainViewBase tabMainView, CB_TabView tab)
	{
		this.tab = tab;
		this.tabMainView = tabMainView;
	}

	public boolean HasContextMenu()
	{
		return false;
	}

	/**
	 * zeigt, falls vorhanden das Contectmenü dieser View an
	 * 
	 * @return gibt true zurück // oder gibt false zurück, falls kein Contextmenü vorhanden ist
	 */
	public final boolean ShowContextMenu()
	{
		Menu cm = getContextMenu();

		if (cm == null) return false;

		cm.Show();
		return true;
	}

	/**
	 * gibt das ContextMenu dieser View zurück
	 * 
	 * @return
	 */
	public Menu getContextMenu()
	{
		return null;
	}
}

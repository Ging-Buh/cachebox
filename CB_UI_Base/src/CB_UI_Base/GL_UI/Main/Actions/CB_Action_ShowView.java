package CB_UI_Base.GL_UI.Main.Actions;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.CB_TabView;
import CB_UI_Base.GL_UI.Main.MainViewBase;
import CB_UI_Base.GL_UI.Menu.Menu;

public abstract class CB_Action_ShowView extends CB_Action {
	protected CB_TabView tab;
	protected MainViewBase tabMainView;

	public CB_Action_ShowView(String translationId, int id) {
		super(translationId, id);
		tab = null;
		tabMainView = null;
	}

	public CB_Action_ShowView(String translationId, String translationExtention, int id) {
		super(translationId, translationExtention, id);
		tab = null;
		tabMainView = null;
	}

	/**
	 * returns the instance of the view (from TabMainView)<br> 
	 * TabMainView.<view><br>
	 * @return
	 */
	public abstract CB_View_Base getView();

	/**
	 * defines in which tab (left or right) the view has to be shown 
	 * @param tabMainView
	 * @param tab
	 */
	public void setTab(MainViewBase tabMainView, CB_TabView tab) {
		this.tab = tab;
		this.tabMainView = tabMainView;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasContextMenu() {
		if (getContextMenu() == null)
			return false;
		else
			return true;
	}

	/**
	 * zeigt, falls vorhanden das Contextmenü dieser View an
	 * 
	 * @return gibt true zurück // oder gibt false zurück, falls kein Contextmenü vorhanden ist
	 */
	public final boolean ShowContextMenu() {
		Menu cm = getContextMenu();
		if (cm == null)
			return false;
		else {
			cm.Show();
			return true;
		}
	}

	/**
	 * gibt das ContextMenu dieser View zurück
	 * 
	 * @return
	 */
	public Menu getContextMenu() {
		return null;
	}
}

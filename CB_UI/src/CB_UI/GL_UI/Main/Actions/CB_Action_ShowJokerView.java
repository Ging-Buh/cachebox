package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GlobalCore;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.JokerView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_ShowJokerView extends CB_Action_ShowView {

    public CB_Action_ShowJokerView() {
	super("joker", MenuID.AID_SHOW_JOKERS);
    }

    @Override
    public void Execute() {
	if ((TabMainView.jokerView == null) && (tabMainView != null) && (tab != null))
	    TabMainView.jokerView = new JokerView(tab.getContentRec(), "JokerView");

	if ((TabMainView.jokerView != null) && (tab != null))
	    tab.ShowView(TabMainView.jokerView);
    }

    @Override
    public boolean getEnabled() {
	if (GlobalCore.getSelectedCache() == null)
	    return false;
	if (GlobalCore.getSelectedCache().getGcCode().startsWith("GC")) // GC-Joker nur zulässig wenn es ein Cache von geocaching.com ist
	{
	    return GlobalCore.JokerisOnline();
	} else {
	    return (false);
	}
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.Icons.get(IconName.jokerPhone_25.ordinal());
    }

    @Override
    public CB_View_Base getView() {
	return TabMainView.jokerView;
    }
}

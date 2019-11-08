package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.ViewID;

public class Action_PlatformActivity extends AbstractAction {
    private ViewID viewID;
    private Sprite sprite;

    public Action_PlatformActivity(String Name, int ID, ViewID viewID, Sprite icon) {
        super(Name, ID);
        this.viewID = viewID;
        this.sprite = icon;
    }

    @Override
    public void Execute() {
        PlatformUIBase.showView(viewID, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public Sprite getIcon() {
        return sprite;
    }
}

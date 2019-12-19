package de.droidcachebox.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.ViewID;

public class PlatformAction extends AbstractAction {
    private ViewID viewID;
    private Sprite sprite;

    public PlatformAction(String Name, int ID, ViewID viewID, Sprite icon) {
        super(Name, ID);
        this.viewID = viewID;
        this.sprite = icon;
    }

    @Override
    public void execute() {
        PlatformUIBase.showView(viewID, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public Sprite getIcon() {
        return sprite;
    }
}

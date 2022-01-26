package de.droidcachebox.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.menuBtn1.contextmenus.executes.ExportGPX;
import de.droidcachebox.menu.menuBtn1.contextmenus.executes.Import;
import de.droidcachebox.menu.menuBtn1.contextmenus.executes.ImportGCPosition;
import de.droidcachebox.menu.menuBtn1.contextmenus.executes.Import_GSAK;
import de.droidcachebox.menu.menuBtn1.contextmenus.executes.SearchOverNameOwnerGcCode;
import de.droidcachebox.menu.menuBtn1.contextmenus.executes.UpdateCachesState;

public class ShowImportMenu extends AbstractShowAction {
    public static final int MI_IMPORT_CBS = 189;
    public static final int MI_IMPORT_GCV = 192;

    public ShowImportMenu() {
        super("ImportMenu");
    }

    @Override
    public void execute() {
        getContextMenu().show();
    }

    @Override
    public CB_View_Base getView() {
        // don't return a view.
        // show menu direct.
        GL.that.runOnGL(this::execute);
        return null;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.cacheListIcon.name());
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu icm = new Menu("ImportMenuTitle");
        icm.addMenuItem("chkState", null, () -> GL.that.postAsync(() -> new UpdateCachesState().execute()));
        icm.addMenuItem("moreImport", null, () -> GL.that.postAsync(() -> new Import().show()));
        icm.addMenuItem("importCachesOverPosition", null, () -> new ImportGCPosition().show());
        icm.addMenuItem("API_IMPORT_NAME_OWNER_CODE", null, SearchOverNameOwnerGcCode::showInstance);
        icm.addMenuItem("GCVoteRatings", null, () -> new Import(MI_IMPORT_GCV).show());
        icm.addMenuItem("GSAKMenuImport", null, () -> new Import_GSAK().show());
        icm.addDivider();
        icm.addMenuItem("GPX_EXPORT", null, () -> new ExportGPX().exportGPX());
        return icm;
    }
}

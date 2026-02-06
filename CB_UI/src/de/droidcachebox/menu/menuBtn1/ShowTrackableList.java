package de.droidcachebox.menu.menuBtn1;

import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Date;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.executes.TrackableListView;
import de.droidcachebox.menu.menuBtn4.executes.TemplateFormatter;
import de.droidcachebox.settings.Settings;

public class ShowTrackableList extends AbstractShowAction {

    private TrackableListView trackableListView;

    public ShowTrackableList() {
        super("TBList");
    }

    @Override
    public void execute() {
        if (trackableListView == null) trackableListView = new TrackableListView();
        ViewManager.leftTab.showView(trackableListView);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public void viewIsHiding() {
        trackableListView = null;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.tbListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return trackableListView;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        if (trackableListView == null) return null;
        final Menu cm = new Menu("TrackableListViewContextMenuTitle");
        if (!isAccessTokenInvalid()) {
            cm.addMenuItem("SearchTB", Sprites.getSprite(IconName.lupe.name()), trackableListView::searchTB);
            cm.addMenuItem("RefreshInventory", null, trackableListView::refreshTbList);
        }
        cm.addMenuItem("all_note", "", Sprites.getSprite(IconName.TBNOTE.name()), (v, x, y, pointer, button) -> {
            cm.close();
            trackableListView.logTBs(((MenuItem) v).getTitle(), LogType.note.gsLogTypeId, TemplateFormatter.replaceTemplate(Settings.AddNoteTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_visit", "", Sprites.getSprite(IconName.TBVISIT.name()), (v, x, y, pointer, button) -> {
            cm.close();
            trackableListView.logTBs(((MenuItem) v).getTitle(), LogType.visited.gsLogTypeId, TemplateFormatter.replaceTemplate(Settings.VisitedTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_dropped", "", Sprites.getSprite(IconName.TBDROP.name()), (v, x, y, pointer, button) -> {
            cm.close();
            trackableListView.logTBs(((MenuItem) v).getTitle(), LogType.dropped_off.gsLogTypeId, TemplateFormatter.replaceTemplate(Settings.DroppedTemplate.getValue(), new Date()));
            trackableListView.refreshTbList();
            return true;
        });
        return cm;
    }

}
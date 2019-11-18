package de.droidcachebox.main.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.gdx.main.AbstractAction;

import java.util.LinkedList;

import static de.droidcachebox.gdx.main.MenuID.AID_TranslateDescription;

public class TranslateDescription extends AbstractAction {
    private static TranslateDescription translateDescription;

    private TranslateDescription() {
        super("TranslateDescription", AID_TranslateDescription);
    }

    public static TranslateDescription getInstance() {
        if (translateDescription == null) translateDescription = new TranslateDescription();
        return translateDescription;
    }

    @Override
    public void Execute() {
        final LinkedList<String> NonLocalImages = new LinkedList<>();
        final LinkedList<String> NonLocalImagesUrl = new LinkedList<>();
        Cache cache = GlobalCore.getSelectedCache();
        NonLocalImages.clear();
        NonLocalImagesUrl.clear();
        String cachehtml = Database.getShortDescription(cache) + Database.getDescription(cache);
        String html = DescriptionImageGrabber.resolveImages(cache, cachehtml, false, NonLocalImages, NonLocalImagesUrl);
        String header = "<!DOCTYPE html><html><head><meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" /></head><body>";
        html = header + html;

        // add 2 empty lines so that the last line of description can be selected with the markers
        // add trailer
        html += "</br></br>" + "</body></html>";

        PlatformUIBase.getClipboard().setContents(html);
        PlatformUIBase.callUrl("https://translate.google.com/translate?sl=auto&tl=" + "de"); //  + "&u=https%3A%2F%2Fcoord.info%2F" + GlobalCore.getSelectedCache().getGcCode());
    }

    @Override
    public Sprite getIcon() {
        return null;
    }
}

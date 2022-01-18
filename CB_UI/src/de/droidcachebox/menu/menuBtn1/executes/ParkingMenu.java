package de.droidcachebox.menu.menuBtn1.executes;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

public class ParkingMenu extends ButtonDialog {

    public ParkingMenu() {
        super("", Translation.get("My_Parking_Area_Title"), MsgBoxButton.Cancel, MsgBoxIcon.None);
        newContentBox();

        CB_RectF imageSize = new CB_RectF(0, 0, innerWidth / 3);
        ImageButton btnSetGPS = new ImageButton(imageSize, "btSetGPS");
        ImageButton btnSelectWP = new ImageButton(imageSize, "btSelectWP");
        ImageButton btnDeleteP = new ImageButton(imageSize, "btDeleteP");

        btnSetGPS.setImage(Sprites.getSpriteDrawable("my-parking-set"));
        btnSelectWP.setImage(Sprites.getSpriteDrawable("my-parking-wp"));
        btnDeleteP.setImage(Sprites.getSpriteDrawable("my-parking-delete"));

        CB_Label lblSetGPS = new CB_Label();
        CB_Label lblSelectWP = new CB_Label();
        CB_Label lblDeleteP = new CB_Label();

        lblSetGPS.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        lblSelectWP.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        lblDeleteP.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

        lblSelectWP.setWrappedText(Translation.get("My_Parking_Area_select"));
        lblSetGPS.setWrappedText(Translation.get("My_Parking_Area_Add"));
        lblDeleteP.setWrappedText(Translation.get("My_Parking_Area_Del"));

        contentBox.addNext(btnSetGPS);
        contentBox.addNext(btnSelectWP);
        contentBox.addLast(btnDeleteP);

        contentBox.addNext(lblSetGPS);
        contentBox.addNext(lblSelectWP);
        contentBox.addLast(lblDeleteP);

        readyContentBox();

        // chk disable select and delete Button
        synchronized (CBDB.getInstance().cacheList) {
            Cache cache = CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList("CBPark");
            if (cache == null) {
                btnSelectWP.disable();
                btnDeleteP.disable();
            }
        }

        btnSetGPS.setClickHandler((view, x, y, pointer, button) -> {
            Settings.ParkingLatitude.setValue(Locator.getInstance().getLatitude());
            Settings.ParkingLongitude.setValue(Locator.getInstance().getLongitude());
            Settings.getInstance().acceptChanges();
            CacheListChangedListeners.getInstance().cacheListChanged();
            close();
            return true;
        });

        btnSelectWP.setClickHandler((view, x, y, pointer, button) -> {
            synchronized (CBDB.getInstance().cacheList) {
                Cache cache = CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList("CBPark");
                if (cache != null)
                    GlobalCore.setSelectedCache(cache);
            }
            close();
            return true;
        });

        btnDeleteP.setClickHandler((view, x, y, pointer, button) -> {
            Settings.ParkingLatitude.setValue(0.0);
            Settings.ParkingLongitude.setValue(0.0);
            Settings.getInstance().acceptChanges();
            CacheListChangedListeners.getInstance().cacheListChanged();
            close();
            return true;
        });
    }
}

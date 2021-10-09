package de.droidcachebox.menu.menuBtn1.executes;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.Linearlayout;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

public class ParkingDialog extends ButtonDialog {

    private SizeF msgBoxContentSize;
    private ImageButton btSetGPS, btSelectWP, btDeleteP;
    private CB_Label lblSetGPS, lblSelectWP, lblDeleteP;

    public ParkingDialog() {
        super((calcMsgBoxSize("Text", true, true, false, false)).getBounds().asFloat(), "Parking-Dialog", "", Translation.get("My_Parking_Area_Title"), MsgBoxButton.Cancel, null, null);

        msgBoxContentSize = getContentSize();
        // initial VariableField
        float textFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

        float innerWidth = msgBoxContentSize.getWidth();

        Linearlayout layout = new Linearlayout(innerWidth, "layout");
        layout.setX(0);
        // layout.setBackground(new ColorDrawable(Color.GREEN));

        CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UiSizes.getInstance().getButtonHeight() * 2);

        btSetGPS = new ImageButton(MTBRec, "btSetGPS");
        btSelectWP = new ImageButton(MTBRec, "btSelectWP");
        btDeleteP = new ImageButton(MTBRec, "btDeleteP");

        btSetGPS.setImage(Sprites.getSpriteDrawable("my-parking-set"));
        btSelectWP.setImage(Sprites.getSpriteDrawable("my-parking-wp"));
        btDeleteP.setImage(Sprites.getSpriteDrawable("my-parking-delete"));

        btSetGPS.setX(0);
        btSelectWP.setX(btSetGPS.getMaxX());
        btDeleteP.setX(btSelectWP.getMaxX());

        Box box = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getInstance().getButtonHeight() * 2), "");

        box.addChild(btSetGPS);
        box.addChild(btSelectWP);
        box.addChild(btDeleteP);

        layout.addChild(box);

        Box box2 = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getInstance().getButtonHeight() * 2), "");

        lblSetGPS = new CB_Label(btSetGPS.scaleCenter(0.8f));
        lblSelectWP = new CB_Label(btSelectWP.scaleCenter(0.8f));
        lblDeleteP = new CB_Label(btDeleteP.scaleCenter(0.8f));

        lblSetGPS.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        lblSelectWP.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);
        lblDeleteP.setFont(Fonts.getSmall()).setHAlignment(HAlignment.CENTER);

        lblSelectWP.setWrappedText(Translation.get("My_Parking_Area_select"));
        lblSetGPS.setWrappedText(Translation.get("My_Parking_Area_Add"));
        lblDeleteP.setWrappedText(Translation.get("My_Parking_Area_Del"));

        box2.addChild(lblSetGPS);
        box2.addChild(lblSelectWP);
        box2.addChild(lblDeleteP);

        layout.addChild(box2);

        this.addChild(layout);

        // chk disable select and delete Button
        synchronized (CBDB.getInstance().cacheList) {
            Cache cache = CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList("CBPark");
            if (cache == null) {
                btSelectWP.disable();
                btDeleteP.disable();
            }
        }

        Size msgBoxSize = MsgBox.calcMsgBoxSize("teste", true, true, false);
        msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight() - (textFieldHeight / 2));
        this.setSize(msgBoxSize.asFloat());

        btSetGPS.setClickHandler((view, x, y, pointer, button) -> {

            Settings.ParkingLatitude.setValue(Locator.getInstance().getLatitude());
            Settings.ParkingLongitude.setValue(Locator.getInstance().getLongitude());
            ViewManager.that.acceptChanges();
            CacheListChangedListeners.getInstance().cacheListChanged();

            close();
            return true;
        });

        btSelectWP.setClickHandler((view, x, y, pointer, button) -> {
            synchronized (CBDB.getInstance().cacheList) {
                Cache cache = CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList("CBPark");
                if (cache != null)
                    GlobalCore.setSelectedCache(cache);
            }
            close();
            return true;
        });

        btDeleteP.setClickHandler((view, x, y, pointer, button) -> {
            Settings.ParkingLatitude.setValue(0.0);
            Settings.ParkingLongitude.setValue(0.0);
            ViewManager.that.acceptChanges();
            CacheListChangedListeners.getInstance().cacheListChanged();
            close();
            return true;
        });

    }

    @Override
    public void dispose() {
        msgBoxContentSize = null;
        if (btSetGPS != null)
            btSetGPS.dispose();
        if (btSelectWP != null)
            btSelectWP.dispose();
        if (btDeleteP != null)
            btDeleteP.dispose();
        if (lblSetGPS != null)
            lblSetGPS.dispose();
        if (lblSelectWP != null)
            lblSelectWP.dispose();
        if (lblDeleteP != null)
            lblDeleteP.dispose();
        super.dispose();
        btSetGPS = null;
        btSelectWP = null;
        btDeleteP = null;
        lblSetGPS = null;
        lblSelectWP = null;
        lblDeleteP = null;
    }

}

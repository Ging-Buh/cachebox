package de.droidcachebox.menu.menuBtn1.executes;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

public class DeleteDialog extends ButtonDialog {

    public DeleteDialog() {
        super("", Translation.get("DeleteCaches"), MsgBoxButton.Cancel, MsgBoxIcon.None);
        newContentBox();
        CB_RectF imageSize = new CB_RectF(0, 0, innerWidth / 3);
        ImageButton btDelFilter = new ImageButton(imageSize, "btSetGPS");
        btDelFilter.setImage(Sprites.getSpriteDrawable("delete-filter"));
        btDelFilter.setClickHandler((v, x, y, pointer, button) -> {
            close();
            new CancelWaitDialog(Translation.get("DelActFilter"), new WorkAnimation(),
                    deletion(FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue()))).show();
            return true;
        });

        ImageButton btDelArchived = new ImageButton(imageSize, "btSelectWP");
        btDelArchived.setImage(Sprites.getSpriteDrawable("delete-archived"));
        btDelArchived.setClickHandler((view, x, y, pointer, button) -> {
            close();
            new CancelWaitDialog(Translation.get("DelArchived"), new WorkAnimation(),
                    deletion("Archived=1")).show();
            return true;
        });

        ImageButton btDelFounds = new ImageButton(imageSize, "btDeleteP");
        btDelFounds.setImage(Sprites.getSpriteDrawable("delete-founds"));
        btDelFounds.setClickHandler((view, x, y, pointer, button) -> {
            close();
            new CancelWaitDialog(Translation.get("DelFound"), new WorkAnimation(),
                    deletion("Found=1")).show();
            return true;
        });

        CB_Label lblDelFilter = new CB_Label(Translation.get("DelActFilter"), Fonts.getSmall(), null, WrapType.WRAPPED).setHAlignment(HAlignment.CENTER);
        CB_Label lblDelArchived = new CB_Label(Translation.get("DelArchived"), Fonts.getSmall(), null, WrapType.WRAPPED).setHAlignment(HAlignment.CENTER);
        CB_Label lblDelFounds = new CB_Label(Translation.get("DelFound"), Fonts.getSmall(), null, WrapType.WRAPPED).setHAlignment(HAlignment.CENTER);

        contentBox.addNext(btDelFilter);
        contentBox.addNext(btDelArchived);
        contentBox.addLast(btDelFounds);

        contentBox.addNext(lblDelFilter);
        contentBox.addNext(lblDelArchived);
        contentBox.addLast(lblDelFounds);

        readyContentBox();
    }

    /**
     * @param what the sql where selection string
     * @return the deletion method
     */
    private RunAndReady deletion(String what) {
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        return new RunAndReady() {
            @Override
            public void ready() {
                if (!isCanceled.get()) {
                    // show all
                    FilterInstances.setLastFilter(new FilterProperties());
                    EditFilterSettings.applyFilter(FilterInstances.getLastFilter());
                }
            }

            @Override
            public void run() {
                long nun = new CachesDAO().delete(
                        what,
                        Settings.SpoilerFolder.getValue(), Settings.SpoilerFolderLocal.getValue(),
                        Settings.DescriptionImageFolder.getValue(),
                        Settings.DescriptionImageFolderLocal.getValue(),
                        isCanceled);
                if (!isCanceled.get()) {
                    GL.that.toast(Translation.get("DeletedCaches", String.valueOf(nun)));
                    LogsTableDAO.getInstance().ClearOrphanedLogs();
                    WaypointDAO.getInstance().clearOrphanedWaypoints();
                }
            }

            @Override
            public void setIsCanceled() {
                isCanceled.set(true);
            }

        };
    }
}

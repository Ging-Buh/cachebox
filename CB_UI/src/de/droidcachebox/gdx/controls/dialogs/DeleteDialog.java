package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.Config;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.LogDAO;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog.IcancelListener;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;

public class DeleteDialog extends ButtonDialog {
    private SizeF msgBoxContentSize;
    private ImageButton btDelFilter, btDelArchived, btDelFounds;
    private CB_Label lblDelFilter, lblDelArchived, lblDelFounds;
    private CancelWaitDialog wd;

    public DeleteDialog() {
        super((calcMsgBoxSize("Text", true, true, false, false)).getBounds().asFloat(), "Delete-Dialog", "", Translation.get("DeleteCaches"), MessageBoxButtons.Cancel, null, null);

        msgBoxContentSize = getContentSize();

        float innerWidth = msgBoxContentSize.getWidth();

        CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UiSizes.getInstance().getButtonHeight() * 2);

        btDelFilter = new ImageButton(MTBRec, "btSetGPS");
        btDelArchived = new ImageButton(MTBRec, "btSelectWP");
        btDelFounds = new ImageButton(MTBRec, "btDeleteP");

        btDelFilter.setImage(Sprites.getSpriteDrawable("delete-filter"));
        btDelArchived.setImage(Sprites.getSpriteDrawable("delete-archived"));
        btDelFounds.setImage(Sprites.getSpriteDrawable("delete-founds"));

        lblDelFilter = new CB_Label(Translation.get("DelActFilter"), Fonts.getSmall(), null, WrapType.WRAPPED).setHAlignment(HAlignment.CENTER);
        lblDelArchived = new CB_Label(Translation.get("DelArchived"), Fonts.getSmall(), null, WrapType.WRAPPED).setHAlignment(HAlignment.CENTER);
        lblDelFounds = new CB_Label(Translation.get("DelFound"), Fonts.getSmall(), null, WrapType.WRAPPED).setHAlignment(HAlignment.CENTER);

        Box box = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getInstance().getButtonHeight()), "");
        box.initRow(BOTTOMUP);
        box.addNext(lblDelFilter);
        box.addNext(lblDelArchived);
        box.addLast(lblDelFounds);
        box.addNext(btDelFilter);
        box.addNext(btDelArchived);
        box.addLast(btDelFounds);
        box.setHeight(box.getHeightFromBottom());
        this.addChild(box);

        this.setHeight(box.getHeight() + this.mFooterHeight + this.mTitleHeight + 3 * margin);

        btDelFilter.setClickHandler((v, x, y, pointer, button) -> {
            close();

            wd = CancelWaitDialog.ShowWait(Translation.get("DelActFilter"), () -> {

            }, new ICancelRunnable() {

                @Override
                public void run() {
                    long nun = CacheListDAO.getInstance().deleteFiltered(FilterInstances.getLastFilter().getSqlWhere(CB_Core_Settings.GcLogin.getValue()), CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(),
                            CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());
                    cleanupLogs();
                    cleanupWaypoints();
                    wd.close();

                    // reset Filter
                    FilterInstances.setLastFilter(new FilterProperties());
                    EditFilterSettings.applyFilter(FilterInstances.getLastFilter());// all Caches

                    String msg = Translation.get("DeletedCaches", String.valueOf(nun));
                    GL.that.Toast(msg);
                }

                @Override
                public boolean doCancel() {
                    // TODO Handle Cancel
                    return false;
                }
            });
            return true;
        });

        btDelArchived.setClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                close();

                wd = CancelWaitDialog.ShowWait(Translation.get("DelArchived"), new IcancelListener() {

                    @Override
                    public void isCanceled() {

                    }
                }, new ICancelRunnable() {

                    @Override
                    public void run() {
                        long nun = CacheListDAO.getInstance().deleteArchived(Config.SpoilerFolder.getValue(), Config.SpoilerFolderLocal.getValue(), Config.DescriptionImageFolder.getValue(), Config.DescriptionImageFolderLocal.getValue());

                        cleanupLogs();
                        cleanupWaypoints();
                        wd.close();

                        EditFilterSettings.applyFilter(FilterInstances.getLastFilter());

                        String msg = Translation.get("DeletedCaches", String.valueOf(nun));
                        GL.that.Toast(msg);
                    }

                    @Override
                    public boolean doCancel() {
                        // TODO Handle Cancel
                        return false;
                    }
                });
                return true;
            }
        });

        btDelFounds.setClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                close();

                wd = CancelWaitDialog.ShowWait(Translation.get("DelFound"), new IcancelListener() {

                    @Override
                    public void isCanceled() {

                    }
                }, new ICancelRunnable() {

                    @Override
                    public void run() {
                        long nun = CacheListDAO.getInstance().deleteFinds(Config.SpoilerFolder.getValue(), Config.SpoilerFolderLocal.getValue(), Config.DescriptionImageFolder.getValue(), Config.DescriptionImageFolderLocal.getValue());
                        cleanupLogs();
                        cleanupWaypoints();
                        wd.close();

                        EditFilterSettings.applyFilter(FilterInstances.getLastFilter());

                        String msg = Translation.get("DeletedCaches", String.valueOf(nun));
                        GL.that.Toast(msg);
                    }

                    @Override
                    public boolean doCancel() {
                        // TODO Handle Cancel
                        return false;
                    }
                });
                return true;
            }
        });

    }

    private void cleanupLogs() {
        LogDAO dao = new LogDAO();
        dao.ClearOrphanedLogs();
    }

    private void cleanupWaypoints() {
        WaypointDAO dao = new WaypointDAO();
        dao.ClearOrphanedWaypoints();
    }

    @Override
    public void dispose() {
        msgBoxContentSize = null;
        btDelFilter.dispose();
        btDelArchived.dispose();
        btDelFounds.dispose();
        lblDelFilter.dispose();
        lblDelArchived.dispose();
        lblDelFounds.dispose();
        super.dispose();
        btDelFilter = null;
        btDelArchived = null;
        btDelFounds = null;
        lblDelFilter = null;
        lblDelArchived = null;
        lblDelFounds = null;
    }

}

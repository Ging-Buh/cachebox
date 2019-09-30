package CB_UI.GL_UI.Controls.Dialogs;

import CB_Core.CB_Core_Settings;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Types.CacheListDAO;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.CB_Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Interfaces.ICancelRunnable;

public class DeleteDialog extends ButtonDialog {
    private SizeF msgBoxContentSize;
    private ImageButton btDelFilter, btDelArchived, btDelFounds;
    private CB_Label lblDelFilter, lblDelArchived, lblDelFounds;
    private CancelWaitDialog wd;

    public DeleteDialog() {
        super((calcMsgBoxSize("Text", true, true, false, false)).getBounds().asFloat(), "Delete-Dialog", "", Translation.get("DeleteCaches"), MessageBoxButtons.Cancel, null, null);

        msgBoxContentSize = getContentSize();

        float innerWidth = msgBoxContentSize.width;

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

        btDelFilter.addClickHandler((v, x, y, pointer, button) -> {
            close();

            wd = CancelWaitDialog.ShowWait(Translation.get("DelActFilter"), () -> {

            }, new ICancelRunnable() {

                @Override
                public void run() {
                    CacheListDAO dao = new CacheListDAO();
                    long nun = dao.deleteFiltered(FilterInstances.getLastFilter().getSqlWhere(CB_Core_Settings.GcLogin.getValue()), CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(),
                            CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());
                    cleanupLogs();
                    cleanupWaypoints();
                    wd.close();

                    // reset Filter
                    FilterInstances.setLastFilter(new FilterProperties());
                    EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());// all Caches

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

        btDelArchived.addClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                close();

                wd = CancelWaitDialog.ShowWait(Translation.get("DelArchived"), new IcancelListener() {

                    @Override
                    public void isCanceled() {

                    }
                }, new ICancelRunnable() {

                    @Override
                    public void run() {
                        CacheListDAO dao = new CacheListDAO();
                        long nun = dao.deleteArchived(Config.SpoilerFolder.getValue(), Config.SpoilerFolderLocal.getValue(), Config.DescriptionImageFolder.getValue(), Config.DescriptionImageFolderLocal.getValue());

                        cleanupLogs();
                        cleanupWaypoints();
                        wd.close();

                        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());

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

        btDelFounds.addClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                close();

                wd = CancelWaitDialog.ShowWait(Translation.get("DelFound"), new IcancelListener() {

                    @Override
                    public void isCanceled() {

                    }
                }, new ICancelRunnable() {

                    @Override
                    public void run() {
                        CacheListDAO dao = new CacheListDAO();
                        long nun = dao.deleteFinds(Config.SpoilerFolder.getValue(), Config.SpoilerFolderLocal.getValue(), Config.DescriptionImageFolder.getValue(), Config.DescriptionImageFolderLocal.getValue());
                        cleanupLogs();
                        cleanupWaypoints();
                        wd.close();

                        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());

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

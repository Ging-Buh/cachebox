package CB_UI.GL_UI.Main.Actions;

import CB_Core.Database;
import CB_Core.Export.GpxSerializer;
import CB_Core.Export.GpxSerializer.ProgressListener;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.*;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog.ICancelListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CB_Action_ShowImportMenu extends CB_Action_ShowView {
    public static final int MI_IMPORT_CBS = 189;
    public static final int MI_IMPORT_GCV = 192;
    private static CB_Action_ShowImportMenu that;
    int actExportedCount = 0;
    private ProgressDialog pD;
    private boolean cancel = false;

    private CB_Action_ShowImportMenu() {
        super("ImportMenu", MenuID.AID_SHOW_IMPORT_MENU);
    }

    public static CB_Action_ShowImportMenu getInstance() {
        if (that == null) that = new CB_Action_ShowImportMenu();
        return that;
    }

    @Override
    public void Execute() {
        getContextMenu().show();
    }

    @Override
    public CB_View_Base getView() {
        // don't return a view.
        // show menu direct.
        GL.that.RunOnGL(() -> Execute());

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
        icm.addMenuItem("chkState", null, () -> GL.that.postAsync(() -> {
            // Sprites.getSprite(IconName.dayGcLiveIcon.name())
            // First check API-Key with visual Feedback
            Log.debug("ImportMenuTitle", "chkAPiLogInWithWaitDialog");
            GlobalCore.chkAPiLogInWithWaitDialog(isAccessTokenInvalid -> {
                Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
                if (!isAccessTokenInvalid) {
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            GL.that.postAsync(() -> new Action_chkState().Execute());
                        }
                    };
                    Timer t = new Timer();
                    t.schedule(tt, 100);
                }
            });
        }));
        icm.addMenuItem("moreImport", null, () -> GL.that.postAsync(() -> new Import().show()));
        icm.addMenuItem("importCachesOverPosition", null, () -> new SearchOverPosition().show()); // "import"
        icm.addMenuItem("API_IMPORT_NAME_OWNER_CODE", null, SearchOverNameOwnerGcCode::ShowInstanz);
        icm.addMenuItem("GCVoteRatings", null, () -> new Import(MI_IMPORT_GCV).show());
        icm.addMenuItem("GSAKMenuImport", null, () -> new Import_GSAK().show());
        icm.addDivider();
        icm.addMenuItem("GPX_EXPORT", null, () -> {
            StringInputBox.Show(WrapType.SINGLELINE,
                    Translation.get("enterFileName"),
                    Translation.get("GPX_EXPORT"),
                    FileIO.GetFileName(Config.gpxExportFileName.getValue()),
                    (which, data) -> {
                        if (which == 1) {
                            final String FileName = StringInputBox.editText.getText();
                            GL.that.RunOnGL(() -> ExportgetFolderStep(FileName));
                        }
                        return true;
                    });
        });
        if (Config.CBS_IP.getValue().length() > 0)
            icm.addMenuItem("ToCBServer", null, () -> new Import_CBServer().show());
        return icm;
    }

    private void ExportgetFolderStep(final String FileName) {
        PlatformConnector.getFolder(FileIO.GetDirectoryName(Config.gpxExportFileName.getValue()),
                Translation.get("selectExportFolder".hashCode()),
                Translation.get("select".hashCode()),
                Path -> GL.that.RunOnGL(() -> ausgebenDatei(FileName, Path)));
    }

    private void ausgebenDatei(final String FileName, String Path) {
        String exportPath = Path + "/" + FileName;
        PlatformConnector.addToMediaScannerList(exportPath);
        File exportFile = FileFactory.createFile(exportPath);
        Config.gpxExportFileName.setValue(exportFile.getPath());
        Config.AcceptChanges();

        // Delete File if exist
        if (exportFile.exists())
            try {
                exportFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }

        // Export all Caches from DB
        final ArrayList<String> allGeocodesForExport = Database.Data.cacheList.getGcCodes();

        final int count = allGeocodesForExport.size();
        actExportedCount = 0;
        // Show with Progress

        final GpxSerializer ser = new GpxSerializer();
        try {
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exportFile.getFileOutputStream(), "UTF-8"));

            pD = ProgressDialog.Show("export", new RunnableReadyHandler() {

                @Override
                public void run() {
                    try {
                        ser.writeGPX(allGeocodesForExport, writer, new ProgressListener() {
                            @Override
                            public void publishProgress(int countExported, String msg) {
                                actExportedCount = countExported;
                                if (pD != null) {
                                    int progress = (countExported * 100) / count;
                                    pD.setProgress("Export: " + countExported + "/" + count, msg, progress);
                                    if (pD.isCanceld())
                                        ser.cancel();
                                }
                            }
                        });
                    } catch (IOException ignored) {
                    }
                }

                @Override
                public boolean doCancel() {
                    return cancel;
                }

                @Override
                public void RunnableIsReady(boolean canceld) {
                    System.out.print("Export READY");
                    if (pD != null) {
                        pD.close();
                        pD.dispose();
                        pD = null;
                    }

                    if (canceld) {
                        MessageBox.show(Translation.get("exportedCanceld".hashCode(), String.valueOf(actExportedCount), String.valueOf(count)), Translation.get("export"), MessageBoxIcon.Stop);
                    } else {
                        MessageBox.show(Translation.get("exported".hashCode(), String.valueOf(actExportedCount)), Translation.get("export"), MessageBoxIcon.Information);
                    }

                }
            });

            pD.setCancelListener(new ICancelListener() {

                @Override
                public void isCanceled() {
                    cancel = true;
                    if (pD.isCanceld())
                        ser.cancel();
                }
            });
        } catch (IOException e) {

        }
    }

    private enum menuId {aktualisiereStatus, getFriends, downloadMap, importOverPosition, importByGcCode, importDiverse}

}

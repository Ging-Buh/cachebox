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
import CB_UI_Base.Events.PlatformConnector.IgetFolderReturnListener;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog.ICancelListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
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

    int actExportedCount = 0;
    private ProgressDialog pD;
    private boolean cancel = false;

    public CB_Action_ShowImportMenu() {
        super("ImportMenu", MenuID.AID_SHOW_IMPORT_MENU);
    }

    @Override
    public void Execute() {
        getContextMenu().Show();
    }

    @Override
    public CB_View_Base getView() {
        // don't return a view.
        // show menu direct.
        GL.that.RunOnGL(new IRunOnGL() {
            @Override
            public void run() {
                Execute();
            }
        });

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
        Menu icm = new Menu("CacheListShowImportMenu");

        icm.addOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switch (((MenuItem) v).getMenuItemId()) {
                    case MenuID.MI_CHK_STATE_API:
                        GL.postAsync(new Runnable() {
                            @Override
                            public void run() {
                                // First check API-Key with visual Feedback
                                Log.debug("MI_CHK_STATE_API", "chkAPiLogInWithWaitDialog");
                                GlobalCore.chkAPiLogInWithWaitDialog(new GlobalCore.iChkReadyHandler() {
                                    @Override
                                    public void checkReady(boolean isAccessTokenInvalid) {
                                        Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
                                        if (!isAccessTokenInvalid) {
                                            TimerTask tt = new TimerTask() {
                                                @Override
                                                public void run() {
                                                    GL.postAsync(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            new CB_Action_chkState().Execute();
                                                        }
                                                    });
                                                }
                                            };
                                            Timer t = new Timer();
                                            t.schedule(tt, 100);
                                        }
                                    }
                                });
                            }
                        });
                        return true;
                    case MenuID.MI_IMPORT_GS_PQ:
                        GL.postAsync(new Runnable() {
                            @Override
                            public void run() {
                                new Import(MenuID.MI_IMPORT_GS_PQ).show();
                            }
                        });
                        return true;
                    case MenuID.MI_IMPORT_GS_API_POSITION:
                        SearchOverPosition.ShowInstanz();
                        return true;
                    case MenuID.MI_IMPORT_GS_API_SEARCH:
                        SearchOverNameOwnerGcCode.ShowInstanz();
                        return true;
                    case MenuID.MI_IMPORT_GPX:
                        new Import(MenuID.MI_IMPORT_GPX).show();
                        return true;
                    case MenuID.MI_EXPORT_CBS:
                        new Import_CBServer().show();
                        return true;
                    case MenuID.MI_IMPORT_GCV:
                        new Import(MenuID.MI_IMPORT_GCV).show();
                        return true;
                    case MenuID.MI_IMPORT:
                        GL.postAsync(new Runnable() {
                            @Override
                            public void run() {
                                new Import().show();
                            }
                        });
                        return true;
                    case MenuID.MI_MAP_DOWNOAD:
                        MapDownload.getInstance().show();
                        return true;
                    case MenuID.MI_EXPORT_RUN:
                        StringInputBox.Show(WrapType.SINGLELINE, Translation.Get("enterFileName"), ((MenuItem) v).getTitle(), FileIO.GetFileName(Config.gpxExportFileName.getValue()), new OnMsgBoxClickListener() {
                            @Override
                            public boolean onClick(int which, Object data) {
                                if (which == 1) {
                                    final String FileName = StringInputBox.editText.getText();
                                    GL.that.RunOnGL(new IRunOnGL() {
                                        @Override
                                        public void run() {
                                            ExportgetFolderStep(FileName);
                                        }
                                    });
                                }
                                return true;
                            }

                        });
                        return true;
                }
                return true;
            }
        });
        icm.addItem(MenuID.MI_CHK_STATE_API, "chkState"); // , Sprites.getSprite(IconName.dayGcLiveIcon.name())
        icm.addItem(MenuID.MI_IMPORT, "moreImport");
        icm.addItem(MenuID.MI_IMPORT_GS_API_POSITION, "API_IMPORT_OVER_POSITION");
        icm.addItem(MenuID.MI_IMPORT_GS_API_SEARCH, "API_IMPORT_NAME_OWNER_CODE");
        if (Config.GcVotePassword.getValue().length() > 0)
            icm.addItem(MenuID.MI_IMPORT_GCV, "GCVoteRatings");
        // icm.addItem(MenuID.MI_IMPORT_GS_PQ, "API_PocketQuery");
        // icm.addItem(MenuID.MI_IMPORT_GPX, "GPX_IMPORT");
        icm.addDivider();
        icm.addItem(MenuID.MI_MAP_DOWNOAD, "MapDownload");
        icm.addDivider();
        icm.addItem(MenuID.MI_EXPORT_RUN, "GPX_EXPORT");
        if (Config.CBS_IP.getValue().length() > 0) icm.addItem(MenuID.MI_EXPORT_CBS, "ToCBServer");
        return icm;
    }

    private void ExportgetFolderStep(final String FileName) {
        PlatformConnector.getFolder(FileIO.GetDirectoryName(Config.gpxExportFileName.getValue()), Translation.Get("selectExportFolder".hashCode()), Translation.Get("select".hashCode()), new IgetFolderReturnListener() {
            @Override
            public void returnFolder(final String Path) {
                GL.that.RunOnGL(new IRunOnGL() {
                    @Override
                    public void run() {
                        ausgebenDatei(FileName, Path);
                    }
                });
            }
        });
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
        final ArrayList<String> allGeocodesForExport = Database.Data.Query.getGcCodes();

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
                        GL_MsgBox.Show(Translation.Get("exportedCanceld".hashCode(), String.valueOf(actExportedCount), String.valueOf(count)), Translation.Get("export"), MessageBoxIcon.Stop);
                    } else {
                        GL_MsgBox.Show(Translation.Get("exported".hashCode(), String.valueOf(actExportedCount)), Translation.Get("export"), MessageBoxIcon.Information);
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

}

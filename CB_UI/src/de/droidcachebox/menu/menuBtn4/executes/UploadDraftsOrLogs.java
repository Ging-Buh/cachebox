package de.droidcachebox.menu.menuBtn4.executes;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.core.GCVote;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.dataclasses.Draft;
import de.droidcachebox.dataclasses.Drafts;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.menu.menuBtn2.executes.Logs;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;

public class UploadDraftsOrLogs {
    private final boolean threadCancel = false;
    private String uploadMeldung = "";
    private boolean apiKeyError = false;
    private ProgressDialog progressDialog;

    public UploadDraftsOrLogs() {
    }

    public void upload(boolean asLog) {
        final AtomicBoolean isCanceled = new AtomicBoolean(false);

        final RunAndReady uploadDrafts = new RunAndReady() {

            @Override
            public void run() {
                progressDialog.setProgress("Upload", "", 0);
                boolean sendGCVote = Settings.GcVotePassword.getEncryptedValue().length() > 0;

                Drafts drafts = new Drafts();
                drafts.loadDrafts(Drafts.LoadingType.CanUpload);

                int count = 0;
                int anzahl = 0;
                for (Draft draft : drafts) {
                    if (!draft.uploaded)
                        anzahl++;
                }

                if (anzahl > 0) {
                    uploadMeldung = "";
                    apiKeyError = false;
                    for (Draft draft : drafts) {
                        if (isCanceled.get())
                            break;

                        if (draft.uploaded)
                            continue;
                        if (threadCancel) // wenn im ProgressDialog Cancel gedrückt
                            // wurde.
                            break;
                        // Progress status Melden
                        progressDialog.setProgress("", draft.CacheName, (100 * count) / anzahl);

                        int result;

                        if (draft.isTbDraft) {
                            // there is no TB draft. we have to log direct
                            result = GroundspeakAPI.uploadTrackableLog(draft.TravelBugCode, draft.TrackingNumber, draft.gcCode, LogType.CB_LogType2GC(draft.type), draft.timestamp, draft.comment);
                        } else {
                            if (sendGCVote) {
                                if (draft.gc_Vote > 0) {
                                    try {
                                        try {
                                            if (!GCVote.sendVote(Settings.GcLogin.getValue(), Settings.GcVotePassword.getValue(), draft.gc_Vote, draft.CacheUrl, draft.gcCode)) {
                                                uploadMeldung += draft.gcCode + "\n" + "GC-Vote Error" + "\n";
                                            }
                                        } catch (Exception e) {
                                            uploadMeldung += draft.gcCode + "\n" + "GC-Vote Error" + "\n";
                                        }

                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            result = GroundspeakAPI.uploadDraftOrLog(draft, asLog);
                        }

                        if (result == GroundspeakAPI.ERROR) {
                            GL.that.toast(GroundspeakAPI.LastAPIError);
                            uploadMeldung = uploadMeldung + draft.gcCode + "\n" + GroundspeakAPI.LastAPIError + "\n";
                        } else {
                            // set draft as uploaded only when upload was working
                            draft.uploaded = true;
                            if (asLog && !draft.isTbDraft) {
                                draft.gcLogReference = GroundspeakAPI.logReferenceCode;
                                Logs.getInstance().resetRenderInitDone(); // if own log is written !
                            }
                            draft.updateDatabase();
                        }
                        count++;
                    }
                }
                progressDialog.close();
            }

            @Override
            public void ready() {
                if (!isCanceled.get()) {
                    if (uploadMeldung.length() == 0) {
                        new ButtonDialog(Translation.get("uploadFinished"), Translation.get("uploadDrafts"), MsgBoxButton.OK, MsgBoxIcon.GC_Live).show();
                    } else {
                        if (!apiKeyError)
                            new ButtonDialog(uploadMeldung, Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
                    }
                }
                DraftsView.getInstance().notifyDataSetChanged();
            }

            @Override
            public void setIsCanceled() {
                isCanceled.set(true);
            }

        };

        GL.that.runOnGL(() -> {
            progressDialog = new ProgressDialog("uploadDrafts", new DownloadAnimation(), uploadDrafts);
            GL.that.showDialog(progressDialog);
        });

    }

}

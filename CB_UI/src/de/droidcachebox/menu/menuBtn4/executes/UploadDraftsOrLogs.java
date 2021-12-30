package de.droidcachebox.menu.menuBtn4.executes;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.core.GCVote;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.dataclasses.Draft;
import de.droidcachebox.dataclasses.Drafts;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.menu.menuBtn2.executes.Logs;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.RunAndReady;

public class UploadDraftsOrLogs {
    private final boolean threadCancel = false;
    private String uploadMeldung = "";
    private boolean apiKeyError = false;
    private ProgressDialog progressDialog;

    public UploadDraftsOrLogs() {
    }

    public void upload(boolean isLog) {
        final AtomicBoolean cancel = new AtomicBoolean(false);

        final RunAndReady uploadDrafts = new RunAndReady() {

            @Override
            public void run() {
                progressDialog.setProgress("Upload", "", 0);

                Drafts drafts = new Drafts();
                drafts.loadDrafts(Drafts.LoadingType.CanUpload);

                int count = 0;
                int anzahl = 0;
                for (Draft draft : drafts) {
                    if (!draft.uploaded)
                        anzahl++;
                }

                boolean sendGCVote = Settings.GcVotePassword.getEncryptedValue().length() > 0;

                if (anzahl > 0) {
                    uploadMeldung = "";
                    apiKeyError = false;
                    for (Draft draft : drafts) {
                        if (cancel.get())
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
                                        sendCacheVote(draft);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            result = GroundspeakAPI.uploadDraftOrLog(draft, isLog);
                        }

                        if (result == GroundspeakAPI.ERROR) {
                            GL.that.toast(GroundspeakAPI.LastAPIError);
                            uploadMeldung = new StringBuilder()
                                    .append(uploadMeldung)
                                    .append(draft.gcCode)
                                    .append("\n")
                                    .append(GroundspeakAPI.LastAPIError)
                                    .append("\n").toString();
                        } else {
                            // set draft as uploaded only when upload was working
                            draft.uploaded = true;
                            if (isLog && !draft.isTbDraft) {
                                draft.gcLogReference = GroundspeakAPI.logReferenceCode;
                                Logs.getInstance().resetIsInitialized(); // if own log is written !
                            }
                            draft.updateDatabase();
                        }
                        count++;
                    }
                }
                progressDialog.close();
            }

            @Override
            public void ready(boolean canceled) {
                if (!canceled) {
                    if (uploadMeldung.length() == 0) {
                        MsgBox.show(Translation.get("uploadFinished"), Translation.get("uploadDrafts"), MsgBoxIcon.GC_Live);
                    } else {
                        if (!apiKeyError)
                            MsgBox.show(uploadMeldung, Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error, null);
                    }
                }
                DraftsView.getInstance().notifyDataSetChanged();
            }
        };

        // ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.

        GL.that.RunOnGL(() -> {
            progressDialog = new ProgressDialog("uploadDrafts", new DownloadAnimation(), uploadDrafts);
            progressDialog.setCancelListener(() -> cancel.set(true));
            GL.that.showDialog(progressDialog);
        });

    }

    private void sendCacheVote(Draft draft) {

        // Stimme abgeben
        try {
            if (!GCVote.sendVote(Settings.GcLogin.getValue(), Settings.GcVotePassword.getValue(), draft.gc_Vote, draft.CacheUrl, draft.gcCode)) {
                uploadMeldung += draft.gcCode + "\n" + "GC-Vote Error" + "\n";
            }
        } catch (Exception e) {
            uploadMeldung += draft.gcCode + "\n" + "GC-Vote Error" + "\n";
        }
    }

}

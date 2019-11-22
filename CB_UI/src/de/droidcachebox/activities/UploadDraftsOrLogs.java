package de.droidcachebox.activities;

import de.droidcachebox.Config;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.core.GCVote;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Draft;
import de.droidcachebox.database.Drafts;
import de.droidcachebox.database.LogTypes;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.views.DraftsView;
import de.droidcachebox.gdx.views.LogView;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.RunnableReadyHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public class UploadDraftsOrLogs {
    private Boolean ThreadCancel = false;
    private String UploadMeldung = "";
    private boolean API_Key_error = false;
    private ProgressDialog PD;

    public UploadDraftsOrLogs() {
    }

    public void upload(boolean direct) {
        final AtomicBoolean cancel = new AtomicBoolean(false);

        final RunnableReadyHandler uploadDrafts = new RunnableReadyHandler() {

            @Override
            public void run() {
                ProgresssChangedEventList.Call("Upload", "", 0);

                Drafts drafts = new Drafts();
                drafts.loadDrafts("(Uploaded=0 or Uploaded is null)", Drafts.LoadingType.Loadall);

                int count = 0;
                int anzahl = 0;
                for (Draft draft : drafts) {
                    if (!draft.uploaded)
                        anzahl++;
                }

                boolean sendGCVote = Config.GcVotePassword.getEncryptedValue().length() > 0;

                if (anzahl > 0) {
                    UploadMeldung = "";
                    API_Key_error = false;
                    for (Draft draft : drafts) {
                        if (cancel.get())
                            break;

                        if (draft.uploaded)
                            continue;
                        if (ThreadCancel) // wenn im ProgressDialog Cancel gedrückt
                            // wurde.
                            break;
                        // Progress status Melden
                        ProgresssChangedEventList.Call(draft.CacheName, (100 * count) / anzahl);

                        int result;

                        if (draft.isTbDraft) {
                            // there is no TB draft. we have to log direct
                            result = GroundspeakAPI.uploadTrackableLog(draft.TravelBugCode, draft.TrackingNumber, draft.gcCode, LogTypes.CB_LogType2GC(draft.type), draft.timestamp, draft.comment);
                        } else {
                            if (sendGCVote) {
                                if (draft.gc_Vote > 0) {
                                    try {
                                        sendCacheVote(draft);
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            result = GroundspeakAPI.UploadDraftOrLog(draft.gcCode, draft.type.getGcLogTypeId(), draft.timestamp, draft.comment, direct);
                        }

                        if (result == GroundspeakAPI.ERROR) {
                            GL.that.Toast(GroundspeakAPI.LastAPIError);
                            UploadMeldung = UploadMeldung + draft.gcCode + "\n" + GroundspeakAPI.LastAPIError + "\n";
                        } else {
                            // set draft as uploaded only when upload was working
                            draft.uploaded = true;
                            if (direct && !draft.isTbDraft) {
                                draft.GcId = GroundspeakAPI.logReferenceCode;
                                LogView.getInstance().resetInitial(); // if own log is written !
                            }
                            draft.UpdateDatabase();
                        }
                        count++;
                    }
                }
                PD.close();
            }

            @Override
            public boolean doCancel() {
                return cancel.get();
            }

            @Override
            public void runnableIsReady(boolean canceld) {
                if (!canceld) {

                    if (!UploadMeldung.equals("")) {
                        if (!API_Key_error)
                            MessageBox.show(UploadMeldung, Translation.get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
                    } else {
                        MessageBox.show(Translation.get("uploadFinished"), Translation.get("uploadDrafts"), MessageBoxIcon.GC_Live);
                    }
                }
                DraftsView.getInstance().notifyDataSetChanged();
            }
        };

        // ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.

        GL.that.RunOnGL(() -> {
            PD = ProgressDialog.Show("uploadDrafts", uploadDrafts);
            PD.setCancelListener(() -> cancel.set(true));
        });

    }

    private void sendCacheVote(Draft draft) {

        // Stimme abgeben
        try {
            if (!GCVote.sendVote(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), draft.gc_Vote, draft.CacheUrl, draft.gcCode)) {
                UploadMeldung += draft.gcCode + "\n" + "GC-Vote Error" + "\n";
            }
        } catch (Exception e) {
            UploadMeldung += draft.gcCode + "\n" + "GC-Vote Error" + "\n";
        }
    }

}

package CB_UI.GL_UI.Main.Actions;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CB_Core_Settings;
import CB_Core.GCVote.GCVote;
import CB_Core.LogTypes;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.FieldNoteList;
import CB_Core.Types.FieldNoteList.LoadingType;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Views.FieldNotesView;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.ProgressDialog.ICancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_Utils.Events.ProgresssChangedEventList;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.concurrent.atomic.AtomicBoolean;

public class CB_Action_UploadFieldNote extends CB_Action {
    public static CB_Action_UploadFieldNote INSTANCE = new CB_Action_UploadFieldNote();
    private Boolean ThreadCancel = false;
    private String UploadMeldung = "";
    private boolean API_Key_error = false;
    private ProgressDialog PD;

    private CB_Action_UploadFieldNote() {
        super("uploadFieldNotes", MenuID.AID_UPLOAD_FIELD_NOTE);
    }

    @Override
    public void Execute() {
        UploadFieldNotes();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

    private void UploadFieldNotes() {
        final AtomicBoolean cancel = new AtomicBoolean(false);

        final RunnableReadyHandler UploadFieldNotesdThread = new RunnableReadyHandler() {

            @Override
            public void run() {
                ProgresssChangedEventList.Call("Upload", "", 0);

                FieldNoteList lFieldNotes = new FieldNoteList();

                lFieldNotes.LoadFieldNotes("(Uploaded=0 or Uploaded is null)", LoadingType.Loadall);

                int count = 0;
                int anzahl = 0;
                for (FieldNoteEntry fieldNote : lFieldNotes) {
                    if (!fieldNote.uploaded)
                        anzahl++;
                }

                boolean sendGCVote = Config.GcVotePassword.getEncryptedValue().length() > 0;

                if (anzahl > 0) {
                    UploadMeldung = "";
                    API_Key_error = false;
                    for (FieldNoteEntry fieldNote : lFieldNotes) {
                        if (cancel.get())
                            break;

                        if (fieldNote.uploaded)
                            continue;
                        if (ThreadCancel) // wenn im ProgressDialog Cancel gedrückt
                            // wurde.
                            break;
                        // Progress status Melden
                        ProgresssChangedEventList.Call(fieldNote.CacheName, (100 * count) / anzahl);

                        int result;

                        if (fieldNote.isTbFieldNote) {
                            // there is no TB draft. we have to log direct
                            result = GroundspeakAPI.uploadTrackableLog(fieldNote.TravelBugCode, fieldNote.TrackingNumber, fieldNote.gcCode, LogTypes.CB_LogType2GC(fieldNote.type), fieldNote.timestamp, fieldNote.comment);
                        } else {
                            if (sendGCVote) {
                                if (fieldNote.gc_Vote > 0)
                                    sendCacheVote(fieldNote);
                            }
                            result = GroundspeakAPI.UploadDraftOrLog(fieldNote.gcCode, fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment, fieldNote.isDirectLog);
                        }

                        if (result == GroundspeakAPI.ERROR) {
                            GL.that.Toast(GroundspeakAPI.LastAPIError);
                            UploadMeldung += fieldNote.gcCode + "\n" + GroundspeakAPI.LastAPIError + "\n";
                        } else {
                            // set fieldnote as uploaded only when upload was working
                            fieldNote.uploaded = true;
                            fieldNote.UpdateDatabase();
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
            public void RunnableIsReady(boolean canceld) {
                if (!canceld) {

                    if (!UploadMeldung.equals("")) {
                        if (!API_Key_error)
                            GL_MsgBox.Show(UploadMeldung, Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, null);
                    } else {
                        GL_MsgBox.Show(Translation.Get("uploadFinished"), Translation.Get("uploadFieldNotes"), MessageBoxIcon.GC_Live);
                    }
                }
                if (FieldNotesView.that != null)
                    FieldNotesView.that.notifyDataSetChanged();
            }
        };

        // ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.

        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                PD = ProgressDialog.Show("Upload FieldNotes", UploadFieldNotesdThread);
                PD.setCancelListener(new ICancelListener() {

                    @Override
                    public void isCanceled() {
                        cancel.set(true);
                    }
                });
            }
        });

    }

    void sendCacheVote(FieldNoteEntry fieldNote) {

        // Stimme abgeben
        try {
            if (!GCVote.SendVotes(CB_Core_Settings.GcLogin.getValue(), CB_Core_Settings.GcVotePassword.getValue(), fieldNote.gc_Vote, fieldNote.CacheUrl, fieldNote.gcCode)) {
                UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
            }
        } catch (Exception e) {
            UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
        }
    }

}

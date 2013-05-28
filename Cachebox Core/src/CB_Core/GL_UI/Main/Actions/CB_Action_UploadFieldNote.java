package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Enums.LogTypes;
import CB_Core.Events.ProgresssChangedEventList;
import CB_Core.GCVote.GCVote;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Controls.Dialogs.ProgressDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_Core.GL_UI.Controls.PopUps.ConnectionError;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.FieldNotesView;
import CB_Core.GL_UI.interfaces.RunnableReadyHandler;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.FieldNoteList;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_UploadFieldNote extends CB_ActionCommand
{
	private Boolean ThreadCancel = false;
	private String UploadMeldung = "";
	private boolean API_Key_error = false;

	public static CB_Action_UploadFieldNote INSTANCE = new CB_Action_UploadFieldNote();

	private CB_Action_UploadFieldNote()
	{
		super("uploadFieldNotes", MenuID.AID_UPLOAD_FIELD_NOTE);
	}

	@Override
	public void Execute()
	{
		GL_MsgBox.Show(Translation.Get("uploadFieldNotes?"), Translation.Get("uploadFieldNotes"), MessageBoxButtons.YesNo,
				MessageBoxIcon.GC_Live, UploadFieldnotesDialogListner, Config.settings.RememberAsk_API_Coast);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.uploadFieldNote_64.ordinal());
	}

	private final GL_MsgBox.OnMsgBoxClickListener UploadFieldnotesDialogListner = new GL_MsgBox.OnMsgBoxClickListener()
	{
		@Override
		public boolean onClick(int which, Object data)
		{
			switch (which)
			{
			case GL_MsgBox.BUTTON_POSITIVE:
				UploadFieldNotes();

				return true;
			}
			return false;
		}

	};

	private ProgressDialog PD;

	private void UploadFieldNotes()
	{
		ThreadCancel = false;
		final RunnableReadyHandler UploadFieldNotesdThread = new RunnableReadyHandler(new Runnable()
		{

			@Override
			public void run()
			{
				ProgresssChangedEventList.Call("Upload", "", 0);

				String accessToken = Config.GetAccessToken();

				FieldNoteList lFieldNotes = new FieldNoteList();
				lFieldNotes.LoadFieldNotes("");

				int count = 0;
				int anzahl = 0;
				for (FieldNoteEntry fieldNote : lFieldNotes)
				{
					if (!fieldNote.uploaded) anzahl++;
				}

				boolean sendGCVote = !Config.settings.GcVotePassword.getEncryptedValue().equalsIgnoreCase("");

				if (anzahl > 0)
				{
					UploadMeldung = "";
					API_Key_error = false;
					for (FieldNoteEntry fieldNote : lFieldNotes)
					{
						if (fieldNote.uploaded) continue;
						if (ThreadCancel) // wenn im ProgressDialog Cancel gedrückt
											// wurde.
						break;
						// Progress status Melden
						ProgresssChangedEventList.Call(fieldNote.CacheName, (100 * count) / anzahl);

						if (sendGCVote && !fieldNote.isTbFieldNote)
						{
							sendCacheVote(fieldNote);
						}

						int result = 0;

						if (fieldNote.isTbFieldNote)
						{
							result = GroundspeakAPI.createTrackableLog(Config.GetAccessToken(), fieldNote.TravelBugCode,
									fieldNote.TrackingNumber, fieldNote.gcCode, LogTypes.CB_LogType2GC(fieldNote.type),
									fieldNote.timestamp, fieldNote.comment);
						}
						else
						{

							result = CB_Core.Api.GroundspeakAPI.CreateFieldNoteAndPublish(accessToken, fieldNote.gcCode,
									fieldNote.type.getGcLogTypeId(), fieldNote.timestamp, fieldNote.comment);
						}

						if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
						{
							GL.that.Toast(ConnectionError.INSTANCE);
							PD.close();
							return;
						}
						if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
						{
							GL.that.Toast(ApiUnavailable.INSTANCE);
							PD.close();
							return;
						}

						if (result == -1)
						{
							UploadMeldung += fieldNote.gcCode + "\n" + CB_Core.Api.GroundspeakAPI.LastAPIError + "\n";
						}
						else
						{
							if (result != -10)
							{
								// set fieldnote as uploaded only when upload was working
								fieldNote.uploaded = true;
								fieldNote.UpdateDatabase();
							}
							else
							{
								API_Key_error = true;
								UploadMeldung = "error";
								ThreadCancel = true;
							}
						}
						count++;
					}
				}

				PD.close();

			}
		})
		{

			@Override
			public void RunnableReady(boolean canceld)
			{
				if (!canceld)
				{

					if (!UploadMeldung.equals(""))
					{
						if (!API_Key_error) GL_MsgBox.Show(UploadMeldung, Translation.Get("Error"), MessageBoxButtons.OK,
								MessageBoxIcon.Error, null);
					}
					else
					{
						GL_MsgBox.Show(Translation.Get("uploadFinished"), Translation.Get("uploadFieldNotes"), MessageBoxIcon.GC_Live);
					}
				}
				if (FieldNotesView.that != null) FieldNotesView.that.notifyDataSetChanged();
			}
		};

		// ProgressDialog Anzeigen und den Abarbeitungs Thread übergeben.
		PD = ProgressDialog.Show("Upload FieldNotes", UploadFieldNotesdThread);

	}

	void sendCacheVote(FieldNoteEntry fieldNote)
	{

		// Stimme abgeben
		try
		{
			if (!GCVote.SendVotes(Config.settings.GcLogin.getValue(), Config.settings.GcVotePassword.getValue(), fieldNote.gc_Vote,
					fieldNote.CacheUrl, fieldNote.gcCode))
			{
				UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
			}
		}
		catch (Exception e)
		{
			UploadMeldung += fieldNote.gcCode + "\n" + "GC-Vote Error" + "\n";
		}
	}

}

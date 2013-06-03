package CB_Core.GL_UI.Activitys;

import java.util.Date;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.TemplateFormatter;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Enums.LogTypes;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.RadioButton;
import CB_Core.GL_UI.Controls.RadioGroup;
import CB_Core.GL_UI.Controls.Animation.DownloadAnimation;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.Dialogs.WaitDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_Core.GL_UI.Controls.PopUps.ConnectionError;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Views.TrackableListView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.Trackable;

public class TB_Log extends ActivityBase
{
	public static TB_Log that;
	private Trackable TB;
	private Button btnClose;
	private ImageButton btnAction;
	private Image icon, CacheIcon;
	private Label lblName, lblPlaced;
	private Box contentBox;
	private LogTypes LT;
	private EditWrapedTextField edit;
	private RadioButton rbDirectLog;
	private RadioButton rbOnlyFieldNote;

	public TB_Log()
	{
		super(ActivityRec(), "TB_Log_Activity");
		createControls();
		that = this;
	}

	public void Show(Trackable TB, LogTypes Type)
	{
		this.TB = TB;
		this.LT = Type;
		layout();
		GL.that.showActivity(this);
	}

	private void createControls()
	{

		btnClose = new Button("Close");
		btnClose.setText(Translation.Get("close"));
		btnClose.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				TB_Log.this.finish();
				return true;
			}
		});

		btnAction = new ImageButton("Action");
		btnAction.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				LogNow();
				return true;
			}
		});

		contentBox = new Box(ActivityRec(), "ContentBox");
		contentBox.setHeight(this.height - (btnClose.getHeight() - margin) * 2.5f);
		contentBox.setBackground(SpriteCache.activityBackground);

		CB_RectF iconRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight());
		iconRec = iconRec.ScaleCenter(0.8f);

		icon = new Image(iconRec, "Icon");
		icon.setWeight(-1);
		lblName = new Label(iconRec, "Name");

		CacheIcon = new Image(iconRec, "CacheIcon");
		CacheIcon.setWeight(-1);
		lblPlaced = new Label(iconRec, "CacheName");

		edit = new EditWrapedTextField("LogInput", TextFieldType.MultiLineWraped);
		edit.setWidth(contentBox.getInnerWidth());
		edit.setHeight(contentBox.getHalfHeight());

		rbDirectLog = new RadioButton("direct_Log");
		rbOnlyFieldNote = new RadioButton("only_FieldNote");

		rbDirectLog.setText(Translation.Get("directLog"));
		rbOnlyFieldNote.setText(Translation.Get("onlyFieldNote"));

		RadioGroup Group = new RadioGroup();
		Group.add(rbOnlyFieldNote);
		Group.add(rbDirectLog);
		if (Config.settings.TB_DirectLog.getValue())
		{
			rbDirectLog.setChecked(true);
		}
		else
		{
			rbOnlyFieldNote.setChecked(true);
		}
	}

	private void layout()
	{
		this.removeChilds();
		this.initRow(BOTTOMUP);
		this.addNext(btnAction);
		this.addLast(btnClose);
		this.addLast(contentBox);
		contentBox.initRow(TOPDOWN, contentBox.getHeight());
		contentBox.setNoBorders();
		contentBox.setMargins(0, 0);
		contentBox.addLast(edit);
		contentBox.addLast(rbDirectLog);
		contentBox.addLast(rbOnlyFieldNote);

		// Show Selected Cache for LogTypes discovered/visited/dropped_off/retrieve
		if (LT == LogTypes.discovered || LT == LogTypes.visited || LT == LogTypes.dropped_off || LT == LogTypes.retrieve)
		{

			Cache c = GlobalCore.getSelectedCache();
			if (c == null)
			{
				// Log Inposible, close Activity and give a Message
				final String errorMsg = Translation.Get("NoCacheSelect");
				this.finish();

				GL.that.RunOnGL(new runOnGL()
				{

					@Override
					public void run()
					{
						GL_MsgBox.Show(errorMsg, "", MessageBoxIcon.Error);
					}
				});
				return;
			}

			String msg = "";
			if (LT == LogTypes.discovered)
			{
				msg = Translation.Get("discoveredAt") + ": " + GlobalCore.br + c.Name;
			}
			if (LT == LogTypes.visited)
			{
				msg = Translation.Get("visitedAt") + ": " + GlobalCore.br + c.Name;
			}
			if (LT == LogTypes.dropped_off)
			{
				msg = Translation.Get("dropped_offAt") + ": " + GlobalCore.br + c.Name;
			}
			if (LT == LogTypes.retrieve)
			{
				msg = Translation.Get("retrieveAt") + ": " + GlobalCore.br + c.Name;
			}

			CacheIcon.setSprite(SpriteCache.BigIcons.get(c.Type.ordinal()));

			lblPlaced.setWidth(contentBox.getInnerWidth() - CacheIcon.getWidth() - (margin * 3));
			lblPlaced.setWrappedText(msg);
			lblPlaced.setHeight(lblPlaced.getTextHeight() + lblPlaced.getTopHeight() + lblPlaced.getBottomHeight());

			contentBox.setMargins(margin, margin * 3);
			contentBox.setRowYPos(contentBox.getRowYPos() - (margin * 3));

			contentBox.addNext(CacheIcon);
			contentBox.addLast(lblPlaced);

		}

		this.setMargins(margin * 2, 0);
		this.addNext(icon);
		icon.setImageURL(TB.getIconUrl());
		this.addLast(lblName);

		lblName.setWrappedText(TB.getName());
		switch (this.LT)
		{
		case discovered:
			btnAction.setImage(SpriteCache.Icons.get(IconName.tbDiscover_58.ordinal()));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.DiscoverdTemplate.getValue(), TB));
			break;
		case visited:
			btnAction.setImage(SpriteCache.Icons.get(IconName.tbVisit_62.ordinal()));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.VisitedTemplate.getValue(), TB));
			break;
		case dropped_off:
			btnAction.setImage(SpriteCache.Icons.get(IconName.tbDrop_59.ordinal()));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.DroppedTemplate.getValue(), TB));
			break;
		case grab_it:
			btnAction.setImage(SpriteCache.Icons.get(IconName.tbGrab_60.ordinal()));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.GrabbedTemplate.getValue(), TB));
			break;
		case retrieve:
			btnAction.setImage(SpriteCache.Icons.get(IconName.tbPicked_61.ordinal()));
			edit.setText(TemplateFormatter.ReplaceTemplate(Config.settings.PickedTemplate.getValue(), TB));
			break;
		case note:
			btnAction.setImage(SpriteCache.Icons.get(IconName.tbNote_63.ordinal()));
			edit.setText("");
			break;
		default:
			break;
		}
	}

	static WaitDialog wd;

	private void LogNow()
	{
		if (rbDirectLog.isChecked()) logOnline();
		else
			createFieldNote();

	}

	private void logOnline()
	{

		wd = CancelWaitDialog.ShowWait("Upload Log", DownloadAnimation.GetINSTANCE(), new IcancelListner()
		{

			@Override
			public void isCanceld()
			{
				// TODO Auto-generated method stub

			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				GroundspeakAPI.LastAPIError = "";
				int result = GroundspeakAPI.createTrackableLog(Config.GetAccessToken(), TB, getCache_GcCode(), LogTypes.CB_LogType2GC(LT),
						new Date(), edit.getText());

				if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
				{
					GL.that.Toast(ConnectionError.INSTANCE);
					if (wd != null) wd.close();
					GL_MsgBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"),
							MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener()
							{

								@Override
								public boolean onClick(int which, Object data)
								{
									switch (which)
									{
									case GL_MsgBox.BUTTON_NEGATIVE:
										logOnline();
										return true;

									case GL_MsgBox.BUTTON_NEUTRAL:
										return true;

									case GL_MsgBox.BUTTON_POSITIVE:
										createFieldNote();
										return true;
									}
									return true;
								}
							});
					return;
				}
				if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
				{
					GL.that.Toast(ApiUnavailable.INSTANCE);
					if (wd != null) wd.close();
					GL_MsgBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"),
							MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener()
							{

								@Override
								public boolean onClick(int which, Object data)
								{
									switch (which)
									{
									case GL_MsgBox.BUTTON_NEGATIVE:
										logOnline();
										return true;

									case GL_MsgBox.BUTTON_NEUTRAL:
										return true;

									case GL_MsgBox.BUTTON_POSITIVE:
										createFieldNote();
										return true;
									}
									return true;
								}
							});
					return;
				}

				if (GroundspeakAPI.LastAPIError.length() > 0)
				{
					GL.that.RunOnGL(new runOnGL()
					{

						@Override
						public void run()
						{
							GL_MsgBox.Show(GroundspeakAPI.LastAPIError, Translation.Get("Error"), MessageBoxIcon.Error);
						}
					});
				}

				if (wd != null) wd.close();
				TB_Log.this.finish();

				// Refresh TB List after Droped Off or Picked or Grabed
				if (LT == LogTypes.dropped_off || LT == LogTypes.retrieve || LT == LogTypes.grab_it)
				{
					GL.that.RunOnGL(new runOnGL()
					{

						@Override
						public void run()
						{
							TrackableListView.that.RefreshTbList();
						}
					});
				}

			}
		});

	}

	private void createFieldNote()
	{
		FieldNoteEntry newFieldNote;
		newFieldNote = new FieldNoteEntry(LT);
		newFieldNote.CacheName = getCache_Name();
		newFieldNote.gcCode = getCache_GcCode();
		newFieldNote.foundNumber = Config.settings.FoundOffset.getValue();
		newFieldNote.timestamp = new Date();
		newFieldNote.CacheId = getCache_ID();
		newFieldNote.comment = edit.getText();
		newFieldNote.CacheUrl = getCache_URL();
		newFieldNote.cacheType = getCache_Type();
		newFieldNote.isTbFieldNote = true;
		newFieldNote.TbName = TB.getName();
		newFieldNote.TbIconUrl = TB.getIconUrl();
		newFieldNote.TravelBugCode = TB.getGcCode();
		newFieldNote.TrackingNumber = TB.getTrackingNumber();
		newFieldNote.fillType();
		newFieldNote.WriteToDatabase();

		TB_Log.this.finish();
	}

	private String getCache_GcCode()
	{
		/**
		 * Muss je nach LogType leer oder gefüllt sein
		 */
		return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().GcCode
				: "";
	}

	private String getCache_Name()
	{
		/**
		 * Muss je nach LogType leer oder gefüllt sein
		 */
		return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().Name : "";
	}

	private long getCache_ID()
	{
		/**
		 * Muss je nach LogType leer oder gefüllt sein
		 */
		return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().Id : -1;
	}

	private String getCache_URL()
	{
		/**
		 * Muss je nach LogType leer oder gefüllt sein
		 */
		return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().Url : "";
	}

	private int getCache_Type()
	{
		/**
		 * Muss je nach LogType leer oder gefüllt sein
		 */
		return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().Type
				.ordinal() : -1;
	}

	@Override
	public void dispose()
	{
		that = null;
		TB = null;

		if (btnClose != null) btnClose.dispose();
		btnClose = null;

		if (btnAction != null) btnAction.dispose();
		btnAction = null;

		if (icon != null) icon.dispose();
		icon = null;

		if (lblName != null) lblName.dispose();
		btnAction = null;

		if (lblName != null) lblName.dispose();
		btnAction = null;

		if (contentBox != null) contentBox.dispose();
		contentBox = null;

		if (edit != null) edit.dispose();
		edit = null;

		LT = null;

	}
}

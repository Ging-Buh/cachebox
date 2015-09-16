/* 
 * Copyright (C) 2014-2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI.GL_UI.Activitys;

import java.util.Date;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Enums.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.FieldNoteEntry;
import CB_Core.Types.Trackable;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.TemplateFormatter;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GL_UI.Views.TrackableListView;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.RadioButton;
import CB_UI_Base.GL_UI.Controls.RadioGroup;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.Dialogs.WaitDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Interfaces.cancelRunnable;

public class TB_Log extends ActivityBase {
    public static TB_Log that;
    private Trackable TB;
    private Button btnClose;
    private ImageButton btnAction;
    private Image icon, CacheIcon;
    private Label lblName, lblPlaced;
    private Box contentBox;
    private LogTypes LT;
    private EditTextField edit;
    private RadioButton rbDirectLog;
    private RadioButton rbOnlyFieldNote;

    public TB_Log() {
	super(ActivityRec(), "TB_Log_Activity");
	createControls();
	that = this;
    }

    public void Show(Trackable TB, LogTypes Type) {
	this.TB = TB;
	this.LT = Type;
	layout();
	GL.that.showActivity(this);
    }

    private void createControls() {

	btnClose = new Button("Close");
	btnClose.setText(Translation.Get("close"));
	btnClose.setOnClickListener(new OnClickListener() {
	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		TB_Log.this.finish();
		return true;
	    }
	});

	btnAction = new ImageButton("Action");
	btnAction.setOnClickListener(new OnClickListener() {
	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		LogNow();
		return true;
	    }
	});

	contentBox = new Box(ActivityRec(), "ContentBox");
	contentBox.setHeight(this.getHeight() - (btnClose.getHeight() - margin) * 2.5f);
	contentBox.setBackground(SpriteCacheBase.activityBackground);

	CB_RectF iconRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight());
	iconRec = iconRec.ScaleCenter(0.8f);

	icon = new Image(iconRec, "Icon", false);
	lblName = new Label(iconRec, "Name");

	CacheIcon = new Image(iconRec, "CacheIcon", false);
	lblPlaced = new Label(iconRec, "CacheName");

	edit = new EditTextField().setWrapType(WrapType.WRAPPED);
	edit.setWidth(contentBox.getInnerWidth());
	edit.setHeight(contentBox.getHalfHeight());

	rbDirectLog = new RadioButton("direct_Log");
	rbOnlyFieldNote = new RadioButton("only_FieldNote");

	rbDirectLog.setText(Translation.Get("directLog"));
	rbOnlyFieldNote.setText(Translation.Get("onlyFieldNote"));

	RadioGroup Group = new RadioGroup();
	Group.add(rbOnlyFieldNote);
	Group.add(rbDirectLog);
	if (Config.TB_DirectLog.getValue()) {
	    rbDirectLog.setChecked(true);
	} else {
	    rbOnlyFieldNote.setChecked(true);
	}
    }

    private void layout() {
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
	if (LT == LogTypes.discovered || LT == LogTypes.visited || LT == LogTypes.dropped_off || LT == LogTypes.retrieve) {

	    Cache c = GlobalCore.getSelectedCache();
	    if (c == null) {
		// Log Inposible, close Activity and give a Message
		final String errorMsg = Translation.Get("NoCacheSelect");
		this.finish();

		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			GL_MsgBox.Show(errorMsg, "", MessageBoxIcon.Error);
		    }
		});
		return;
	    }

	    String msg = "";
	    if (LT == LogTypes.discovered) {
		msg = Translation.Get("discoveredAt") + ": " + GlobalCore.br + c.getName();
	    }
	    if (LT == LogTypes.visited) {
		msg = Translation.Get("visitedAt") + ": " + GlobalCore.br + c.getName();
	    }
	    if (LT == LogTypes.dropped_off) {
		msg = Translation.Get("dropped_offAt") + ": " + GlobalCore.br + c.getName();
	    }
	    if (LT == LogTypes.retrieve) {
		msg = Translation.Get("retrieveAt") + ": " + GlobalCore.br + c.getName();
	    }

	    CacheIcon.setSprite(SpriteCacheBase.BigIcons.get(c.Type.ordinal()), false);

	    lblPlaced.setWidth(contentBox.getInnerWidth() - CacheIcon.getWidth() - (margin * 3));
	    lblPlaced.setWrappedText(msg);
	    lblPlaced.setHeight(lblPlaced.getTextHeight() + lblPlaced.getTopHeight() + lblPlaced.getBottomHeight());

	    contentBox.setMargins(margin, margin * 3);
	    contentBox.setRowYPos(contentBox.getRowYPos() - (margin * 3));

	    contentBox.addNext(CacheIcon, FIXED);
	    contentBox.addLast(lblPlaced);

	}

	this.setMargins(margin * 2, 0);
	this.addNext(icon, FIXED);
	icon.setImageURL(TB.getIconUrl());
	lblName.setWrappedText(TB.getName());
	this.addLast(lblName);

	switch (this.LT) {
	case discovered:
	    btnAction.setImage(SpriteCacheBase.Icons.get(IconName.tbDiscover_58.ordinal()));
	    edit.setText(TemplateFormatter.ReplaceTemplate(Config.DiscoverdTemplate.getValue(), TB));
	    break;
	case visited:
	    btnAction.setImage(SpriteCacheBase.Icons.get(IconName.tbVisit_62.ordinal()));
	    edit.setText(TemplateFormatter.ReplaceTemplate(Config.VisitedTemplate.getValue(), TB));
	    break;
	case dropped_off:
	    btnAction.setImage(SpriteCacheBase.Icons.get(IconName.tbDrop_59.ordinal()));
	    edit.setText(TemplateFormatter.ReplaceTemplate(Config.DroppedTemplate.getValue(), TB));
	    break;
	case grab_it:
	    btnAction.setImage(SpriteCacheBase.Icons.get(IconName.tbGrab_60.ordinal()));
	    edit.setText(TemplateFormatter.ReplaceTemplate(Config.GrabbedTemplate.getValue(), TB));
	    break;
	case retrieve:
	    btnAction.setImage(SpriteCacheBase.Icons.get(IconName.tbPicked_61.ordinal()));
	    edit.setText(TemplateFormatter.ReplaceTemplate(Config.PickedTemplate.getValue(), TB));
	    break;
	case note:
	    btnAction.setImage(SpriteCacheBase.Icons.get(IconName.tbNote_63.ordinal()));
	    edit.setText("");
	    break;
	default:
	    break;
	}
    }

    static WaitDialog wd;

    private void LogNow() {
	if (rbDirectLog.isChecked())
	    logOnline();
	else
	    createFieldNote();

    }

    private void logOnline() {

	wd = CancelWaitDialog.ShowWait("Upload Log", DownloadAnimation.GetINSTANCE(), new IcancelListner() {

	    @Override
	    public void isCanceld() {

	    }
	}, new cancelRunnable() {

	    @Override
	    public void run() {
		GroundspeakAPI.LastAPIError = "";
		int result = GroundspeakAPI.createTrackableLog(TB, getCache_GcCode(), LogTypes.CB_LogType2GC(LT), new Date(), edit.getText(), null);

		if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
		    GL.that.Toast(ConnectionError.INSTANCE);
		    if (wd != null)
			wd.close();
		    GL_MsgBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {

			@Override
			public boolean onClick(int which, Object data) {
			    switch (which) {
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
		if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
		    GL.that.Toast(ApiUnavailable.INSTANCE);
		    if (wd != null)
			wd.close();
		    GL_MsgBox.Show(Translation.Get("CreateFieldnoteInstead"), Translation.Get("UploadFailed"), MessageBoxButtons.YesNoRetry, MessageBoxIcon.Question, new OnMsgBoxClickListener() {

			@Override
			public boolean onClick(int which, Object data) {
			    switch (which) {
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

		if (GroundspeakAPI.LastAPIError.length() > 0) {
		    GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
			    GL_MsgBox.Show(GroundspeakAPI.LastAPIError, Translation.Get("Error"), MessageBoxIcon.Error);
			}
		    });
		}

		if (wd != null)
		    wd.close();
		TB_Log.this.finish();

		// Refresh TB List after Droped Off or Picked or Grabed
		if (LT == LogTypes.dropped_off || LT == LogTypes.retrieve || LT == LogTypes.grab_it) {
		    GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
			    TrackableListView.that.RefreshTbList();
			}
		    });
		}

	    }

	    @Override
	    public boolean cancel() {
		// TODO Handle Cancel
		return false;
	    }
	});

    }

    private void createFieldNote() {
	FieldNoteEntry newFieldNote;
	newFieldNote = new FieldNoteEntry(LT);
	newFieldNote.CacheName = getCache_Name();
	newFieldNote.gcCode = getCache_GcCode();
	newFieldNote.foundNumber = Config.FoundOffset.getValue();
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

    private String getCache_GcCode() {
	/**
	 * Muss je nach LogType leer oder gefüllt sein
	 */
	return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().getGcCode() : "";
    }

    private String getCache_Name() {
	/**
	 * Muss je nach LogType leer oder gefüllt sein
	 */
	return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().getName() : "";
    }

    private long getCache_ID() {
	/**
	 * Muss je nach LogType leer oder gefüllt sein
	 */
	return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().Id : -1;
    }

    private String getCache_URL() {
	/**
	 * Muss je nach LogType leer oder gefüllt sein
	 */
	return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().getUrl() : "";
    }

    private int getCache_Type() {
	/**
	 * Muss je nach LogType leer oder gefüllt sein
	 */
	return (LT == LogTypes.dropped_off || LT == LogTypes.visited || LT == LogTypes.retrieve) ? GlobalCore.getSelectedCache().Type.ordinal() : -1;
    }

    @Override
    public void dispose() {
	that = null;
	TB = null;

	if (btnClose != null)
	    btnClose.dispose();
	btnClose = null;

	if (btnAction != null)
	    btnAction.dispose();
	btnAction = null;

	if (icon != null)
	    icon.dispose();
	icon = null;

	if (lblName != null)
	    lblName.dispose();
	btnAction = null;

	if (lblName != null)
	    lblName.dispose();
	btnAction = null;

	if (contentBox != null)
	    contentBox.dispose();
	contentBox = null;

	if (edit != null)
	    edit.dispose();
	edit = null;

	LT = null;

    }
}

/* 
 * Copyright (C) 2015 team-cachebox.de
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.FieldNoteEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView.FilterSetEntry;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListViewItem;
import CB_UI.GL_UI.Views.FieldNoteViewItem;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.RadioButton;
import CB_UI_Base.GL_UI.Controls.RadioGroup;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.math.Vector2;

public class EditFieldNotes extends ActivityBase implements KeyboardFocusChangedEvent {
    private FieldNoteEntry altfieldNote;
    private FieldNoteEntry fieldNote;
    private Button bOK = null;
    private Button bCancel = null;
    private Label tvCacheName = null;
    private EditTextField etComment = null;
    private Image ivTyp = null;
    private Image ivTbIcon = null;
    private Label tvFounds = null;
    private EditTextField tvDate = null;
    private EditTextField tvTime = null;
    private Label lblDate = null;
    private Label lblTime = null;
    private Box scrollBox = null;
    FilterSetListViewItem GcVote;
    private boolean isNewFieldNote = false;

    private RadioButton rbDirectLog;
    private RadioButton rbOnlyFieldNote;

    public interface ReturnListner {
	public void returnedFieldNote(FieldNoteEntry fn, boolean isNewFieldNote, boolean directlog);
    }

    private ReturnListner mReturnListner;

    public EditFieldNotes(FieldNoteEntry note, ReturnListner listner, boolean isNewFieldNote) {
	super(ActivityBase.ActivityRec(), "");
	setFieldNote(note, listner, isNewFieldNote);

	scrollBox = new Box(ActivityBase.ActivityRec(), "");
	this.addChild(scrollBox);
	iniOkCancel();
	iniNameLabel();
	iniImage();
	iniFoundLabel();
	iniDate();
	iniTime();
	iniGC_VoteItem();
	iniCommentTextField();
	if (note.type.isDirectLogType())
	    iniOptions(note, isNewFieldNote);// show only if possible
	iniTextfieldFocus();

    }

    private void setDefaultValues() {
	tvCacheName.setText(fieldNote.isTbFieldNote ? fieldNote.TbName : fieldNote.CacheName);

	if (fieldNote.isTbFieldNote)
	    tvFounds.setText("");
	else
	    tvFounds.setText("Founds: #" + fieldNote.foundNumber);
	DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
	String sDate = iso8601Format.format(fieldNote.timestamp);
	tvDate.setText(sDate);
	iso8601Format = new SimpleDateFormat("HH:mm");
	String sTime = iso8601Format.format(fieldNote.timestamp);
	tvTime.setText(sTime);
	ivTyp.setDrawable(FieldNoteViewItem.getTypeIcon(fieldNote));
	if (fieldNote.isTbFieldNote)
	    ivTbIcon.setImageURL(fieldNote.TbIconUrl);
    }

    private void iniOkCancel() {
	CB_RectF btnRec = new CB_RectF(leftBorder, this.getBottomHeight(), innerWidth / 2, UI_Size_Base.that.getButtonHeight());
	bOK = new Button(btnRec, "OkButton");
	bOK.setText(Translation.Get("ok"));

	btnRec.setX(bOK.getMaxX());
	bCancel = new Button(btnRec, "CancelButton");
	bCancel.setText(Translation.Get("cancel"));

	this.addChild(bOK);
	this.addChild(bCancel);

	bOK.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		if (mReturnListner != null) {

		    if (fieldNote.type.isDirectLogType()) {
			fieldNote.isDirectLog = rbDirectLog.isChecked();
		    } else {
			fieldNote.isDirectLog = false;
		    }

		    fieldNote.comment = etComment.getText();
		    if (GcVote != null) {
			fieldNote.gc_Vote = (int) (GcVote.getValue() * 100);
		    }

		    // parse Date and Time
		    String date = tvDate.getText();
		    String time = tvTime.getText();

		    date = date.replace("-", ".");
		    time = time.replace(":", ".");

		    try {
			Date timestamp;
			DateFormat formatter;

			formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
			timestamp = (Date) formatter.parse(date + "." + time + ".00");

			fieldNote.timestamp = timestamp;
		    } catch (ParseException e) {
			final GL_MsgBox msg = GL_MsgBox.Show(Translation.Get("wrongDate"), Translation.Get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, new OnMsgBoxClickListener() {

			    @Override
			    public boolean onClick(int which, Object data) {
				Timer runTimer = new Timer();
				TimerTask task = new TimerTask() {

				    @Override
				    public void run() {
					EditFieldNotes.this.show();
				    }
				};

				runTimer.schedule(task, 200);

				return true;
			    }
			});

			Timer runTimer = new Timer();
			TimerTask task = new TimerTask() {

			    @Override
			    public void run() {
				GL.that.showDialog(msg);
			    }
			};

			runTimer.schedule(task, 200);
			return true;
		    }

		    // check of changes
		    if (!altfieldNote.equals(fieldNote)) {
			fieldNote.uploaded = false;
			fieldNote.UpdateDatabase();
		    }

		    boolean dl = false;
		    if (fieldNote.isDirectLog)
			dl = true;

		    mReturnListner.returnedFieldNote(fieldNote, isNewFieldNote, dl);
		}
		finish();
		return true;
	    }
	});

	bCancel.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		if (mReturnListner != null)
		    mReturnListner.returnedFieldNote(null, false, false);
		finish();
		return true;
	    }
	});

    }

    private void iniNameLabel() {
	tvCacheName = new Label(leftBorder + margin, getHeight() - this.getTopHeight() - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight, "CacheNameLabel");
	tvCacheName.setFont(Fonts.getBig());
	tvCacheName.setText(fieldNote.CacheName);
	scrollBox.addChild(tvCacheName);
    }

    private float secondTab = 0;

    private void iniImage() {
	ivTyp = new Image(leftBorder + margin, tvCacheName.getY() - margin - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), "", false);
	scrollBox.addChild(ivTyp);

	ivTbIcon = new Image(ivTyp.getMaxX() + (margin * 3), tvCacheName.getY() - margin - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), "", false);
	scrollBox.addChild(ivTbIcon);

	secondTab = ivTyp.getMaxX() + (margin * 3);
    }

    private void iniFoundLabel() {
	tvFounds = new Label(secondTab, ivTyp.getMaxY() - UI_Size_Base.that.getButtonHeight(), getWidth() - secondTab - rightBorder - margin, UI_Size_Base.that.getButtonHeight(), "CacheNameLabel");
	tvFounds.setFont(Fonts.getBig());
	scrollBox.addChild(tvFounds);
    }

    private float LabelWidth = 0;

    private void iniDate() {
	LabelWidth = Math.max(Fonts.Measure(Translation.Get("date")).width, Fonts.Measure(Translation.Get("time")).width);
	LabelWidth *= 1.3;// use Big Font

	lblDate = new Label(secondTab, tvFounds.getY() - UI_Size_Base.that.getButtonHeight() - (margin * 3), LabelWidth, UI_Size_Base.that.getButtonHeight(), "");
	lblDate.setFont(Fonts.getBig());
	lblDate.setText(Translation.Get("date") + ":");
	scrollBox.addChild(lblDate);
	CB_RectF rec = new CB_RectF(lblDate.getMaxX() + margin, lblDate.getY() - margin, getWidth() - lblDate.getMaxX() - margin - rightBorder, UI_Size_Base.that.getButtonHeight());

	tvDate = new EditTextField(rec, this);
	scrollBox.addChild(tvDate);
    }

    private void iniTime() {

	lblTime = new Label(secondTab, lblDate.getY() - UI_Size_Base.that.getButtonHeight() - (margin * 3), LabelWidth, UI_Size_Base.that.getButtonHeight(), "");
	lblTime.setFont(Fonts.getBig());
	lblTime.setText(Translation.Get("time") + ":");
	scrollBox.addChild(lblTime);
	CB_RectF rec = new CB_RectF(lblTime.getMaxX() + margin, lblTime.getY() - margin, getWidth() - lblTime.getMaxX() - margin - rightBorder, UI_Size_Base.that.getButtonHeight());

	tvTime = new EditTextField(rec, this);
	scrollBox.addChild(tvTime);
    }

    private void iniGC_VoteItem() {

	if (!CB_Core_Settings.GcVotePassword.getEncryptedValue().equalsIgnoreCase("")) {
	    float itemHeight = UI_Size_Base.that.getButtonHeight() * 1.1f;

	    FilterSetEntry tmp = new FilterSetEntry(Translation.Get("maxRating"), SpriteCacheBase.Stars.toArray(), FilterSetListView.NUMERICK_ITEM, 0, 5, fieldNote.gc_Vote / 100.0, 0.5f);
	    GcVote = new FilterSetListViewItem(new CB_RectF(leftBorder, lblTime.getY() - itemHeight - margin, innerWidth, itemHeight), 0, tmp);
	    scrollBox.addChild(GcVote);
	}

    }

    private void iniCommentTextField() {
	CB_RectF rec;
	if (GcVote != null) {
	    rec = new CB_RectF(leftBorder, GcVote.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth, UI_Size_Base.that.getButtonHeight());
	} else {
	    rec = new CB_RectF(leftBorder, lblTime.getY() - UI_Size_Base.that.getButtonHeight() - margin, innerWidth, UI_Size_Base.that.getButtonHeight());
	}

	etComment = new EditTextField(this, rec, WrapType.WRAPPED, "DescTextField");
	etComment.setText(fieldNote.comment);

	// set Size to linecount
	float maxTextFieldHeight = this.getHeight() / 2.5f;
	float rand = etComment.getStyle().background.getBottomHeight() + etComment.getStyle().background.getTopHeight();
	float descriptionHeight = Math.min(maxTextFieldHeight, etComment.getMeasuredHeight() + rand);
	descriptionHeight = Math.max(descriptionHeight, UI_Size_Base.that.getButtonHeight());
	etComment.setHeight(descriptionHeight);
	if (GcVote != null) {
	    etComment.setY(GcVote.getY() - descriptionHeight);
	} else {
	    etComment.setY(lblTime.getY() - descriptionHeight - margin);
	}

	etComment.setTextFieldListener(new TextFieldListener() {

	    @Override
	    public void keyTyped(EditTextFieldBase textField, char key) {

	    }

	    @Override
	    public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight) {
		layoutTextFields();
	    }
	});

	scrollBox.addChild(etComment);

    }

    private void iniOptions(FieldNoteEntry note, boolean isNewFieldNote) {
	rbDirectLog = new RadioButton("direct_Log");
	rbOnlyFieldNote = new RadioButton("only_FieldNote");

	rbDirectLog.setText(Translation.Get("directLog"));
	rbOnlyFieldNote.setText(Translation.Get("onlyFieldNote"));

	RadioGroup Group = new RadioGroup();
	Group.add(rbOnlyFieldNote);
	Group.add(rbDirectLog);

	// layout
	rbDirectLog.setWidth(scrollBox.getWidth());
	rbOnlyFieldNote.setWidth(scrollBox.getWidth());

	rbDirectLog.setY(etComment.getY() - margin - rbDirectLog.getHeight());
	rbOnlyFieldNote.setY(rbDirectLog.getY() - margin - rbOnlyFieldNote.getHeight());

	scrollBox.addChild(rbDirectLog);
	scrollBox.addChild(rbOnlyFieldNote);

	if (isNewFieldNote) {
	    rbOnlyFieldNote.setChecked(true);
	} else {
	    if (note.isDirectLog) {
		rbDirectLog.setChecked(true);
	    } else {
		rbOnlyFieldNote.setChecked(true);
	    }
	}

    }

    private void iniTextfieldFocus() {
	registerTextField(etComment);
	registerTextField(tvDate);
	registerTextField(tvTime);
    }

    private ArrayList<EditTextField> allTextFields = new ArrayList<EditTextField>();

    public void registerTextField(final EditTextField textField) {
	textField.setOnscreenKeyboard(new OnscreenKeyboard() {
	    @Override
	    public void show(boolean arg0) {
		scrollToY(textField.getY(), textField.getMaxY());
	    }
	});

	allTextFields.add(textField);
    }

    private void scrollToY(float y, float maxY) {
	if (y < this.getHalfHeight())// wird von softKeyboard verdeckt
	{
	    scrollBox.setY(this.getHeight() - maxY - MeasuredLabelHeight);
	} else {
	    scrollBox.setY(0);
	}
    }

    private void layoutTextFields() {
	float maxTextFieldHeight = this.getHeight() / 2.5f;
	float rand = etComment.getStyle().background.getBottomHeight() + etComment.getStyle().background.getTopHeight();
	float descriptionHeight = Math.min(maxTextFieldHeight, etComment.getMeasuredHeight() + rand);

	descriptionHeight = Math.max(descriptionHeight, UI_Size_Base.that.getButtonHeight());

	etComment.setHeight(descriptionHeight);

	if (GcVote != null) {
	    etComment.setY(GcVote.getY() - descriptionHeight);
	} else {
	    etComment.setY(lblTime.getY() - descriptionHeight - margin);
	}

	if (rbDirectLog != null)
	    rbDirectLog.setY(etComment.getY() - margin - rbDirectLog.getHeight());
	if (rbOnlyFieldNote != null)
	    rbOnlyFieldNote.setY(rbDirectLog.getY() - margin - rbOnlyFieldNote.getHeight());

	scrollToY(etComment.getY(), etComment.getMaxY());

    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
	super.onTouchDown(x, y, pointer, button);

	if (etComment.contains(x, y)) {
	    // TODO close SoftKeyboard
	    scrollBox.setY(0);
	}

	// for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
	for (Iterator<GL_View_Base> iterator = scrollBox.getchilds().reverseIterator(); iterator.hasNext();) {
	    // Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
	    GL_View_Base view = iterator.next();

	    if (view instanceof FilterSetListViewItem) {
		if (view.contains(x, y)) {
		    ((FilterSetListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());
		}
	    }
	}
	return true;
    }

    @Override
    public void dispose() {
	super.dispose();
	mReturnListner = null;
	fieldNote = null;
	bOK = null;
	bCancel = null;
	tvCacheName = null;
	etComment = null;
	ivTyp = null;
	tvFounds = null;
	tvDate = null;
	tvTime = null;
	lblDate = null;
	lblTime = null;
	scrollBox = null;
	GcVote = null;
    }

    @Override
    public void onShow() {
	KeyboardFocusChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
	KeyboardFocusChangedEventList.Remove(this);
    }

    @Override
    public void KeyboardFocusChanged(EditTextFieldBase focus) {
	if (focus == null) {
	    if (scrollBox != null)
		scrollBox.setY(0);
	}
    }

    public void setFieldNote(FieldNoteEntry note, ReturnListner listner, boolean isNewFieldNote) {
	this.isNewFieldNote = isNewFieldNote;
	mReturnListner = listner;
	fieldNote = note;
	altfieldNote = note.copy();
	setDefaultValues();
    }
}

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

import CB_Core.CB_Core_Settings;
import CB_Core.Database;
import CB_Core.Types.Draft;
import CB_Core.Types.Drafts;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListView.FilterSetEntry;
import CB_UI.GL_UI.Activitys.FilterSettings.FilterSetListViewItem;
import CB_UI.GL_UI.Views.DraftViewItem;
import CB_UI.TemplateFormatter;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.text.InputType;
import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class EditDraft extends ActivityBase implements KeyboardFocusChangedEvent {
    private FilterSetListViewItem GcVote;
    private CB_Label title;
    private Draft altfieldNote;
    private Draft draft;
    private CB_Button btnOK = null;
    private CB_Button btnCancel = null;
    private EditTextField etComment = null;
    private Image ivTyp = null;
    private CB_Label tvFounds = null;
    private EditTextField tvDate = null;
    private EditTextField tvTime = null;
    private ScrollBox scrollBox = null;
    private Box scrollBoxContent;
    private boolean isNewDraft;
    private RadioButton rbDirectLog;
    private RadioButton rbOnlyDraft;
    private IReturnListener mReturnListener;
    private CB_Button btnHow;

    public EditDraft(Draft note, IReturnListener listener, boolean isNewDraft) {
        super(ActivityBase.ActivityRec(), "");
        this.isNewDraft = isNewDraft;
        mReturnListener = listener;
        draft = note;
        altfieldNote = note.copy();
        initLayoutWithValues();
    }

    public GL_View_Base touchDown(int x, int y, int pointer, int button) {
        if (GcVote != null && GcVote.getWorldRec().contains(x, y)) {
            GcVote.onTouchDown(x, y, pointer, button);
            GcVote.lastItemTouchPos = new Vector2(x - GcVote.getWorldRec().getX(), y - GcVote.getWorldRec().getY());
            return GcVote;
        } else {
            return super.touchDown(x, y, pointer, button);
        }
    }

    private void initLayoutWithValues() {
        initRow(BOTTOMUP);
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        addNext(btnOK);
        addLast(btnCancel);
        scrollBox = new ScrollBox(innerWidth, getAvailableHeight());
        scrollBox.setBackground(this.getBackground());

        scrollBoxContent = new Box(scrollBox.getInnerWidth(), 0);
        scrollBoxContent.initRow(BOTTOMUP);
        if (draft.type.isDirectLogType())
            iniOptions();
        iniGC_VoteItem();
        initLogText();
        iniDate();
        iniTitle();
        scrollBoxContent.adjustHeight();

        scrollBox.setVirtualHeight(scrollBoxContent.getHeight());
        scrollBox.addChild(scrollBoxContent);
        addLast(scrollBox);

        setOkAndCancelClickHandlers();
        etComment.showLastLines();
    }

    private void setValuesToLayout() {
        // initLogText
        etComment.setText(draft.comment);
        // Date
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = iso8601Format.format(draft.timestamp);
        tvDate.setText(sDate);
        // Time
        iso8601Format = new SimpleDateFormat("HH:mm");
        String sTime = iso8601Format.format(draft.timestamp);
        tvTime.setText(sTime);
        // iniOptions();
        if (draft.type.isDirectLogType()) {
            if (isNewDraft) {
                rbOnlyDraft.setChecked(true);
            } else {
                if (draft.isDirectLog) {
                    rbDirectLog.setChecked(true);
                } else {
                    rbOnlyDraft.setChecked(true);
                }
            }
            rbDirectLog.setChecked(true);
        } else {
            rbOnlyDraft.setChecked(true);
        }
        tvFounds.setText("#" + draft.foundNumber);
        if (draft.isTbDraft)
            tvFounds.setText("");
        //
        title.setText(draft.isTbDraft ? draft.TbName : draft.CacheName);
    }

    private void setOkAndCancelClickHandlers() {
        btnOK.addClickHandler((v, x, y, pointer, button) -> {
            if (mReturnListener != null) {

                if (draft.type.isDirectLogType()) {
                    draft.isDirectLog = rbDirectLog.isChecked();
                } else {
                    draft.isDirectLog = false;
                }

                draft.comment = etComment.getText();

                if (GcVote != null) {
                    draft.gc_Vote = (int) (GcVote.getValue() * 100);
                } else draft.gc_Vote = 0;

                // parse Date and Time
                String date = tvDate.getText();
                String time = tvTime.getText();

                date = date.replace("-", ".");
                time = time.replace(":", ".");

                try {
                    Date timestamp;
                    DateFormat formatter;

                    formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
                    timestamp = formatter.parse(date + "." + time + ".00");

                    draft.timestamp = timestamp;
                } catch (ParseException e) {
                    final MessageBox msg = MessageBox.show(Translation.get("wrongDate"), Translation.get("Error"), MessageBoxButtons.OK, MessageBoxIcon.Error, new OnMsgBoxClickListener() {

                        @Override
                        public boolean onClick(int which, Object data) {
                            Timer runTimer = new Timer();
                            TimerTask task = new TimerTask() {

                                @Override
                                public void run() {
                                    show();
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
                if (!altfieldNote.equals(draft)) {
                    draft.uploaded = false;
                    draft.UpdateDatabase();
                    Drafts.CreateGeoCacheVisits(Config.DraftsGarminPath.getValue());
                }

                boolean dl = false;
                if (draft.isDirectLog)
                    dl = true;

                mReturnListener.returnedFieldNote(draft, isNewDraft, dl);
            }
            finish();
            return true;
        });

        btnCancel.addClickHandler((v, x, y, pointer, button) -> {
            if (mReturnListener != null)
                mReturnListener.returnedFieldNote(null, false, false);
            finish();
            return true;
        });

    }

    private void iniTitle() {
        ivTyp = new Image(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), "", false);
        if (draft.isTbDraft) {
            ivTyp.setImageURL(draft.TbIconUrl);
        } else {
            ivTyp.setDrawable(DraftViewItem.getTypeIcon(draft));
        }
        scrollBoxContent.addNext(ivTyp, FIXED);

        title = new CB_Label(draft.isTbDraft ? draft.TbName : draft.CacheName);
        title.setFont(Fonts.getBig());
        scrollBoxContent.addLast(title);
    }

    private void iniDate() {
        tvFounds = new CB_Label("#" + draft.foundNumber);
        if (draft.isTbDraft)
            tvFounds.setText("");
        tvFounds.setFont(Fonts.getBig());
        tvFounds.setWidth(tvFounds.getTextWidth());
        scrollBoxContent.addNext(tvFounds, FIXED);

        tvDate = new EditTextField(this, "*" + Translation.get("date"));
        tvDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        scrollBoxContent.addNext(tvDate, 0.4f);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = iso8601Format.format(draft.timestamp);
        tvDate.setText(sDate);
        tvTime = new EditTextField(this, "*" + Translation.get("time"));
        tvTime.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
        scrollBoxContent.addLast(tvTime, 0.4f);
        String sTime = new SimpleDateFormat("HH:mm").format(draft.timestamp);
        tvTime.setText(sTime);
    }

    private void iniGC_VoteItem() {
        if (CB_Core_Settings.GcVotePassword.getEncryptedValue().length() > 0) {
            if (!draft.isTbDraft) {
                FilterSetEntry tmp = new FilterSetEntry(Translation.get("maxRating"), Sprites.Stars.toArray(), FilterSetListView.NUMERIC_ITEM, 0, 5, draft.gc_Vote / 100.0, 0.5f);
                GcVote = new FilterSetListViewItem(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 1.1f), 0, tmp);
                scrollBoxContent.addLast(GcVote);
            }
        }
    }

    private void initLogText() {
        etComment = new EditTextField(this, "etComment").setWrapType(WrapType.WRAPPED);
        etComment.setHeight(getHeight() / 2.5f);
        scrollBoxContent.addLast(etComment);
        etComment.setText(draft.comment);

        btnHow = new CB_Button("=");
        btnHow.addClickHandler((v, x, y, pointer, button) -> {
            if (btnHow.getText().equals("="))
                btnHow.setText("+");
            else if (btnHow.getText().equals("+"))
                btnHow.setText("|");
            else btnHow.setText("=");
            return true;
        });
        scrollBoxContent.addNext(btnHow, FIXED);

        CB_Button btnFromNotes = new CB_Button(Translation.get("fromNotes"));
        btnFromNotes.addClickHandler((v, x, y, pointer, button) -> {
            String text = Database.GetNote(draft.CacheId);
            if (text.length() > 0) {
                String sBegin = "<Import from Geocaching.com>";
                String sEnd = "</Import from Geocaching.com>";
                int iBegin = text.indexOf(sBegin);
                int iEnd = text.indexOf(sEnd) + sEnd.length();
                if (iBegin > 0 && iEnd > 0 && iBegin < iEnd) {
                    text = text.substring(0, iBegin) + text.substring(iEnd);
                }
                sBegin = "<Solver>";
                sEnd = "</Solver>";
                iBegin = text.indexOf(sBegin);
                iEnd = text.indexOf(sEnd) + sEnd.length();
                if (iBegin > 0 && iEnd > 0 && iBegin < iEnd) {
                    text = text.substring(0, iBegin).trim() + text.substring(iEnd).trim();
                }
            }
            setupLogText(text);
            return true;
        });
        scrollBoxContent.addNext(btnFromNotes);

        CB_Button btnFromFile = new CB_Button(Translation.get("fromFile"));
        btnFromFile.addClickHandler((v, x, y, pointer, button) -> {
            String mPath = Config.TemplateLastUsedPath.getValue();
            if (mPath.length() == 0) {
                mPath = Config.mWorkPath + "/User";
            }
            mPath = mPath + "/" + Config.TemplateLastUsedName.getValue();
            PlatformConnector.getFile(mPath, "*.txt", Translation.get("TemplateTitleSelect"), Translation.get("TemplateButtonSelect"), PathAndName -> {
                File file = FileFactory.createFile(PathAndName);
                BufferedReader br = null;
                String strLine;
                StringBuilder text = new StringBuilder();
                try {
                    br = new BufferedReader(new InputStreamReader(file.getFileInputStream()));
                    while ((strLine = br.readLine()) != null) {
                        text.append(strLine).append("\n");
                    }
                    Config.TemplateLastUsedPath.setValue(file.getParent());
                    Config.TemplateLastUsedName.setValue(file.getName());
                    Config.AcceptChanges();
                } catch (Exception ignored) {
                }
                try {
                    if (br != null) br.close();
                } catch (Exception ignored) {
                }
                setupLogText(text.toString());
            });
            return true;
        });
        scrollBoxContent.addLast(btnFromFile);
    }

    private void setupLogText(String text) {
        // todo for ##owner## the cache must be selected (if not first log)
        text = TemplateFormatter.ReplaceTemplate(text, draft);
        switch (btnHow.getText()) {
            case "=":
                etComment.setText(text);
                break;
            case "+":
                etComment.setText(etComment.getText() + text);
                break;
            case "|":
                etComment.setFocus(true);
                for (int i = 0; i < text.length(); i++) {
                    etComment.keyTyped(text.charAt(i));
                }
                etComment.setFocus(false);
                break;
        }
    }

    private void iniOptions() {
        rbDirectLog = new RadioButton("direct_Log");
        rbOnlyDraft = new RadioButton("only_FieldNote");

        rbDirectLog.setText(Translation.get("directLog"));
        rbOnlyDraft.setText(Translation.get("onlyDraft"));

        RadioGroup Group = new RadioGroup();
        Group.add(rbOnlyDraft);
        Group.add(rbDirectLog);

        scrollBoxContent.addLast(rbDirectLog);
        scrollBoxContent.addLast(rbOnlyDraft);

        if (isNewDraft) {
            rbOnlyDraft.setChecked(true);
        } else {
            if (draft.isDirectLog) {
                rbDirectLog.setChecked(true);
            } else {
                rbOnlyDraft.setChecked(true);
            }
        }
        rbDirectLog.setChecked(true);
        rbOnlyDraft.setChecked(false);
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        super.onTouchDown(x, y, pointer, button);

        if (etComment.contains(x, y)) {
            scrollBoxContent.setY(0);
        }

        // for GCVote
        // for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
        for (Iterator<GL_View_Base> iterator = scrollBox.getchilds().reverseIterator(); iterator.hasNext(); ) {
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
        mReturnListener = null;
        draft = null;
        btnOK = null;
        btnCancel = null;
        etComment = null;
        ivTyp = null;
        tvFounds = null;
        tvDate = null;
        tvTime = null;
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
    public void KeyboardFocusChanged(EditTextField editTextField) {
        if (editTextField == null) {
            if (scrollBoxContent != null)
                scrollBoxContent.setY(0);
        } else {
            scrollBoxContent.setY(scrollBoxContent.getHeight() - editTextField.getMaxY());
        }
    }

    public void setDraft(Draft note, IReturnListener listener, boolean isNewFieldNote) {
        this.isNewDraft = isNewFieldNote;
        mReturnListener = listener;
        draft = note;
        setValuesToLayout();
        altfieldNote = note.copy();
    }

    public interface IReturnListener {
        void returnedFieldNote(Draft fn, boolean isNewFieldNote, boolean directlog);
    }
}

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
package de.droidcachebox.gdx.activities;

import static de.droidcachebox.gdx.controls.FilterSetListViewItem.NUMERIC_ITEM;

import android.text.InputType;

import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.Config;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.TemplateFormatter;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.Draft;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.FilterSetListViewItem;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.DraftViewItem;
import de.droidcachebox.menu.menuBtn4.executes.DraftsView;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

public class EditDraft extends ActivityBase implements KeyboardFocusChangedEventList.KeyboardFocusChangedEvent {
    private static final String sKlasse = "EditDraft";
    private final CB_Button btnLog;
    private final CB_Button btnDraft;
    private final CB_Label title;
    private final CB_CheckBox giveFavoritePoint;
    private final CB_Button enableGiveFavoritePoint;
    private final CB_Button btnOK;
    private final CB_Button btnCancel;
    private final EditTextField etComment;
    private final EditTextField tvDate;
    private final EditTextField tvTime;
    private final CB_Label tvFinds;
    private final FilterSetListViewItem gcVoteItem;
    private ScrollBox scrollBox;
    private Box scrollBoxContent;
    private Draft originalDraft;
    private Draft currentDraft;
    private boolean isNewDraft;
    private IDraftsView draftsView;
    private final OnClickListener saveLog = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
            if (draftsView != null) {
                SaveMode clickedBy = SaveMode.OnlyLocal;
                if (view == btnLog) clickedBy = SaveMode.Log;
                else if (view == btnDraft) clickedBy = SaveMode.Draft;
                try {
                    currentDraft.isDirectLog = false;

                    currentDraft.comment = etComment.getText().trim();

                    if (gcVoteItem != null) {
                        currentDraft.gc_Vote = (int) (gcVoteItem.getValue() * 100);
                    } else currentDraft.gc_Vote = 0;
                } catch (Exception ex) {
                    Log.err(sKlasse, ex);
                }
                try {

                    // parse Date and Time
                    String date = tvDate.getText();
                    String time = tvTime.getText();

                    date = date.replace("-", ".");
                    time = time.replace(":", ".");

                    Date timestamp;
                    DateFormat formatter;

                    formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US);
                    timestamp = formatter.parse(date + "." + time + ".00");

                    currentDraft.timestamp = timestamp;
                } catch (ParseException e) {
                    final MsgBox msg = MsgBox.show(Translation.get("wrongDate"), Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error,
                            (which, data) -> {
                                Timer runTimer = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        show();
                                    }
                                };
                                runTimer.schedule(task, 200);
                                return true;
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
                if (!originalDraft.equals(currentDraft)) {
                    currentDraft.uploaded = false;
                    currentDraft.updateDatabase();
                    Log.info(sKlasse, "Draft written to database.");
                    DraftsView.createGeoCacheVisits();
                    Log.info(sKlasse, "GeoCacheVisits written.");
                }
                draftsView.addOrChangeDraft(currentDraft, isNewDraft, clickedBy);
            }
            finish();
            return true;
        }
    };
    private CB_Button btnHow;

    public EditDraft(Draft _draft, IDraftsView _draftsView, boolean _isNewDraft) {
        super("EditDraft");
        btnOK = new CB_Button(Translation.get("ok"));
        btnLog = new CB_Button(Translation.get("GCLog"));
        btnDraft = new CB_Button(Translation.get("GCDraft"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        giveFavoritePoint = new CB_CheckBox(); //favoritePoints
        enableGiveFavoritePoint = new CB_Button(Translation.get("enableGiveFavoritePoint"));
        etComment = new EditTextField(this, "").setWrapType(WrapType.WRAPPED);
        tvDate = new EditTextField(this, "");
        tvTime = new EditTextField(this, "");
        tvFinds = new CB_Label("");
        title = new CB_Label("");
        if (CB_Core_Settings.GcVotePassword.getEncryptedValue().length() > 0) {
            FilterSetListViewItem.FilterSetEntry gcVoteSelection = new FilterSetListViewItem.FilterSetEntry(Translation.get("maxRating"), Sprites.Stars.toArray(), NUMERIC_ITEM, 0, 5, 0, 0.5f);
            gcVoteItem = new FilterSetListViewItem(new CB_RectF(0, 0, innerWidth, UiSizes.getInstance().getButtonHeight() * 1.1f), 0, gcVoteSelection);
        } else gcVoteItem = null;
        // == setDraft
        isNewDraft = _isNewDraft;
        draftsView = _draftsView;
        currentDraft = _draft;
        originalDraft = new Draft(currentDraft);
        setValues();
    }

    public GL_View_Base touchDown(int x, int y, int pointer, int button) {
        if (gcVoteItem != null && gcVoteItem.getWorldRec().contains(x, y)) {
            gcVoteItem.onTouchDown(x, y, pointer, button);
            gcVoteItem.lastItemTouchPos = new Vector2(x - gcVoteItem.getWorldRec().getX(), y - gcVoteItem.getWorldRec().getY());
            return gcVoteItem;
        } else {
            return super.touchDown(x, y, pointer, button);
        }
    }

    private void initLayout() {
        initRow(BOTTOMUP);
        addNext(btnOK);
        addNext(btnLog);
        addNext(btnDraft);
        addLast(btnCancel);
        scrollBox = new ScrollBox(innerWidth, getAvailableHeight());
        scrollBox.setBackground(this.getBackground());

        scrollBoxContent = new Box(scrollBox.getInnerWidth(), 0);
        scrollBoxContent.initRow(BOTTOMUP);
        giveFavoritePoint.setChecked(false);
        if (GroundspeakAPI.hasBeenOnline()) {
            if (GroundspeakAPI.isPremiumMember()) {
                /*
                // for checking we have to expand the database/cache by a field for isFavoritedByMe
                // will get online and save that temporarily here
                // get the geocache from cachelist, from database, from groundspeak
                Cache geoCache = Database.Data.cacheList.getCacheByIdFromCacheList(currentDraft.CacheId);
                if (geoCache == null) {
                    // if filtered try db direct
                    CacheDAO dao = new CacheDAO();
                    geoCache = dao.getFromDbByCacheId(currentDraft.CacheId);
                    if (geoCache == null) {
                        // if not in DB
                        // ask groundspeak db (geocaching.com)
                        // or simply try to write
                    }
                }
                 */
                if (GroundspeakAPI.fetchMyUserInfos().favoritePoints > 0) {
                    GroundspeakAPI.cacheIsFavoritedByMe = false;
                    // ArrayList<GroundspeakAPI.GeoCacheRelated> result =
                    GroundspeakAPI.fetchGeoCache(new GroundspeakAPI.Query().resultForStatusFields(), currentDraft.gcCode);
                    if (!GroundspeakAPI.cacheIsFavoritedByMe) {
                        scrollBoxContent.addNext(giveFavoritePoint, FIXED);
                        CB_Label lblGiveFavoritePoint = new CB_Label(Translation.get("giveFavoritePoint"));
                        scrollBoxContent.addLast(lblGiveFavoritePoint);
                    }
                }
            }
        }
        if (CB_Core_Settings.GcVotePassword.getEncryptedValue().length() > 0) {
            if (!currentDraft.isTbDraft) {
                gcVoteItem.setValue(currentDraft.gc_Vote / 100.0);
                scrollBoxContent.addLast(gcVoteItem);
            }
        }
        // enable Favpoint
        if (!GroundspeakAPI.hasBeenOnline()) {
            Log.err(sKlasse, "add enable button GiveFavoritePoint");
            scrollBoxContent.addLast(enableGiveFavoritePoint);
        }
        initLogText();
        // no of finds
        if (currentDraft.isTbDraft)
            tvFinds.setText("");
        else
            tvFinds.setText("#" + currentDraft.foundNumber);
        tvFinds.setFont(Fonts.getBig());
        tvFinds.setWidth(tvFinds.getTextWidth());
        scrollBoxContent.addNext(tvFinds, FIXED);
        // Date Time
        tvDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        scrollBoxContent.addNext(tvDate, 0.4f);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String sDate = iso8601Format.format(currentDraft.timestamp);
        tvDate.setText(sDate);
        tvTime.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
        scrollBoxContent.addLast(tvTime, 0.4f);
        String sTime = new SimpleDateFormat("HH:mm", Locale.US).format(currentDraft.timestamp);
        tvTime.setText(sTime);
        // title
        Image ivTyp = new Image(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight(), "", false);
        if (currentDraft.isTbDraft) {
            ivTyp.setImageURL(currentDraft.TbIconUrl);
        } else {
            ivTyp.setDrawable(DraftViewItem.getTypeIcon(currentDraft));
        }
        scrollBoxContent.addNext(ivTyp, FIXED);
        title.setText(currentDraft.isTbDraft ? currentDraft.TbName : currentDraft.CacheName);
        title.setFont(Fonts.getBig());
        scrollBoxContent.addLast(title);

        scrollBoxContent.adjustHeight();

        scrollBox.setVirtualHeight(scrollBoxContent.getHeight());
        scrollBox.addChild(scrollBoxContent);
        addLast(scrollBox);

        btnOK.setClickHandler(saveLog);
        btnLog.setClickHandler(saveLog);
        btnDraft.setClickHandler(saveLog);

        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (draftsView != null)
                draftsView.addOrChangeDraft(null, false, SaveMode.Cancel);
            finish();
            return true;
        });
        enableGiveFavoritePoint.setClickHandler((view, x, y, pointer, button) -> {
            GL.that.postAsync(() -> {
                GroundspeakAPI.fetchMyUserInfos();
                initLayout();
            });
            return true;
        });
        etComment.showLastLines();
    }

    private void setValues() {
        // initLogText
        etComment.setText(currentDraft.comment);
        // Date
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String sDate = iso8601Format.format(currentDraft.timestamp);
        tvDate.setText(sDate);
        // Time
        iso8601Format = new SimpleDateFormat("HH:mm", Locale.US);
        String sTime = iso8601Format.format(currentDraft.timestamp);
        tvTime.setText(sTime);
        // iniOptions();
        tvFinds.setText("#" + currentDraft.foundNumber);
        if (currentDraft.isTbDraft)
            tvFinds.setText("");
        //
        title.setText(currentDraft.isTbDraft ? currentDraft.TbName : currentDraft.CacheName);
        if (CB_Core_Settings.GcVotePassword.getEncryptedValue().length() > 0) {
            if (!currentDraft.isTbDraft) {
                gcVoteItem.setValue(currentDraft.gc_Vote / 100.0);
            }
        }
    }

    private void initLogText() {
        etComment.setHeight(getHeight() / 2.5f);
        scrollBoxContent.addLast(etComment);
        etComment.setText(currentDraft.comment);
        btnHow = new CB_Button("=");
        btnHow.setClickHandler((v, x, y, pointer, button) -> {
            if (btnHow.getText().equals("="))
                btnHow.setText("+");
            else if (btnHow.getText().equals("+"))
                btnHow.setText("|");
            else btnHow.setText("=");
            return true;
        });
        scrollBoxContent.addNext(btnHow, FIXED);

        CB_Button btnFromNotes = new CB_Button(Translation.get("fromNotes"));
        btnFromNotes.setClickHandler((v, x, y, pointer, button) -> {
            String text = CacheDAO.getInstance().getNote(currentDraft.CacheId);
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
        btnFromFile.setClickHandler((v, x, y, pointer, button) -> {
            String mPath = Settings.TemplateLastUsedPath.getValue();
            if (mPath.length() == 0) {
                mPath = Config.workPath + "/User";
            }
            mPath = mPath + "/" + Settings.TemplateLastUsedName.getValue();
            new FileOrFolderPicker(mPath, "*.txt", Translation.get("TemplateTitleSelect"), Translation.get("TemplateButtonSelect"), abstractFile -> {
                BufferedReader br = null;
                String strLine;
                StringBuilder text = new StringBuilder();
                try {
                    br = new BufferedReader(new InputStreamReader(abstractFile.getFileInputStream()));
                    while ((strLine = br.readLine()) != null) {
                        text.append(strLine).append("\n");
                    }
                    Settings.TemplateLastUsedPath.setValue(abstractFile.getParent());
                    Settings.TemplateLastUsedName.setValue(abstractFile.getName());
                    Config.that.acceptChanges();
                } catch (Exception ignored) {
                }
                try {
                    if (br != null) br.close();
                } catch (Exception ignored) {
                }
                setupLogText(text.toString());
            }).show();
            return true;
        });
        scrollBoxContent.addLast(btnFromFile);
    }

    private void setupLogText(String text) {
        // todo for ##owner## the cache must be selected (if not first log)
        text = TemplateFormatter.ReplaceTemplate(text, currentDraft);
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
    public void onShow() {
        GL.that.postAsync(() -> {
            initLayout();
            KeyboardFocusChangedEventList.add(this);
        });
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.remove(this);
    }

    @Override
    public void keyboardFocusChanged(EditTextField editTextField) {
        if (editTextField == null) {
            if (scrollBoxContent != null)
                scrollBoxContent.setY(0);
        } else {
            scrollBoxContent.setY(scrollBoxContent.getHeight() - editTextField.getMaxY());
        }
    }

    public void setDraft(Draft _draft, IDraftsView _draftsView, boolean _isNewDraft) {
        isNewDraft = _isNewDraft;
        draftsView = _draftsView;
        currentDraft = _draft;
        originalDraft = new Draft(currentDraft);
        setValues();
    }

    public enum SaveMode {Cancel, OnlyLocal, Draft, Log, LocalUpdate}

    public interface IDraftsView {
        void addOrChangeDraft(Draft fn, boolean isNewDraft, SaveMode saveMode);
    }
}

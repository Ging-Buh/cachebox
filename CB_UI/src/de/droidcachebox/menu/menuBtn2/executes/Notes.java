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
package de.droidcachebox.menu.menuBtn2.executes;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class Notes extends CB_View_Base implements CacheSelectionChangedListeners.CacheSelectionChangedListener, KeyboardFocusChangedEventList.KeyboardFocusChangedEvent {
    private static Notes notes;
    private final EditTextField note;
    private final float notesDefaultYPos;
    private final CB_Button btnUpload;
    private float notesHeight;
    private Cache currentCache;
    private String notesText;

    private Notes() {
        super(ViewManager.leftTab.getContentRec(), "NotesView");

        initRow(BOTTOMUp);
        CB_Button getSolverButton = new CB_Button(Translation.get("getSolver"));
        // getSolverButton.disable();
        addNext(getSolverButton);
        btnUpload = new CB_Button(Translation.get("Upload"));
        addLast(btnUpload);
        notesHeight = getAvailableHeight();
        note = new EditTextField(new CB_RectF(0, 0, getWidth(), notesHeight), this, "notes", WrapType.WRAPPED);
        addLast(note);
        notesDefaultYPos = note.getY();

        btnUpload.setClickHandler((v, x, y, pointer, button) -> {
            final CB_Button b = (CB_Button) v;
            if (note.getText().length() > 0) {
                b.setText("Cancel");
                GL.that.RunOnGL(() -> {
                    String UploadText = note.getText().replace("<Import from Geocaching.com>", "").replace("</Import from Geocaching.com>", "").trim();
                    int result = GroundspeakAPI.uploadCacheNote(currentCache.getGeoCacheCode(), UploadText);
                    b.disable();
                    if (result == 0) {
                        b.setText(Translation.get("successful"));
                    } else {
                        b.setText(Translation.get("Error"));
                    }
                });
            }
            return true;
        });

        getSolverButton.setClickHandler((v, x, y, pointer, button) -> {
            String solver;
            if (currentCache != null) {
                solver = CacheDAO.getInstance().getSolver(currentCache);
            } else solver = null;
            solver = solver != null ? "<Solver>\r\n" + solver + "\r\n</Solver>" : "";
            String text = note.getText();
            int i1 = text.indexOf("<Solver>");
            if (i1 > -1) {
                int i2 = text.indexOf("</Solver>");
                String t1 = text.substring(0, i1);
                String t2;
                if (i2 > -1) {
                    t2 = text.substring(i2 + 9);
                } else {
                    t2 = text.substring(i1);
                }
                text = t1 + t2 + solver;
            } else {
                text = text + solver;
            }
            note.setText(text);
            return true;
        });

    }

    public static Notes getInstance() {
        if (notes == null) notes = new Notes();
        return notes;
    }

    @Override
    public void onResized(CB_RectF rec) {
        notesHeight = rec.getHeight() - btnUpload.getHeight();
        note.setHeight(notesHeight);
    }

    @Override
    public void keyboardFocusChanged(EditTextField editTextField) {
        btnUpload.setText(Translation.get("Upload"));
        btnUpload.enable();
        if (editTextField == note) {
            note.setHeight(getHalfHeight());
            note.setY(getHalfHeight());
        } else {
            note.setHeight(notesHeight);
            note.setY(notesDefaultYPos);
        }
    }

    @Override
    public void onShow() {
        CacheSelectionChangedListeners.getInstance().addListener(this);
        KeyboardFocusChangedEventList.add(this);
        loadNotes(GlobalCore.getSelectedCache());
    }

    @Override
    public void onHide() {
        CacheSelectionChangedListeners.getInstance().remove(this);
        KeyboardFocusChangedEventList.remove(this);
        saveNotes();
    }

    private void loadNotes(Cache newCache) {
        if (currentCache != newCache) {
            currentCache = newCache;
            notesText = currentCache != null ? CacheDAO.getInstance().getNote(currentCache.generatedId) : "";
            if (notesText == null)
                notesText = "";
            note.setText(notesText);
            note.showFromLineNo(0);
            btnUpload.setText(Translation.get("Upload"));
            btnUpload.enable();
        }
    }

    private void saveNotes() {
        // Save changed Note text to Database
        String text = note.getText();
        if (!notesText.equals(text)) {
            if (text != null) {
                try {
                    if (currentCache != null && !currentCache.isDisposed())
                        CacheDAO.getInstance().setNote(currentCache, text);
                } catch (Exception e) {
                    Log.err("NotesView", "Write note to database", e);
                }
            }
            else {
                Log.err("NotesView", "null text can not be written to database");
            }
        }
    }

    @Override
    public void handleCacheChanged(Cache cache, Waypoint waypoint) {
        saveNotes();
        loadNotes(cache);
    }

}

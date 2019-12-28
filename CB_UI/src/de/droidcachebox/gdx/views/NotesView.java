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
package de.droidcachebox.gdx.views;

import de.droidcachebox.*;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Waypoint;
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
public class NotesView extends CB_View_Base implements SelectedCacheChangedEventListener, KeyboardFocusChangedEventList.KeyboardFocusChangedEvent {
    private static NotesView that;
    private boolean mustLoadNotes;
    private EditTextField notes;
    private float notesDefaultYPos;
    private float notesHeight;
    private CB_Button btnUpload;
    private Cache aktCache;

    private NotesView() {
        super(ViewManager.leftTab.getContentRec(), "NotesView");

        aktCache = GlobalCore.getSelectedCache();
        mustLoadNotes = true;

        initRow(BOTTOMUP);
        CB_Button getSolverButton = new CB_Button(Translation.get("getSolver"));
        // getSolverButton.disable();
        addNext(getSolverButton);
        btnUpload = new CB_Button(Translation.get("Upload"));
        addLast(btnUpload);
        notesHeight = getAvailableHeight();
        notes = new EditTextField(new CB_RectF(0, 0, getWidth(), notesHeight), this, "notes", WrapType.WRAPPED);
        this.addLast(notes);
        notesDefaultYPos = notes.getY();

        SelectedCacheChangedEventListeners.getInstance().add(this);

        btnUpload.setClickHandler((v, x, y, pointer, button) -> {
            final CB_Button b = (CB_Button) v;
            if (notes.getText().length() > 0) {
                b.setText("Cancel");
                GL.that.RunOnGL(() -> {
                    String UploadText = notes.getText().replace("<Import from Geocaching.com>", "").replace("</Import from Geocaching.com>", "").trim();
                    int result = GroundspeakAPI.uploadCacheNote(aktCache.getGcCode(), UploadText);
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
            if (aktCache != null) {
                solver = Database.getSolver(aktCache);
            } else solver = null;
            solver = solver != null ? "<Solver>\r\n" + solver + "\r\n</Solver>" : "";
            String text = notes.getText();
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
            notes.setText(text);
            return true;
        });

    }

    public static NotesView getInstance() {
        if (that == null) that = new NotesView();
        return that;
    }

    @Override
    public void onResized(CB_RectF rec) {
        notesHeight = rec.getHeight() - btnUpload.getHeight();
        notes.setHeight(notesHeight);
    }

    @Override
    public void keyboardFocusChanged(EditTextField editTextField) {
        btnUpload.setText(Translation.get("Upload"));
        btnUpload.enable();
        if (editTextField == notes) {
            notes.setHeight(this.getHalfHeight());
            notes.setY(this.getHalfHeight());
        } else {
            notes.setHeight(notesHeight);
            notes.setY(notesDefaultYPos);
        }
    }

    @Override
    public void onShow() {
        KeyboardFocusChangedEventList.Add(this);
        if (mustLoadNotes) {
            String text = aktCache != null ? Database.getNote(aktCache) : "";
            if (text == null)
                text = "";
            notes.setText(text);
            notes.showFromLineNo(0);
            mustLoadNotes = false;
            btnUpload.setText(Translation.get("Upload"));
            btnUpload.enable();
        }
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.remove(this);
        // Save changed Note text to Database
        String text = notes.getText();
        if (text != null) {
            try {
                Database.setNote(aktCache, text);
            } catch (Exception e) {
                String sKlasse = "NotesView";
                Log.err(sKlasse, "Write note to database", e);
            }
        }
    }

    @Override
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
        // view must be refilled with values
        aktCache = cache;
        mustLoadNotes = true;
    }

}

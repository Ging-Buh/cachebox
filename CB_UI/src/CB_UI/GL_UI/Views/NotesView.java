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
package CB_UI.GL_UI.Views;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Log.Log;

/**
 * @author Longri
 */
public class NotesView extends CB_View_Base implements SelectedCacheEvent, KeyboardFocusChangedEvent {
    private final String sKlasse = "NotesView";
    boolean mustLoadNotes;
    private EditTextField notes;
    private float notesDefaultYPos;
    private float notesHeight;
    private Button uploadButton;
    private Button getSolverButton;
    private Cache aktCache;

    // TODO insert translations
    public NotesView(CB_RectF rec, String Name) {
        super(rec, Name);

        aktCache = GlobalCore.getSelectedCache();
        mustLoadNotes = true;

        initRow(BOTTOMUP);
        getSolverButton = new Button("getSolver");
        // getSolverButton.disable();
        addNext(getSolverButton);
        uploadButton = new Button("Upload");
        addLast(uploadButton);
        notesHeight = getAvailableHeight();
        notes = new EditTextField(new CB_RectF(0, 0, getWidth(), notesHeight), this, "notes", WrapType.WRAPPED);
        this.addLast(notes);
        notesDefaultYPos = notes.getY();

        SelectedCacheEventList.Add(this);

        uploadButton.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                final Button b = (Button) v;
                if (notes.getText().length() > 0) {
                    b.setText("Cancel");
                    GL.that.RunOnGL(new IRunOnGL() {
                        @Override
                        public void run() {
                            String UploadText = notes.getText().replace("<Import from Geocaching.com>","").replace("</Import from Geocaching.com>","").trim();
                            int result = GroundspeakAPI.uploadCacheNote(aktCache.getGcCode(), UploadText);
                            b.disable();
                            if (result == 0) {
                                b.setText("erfolgreich");
                            } else {
                                b.setText("Fehler");
                            }
                        }
                    });
                }
                return true;
            }

        });

        getSolverButton.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                String solver;
                if (aktCache != null) {
                    solver = Database.GetSolver(aktCache);
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
            }
        });

    }

    @Override
    public void KeyboardFocusChanged(EditTextField editTextField) {
        chkFocus(editTextField);
    }

    // Handling keyboard popup
    private void chkFocus(EditTextField editTextField) {
        uploadButton.setText("Upload");
        uploadButton.enable();
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
            String text = aktCache != null ? Database.GetNote(aktCache) : "";
            if (text == null)
                text = "";
            notes.setText(text);
            notes.showFromLineNo(0);
            mustLoadNotes = false;
        }
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.Remove(this);
        // Save changed Note text to Database
        String text = notes.getText();
        if (text != null) {
            try {
                Database.SetNote(aktCache, text);
            } catch (Exception e) {
               Log.err(sKlasse, "Write note to database", e);
            }
        }
    }

    @Override
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
        aktCache = cache;
        mustLoadNotes = true;
    }

}

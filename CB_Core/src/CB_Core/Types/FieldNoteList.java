/*
 * Copyright (C) 2014 team-cachebox.de
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
package CB_Core.Types;

import CB_Core.CB_Core_Settings;
import CB_Core.Database;
import CB_Core.LogTypes;
import CB_Utils.Log.Log;
import CB_Utils.Util.IChanged;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import de.cb.sqlite.CoreCursor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FieldNoteList extends ArrayList<FieldNoteEntry> {
    private static final String log = "FieldNoteList";
    private static final long serialVersionUID = 1L;
    private boolean croppedList = false;
    private int actCroppedLength = -1;

    public FieldNoteList() {
        IChanged settingsChangedListener = () -> {
            synchronized (FieldNoteList.this) {
                FieldNoteList.this.clear();
                croppedList = false;
                actCroppedLength = -1;
            }
        };
        CB_Core_Settings.FieldNotesLoadAll.addSettingChangedListener(settingsChangedListener);
        CB_Core_Settings.FieldNotesLoadLength.addSettingChangedListener(settingsChangedListener);
    }

    /**
     * @param dirFileName Config.settings.FieldNotesGarminPath.getValue()
     */
    public static void CreateVisitsTxt(String dirFileName) {
        FieldNoteList lFieldNotes = new FieldNoteList();
        lFieldNotes.LoadFieldNotes("", "Timestamp ASC", LoadingType.Loadall);

        File txtFile = FileFactory.createFile(dirFileName);
        FileOutputStream writer;
        try {
            writer = txtFile.getFileOutputStream();

            // write utf8 bom EF BB BF
            byte[] bom = {(byte) 239, (byte) 187, (byte) 191};
            writer.write(bom);

            for (FieldNoteEntry fieldNote : lFieldNotes) {
                String log = fieldNote.gcCode + "," + fieldNote.GetDateTimeString() + "," + fieldNote.type.toString() + ",\"" + fieldNote.comment + "\"\n";
                writer.write((log + "\n").getBytes(StandardCharsets.UTF_8));
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isCropped() {
        return croppedList;
    }

    public void LoadFieldNotes(String where, LoadingType loadingType) {
        synchronized (this) {
            LoadFieldNotes(where, "", loadingType);
        }
    }

    private void LoadFieldNotes(String where, String order, LoadingType loadingType) {
        synchronized (this) {
            // List clear?
            if (loadingType == LoadingType.Loadall || loadingType == LoadingType.LoadNew || loadingType == LoadingType.loadNewLastLength) {
                this.clear();
            }

            String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog from FieldNotes";
            if (!where.equals("")) {
                sql += " where " + where;
            }
            if (order.length() == 0) {
                sql += " order by FoundNumber DESC, Timestamp DESC";
            } else {
                sql += " order by " + order;
            }

            // SQLite Limit ?
            boolean maybeCropped = !CB_Core_Settings.FieldNotesLoadAll.getValue() && loadingType != LoadingType.Loadall;

            if (maybeCropped) {
                switch (loadingType) {
                    case LoadNew:
                        actCroppedLength = CB_Core_Settings.FieldNotesLoadLength.getValue();
                        sql += " LIMIT " + (actCroppedLength + 1);
                        break;
                    case loadNewLastLength:
                        if (actCroppedLength == -1)
                            actCroppedLength = CB_Core_Settings.FieldNotesLoadLength.getValue();
                        sql += " LIMIT " + (actCroppedLength + 1);
                        break;
                    case loadMore:
                        int Offset = actCroppedLength;
                        actCroppedLength += CB_Core_Settings.FieldNotesLoadLength.getValue();
                        sql += " LIMIT " + (CB_Core_Settings.FieldNotesLoadLength.getValue() + 1);
                        sql += " OFFSET " + Offset;
                }
            }

            try {
                CoreCursor reader = Database.FieldNotes.sql.rawQuery(sql, null);
                if (reader != null) {
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        FieldNoteEntry fne = new FieldNoteEntry(reader);
                        if (!this.contains(fne)) {
                            this.add(fne);
                        }
                        reader.moveToNext();
                    }
                    reader.close();
                }
            } catch (Exception exc) {
                Log.err(log, "FieldNoteList", "LoadFieldNotes", exc);
            }

            // check Cropped
            if (maybeCropped) {
                if (this.size() > actCroppedLength) {
                    croppedList = true;
                    // remove last item
                    this.remove(this.size() - 1);
                } else {
                    croppedList = false;
                }
            }
        }
    }

    public void DeleteFieldNoteByCacheId(long cacheId, LogTypes type) {
        synchronized (this) {
            int foundNumber = 0;
            FieldNoteEntry fne = null;
            // löscht eine evtl. vorhandene FieldNote vom type für den Cache cacheId
            for (FieldNoteEntry fn : this) {
                if ((fn.CacheId == cacheId) && (fn.type == type)) {
                    fne = fn;
                }
            }
            if (fne != null) {
                if (fne.type == LogTypes.found)
                    foundNumber = fne.foundNumber;
                this.remove(fne);
                fne.DeleteFromDatabase();
            }
            decreaseFoundNumber(foundNumber);
        }
    }

    public void DeleteFieldNote(FieldNoteEntry fnToDelete) {
        synchronized (this) {
            int foundNumber = 0;
            FieldNoteEntry fne = null;
            for (FieldNoteEntry fn : this) {
                if (fn.Id == fnToDelete.Id) {
                    fne = fn;
                }
            }
            if (fne != null) {
                if (fne.type == LogTypes.found)
                    foundNumber = fne.foundNumber;
                this.remove(fne);
                fne.DeleteFromDatabase();
            }
            decreaseFoundNumber(foundNumber);
        }
    }

    private void decreaseFoundNumber(int deletedFoundNumber) {
        if (deletedFoundNumber > 0) {
            // alle FoundNumbers anpassen, die größer sind
            for (FieldNoteEntry fn : this) {
                if ((fn.type == LogTypes.found) && (fn.foundNumber > deletedFoundNumber)) {
                    int oldFoundNumber = fn.foundNumber;
                    fn.foundNumber--;
                    fn.comment = fn.comment.replaceAll("#" + oldFoundNumber, "#" + fn.foundNumber);
                    fn.fillType();
                    fn.UpdateDatabase();
                }
            }
        }
    }

    public boolean contains(FieldNoteEntry fne) {
        synchronized (this) {
            for (FieldNoteEntry item : this) {
                if (fne.equals(item))
                    return true;
            }
            return false;
        }
    }

    public enum LoadingType {
        Loadall, LoadNew, loadMore, loadNewLastLength
    }
}

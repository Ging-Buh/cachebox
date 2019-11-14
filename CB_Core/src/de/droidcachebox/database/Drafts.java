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
package de.droidcachebox.database;

import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

public class Drafts extends ArrayList<de.droidcachebox.database.Draft> {
    private static final String log = "Drafts";
    private static final long serialVersionUID = 1L;
    private boolean croppedList = false;
    private int actCroppedLength = -1;

    public Drafts() {
        IChanged settingsChangedListener = () -> {
            synchronized (Drafts.this) {
                Drafts.this.clear();
                croppedList = false;
                actCroppedLength = -1;
            }
        };
        CB_Core_Settings.DraftsLoadAll.addSettingChangedListener(settingsChangedListener);
        CB_Core_Settings.DraftsLoadLength.addSettingChangedListener(settingsChangedListener);
    }

    public boolean isCropped() {
        return croppedList;
    }

    public void loadDrafts(String where, LoadingType loadingType) {
        synchronized (this) {
            loadDrafts(where, "", loadingType);
        }
    }

    public void loadDrafts(String where, String order, LoadingType loadingType) {
        synchronized (this) {
            // List clear?
            if (loadingType == LoadingType.Loadall || loadingType == LoadingType.LoadNew || loadingType == LoadingType.loadNewLastLength) {
                this.clear();
            }

            String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog, GcId from FieldNotes";
            if (!where.equals("")) {
                sql += " where " + where;
            }
            if (order.length() == 0) {
                sql += " order by FoundNumber DESC, Timestamp DESC";
            } else {
                sql += " order by " + order;
            }

            // SQLite Limit ?
            boolean maybeCropped = !CB_Core_Settings.DraftsLoadAll.getValue() && loadingType != LoadingType.Loadall;

            if (maybeCropped) {
                switch (loadingType) {
                    case LoadNew:
                        actCroppedLength = CB_Core_Settings.DraftsLoadLength.getValue();
                        sql += " LIMIT " + (actCroppedLength + 1);
                        break;
                    case loadNewLastLength:
                        if (actCroppedLength == -1)
                            actCroppedLength = CB_Core_Settings.DraftsLoadLength.getValue();
                        sql += " LIMIT " + (actCroppedLength + 1);
                        break;
                    case loadMore:
                        int Offset = actCroppedLength;
                        actCroppedLength += CB_Core_Settings.DraftsLoadLength.getValue();
                        sql += " LIMIT " + (CB_Core_Settings.DraftsLoadLength.getValue() + 1);
                        sql += " OFFSET " + Offset;
                }
            }

            try {
                CoreCursor reader = Database.Drafts.sql.rawQuery(sql, null);
                if (reader != null) {
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        de.droidcachebox.database.Draft fne = new de.droidcachebox.database.Draft(reader);
                        if (!this.contains(fne)) {
                            this.add(fne);
                        }
                        reader.moveToNext();
                    }
                    reader.close();
                }
            } catch (Exception exc) {
                Log.err(log, "Drafts", "loadDrafts", exc);
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

    public void DeleteDraftByCacheId(long cacheId, LogTypes type) {
        synchronized (this) {
            int foundNumber = 0;
            de.droidcachebox.database.Draft fne = null;
            // löscht eine evtl. vorhandene draft vom type für den Cache cacheId
            for (de.droidcachebox.database.Draft fn : this) {
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

    public void deleteDraft(de.droidcachebox.database.Draft fnToDelete) {
        synchronized (this) {
            int foundNumber = 0;
            de.droidcachebox.database.Draft fne = null;
            for (de.droidcachebox.database.Draft fn : this) {
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
            for (de.droidcachebox.database.Draft fn : this) {
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

    public boolean contains(de.droidcachebox.database.Draft fne) {
        synchronized (this) {
            for (Draft item : this) {
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

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

import com.badlogic.gdx.utils.Array;

import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.utils.IChanged;
import de.droidcachebox.utils.log.Log;

public class Drafts extends Array<Draft> {
    private static final String sKlasse = "Drafts";
    // private static final long serialVersionUID = 1L;
    private boolean isCropped;
    private int currentCroppedLength;
    private IChanged settingsChangedListener;

    public Drafts() {
        isCropped = false;
        currentCroppedLength = -1;
        settingsChangedListener = null;
    }

    public void addSettingsChangedHandler() {
        if (settingsChangedListener == null) {
            settingsChangedListener = () -> {
                synchronized (Drafts.this) {
                    clear();
                    isCropped = false;
                    currentCroppedLength = -1;
                }
            };
        }
        CB_Core_Settings.DraftsLoadAll.addSettingChangedListener(settingsChangedListener);
        CB_Core_Settings.DraftsLoadLength.addSettingChangedListener(settingsChangedListener);
    }

    public void removeSettingsChangedHandler() {
        if (settingsChangedListener != null) {
            CB_Core_Settings.DraftsLoadAll.removeSettingChangedListener(settingsChangedListener);
            CB_Core_Settings.DraftsLoadLength.removeSettingChangedListener(settingsChangedListener);
        }
    }

    public boolean isCropped() {
        return isCropped;
    }

    public void loadDrafts(LoadingType loadingType) {
        String where = "";
        synchronized (this) {
            if (loadingType != LoadingType.LoadMore) clear();
            if (loadingType == LoadingType.CanUpload) {
                where = "(Uploaded=0 or Uploaded is null)";
                loadingType = LoadingType.Loadall;
            }

            String sql = "select CacheId, GcCode, Name, CacheType, Timestamp, Type, FoundNumber, Comment, Id, Url, Uploaded, gc_Vote, TbFieldNote, TbName, TbIconUrl, TravelBugCode, TrackingNumber, directLog, GcId from FieldNotes";
            if (!where.equals("")) {
                sql += " where " + where;
            }

            if (loadingType == LoadingType.LoadAscending) {
                sql += " order by Timestamp ASC"; // FoundNumber DESC,
            }
            else {
                sql += " order by Timestamp DESC"; // FoundNumber DESC,
            }

            // SQLite Limit ?
            boolean maybeCropped = !CB_Core_Settings.DraftsLoadAll.getValue() && loadingType != LoadingType.Loadall;

            if (maybeCropped) {
                switch (loadingType) {
                    case LoadNew:
                        currentCroppedLength = CB_Core_Settings.DraftsLoadLength.getValue();
                        sql += " LIMIT " + (currentCroppedLength + 1);
                        break;
                    case LoadNewLastLength:
                        if (currentCroppedLength == -1)
                            currentCroppedLength = CB_Core_Settings.DraftsLoadLength.getValue();
                        sql += " LIMIT " + (currentCroppedLength + 1);
                        break;
                    case LoadMore:
                        int Offset = currentCroppedLength;
                        currentCroppedLength += CB_Core_Settings.DraftsLoadLength.getValue();
                        sql += " LIMIT " + (CB_Core_Settings.DraftsLoadLength.getValue() + 1);
                        sql += " OFFSET " + Offset;
                }
            }

            try {
                CoreCursor reader = DraftsDatabase.Drafts.sql.rawQuery(sql, null);
                if (reader != null) {
                    reader.moveToFirst();
                    while (!reader.isAfterLast()) {
                        Draft fne = new Draft(reader);
                        if (!contains(fne, false)) {
                            add(fne);
                        }
                        reader.moveToNext();
                    }
                    reader.close();
                }
            } catch (Exception exc) {
                Log.err(sKlasse, "Drafts", "loadDrafts", exc);
            }

            // check Cropped
            if (maybeCropped) {
                if (size > currentCroppedLength) {
                    isCropped = true;
                    // remove last item
                    removeIndex(size - 1);
                } else {
                    isCropped = false;
                }
            }
        }
    }

    public void deleteDraftByCacheId(long cacheId, LogType type) {
        synchronized (this) {
            int foundNumber = 0;
            Draft fne = null;
            // löscht eine evtl. vorhandene draft vom type für den Cache cacheId
            for (Draft fn : this) {
                if ((fn.CacheId == cacheId) && (fn.type == type)) {
                    fne = fn;
                }
            }
            if (fne != null) {
                if (fne.type == LogType.found)
                    foundNumber = fne.foundNumber;
                removeValue(fne, true); // fne is an object of this (list)
                fne.deleteFromDatabase();
            }
            decreaseFoundNumber(foundNumber);
        }
    }

    public void deleteDraftById(Draft fnToDelete) {
        synchronized (this) {
            int foundNumber = 0;
            Draft fne = null;
            for (Draft fn : this) {
                if (fn.Id == fnToDelete.Id) {
                    fne = fn;
                }
            }
            if (fne != null) {
                if (fne.type == LogType.found)
                    foundNumber = fne.foundNumber;
                removeValue(fne, true); // fne is an object of this (list)
                fne.deleteFromDatabase();
            }
            decreaseFoundNumber(foundNumber);
        }
    }

    private void decreaseFoundNumber(int deletedFoundNumber) {
        // todo: make sure all drafts are loaded or at least those not already uploaded
        if (deletedFoundNumber > 0) {
            // alle FoundNumbers anpassen, die größer sind
            for (Draft fn : this) {
                if ((fn.type == LogType.found) && (fn.foundNumber > deletedFoundNumber)) {
                    int oldFoundNumber = fn.foundNumber;
                    fn.foundNumber--;
                    fn.comment = fn.comment.replaceAll("#" + oldFoundNumber, "#" + fn.foundNumber);
                    fn.updateDatabase();
                }
            }
        }
    }

    public enum LoadingType {
        Loadall, LoadNew, LoadMore, LoadNewLastLength, CanUpload, LoadAscending
    }
}

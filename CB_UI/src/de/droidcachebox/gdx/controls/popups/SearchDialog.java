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
package de.droidcachebox.gdx.controls.popups;

import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.fetchGeoCache;
import static de.droidcachebox.core.GroundspeakAPI.fetchGeoCaches;
import static de.droidcachebox.core.GroundspeakAPI.isPremiumMember;
import static de.droidcachebox.core.GroundspeakAPI.searchGeoCaches;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.ImageDAO;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.dataclasses.ImageEntry;
import de.droidcachebox.dataclasses.LogEntry;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Slider;
import de.droidcachebox.gdx.Slider.YPositionChanged;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.activities.ImportGCPosition;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextField.TextFieldListener;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.MultiToggleButton;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.menuBtn1.executes.GeoCaches;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.TestCancelRunnable;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class SearchDialog extends PopUp_Base {
    private static final String log = "SearchDialog";
    public static SearchDialog that;

    private final YPositionChanged listener = new YPositionChanged() {

        @Override
        public void Position(float SliderTop, float SliderBottom) {
            setY(GeoCaches.getInstance().getMaxY() - that.getHeight());
        }
    };
    /**
     * Option Title, der drei Optionen Title/GC-Code/Owner
     */
    private final MultiToggleButton mTglBtnTitle;
    /**
     * Option GC-Code, der drei Optionen Title/GC-Code/Owner
     */
    private final MultiToggleButton mTglBtnGc;
    /**
     * Option Owner, der drei Optionen Title/GC-Code/Owner
     */
    private final MultiToggleButton mTglBtnOwner;
    /**
     * Option Online Suche On/Off
     */
    private final MultiToggleButton mTglBtnOnline;
    /**
     * Button, welcher eine Suchanfrage als Filter verwendet
     */
    private final ImageButton mBtnFilter;
    /**
     * Button, der eine Suche Startet
     */
    private final CB_Button mBtnSearch;
    /**
     * Button, der den nächsten Treffer einer gestarteten Suche findet
     */
    private final CB_Button mBtnNext;
    /**
     * Button, der den Search Dialog schliesst
     */
    private final CB_Button mBtnCancel;
    /**
     * Such Eingabe Feld
     */
    private final EditTextField mEingabe;
    private CancelWaitDialog wd = null;
    private MsgBox msgBox;
    /**
     * True, wenn eine Suche läuft und der Iterator mit Next weiter durchlaufen werden kann.
     */
    private boolean mSearchAktive = false;
    /**
     * Enthält den Aktuellen Such Modus <br/>
     * 0 = Title <br/>
     * 1 = Gc-Code <br/>
     * 2 = Owner <br/>
     */
    private SearchMode mSearchState = SearchMode.Title;
    /**
     * Index of the beginning search
     */
    private int beginnSearchIndex = -1;

    public SearchDialog() {
        super(new CB_RectF(), "SearchDialog");

        that = this;

        this.setSize(UiSizes.getInstance().getCacheListItemSize().asFloat());

        this.setBackground(Sprites.ListBack);

        float margin = UiSizes.getInstance().getMargin();
        float btnWidth = (this.getWidth() - (margin * 7)) / 4;

        CB_RectF rec = new CB_RectF(0, 0, btnWidth, UiSizes.getInstance().getButtonHeight());

        mTglBtnTitle = new MultiToggleButton(rec, "mTglBtnTitle");
        mTglBtnGc = new MultiToggleButton(rec, "mTglBtnGc");
        mTglBtnOwner = new MultiToggleButton(rec, "mTglBtnOwner");
        mTglBtnOnline = new MultiToggleButton(rec, "mTglBtnOnline");

        rec.setWidth((this.getWidth() - (margin * 5)) / 4);

        mBtnFilter = new ImageButton(rec, "mBtnFilter");
        mBtnSearch = new CB_Button(rec, "mBtnSearch");
        mBtnNext = new CB_Button(rec, "mBtnNext");
        mBtnCancel = new CB_Button(rec, "mBtnCancel");

        rec.setWidth(this.getWidth() - (margin * 2));

        mEingabe = new EditTextField(rec, this, "mEingabe", WrapType.SINGLELINE);

        mEingabe.setTextFieldListener(new TextFieldListener() {

            @Override
            public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {

            }

            @Override
            public void keyTyped(EditTextField textField, char key) {
                textBox_TextChanged();
            }
        });

        mEingabe.setText("");

        // layout! da es sich nicht ändert, brauchen wir es nicht in eine Methode packen
        float y = margin;

        mBtnFilter.setPos(margin, y);
        mBtnSearch.setPos(mBtnFilter.getMaxX() + margin, y);
        mBtnNext.setPos(mBtnSearch.getMaxX() + margin, y);
        mBtnCancel.setPos(mBtnNext.getMaxX() + margin, y);

        mEingabe.setPos(margin, mBtnCancel.getMaxY() + margin);

        y = mEingabe.getMaxY() + margin;

        mTglBtnOnline.setPos(margin, y);
        mTglBtnTitle.setPos(mTglBtnOnline.getMaxX() + margin + margin, y);
        mTglBtnGc.setPos(mTglBtnTitle.getMaxX() + margin, y);
        mTglBtnOwner.setPos(mTglBtnGc.getMaxX() + margin, y);

        // die Höhe nach dem Verbrauchten Platz einstellen

        this.setHeight(mTglBtnOwner.getMaxY() + margin);

        // Controls zum Dialog hinzufügen
        this.addChild(mTglBtnTitle);
        this.addChild(mTglBtnGc);
        this.addChild(mTglBtnOwner);
        this.addChild(mTglBtnOnline);
        this.addChild(mBtnFilter);
        this.addChild(mBtnSearch);
        this.addChild(mBtnNext);
        this.addChild(mBtnCancel);
        this.addChild(mEingabe);

        setLang();
        switchSearchMode(SearchMode.Title);

        mBtnCancel.setClickHandler((v, x, y1, pointer, button) -> {
            close();
            return true;
        });

        mTglBtnTitle.setClickHandler((v, x, y12, pointer, button) -> {
            switchSearchMode(SearchMode.Title);
            return true;
        });

        mTglBtnGc.setClickHandler((v, x, y13, pointer, button) -> {
            switchSearchMode(SearchMode.GcCode);
            return true;
        });

        mTglBtnOwner.setClickHandler((v, x, y14, pointer, button) -> {
            switchSearchMode(SearchMode.Owner);
            return true;
        });

        mBtnSearch.setClickHandler((v, x, y15, pointer, button) -> {
            GL.that.setFocusedEditTextField(null);
            mSearchAktive = false;
            beginnSearchIndex = 0;
            searchNow(false);
            return true;
        });

        mBtnNext.setClickHandler((v, x, y16, pointer, button) -> {
            GL.that.setFocusedEditTextField(null);
            searchNow(true);
            return true;

        });

        mBtnFilter.setClickHandler((v, x, y17, pointer, button) -> {
            GL.that.setFocusedEditTextField(null);
            if (mTglBtnOnline.getState() == 1) {
                close();
                GL.that.postAsync(this::askPremium);
            } else {
                setFilter();
            }
            return true;
        });

        mTglBtnOnline.setClickHandler((v, x, y18, pointer, button) -> {
            setFilterBtnState();
            textBox_TextChanged();
            return true;
        });

    }

    private void setFilterBtnState() {
        if (mTglBtnOnline.getState() == 0) {
            mBtnFilter.clearImage();
            mBtnFilter.setText(Translation.get("Filter"));
        } else {
            mBtnFilter.setImage(new SpriteDrawable(Sprites.getSprite(IconName.targetDay.name())));
            mBtnFilter.setText("");
        }
    }

    /**
     * Setzt die übersetzten Texte auf die Buttons
     */
    private void setLang() {
        mTglBtnTitle.initialOn_Off_ToggleStates(Translation.get("Title"), Translation.get("Title"));
        mTglBtnGc.initialOn_Off_ToggleStates(Translation.get("GCCode"), Translation.get("GCCode"));
        mTglBtnOwner.initialOn_Off_ToggleStates(Translation.get("Owner"), Translation.get("Owner"));
        mTglBtnOnline.initialOn_Off_ToggleStates("Online", "Online");

        // der State muss erstmal gesetzt werden, damit die Anzeige
        // Aktuallisiert wird
        mTglBtnOnline.setState(0);

        mBtnFilter.setText(Translation.get("Filter"));
        mBtnSearch.setText(Translation.get("Search"));
        mBtnNext.setText(Translation.get("Next"));
        mBtnCancel.setText(Translation.get("abort"));

    }

    /**
     * Schaltet den Such Modus um.
     *
     * @param state <br/>
     *              0 = Title <br/>
     *              1 = Gc-Code <br/>
     *              2 = Owner <br/>
     */
    private void switchSearchMode(SearchMode state) {
        mSearchState = state;

        if (state == SearchMode.Title) {
            mTglBtnTitle.setState(1);
            mTglBtnGc.setState(0);
            mTglBtnOwner.setState(0);
        }
        if (state == SearchMode.GcCode) {
            mTglBtnTitle.setState(0);
            mTglBtnGc.setState(1);
            mTglBtnOwner.setState(0);
        }
        if (state == SearchMode.Owner) {
            mTglBtnTitle.setState(0);
            mTglBtnGc.setState(0);
            mTglBtnOwner.setState(1);
        }

    }

    private void textBox_TextChanged() {

        boolean isText = mEingabe.getText().length() != 0;
        mBtnSearch.setEnable(isText);
        mBtnNext.disable();

        if (mTglBtnOnline.getState() == 0) {
            mBtnFilter.setEnable(isText);
        } else {
            mBtnFilter.enable();
        }

        // Sofort Filter hat eine schlechte Performance, deshalb habe ich ihn ersteinmal abgeschalten.
        // Es wäre aber ein schönes Feature!
        // filterSearchByTextChnge();
    }

    /**
     * Die aktive CahcheList wird durchsucht gefilterte Caches werden dabei nicht berücksichtigt.
     *
     * @param ignoreOnlineSearch (True, wenn Lokal gesucht werden soll, obwohl der MultiToggleButton "Online" aktiviert ist.
     */
    private void searchNow(boolean ignoreOnlineSearch) {

        if (ignoreOnlineSearch || mTglBtnOnline.getState() == 0) {

            String searchPattern = mEingabe.getText().toLowerCase();
            searchPattern = searchPattern.replace("\n", "");
            searchPattern = searchPattern.replace("\r", "");

            boolean criterionMatches = false;

            synchronized (CBDB.getInstance().cacheList) {

                if (!mSearchAktive) {
                    mSearchAktive = true;
                }

                Cache tmp = null;
                if (beginnSearchIndex < 0) beginnSearchIndex = 0;
                for (int i = beginnSearchIndex, n = CBDB.getInstance().cacheList.size(); i < n; i++) {
                    tmp = CBDB.getInstance().cacheList.get(i);

                    switch (mSearchState) {
                        case Title:
                            criterionMatches = tmp.getGeoCacheName().toLowerCase().contains(searchPattern);
                            break;
                        case GcCode:
                            criterionMatches = tmp.getGeoCacheCode().toLowerCase().contains(searchPattern);
                            break;
                        case Owner:
                            criterionMatches = tmp.getOwner().toLowerCase().contains(searchPattern);
                            break;
                    }
                    if (criterionMatches) {
                        beginnSearchIndex = i + 1;
                        break;
                    }
                }

                if (!criterionMatches) {
                    mBtnNext.disable();
                    mSearchAktive = false;
                    MsgBox.show(Translation.get("NoCacheFound"), Translation.get("Search"), MsgBoxButton.OK, MsgBoxIcon.Asterisk, null);
                } else {
                    Waypoint finalWp = tmp.getCorrectedFinal();
                    if (finalWp == null)
                        finalWp = tmp.getStartWaypoint();
                    GlobalCore.setSelectedWaypoint(tmp, finalWp);
                    // deactivate autoResort when Cache is selected by hand
                    GlobalCore.setAutoResort(false);

                    mBtnNext.enable();

                }
            }
        } else {
            searchAPI();
        }

    }

    /**
     * Sucht mit den Vorgaben nach Caches über die API. Die Gefundenen Caches werden in die DB eingetragen und im Anschluss wird der lokale
     * Suchvorgang gestartet.
     */
    private void searchAPI() {

        GlobalCore.chkAPiLogInWithWaitDialog(invalidAccessToken -> {

            if (invalidAccessToken) {
                GL.that.RunOnGL(() -> MsgBox.show(Translation.get("apiKeyNeeded"), Translation.get("Clue"), MsgBoxButton.OK, MsgBoxIcon.Exclamation, null));
            } else {
                wd = new CancelWaitDialog(Translation.get("Search"), new DownloadAnimation(), this::closeWaitDialog, new TestCancelRunnable() {

                    @Override
                    public void run() {
                        if (isPremiumMember()) {
                            searchOnlineNow();
                        } else {
                            MsgBox.show(Translation.get("GC_basic"), Translation.get("GC_title"), MsgBoxButton.OKCancel, MsgBoxIcon.Powerd_by_GC_Live,
                                    (which, data) -> {
                                        if (which == MsgBox.BTN_LEFT_POSITIVE) {
                                            searchOnlineNow();
                                        } else
                                            closeWaitDialog();
                                        return true;
                                    });
                        }
                    }

                    @Override
                    public boolean checkCanceled() {
                        return false;
                    }
                });
                wd.show();
            }
        });

    }

    private void closeWaitDialog() {
        Log.debug(log, "SEARCH Close WD");
        if (wd != null)
            wd.close();
    }

    private void searchOnlineNow() {
        Log.debug(log, "searchOnlineNow");
        wd = new CancelWaitDialog(Translation.get("searchOverAPI"), new DownloadAnimation(), this::closeWaitDialog, new TestCancelRunnable() {

            @Override
            public void run() {

                Coordinate searchCoord;
                if (ShowMap.getInstance().normalMapView.isVisible()) {
                    searchCoord = ShowMap.getInstance().normalMapView.center;
                } else {
                    searchCoord = Locator.getInstance().getMyPosition();
                }
                if (searchCoord == null) {
                    return;
                }

                // alle per API importierten Caches landen in der Category und GpxFilename API-Import Category suchen, die dazu gehört
                Category category = CoreData.categories.getCategory("API-Import");
                if (category == null)
                    return; // should not happen!!!
                GpxFilename gpxFilename = category.addGpxFilename("API-Import");
                if (gpxFilename == null)
                    return;

                GroundspeakAPI.Query q = new GroundspeakAPI.Query()
                        .setMaxToFetch(50)
                        .resultWithFullFields()
                        // .resultWithImages(30)
                        ;
                if (Settings.numberOfLogs.getValue() > 0) {
                    q.resultWithLogs(Settings.numberOfLogs.getValue());
                }

                if (Settings.SearchWithoutFounds.getValue()) q.excludeFinds();
                if (Settings.SearchWithoutOwns.getValue()) q.excludeOwn();
                if (Settings.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

                String searchPattern = mEingabe.getText();
                ArrayList<GroundspeakAPI.GeoCacheRelated> geoCacheRelateds;
                switch (mSearchState) {
                    case Title:
                        q.searchInCircleOf100Miles(searchCoord)
                                .searchForTitle(searchPattern);
                        geoCacheRelateds = searchGeoCaches(q);
                        break;
                    case Owner:
                        q.searchInCircleOf100Miles(searchCoord)
                                .searchForOwner(searchPattern);
                        geoCacheRelateds = searchGeoCaches(q);
                        break;
                    default: // GCCode
                        // API 1.0 doesn't allow a pattern (only one GCCode, else handle a list of GCCodes
                        if (searchPattern.contains(",")) {
                            geoCacheRelateds = fetchGeoCaches(q, searchPattern);
                        } else {
                            geoCacheRelateds = fetchGeoCache(q, searchPattern);
                        }
                        break;
                }

                if (geoCacheRelateds.size() > 0) {

                    CBDB.getInstance().beginTransaction();

                    CacheDAO cacheDAO = CacheDAO.getInstance();
                    ImageDAO imageDAO = new ImageDAO();
                    WaypointDAO waypointDAO = WaypointDAO.getInstance();

                    int counter = 0;

                    synchronized (CBDB.getInstance().cacheList) {
                        for (GeoCacheRelated geoCacheRelated : geoCacheRelateds) {
                            Cache cache = geoCacheRelated.cache;
                            counter++;
                            if (CBDB.getInstance().cacheList.getCacheByIdFromCacheList(cache.generatedId) == null) {
                                CBDB.getInstance().cacheList.add(cache);
                                if (cache.getGPXFilename_ID() == 0) {
                                    cache.setGPXFilename_ID(gpxFilename.Id);
                                }
                                cacheDAO.writeToDatabase(cache);
                                for (LogEntry log : geoCacheRelated.logs) {
                                    LogsTableDAO.getInstance().WriteLogEntry(log);
                                }
                                for (ImageEntry image : geoCacheRelated.images) {
                                    imageDAO.writeToDatabase(image, false);
                                }
                                for (int i = 0, n = cache.getWayPoints().size(); i < n; i++) {
                                    Waypoint waypoint = cache.getWayPoints().get(i);
                                    waypointDAO.WriteToDatabase(waypoint, false); // do not store replication information here
                                }
                            }
                        }
                    }

                    CBDB.getInstance().setTransactionSuccessful();
                    CBDB.getInstance().endTransaction();

                    CacheDAO.getInstance().updateCacheCountForGPXFilenames();

                    CacheListChangedListeners.getInstance().cacheListChanged();

                    if (counter == 1) {
                        // select this Cache
                        Cache cache = CBDB.getInstance().cacheList.getCacheByIdFromCacheList(geoCacheRelateds.get(0).cache.generatedId);
                        GlobalCore.setSelectedCache(cache);
                    }

                }
                closeWaitDialog();
            }

            @Override
            public boolean checkCanceled() {
                return false;
            }
        });
        wd.show();
    }

    /**
     * setzt den Filter auf die Such Anfrage
     */
    private void setFilter() {
        String searchPattern = mEingabe.getText().toLowerCase();

        FilterInstances.getLastFilter().filterName = "";
        FilterInstances.getLastFilter().filterGcCode = "";
        FilterInstances.getLastFilter().filterOwner = "";

        if (mSearchState == SearchMode.Title)
            FilterInstances.getLastFilter().filterName = searchPattern;
        else if (mSearchState == SearchMode.GcCode)
            FilterInstances.getLastFilter().filterGcCode = searchPattern;
        if (mSearchState == SearchMode.Owner)
            FilterInstances.getLastFilter().filterOwner = searchPattern;

        EditFilterSettings.applyFilter(FilterInstances.getLastFilter());

    }

    public void doSearchOnline(final String searchPattern, final SearchMode searchMode) {
        Log.debug(log, "doSearchOnline " + searchPattern);
        try {
            GL.that.RunOnGL(() -> {
                mEingabe.setText(searchPattern);
                GL.that.RunOnGL(() -> {
                    switchSearchMode(searchMode);
                    GL.that.RunOnGL(() -> {
                        mTglBtnOnline.setState(1);
                        GL.that.RunOnGL(() -> {
                            setFilterBtnState();
                            GL.that.RunOnGL(mBtnSearch::performClick);
                        });
                    });
                });
            });
        } catch (Exception e) {
            Log.err(log, "doSearchOnline", e);
        }
    }

    @Override
    public void onShow() {
        try {

            setY(GeoCaches.getInstance().getMaxY() - this.getHeight());
            GeoCaches.getInstance().setTopPlaceHolder(this.getHeight());

            if (GL.that.PopUpIsHidden())
                that.showNotCloseAutomaticly();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Slider.that.registerPosChangedEvent(listener);
    }

    @Override
    public void onHide() {
        Slider.that.removePosChangedEvent(listener);
        GeoCaches.getInstance().resetPlaceHolder();
    }

    private void askPremium() {

        // First check API-Key with visual Feedback
        GlobalCore.chkAPiLogInWithWaitDialog(invalidAccessToken -> {
            if (invalidAccessToken) {
                GL.that.RunOnGL(() -> MsgBox.show(Translation.get("apiKeyNeeded"), Translation.get("Clue"), MsgBoxButton.OK, MsgBoxIcon.Exclamation, null));
            } else {
                closeWD();
                GL.that.RunOnGL(() -> {
                    if (isPremiumMember()) {
                        showTargetApiDialog();
                    } else {
                        msgBox = MsgBox.show(Translation.get("GC_basic"), Translation.get("GC_title"), MsgBoxButton.OKCancel, MsgBoxIcon.Powerd_by_GC_Live,
                                (which, data) -> {
                                    closeMsgBox();
                                    if (which == MsgBox.BTN_LEFT_POSITIVE) {
                                        showTargetApiDialog();
                                    }
                                    return true;
                                });
                    }
                });
            }
        });
    }

    private void closeMsgBox() {
        msgBox.close();
    }

    private void closeWD() {
        if (wd != null)
            wd.close();
    }

    private void showTargetApiDialog() {
        GL.that.RunOnGL(() -> new ImportGCPosition().show());
    }

    @Override
    public void dispose() {
        // do nothing is static dialog
    }

    public enum SearchMode {
        Title, GcCode, Owner
    }

}

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
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
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
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextField.TextFieldListener;
import de.droidcachebox.gdx.controls.ImageButton;
import de.droidcachebox.gdx.controls.MultiToggleButton;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.menuBtn1.contextmenus.executes.ImportGCPosition;
import de.droidcachebox.menu.menuBtn1.executes.GeoCaches;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class SearchDialog extends PopUp_Base {
    private static final String sClass = "SearchDialog";

    private final YPositionChanged listener = new YPositionChanged() {

        @Override
        public void Position(float SliderTop, float SliderBottom) {
            setY(GeoCaches.getInstance().getMaxY() - SearchDialog.this.getHeight());
        }
    };
    private final MultiToggleButton mTglBtnTitle;
    private final MultiToggleButton mTglBtnGc;
    private final MultiToggleButton mTglBtnOwner;
    private final MultiToggleButton mTglBtnOnline;
    private final ImageButton mBtnFilter;
    private final CB_Button mBtnSearch;
    private final CB_Button mBtnNext;
    private final CB_Button mBtnCancel;
    private final EditTextField mInput;
    AtomicBoolean isCanceled = new AtomicBoolean(false);
    private boolean mSearchIsActive = false;
    /**
     * for current Search Mode <br/>
     * 0 = Title <br/>
     * 1 = Gc-Code <br/>
     * 2 = Owner <br/>
     */
    private SearchMode mSearchState = SearchMode.Title;
    private int beginSearchIndex = -1;

    public SearchDialog() {
        super(new CB_RectF(), "SearchDialog");

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

        mInput = new EditTextField(rec, this, "Input", WrapType.SINGLELINE);

        mInput.setTextFieldListener(new TextFieldListener() {

            @Override
            public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {

            }

            @Override
            public void keyTyped(EditTextField textField, char key) {
                textBox_TextChanged();
            }
        });

        mInput.setText("");

        // fixed layout!
        float y = margin;

        mBtnFilter.setPos(margin, y);
        mBtnSearch.setPos(mBtnFilter.getMaxX() + margin, y);
        mBtnNext.setPos(mBtnSearch.getMaxX() + margin, y);
        mBtnCancel.setPos(mBtnNext.getMaxX() + margin, y);

        mInput.setPos(margin, mBtnCancel.getMaxY() + margin);

        y = mInput.getMaxY() + margin;

        mTglBtnOnline.setPos(margin, y);
        mTglBtnTitle.setPos(mTglBtnOnline.getMaxX() + margin + margin, y);
        mTglBtnGc.setPos(mTglBtnTitle.getMaxX() + margin, y);
        mTglBtnOwner.setPos(mTglBtnGc.getMaxX() + margin, y);
        this.setHeight(mTglBtnOwner.getMaxY() + margin);

        this.addChild(mTglBtnTitle);
        this.addChild(mTglBtnGc);
        this.addChild(mTglBtnOwner);
        this.addChild(mTglBtnOnline);
        this.addChild(mBtnFilter);
        this.addChild(mBtnSearch);
        this.addChild(mBtnNext);
        this.addChild(mBtnCancel);
        this.addChild(mInput);

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
            mSearchIsActive = false;
            beginSearchIndex = 0;
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

    private void setLang() {
        mTglBtnTitle.initialOn_Off_ToggleStates(Translation.get("Title"), Translation.get("Title"));
        mTglBtnGc.initialOn_Off_ToggleStates(Translation.get("GCCode"), Translation.get("GCCode"));
        mTglBtnOwner.initialOn_Off_ToggleStates(Translation.get("Owner"), Translation.get("Owner"));
        mTglBtnOnline.initialOn_Off_ToggleStates("Online", "Online");

        mTglBtnOnline.setState(0); // to update view

        mBtnFilter.setText(Translation.get("Filter"));
        mBtnSearch.setText(Translation.get("Search"));
        mBtnNext.setText(Translation.get("Next"));
        mBtnCancel.setText(Translation.get("abort"));

    }

    /**
     * changes search mode.
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

        boolean isText = mInput.getText().length() != 0;
        mBtnSearch.setEnable(isText);
        mBtnNext.disable();

        if (mTglBtnOnline.getState() == 0) {
            mBtnFilter.setEnable(isText);
        } else {
            mBtnFilter.enable();
        }
        // filterSearchByTextChange(); !!performance // would be a nice feature
    }

    /**
     * using the current cache list, so filtered caches are not shown
     *
     * @param ignoreOnlineSearch (True, if search local, although MultiToggleButton "Online" is active.
     */
    private void searchNow(boolean ignoreOnlineSearch) {

        if (ignoreOnlineSearch || mTglBtnOnline.getState() == 0) {

            String searchPattern = mInput.getText().toLowerCase();
            searchPattern = searchPattern.replace("\n", "");
            searchPattern = searchPattern.replace("\r", "");

            boolean criterionMatches = false;

            synchronized (CBDB.getInstance().cacheList) {
                mSearchIsActive = true;
                Cache tmp = null;
                if (beginSearchIndex < 0) beginSearchIndex = 0;
                for (int i = beginSearchIndex, n = CBDB.getInstance().cacheList.size(); i < n; i++) {
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
                        beginSearchIndex = i + 1;
                        break;
                    }
                }

                if (!criterionMatches) {
                    mBtnNext.disable();
                    mSearchIsActive = false;
                    // new ButtonDialog(Translation.get("NoCacheFound"), Translation.get("Search"), MsgBoxButton.OK, MsgBoxIcon.Asterisk).show();
                    // ButtonDialog implicit closes/disposes the search when shown, so no message
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
     * search with GroundSpeak API, enter finds into db, then search
     */
    private void searchAPI() {

        GlobalCore.chkAPiLogInWithWaitDialog(invalidAccessToken -> {

            if (invalidAccessToken) {
                new ButtonDialog(Translation.get("apiKeyNeeded"), Translation.get("Clue"), MsgBoxButton.OK, MsgBoxIcon.Exclamation).show();
            } else {
                new CancelWaitDialog(Translation.get("Search"), new DownloadAnimation(), new RunAndReady() {
                    @Override
                    public void ready() {
                        if (!isPremiumMember()) {
                            ButtonDialog bd = new ButtonDialog(Translation.get("GC_basic"), Translation.get("GC_title"), MsgBoxButton.OKCancel, MsgBoxIcon.Powerd_by_GC_Live);
                            bd.setButtonClickHandler((which, data) -> {
                                if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                                    searchOnlineNow();
                                }
                                return true;
                            });
                            bd.show();
                        }
                    }

                    @Override
                    public void run() {
                        if (isPremiumMember()) {
                            searchOnlineNow();
                        }
                    }

                    @Override
                    public void setIsCanceled() {
                        isCanceled.set(true);
                    }

                }).show();
            }
        });

    }

    private void searchOnlineNow() {
        Log.debug(sClass, "searchOnlineNow");
        new CancelWaitDialog(Translation.get("searchOverAPI"), new DownloadAnimation(), new RunAndReady() {
            ArrayList<GeoCacheRelated> geoCacheRelateds;
            GpxFilename gpxFilename;
            @Override
            public void ready() {

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
            }

            @Override
            public void run() {

                Coordinate searchCoordinate;
                if (ShowMap.getInstance().normalMapView.isVisible()) {
                    searchCoordinate = ShowMap.getInstance().normalMapView.center;
                } else {
                    searchCoordinate = Locator.getInstance().getMyPosition();
                }
                if (searchCoordinate == null) {
                    return;
                }

                Category category = CoreData.categories.getCategory("API-Import");
                if (category == null)
                    return; // should not happen!!!
                gpxFilename = category.addGpxFilename("API-Import");
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

                String searchPattern = mInput.getText();
                switch (mSearchState) {
                    case Title:
                        q.searchInCircleOf100Miles(searchCoordinate)
                                .searchForTitle(searchPattern);
                        geoCacheRelateds = searchGeoCaches(q);
                        break;
                    case Owner:
                        q.searchInCircleOf100Miles(searchCoordinate)
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
            }

            @Override
            public void setIsCanceled() {
                isCanceled.set(true);
            }

        }).show();
    }

    /**
     * filter from search
     */
    private void setFilter() {
        String searchPattern = mInput.getText().toLowerCase();

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
        Log.debug(sClass, "doSearchOnline " + searchPattern);
        try {
            GL.that.runOnGL(() -> {
                mInput.setText(searchPattern);
                GL.that.runOnGL(() -> {
                    switchSearchMode(searchMode);
                    GL.that.runOnGL(() -> {
                        mTglBtnOnline.setState(1);
                        GL.that.runOnGL(() -> {
                            setFilterBtnState();
                            GL.that.runOnGL(mBtnSearch::performClick);
                        });
                    });
                });
            });
        } catch (Exception e) {
            Log.err(sClass, "doSearchOnline", e);
        }
    }

    @Override
    public void onShow() {
        try {

            setY(GeoCaches.getInstance().getMaxY() - this.getHeight());
            GeoCaches.getInstance().setTopPlaceHolder(this.getHeight());

            if (GL.that.PopUpIsHidden())
                showNotCloseAutomaticly();
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
                new ButtonDialog(Translation.get("apiKeyNeeded"), Translation.get("Clue"), MsgBoxButton.OK, MsgBoxIcon.Exclamation).show();
            } else {
                if (isPremiumMember()) {
                    new ImportGCPosition().show();
                } else {
                    ButtonDialog bd = new ButtonDialog(Translation.get("GC_basic"), Translation.get("GC_title"), MsgBoxButton.OKCancel, MsgBoxIcon.Powerd_by_GC_Live);
                    bd.setButtonClickHandler((which, data) -> {
                        if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                            new ImportGCPosition().show();                                }
                        return true;
                    });
                    bd.show();
                }
            }
        });
    }

    public enum SearchMode {
        Title, GcCode, Owner
    }

}

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
package CB_UI.GL_UI.Controls.PopUps;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.*;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Types.*;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.ImportGCPosition;
import CB_UI.GL_UI.Controls.Slider;
import CB_UI.GL_UI.Controls.Slider.YPositionChanged;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GlobalCore;
import CB_UI.GlobalCore.iChkReadyHandler;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

import static CB_Core.Api.GroundspeakAPI.*;

/**
 * @author Longri
 */
public class SearchDialog extends PopUp_Base {
    private static final String log = "SearchDialog";
    public static SearchDialog that;

    private final YPositionChanged listener = new YPositionChanged() {

        @Override
        public void Position(float SliderTop, float SliderBottom) {
            setY(CacheListView.getInstance().getMaxY() - that.getHeight());
        }
    };

    CancelWaitDialog wd = null;
    MessageBox MSB;
    /**
     * True, wenn eine Suche läuft und der Iterator mit Next weiter durchlaufen werden kann.
     */
    private boolean mSearchAktive = false;
    /**
     * Option Title, der drei Optionen Title/GC-Code/Owner
     */
    private MultiToggleButton mTglBtnTitle;
    /**
     * Option GC-Code, der drei Optionen Title/GC-Code/Owner
     */
    private MultiToggleButton mTglBtnGc;
    /**
     * Option Owner, der drei Optionen Title/GC-Code/Owner
     */
    private MultiToggleButton mTglBtnOwner;
    /**
     * Option Online Suche On/Off
     */
    private MultiToggleButton mTglBtnOnline;
    /**
     * Button, welcher eine Suchanfrage als Filter verwendet
     */
    private ImageButton mBtnFilter;
    /**
     * Button, der eine Suche Startet
     */
    private CB_Button mBtnSearch;
    /**
     * Button, der den nächsten Treffer einer gestarteten Suche findet
     */
    private CB_Button mBtnNext;
    /**
     * Button, der den Search Dialog schliesst
     */
    private CB_Button mBtnCancel;
    /**
     * Such Eingabe Feld
     */
    private EditTextField mEingabe;
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

        float margin = UI_Size_Base.ui_size_base.getMargin();
        float btnWidth = (this.getWidth() - (margin * 7)) / 4;

        CB_RectF rec = new CB_RectF(0, 0, btnWidth, UI_Size_Base.ui_size_base.getButtonHeight());

        mTglBtnTitle = new MultiToggleButton(rec, "mTglBtnTitle");
        mTglBtnGc = new MultiToggleButton(rec, "mTglBtnGc");
        mTglBtnOwner = new MultiToggleButton(rec, "mTglBtnOwner");
        mTglBtnOnline = new MultiToggleButton(rec, "mTglBtnOnline");

        rec.setWidth(btnWidth = (this.getWidth() - (margin * 5)) / 4);

        mBtnFilter = new ImageButton(rec, "mBtnFilter");
        mBtnSearch = new CB_Button(rec, "mBtnSearch");
        mBtnNext = new CB_Button(rec, "mBtnNext");
        mBtnCancel = new CB_Button(rec, "mBtnCancel");

        rec.setWidth(this.getWidth() - (margin * 2));

        mEingabe = new EditTextField(rec, this, "mEingabe", WrapType.SINGLELINE);

        mEingabe.setTextFieldListener(new TextFieldListener() {

            @Override
            public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight) {

            }

            @Override
            public void keyTyped(EditTextFieldBase textField, char key) {
                textBox_TextChanged();
            }
        });

        mEingabe.setText("");

        // Layout! da es sich nicht ändert, brauchen wir es nicht in eine Methode packen
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
        switchSearcheMode(SearchMode.Title);

        mBtnCancel.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                close();
                return true;
            }
        });

        mTglBtnTitle.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchSearcheMode(SearchMode.Title);
                return true;
            }
        });

        mTglBtnGc.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchSearcheMode(SearchMode.GcCode);
                return true;
            }
        });

        mTglBtnOwner.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchSearcheMode(SearchMode.Owner);
                return true;
            }
        });

        mBtnSearch.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                GL.that.setFocusedEditTextField(null);
                mSearchAktive = false;
                beginnSearchIndex = 0;
                searchNow(false);
                return true;
            }
        });

        mBtnNext.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                GL.that.setFocusedEditTextField(null);
                searchNow(true);
                return true;

            }
        });

        mBtnFilter.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                GL.that.setFocusedEditTextField(null);
                if (mTglBtnOnline.getState() == 1) {
                    close();
                    GL.that.postAsync(() -> askPremium());
                } else {
                    setFilter();
                }
                return true;
            }
        });

        mTglBtnOnline.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                setFilterBtnState();
                textBox_TextChanged();
                return true;
            }
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
    private void switchSearcheMode(SearchMode state) {
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

        // TODO Sofort Filter hat eine schlechte Performance, deshalb habe ich ihn ersteinmal abgeschalten.
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

            synchronized (Database.Data.cacheList) {

                if (!mSearchAktive) {
                    mSearchAktive = true;
                }

                Cache tmp = null;
                if (beginnSearchIndex < 0) beginnSearchIndex = 0;
                for (int i = beginnSearchIndex, n = Database.Data.cacheList.size(); i < n; i++) {
                    tmp = Database.Data.cacheList.get(i);

                    switch (mSearchState) {
                        case Title:
                            criterionMatches = tmp.getName().toLowerCase().contains(searchPattern);
                            break;
                        case GcCode:
                            criterionMatches = tmp.getGcCode().toLowerCase().contains(searchPattern);
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
                    MessageBox.show(Translation.get("NoCacheFound"), Translation.get("Search"), MessageBoxButtons.OK, MessageBoxIcon.Asterisk, null);
                } else {
                    if (tmp != null) {
                        Waypoint finalWp = tmp.getCorrectedFinal();
                        if (finalWp == null)
                            finalWp = tmp.getStartWaypoint();
                        GlobalCore.setSelectedWaypoint(tmp, finalWp);
                    }
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

        GlobalCore.chkAPiLogInWithWaitDialog(new iChkReadyHandler() {

            @Override
            public void checkReady(boolean invalidAccessToken) {

                if (invalidAccessToken) {
                    GL.that.RunOnGL(() -> MessageBox.show(Translation.get("apiKeyNeeded"), Translation.get("Clue"), MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null));
                } else {

                    wd = CancelWaitDialog.ShowWait(Translation.get("Search"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

                        @Override
                        public void isCanceled() {
                            closeWaitDialog();
                        }
                    }, new ICancelRunnable() {

                        @Override
                        public void run() {
                            if (isPremiumMember()) {
                                searchOnlineNow();
                            } else {
                                MessageBox.show(Translation.get("GC_basic"), Translation.get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live,
                                        new OnMsgBoxClickListener() {
                                            @Override
                                            public boolean onClick(int which, Object data) {
                                                if (which == MessageBox.BUTTON_POSITIVE) {
                                                    searchOnlineNow();
                                                } else
                                                    closeWaitDialog();
                                                return true;
                                            }
                                        });
                            }
                        }

                        @Override
                        public boolean doCancel() {
                            // TODO Handle Cancel
                            return false;
                        }
                    });

                }
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
        wd = CancelWaitDialog.ShowWait(Translation.get("searchOverAPI"), DownloadAnimation.GetINSTANCE(), () -> closeWaitDialog(), new ICancelRunnable() {

            @Override
            public void run() {

                Coordinate searchCoord;
                if (CB_Action_ShowMap.getInstance().normalMapView.isVisible()) {
                    searchCoord = CB_Action_ShowMap.getInstance().normalMapView.center;
                } else {
                    searchCoord = Locator.getInstance().getMyPosition();
                }
                if (searchCoord == null) {
                    return;
                }

                // alle per API importierten Caches landen in der Category und GpxFilename API-Import Category suchen, die dazu gehört
                Category category = CoreSettingsForward.Categories.getCategory("API-Import");
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
                if (CB_Core_Settings.numberOfLogs.getValue() > 0) {
                    q.resultWithLogs(CB_Core_Settings.numberOfLogs.getValue());
                }

                if (Config.SearchWithoutFounds.getValue()) q.excludeFinds();
                if (Config.SearchWithoutOwns.getValue()) q.excludeOwn();
                if (Config.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

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
                        // todo API 1.0 doesn't allow a pattern (only one GCCode, else handle a list of GCCodes
                        if (searchPattern.contains(",")) {
                            geoCacheRelateds = fetchGeoCaches(q, searchPattern);
                        } else {
                            geoCacheRelateds = fetchGeoCache(q, searchPattern);
                        }
                        break;
                }

                if (geoCacheRelateds.size() > 0) {

                    Database.Data.sql.beginTransaction();

                    CacheDAO cacheDAO = new CacheDAO();
                    LogDAO logDAO = new LogDAO();
                    ImageDAO imageDAO = new ImageDAO();
                    WaypointDAO waypointDAO = new WaypointDAO();

                    int counter = 0;

                    synchronized (Database.Data.cacheList) {
                        for (int j = 0; j < geoCacheRelateds.size(); j++) {
                            GeoCacheRelated geoCacheRelated = geoCacheRelateds.get(j);
                            Cache cache = geoCacheRelated.cache;
                            counter++;
                            if (Database.Data.cacheList.getCacheByIdFromCacheList(cache.Id) == null) {
                                Database.Data.cacheList.add(cache);

                                if (cache.getGPXFilename_ID() == 0) {
                                    cache.setGPXFilename_ID(gpxFilename.Id);
                                } else {
                                    // todo check if this must be done
                                    // get akt category, if not pinned.
                                }

                                cacheDAO.WriteToDatabase(cache);

                                for (LogEntry log : geoCacheRelated.logs) {
                                    logDAO.WriteToDatabase(log);
                                }

                                for (ImageEntry image : geoCacheRelated.images) {
                                    imageDAO.WriteToDatabase(image, false);
                                }

                                for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
                                    Waypoint waypoint = cache.waypoints.get(i);
                                    waypointDAO.WriteToDatabase(waypoint, false); // do not store replication information here
                                }
                            }
                        }
                    }

                    Database.Data.sql.setTransactionSuccessful();
                    Database.Data.sql.endTransaction();

                    Database.Data.GPXFilenameUpdateCacheCount();

                    CacheListChangedEventList.Call();

                    if (counter == 1) {
                        // select this Cache
                        Cache cache = Database.Data.cacheList.getCacheByIdFromCacheList(geoCacheRelateds.get(0).cache.Id);
                        GlobalCore.setSelectedCache(cache);
                    }

                }
                closeWaitDialog();
            }

            @Override
            public boolean doCancel() {
                // TODO Handle Cancel
                return false;
            }
        });
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

        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());

    }

    public void doSearchOnline(final String searchPattern, final SearchMode searchMode) {
        Log.debug(log, "doSearchOnline " + searchPattern);
        try {
            GL.that.RunOnGL(() -> {
                mEingabe.setText(searchPattern);
                GL.that.RunOnGL(() -> {
                    switchSearcheMode(searchMode);
                    GL.that.RunOnGL(() -> {
                        mTglBtnOnline.setState(1);
                        GL.that.RunOnGL(() -> {
                            setFilterBtnState();
                            GL.that.RunOnGL(() -> mBtnSearch.performClick());
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

            setY(CacheListView.getInstance().getMaxY() - this.getHeight());
            CacheListView.getInstance().setTopPlaceHolder(this.getHeight());

            if (!GL.that.PopUpIsShown())
                that.showNotCloseAutomaticly();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Slider.that.registerPosChangedEvent(listener);
    }

    @Override
    public void onHide() {
        Slider.that.removePosChangedEvent(listener);
        CacheListView.getInstance().resetPlaceHolder();
    }

    private void askPremium() {

        // First check API-Key with visual Feedback
        GlobalCore.chkAPiLogInWithWaitDialog(new iChkReadyHandler() {

            @Override
            public void checkReady(boolean invalidAccessToken) {
                if (invalidAccessToken) {
                    GL.that.RunOnGL(() -> MessageBox.show(Translation.get("apiKeyNeeded"), Translation.get("Clue"), MessageBoxButtons.OK, MessageBoxIcon.Exclamation, null));
                } else {
                    closeWD();
                    GL.that.RunOnGL(() -> {
                        if (isPremiumMember()) {
                            showTargetApiDialog();
                        } else {
                            MSB = MessageBox.show(Translation.get("GC_basic"), Translation.get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, new OnMsgBoxClickListener() {
                                @Override
                                public boolean onClick(int which, Object data) {
                                    closeMsgBox();
                                    if (which == MessageBox.BUTTON_POSITIVE) {
                                        showTargetApiDialog();
                                    }
                                    return true;
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private void closeMsgBox() {
        MSB.close();
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

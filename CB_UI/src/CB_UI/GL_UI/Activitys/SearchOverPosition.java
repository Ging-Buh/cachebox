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
package CB_UI.GL_UI.Activitys;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Api.SearchCoordinate;
import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Types.*;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI.GL_UI.Controls.CoordinateButton.ICoordinateChangedListener;
import CB_UI.GL_UI.Views.MapView;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Interfaces.ICancel;
import CB_Utils.Lists.CB_List;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

public class SearchOverPosition extends ActivityBase {
    private final float lineHeight;
    private Button bOK, bCancel, btnPlus, btnMinus;
    private Label lblTitle, lblRadius, lblRadiusEinheit, lblMarkerPos, lblExcludeFounds, lblOnlyAvailable, lblExcludeHides;
    private Image gsLogo;
    private CoordinateButton coordBtn;
    private ChkBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private EditTextField Radius;
    private MultiToggleButton tglBtnGPS, tglBtnMap;
    private Coordinate actSearchPos;
    private volatile Thread thread;
    private ImportAnimation dis;
    private Box box;
    private boolean importRuns = false;

    /**
     * 0=GPS, 1= Map, 2= Manuell
     */
    private int searcheState = 0;
    private boolean isCanceld = false;
    ICancel icancel = new ICancel() {

        @Override
        public boolean cancel() {
            return isCanceld;
        }
    };

    public SearchOverPosition() {
        super(ActivityRec(), "searchOverPosActivity");
        lineHeight = UI_Size_Base.that.getButtonHeight();

        createOkCancelBtn();
        createBox();
        createTitleLine();
        createRadiusLine();
        createChkBoxLines();
        createToggleButtonLine();
        createCoordButton();

        initialContent();
    }

    public static SearchOverPosition ShowInstanz() {

        SearchOverPosition ret = new SearchOverPosition();
        ret.initialCoordinates();
        ret.show();
        return ret;
    }

    private void createOkCancelBtn() {
        bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
        bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        // Translations
        bOK.setText(Translation.Get("import"));
        bCancel.setText(Translation.Get("cancel"));

        this.addChild(bOK);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                ImportNow();
                return true;
            }

        });

        this.addChild(bCancel);
        bCancel.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (importRuns) {
                    isCanceld = true;
                } else {
                    finish();
                }
                return true;
            }
        });

    }

    private void createBox() {
        box = new Box(ActivityRec(), "ScrollBox");
        this.addChild(box);
        box.setHeight(this.getHeight() - lineHeight - bOK.getMaxY() - margin - margin);
        box.setY(bOK.getMaxY() + margin);
        box.setBackground(this.getBackground());
    }

    private void createTitleLine() {

        float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

        gsLogo = new Image(innerWidth - margin - lineHeight, this.getHeight() - this.getTopHeight() - lineHeight - margin, lineHeight, lineHeight, "", false);
        gsLogo.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.dayGcLiveIcon.name())));
        this.addChild(gsLogo);

        lblTitle = new Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - (margin * 4) - gsLogo.getWidth(), lineHeight);
        lblTitle.setFont(Fonts.getBig());
        lblTitle.setWrappedText(Translation.Get("importCachesOverPosition"));
        this.addChild(lblTitle);

    }

    private void createRadiusLine() {
        String sRadius = Translation.Get("Radius");
        String sEinheit = Config.ImperialUnits.getValue() ? "mi" : "km";

        float wRadius = Fonts.Measure(sRadius).width;
        float wEinheit = Fonts.Measure(sEinheit).width;

        float y = box.getHeight() - margin - lineHeight;

        lblRadius = new Label(this.name + " lblRadius", margin, y, wRadius, lineHeight);
        lblRadius.setText(sRadius);
        box.addChild(lblRadius);

        CB_RectF rec = new CB_RectF(lblRadius.getMaxX() + margin, y, UI_Size_Base.that.getButtonWidthWide(), lineHeight);
        Radius = new EditTextField(rec, this, this.name + " Radius");
        box.addChild(Radius);

        lblRadiusEinheit = new Label(this.name + " lblRadiusEinheit", Radius.getMaxX(), y, wEinheit, lineHeight);
        lblRadiusEinheit.setText(sEinheit);
        box.addChild(lblRadiusEinheit);

        btnMinus = new Button(lblRadiusEinheit.getMaxX() + (margin * 3), y, lineHeight, lineHeight, "");
        btnMinus.setText("-");
        box.addChild(btnMinus);

        btnPlus = new Button(btnMinus.getMaxX() + (margin * 2), y, lineHeight, lineHeight, "");
        btnPlus.setText("+");
        box.addChild(btnPlus);

    }

    private void createChkBoxLines() {
        checkBoxOnlyAvailable = new ChkBox("");
        checkBoxOnlyAvailable.setPos(margin, Radius.getY() - margin - checkBoxOnlyAvailable.getHeight());
        box.addChild(checkBoxOnlyAvailable);

        checkBoxExcludeHides = new ChkBox("");
        checkBoxExcludeHides.setPos(margin, checkBoxOnlyAvailable.getY() - margin - checkBoxExcludeHides.getHeight());
        box.addChild(checkBoxExcludeHides);

        checkBoxExcludeFounds = new ChkBox("");
        checkBoxExcludeFounds.setPos(margin, checkBoxExcludeHides.getY() - margin - checkBoxExcludeFounds.getHeight());
        box.addChild(checkBoxExcludeFounds);

        lblOnlyAvailable = new Label(this.name + " lblOnlyAvailable", checkBoxOnlyAvailable, Translation.Get("SearchOnlyAvailable"));
        lblOnlyAvailable.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblOnlyAvailable.setWidth(this.getWidth() - margin - checkBoxOnlyAvailable.getMaxX() - margin);
        box.addChild(lblOnlyAvailable);

        lblExcludeHides = new Label(this.name + " lblExcludeHides", checkBoxExcludeHides, Translation.Get("SearchWithoutOwns"));
        lblExcludeHides.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblExcludeHides.setWidth(this.getWidth() - margin - checkBoxExcludeHides.getMaxX() - margin);
        box.addChild(lblExcludeHides);

        lblExcludeFounds = new Label(this.name + " lblExcludeFounds", checkBoxExcludeFounds, Translation.Get("SearchWithoutFounds"));
        lblExcludeFounds.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblExcludeFounds.setWidth(this.getWidth() - margin - checkBoxExcludeFounds.getMaxX() - margin);
        box.addChild(lblExcludeFounds);

    }

    private void createToggleButtonLine() {
        float y = lblExcludeFounds.getY() - margin - UI_Size_Base.that.getButtonHeight();

        tglBtnGPS = new MultiToggleButton(leftBorder, y, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "");
        tglBtnMap = new MultiToggleButton(tglBtnGPS.getMaxX(), y, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "");

        tglBtnGPS.setFont(Fonts.getSmall());
        tglBtnMap.setFont(Fonts.getSmall());

        tglBtnGPS.initialOn_Off_ToggleStates(Translation.Get("FromGps"), Translation.Get("FromGps"));
        tglBtnMap.initialOn_Off_ToggleStates(Translation.Get("FromMap"), Translation.Get("FromMap"));

        box.addChild(tglBtnGPS);
        box.addChild(tglBtnMap);

        if (MapView.that == null)
            tglBtnMap.disable();

    }

    private void createCoordButton() {
        CB_RectF rec = new CB_RectF(margin, tglBtnGPS.getY() - margin - lineHeight, this.getWidth() - (margin * 2), lineHeight);
        lblMarkerPos = new Label(this.name + " lblMarkerPos", rec, Translation.Get("CurentMarkerPos"));
        box.addChild(lblMarkerPos);

        coordBtn = new CoordinateButton(rec, name, null, null);
        coordBtn.setY(lblMarkerPos.getY() - margin - lineHeight);
        box.addChild(coordBtn);

    }

    private void initialContent() {

        btnPlus.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                incrementRadius(1);
                return true;
            }

        });

        btnMinus.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                incrementRadius(-1);
                return true;
            }
        });

        tglBtnGPS.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                actSearchPos = Locator.getCoordinate();
                setToggleBtnState(0);
                return true;
            }
        });

        tglBtnMap.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (MapView.that == null) {
                    actSearchPos = new CoordinateGPS(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());
                } else {
                    actSearchPos = MapView.that.center;
                }

                setToggleBtnState(1);
                return true;
            }
        });

        coordBtn.setCoordinateChangedListener(new ICoordinateChangedListener() {

            @Override
            public void coordinateChanged(Coordinate coord) {
                if (coord != null) {
                    actSearchPos = coord;
                    setToggleBtnState(2);
                }
                SearchOverPosition.this.show();
            }
        });

        if (MapView.that != null && MapView.that.isVisible()) {
            actSearchPos = MapView.that.center;
            searcheState = 1;
        } else {
            actSearchPos = Locator.getCoordinate();
            searcheState = 0;
        }

        checkBoxExcludeFounds.setChecked(Config.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Config.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Config.SearchWithoutOwns.getValue());
        Radius.setText(String.valueOf(Config.lastSearchRadius.getValue()));
        setToggleBtnState();

    }

    private void initialCoordinates() {
        // initiate Coordinates to actual Map-Center or actual GPS Coordinate
        switch (searcheState) {
            case 0:
                actSearchPos = Locator.getCoordinate();
                break;
            case 1:
                if (MapView.that == null) {
                    actSearchPos = new CoordinateGPS(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());
                } else {
                    actSearchPos = MapView.that.center;
                }
                break;
        }
        setToggleBtnState();
    }

    private void incrementRadius(int value) {
        try {
            int ist = Integer.parseInt(Radius.getText().toString());
            ist += value;

            if (ist > 100)
                ist = 100;
            if (ist < 1)
                ist = 1;

            Radius.setText(String.valueOf(ist));
        } catch (NumberFormatException e) {

        }
    }

    /**
     * 0=GPS, 1= Map, 2= Manuell
     */
    public void setToggleBtnState(int value) {
        searcheState = value;
        setToggleBtnState();
    }

    private void setToggleBtnState() {// 0=GPS, 1= Map, 2= Manuell
        switch (searcheState) {
            case 0:
                tglBtnGPS.setState(1);
                tglBtnMap.setState(0);
                break;
            case 1:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(1);
                break;
            case 2:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(0);
                break;

        }
        coordBtn.setCoordinate(actSearchPos);

    }

    private void ImportNow() {
        isCanceld = false;
        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

        int radius = 0;
        try {
            radius = Integer.parseInt(Radius.getText().toString());
        } catch (NumberFormatException e) {
            // Kein Integer
            e.printStackTrace();
        }

        if (radius != 0)
            Config.lastSearchRadius.setValue(radius);

        Config.AcceptChanges();

        bOK.disable();

        // disable UI
        dis = new ImportAnimation(box);
        dis.setBackground(getBackground());

        this.addChild(dis, false);

        importRuns = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean threadCanceld = false;

                try {
                    if (actSearchPos != null) {

                        // alle per API importierten Caches landen in der Category und
                        // GpxFilename
                        // API-Import
                        // Category suchen, die dazu gehört
                        Category category = CoreSettingsForward.Categories.getCategory("API-Import");
                        if (category != null) // should not happen!!!
                        {
                            GpxFilename gpxFilename = category.addGpxFilename("API-Import");
                            if (gpxFilename != null) {
                                CB_List<Cache> apiCaches = new CB_List<Cache>();
                                ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
                                ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
                                SearchCoordinate searchC = new SearchCoordinate(50, actSearchPos, Config.lastSearchRadius.getValue() * 1000);

                                searchC.excludeFounds = Config.SearchWithoutFounds.getValue();
                                searchC.excludeHides = Config.SearchWithoutOwns.getValue();
                                searchC.available = Config.SearchOnlyAvailable.getValue();

                                dis.setAnimationType(AnimationType.Download);
                                CB_UI.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, gpxFilename.Id, icancel);
                                dis.setAnimationType(AnimationType.Work);
                                if (apiCaches.size() > 0) {
                                    GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
                                }

                            }
                        }
                    }
                } catch (Exception e) {
                    // Thread abgebrochen!
                    threadCanceld = true;
                }

                if (!threadCanceld) {
                    CacheListChangedEventList.Call();
                    if (dis != null) {
                        SearchOverPosition.this.removeChildsDirekt(dis);
                        dis.dispose();
                        dis = null;
                    }
                    bOK.enable();
                    finish();
                } else {

                    // Notify Map
                    if (MapView.that != null)
                        MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);
                    if (dis != null) {
                        SearchOverPosition.this.removeChildsDirekt(dis);
                        dis.dispose();
                        dis = null;
                    }
                    bOK.enable();
                }
                importRuns = false;
            }

        });

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

    }

    @Override
    public void dispose() {
        if (bOK != null)
            bOK.dispose();
        bOK = null;
        if (bCancel != null)
            bCancel.dispose();
        bCancel = null;
        if (btnPlus != null)
            btnPlus.dispose();
        btnPlus = null;
        if (btnMinus != null)
            btnMinus.dispose();
        btnMinus = null;
        if (lblTitle != null)
            lblTitle.dispose();
        lblTitle = null;
        if (lblRadius != null)
            lblRadius.dispose();
        lblRadius = null;
        if (lblRadiusEinheit != null)
            lblRadiusEinheit.dispose();
        lblRadiusEinheit = null;
        if (lblMarkerPos != null)
            lblMarkerPos.dispose();
        lblMarkerPos = null;
        if (lblExcludeFounds != null)
            lblExcludeFounds.dispose();
        lblExcludeFounds = null;
        if (lblOnlyAvailable != null)
            lblOnlyAvailable.dispose();
        lblOnlyAvailable = null;
        if (lblExcludeHides != null)
            lblExcludeHides.dispose();
        lblExcludeHides = null;
        if (gsLogo != null)
            gsLogo.dispose();
        gsLogo = null;
        if (coordBtn != null)
            coordBtn.dispose();
        coordBtn = null;
        if (checkBoxExcludeFounds != null)
            checkBoxExcludeFounds.dispose();
        checkBoxExcludeFounds = null;
        if (checkBoxOnlyAvailable != null)
            checkBoxOnlyAvailable.dispose();
        checkBoxOnlyAvailable = null;
        if (checkBoxExcludeHides != null)
            checkBoxExcludeHides.dispose();
        checkBoxExcludeHides = null;
        if (Radius != null)
            Radius.dispose();
        Radius = null;
        if (tglBtnGPS != null)
            tglBtnGPS.dispose();
        tglBtnGPS = null;
        if (tglBtnMap != null)
            tglBtnMap.dispose();
        tglBtnMap = null;
        if (dis != null)
            dis.dispose();
        dis = null;
        if (box != null)
            box.dispose();
        box = null;

        actSearchPos = null;

        super.dispose();

    }

}

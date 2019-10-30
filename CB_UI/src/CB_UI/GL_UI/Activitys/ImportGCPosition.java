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

import CB_Core.CB_Core_Settings;
import CB_Core.CacheListChangedListeners;
import CB_Core.CoreSettingsForward;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_UI.GlobalCore;
import CB_UI.WriteIntoDB;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_Utils.Interfaces.ICancel;
import CB_Utils.Log.Log;
import CB_Utils.Util.UnitFormatter;
import android.text.InputType;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static CB_Core.Api.GroundspeakAPI.*;
import static CB_Locator.Map.MapViewBase.INITIAL_WP_LIST;

public class ImportGCPosition extends ActivityBase implements KeyboardFocusChangedEvent {
    private static final String log = "ImportGCPosition";
    private CB_Button btnOK, btnCancel, btnPlus, btnMinus;
    private CB_Label lblHeader, lblRadius, lblRadiusEinheit, lblExcludeFounds, lblOnlyAvailable, lblExcludeHides;
    private Image gsLogo;
    private CoordinateButton coordinateButton;
    private CB_CheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private EditTextField txtRadius;
    private MultiToggleButton tglBtnGPS, tglBtnMap, tglBtnWeb;
    private Coordinate actSearchPos;
    private ImportAnimation dis;
    private Box box;
    private ScrollBox scrollBox;
    private boolean importRuns = false;
    private int searchState = 0; // 0=GPS, 1= Map, 2= Manuell
    private boolean isCanceld = false;
    ICancel icancel = () -> isCanceld;
    private CB_Label lblPublished;
    private CB_Button btnBeforeAfterEqual;
    private EditTextField edtDate;
    private CB_Label lblImportLimit;
    private EditTextField edtImportLimit;
    private CB_Label lblCacheName;
    private EditTextField edtCacheName;
    private CB_Label lblOwner;
    private EditTextField edtOwner;
    private CB_Label lblCategory;
    private EditTextField edtCategory;
    private SimpleDateFormat simpleDateFormat;
    private SearchCoordinates searchCoordinates;
    public ImportGCPosition() {
        super(ActivityRec(), "searchOverPosActivity");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.removeChilds();

        // add to this
        // createHeaderLine();
        btnOK = new CB_Button(Translation.get("import"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        this.initRow(BOTTOMUP);
        this.addNext(btnOK);
        this.addLast(btnCancel);
        scrollBox = new ScrollBox(0, this.getAvailableHeight());
        scrollBox.setBackground(this.getBackground());
        this.addLast(scrollBox);
        box = new Box(scrollBox.getInnerWidth(), 0); // height will be adjusted after containing all controls
        // add to box
        coordinateButton = new CoordinateButton("");
        box.addLast(coordinateButton);
        createToggleButtonLine();
        createRadiusLine();
        createImportLimitLine();
        createCacheNameLine();
        createOwnerLine();
        createPublishedLine();
        createCategoryLine();
        createChkBoxLines();
        box.adjustHeight();
        scrollBox.addChild(box);
        scrollBox.setVirtualHeight(box.getHeight());

        initClickHandlersAndContent();

    }

    private void createHeaderLine() {
        lblHeader = new CB_Label(Translation.get("importCachesOverPosition"));
        lblHeader.setFont(Fonts.getBig()).setHAlignment(CB_Label.HAlignment.CENTER);
        float lineHeight = lblHeader.getHeight();
        gsLogo = new Image(0, 0, lineHeight, lineHeight, "", false);
        gsLogo.setDrawable(new SpriteDrawable(Sprites.getSprite(Sprites.IconName.dayGcLiveIcon.name())));
        this.addNext(lblHeader); // sets the width
        this.addLast(gsLogo, FIXED);
        lblHeader.setWrapType(WrapType.WRAPPED);
        lblHeader.updateHeight(this, true);
    }

    private void createRadiusLine() {
        String sRadius = Translation.get("Radius");
        String sEinheit = Config.ImperialUnits.getValue() ? "mi" : "km";

        float wRadius = Fonts.Measure(sRadius).width;
        float wEinheit = Fonts.Measure(sEinheit).width;

        lblRadius = new CB_Label(sRadius);
        lblRadius.setWidth(wRadius);
        //box.addNext(lblRadius, FIXED);
        box.addNext(lblRadius);

        txtRadius = new EditTextField(this, "*" + Translation.get("Radius"));
        txtRadius.setInputType(InputType.TYPE_CLASS_NUMBER);
        //box.addNext(txtRadius);
        box.addNext(txtRadius);

        lblRadiusEinheit = new CB_Label(sEinheit);
        lblRadiusEinheit.setWidth(wEinheit);
        //box.addNext(lblRadiusEinheit, FIXED);
        box.addNext(lblRadiusEinheit, FIXED);

        btnMinus = new CB_Button("-");
        //box.addNext(btnMinus);
        box.addNext(btnMinus);

        btnPlus = new CB_Button("+");
        box.addLast(btnPlus);

    }

    private void createCategoryLine() {
        lblCategory = new CB_Label(Translation.get("category"));
        lblCategory.setWidth(Fonts.Measure(lblCategory.getText()).width);
        box.addNext(lblCategory, FIXED);
        edtCategory = new EditTextField(this, "*" + Translation.get("category"));
        box.addLast(edtCategory);
    }

    private void createImportLimitLine() {
        lblImportLimit = new CB_Label(Translation.get("ImportLimit"));
        lblImportLimit.setWidth(Fonts.Measure(lblImportLimit.getText()).width);
        box.addNext(lblImportLimit, FIXED);
        edtImportLimit = new EditTextField(this, "*" + Translation.get("ImportLimit"));
        edtImportLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addLast(edtImportLimit);
    }

    private void createCacheNameLine() {
        lblCacheName = new CB_Label(Translation.get("Title"));
        lblCacheName.setWidth(Fonts.Measure(lblCacheName.getText()).width);
        box.addNext(lblCacheName, FIXED);
        edtCacheName = new EditTextField(this, "*" + Translation.get("Title"));
        box.addLast(edtCacheName);
    }

    private void createOwnerLine() {
        lblOwner = new CB_Label(Translation.get("Owner"));
        lblOwner.setWidth(Fonts.Measure(lblOwner.getText()).width);
        box.addNext(lblOwner, FIXED);
        edtOwner = new EditTextField(this, "*" + Translation.get("Owner"));
        box.addLast(edtOwner);
    }

    private void createPublishedLine() {
        lblPublished = new CB_Label(Translation.get("published"));
        box.addNext(lblPublished);
        btnBeforeAfterEqual = new CB_Button("<=");
        btnBeforeAfterEqual.setWidth(Fonts.Measure(". <= .").width);
        box.addNext(btnBeforeAfterEqual, FIXED);
        edtDate = new EditTextField(this, "*" + Translation.get("published"));
        edtDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        edtDate.setWidth(Fonts.Measure(". 2018-12-31 .").width);
        edtDate.setText(simpleDateFormat.format(new Date()));
        box.addLast(edtDate, FIXED);
    }

    private void createChkBoxLines() {

        checkBoxOnlyAvailable = new CB_CheckBox("");
        box.addNext(checkBoxOnlyAvailable, FIXED);
        lblOnlyAvailable = new CB_Label(Translation.get("SearchOnlyAvailable"));
        box.addLast(lblOnlyAvailable);

        checkBoxExcludeHides = new CB_CheckBox("");
        box.addNext(checkBoxExcludeHides, FIXED);
        lblExcludeHides = new CB_Label(Translation.get("SearchWithoutOwns"));
        box.addLast(lblExcludeHides);

        checkBoxExcludeFounds = new CB_CheckBox("");
        box.addNext(checkBoxExcludeFounds, FIXED);
        lblExcludeFounds = new CB_Label(Translation.get("SearchWithoutFounds"));
        box.addLast(lblExcludeFounds);

    }

    private void createToggleButtonLine() {

        tglBtnGPS = new MultiToggleButton("");
        tglBtnMap = new MultiToggleButton("");
        tglBtnWeb = new MultiToggleButton("");

        tglBtnGPS.setFont(Fonts.getSmall());
        tglBtnMap.setFont(Fonts.getSmall());
        tglBtnWeb.setFont(Fonts.getSmall());

        tglBtnGPS.initialOn_Off_ToggleStates(Translation.get("FromGps"), Translation.get("FromGps"));
        tglBtnMap.initialOn_Off_ToggleStates(Translation.get("FromMap"), Translation.get("FromMap"));
        tglBtnWeb.initialOn_Off_ToggleStates(Translation.get("FromWeb"), Translation.get("FromWeb"));

        box.addNext(tglBtnGPS);
        box.addNext(tglBtnMap);
        box.addLast(tglBtnWeb);

    }

    private void initClickHandlersAndContent() {

        btnOK.addClickHandler((v, x, y, pointer, button) -> {
            ImportNow();
            return true;
        });

        btnCancel.addClickHandler((v, x, y, pointer, button) -> {
            if (importRuns) {
                isCanceld = true;
            } else {
                finish();
            }
            return true;
        });

        btnPlus.addClickHandler((v, x, y, pointer, button) -> {
            incrementRadius(1);
            return true;
        });

        btnMinus.addClickHandler((v, x, y, pointer, button) -> {
            incrementRadius(-1);
            return true;
        });

        tglBtnGPS.addClickHandler((v, x, y, pointer, button) -> {
            actSearchPos = Locator.getInstance().getMyPosition();
            setToggleBtnState(0);
            return true;
        });

        tglBtnMap.addClickHandler((v, x, y, pointer, button) -> {
            actSearchPos = CB_Action_ShowMap.getInstance().normalMapView.center;
            setToggleBtnState(1);
            return true;
        });

        tglBtnWeb.addClickHandler((v, x, y, pointer, button) -> {
            actSearchPos = Locator.getInstance().getMyPosition();
            searchCoordinates = new SearchCoordinates() {
                public void callBack(Coordinate coordinate) {
                    if (coordinate != null) {
                        actSearchPos = coordinate;
                        setToggleBtnState(2);
                    }
                    searchCoordinates.doFinish();
                }
            };
            searchCoordinates.doShow();
            return true;
        });

        btnBeforeAfterEqual.addClickHandler((v, x, y, pointer, button) -> {
            switch (btnBeforeAfterEqual.getText()) {
                case "X":
                    btnBeforeAfterEqual.setText("<=");
                    break;
                case "<=":
                    btnBeforeAfterEqual.setText("=");
                    break;
                case "=":
                    btnBeforeAfterEqual.setText(">=");
                    break;
                default:
                    btnBeforeAfterEqual.setText("X");
                    break;
            }
            return true;
        });

        if (CB_Action_ShowMap.getInstance().normalMapView.isVisible()) {
            actSearchPos = CB_Action_ShowMap.getInstance().normalMapView.center;
            searchState = 1;
        } else {
            actSearchPos = Locator.getInstance().getMyPosition();
            searchState = 0;
        }
        setToggleBtnState(searchState);

        checkBoxExcludeFounds.setChecked(Config.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Config.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Config.SearchWithoutOwns.getValue());
        txtRadius.setText(String.valueOf(Config.lastSearchRadius.getValue()));

        edtCategory.setText("API-Import");
        if (GlobalCore.isSetSelectedCache()) {
            long id = GlobalCore.getSelectedCache().getGPXFilename_ID();
            Category c = CoreSettingsForward.Categories.getCategoryByGpxFilenameId(id);
            if (c != null)
                edtCategory.setText(c.GpxFilename);
        }
        edtCategory.setCursorPosition(0);

        Category category = CoreSettingsForward.Categories.getCategory(edtCategory.getText());
        edtDate.setText(simpleDateFormat.format(category.LastImported()));
        /*
        if (category.size() == 0)
            btnBeforeAfterEqual.setText("<=");
        else
            btnBeforeAfterEqual.setText(">=");
         */
        btnBeforeAfterEqual.setText("X");
        edtImportLimit.setText("" + Config.ImportLimit.getValue());
    }

    @Override
    public void KeyboardFocusChanged(EditTextField editTextField) {
        if (editTextField != null) {
            scrollToY(editTextField);
            editTextField.setCursorPosition(editTextField.getText().length());
        } else {
            if (GL.that.getFocusedEditTextField().equals(edtCategory)) {
                Category category = CoreSettingsForward.Categories.getCategory(edtCategory.getText());
                edtDate.setText(simpleDateFormat.format(category.LastImported()));
                /*
                if (category.size() == 0)
                    btnBeforeAfterEqual.setText("<=");
                else
                    btnBeforeAfterEqual.setText(">=");
                */
                btnBeforeAfterEqual.setText("X");
            }
        }
    }

    @Override
    public void onShow() {
        KeyboardFocusChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.Remove(this);
    }

    private void scrollToY(final EditTextField editTextField) {
        scrollBox.scrollTo(-scrollBox.getVirtualHeight() + editTextField.getY() + editTextField.getHeight());
    }

    private void incrementRadius(int direction) {
        try {
            int ist = Integer.parseInt(txtRadius.getText());
            ist += direction;
            if (ist > 100)
                ist = 100;
            if (ist < 1)
                ist = 1;
            txtRadius.setText(String.valueOf(ist));
        } catch (NumberFormatException ignored) {
        }
    }

    private void setToggleBtnState(int value) {// 0=GPS, 1= Map, 2= Web, 3= Manuell
        searchState = value;
        switch (searchState) {
            case 0:
                tglBtnGPS.setState(1);
                tglBtnMap.setState(0);
                tglBtnWeb.setState(0);
                break;
            case 1:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(1);
                tglBtnWeb.setState(0);
                break;
            case 2:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(0);
                tglBtnWeb.setState(1);
                break;
            case 3:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(0);
                tglBtnWeb.setState(0);
                break;

        }
        coordinateButton.setCoordinate(actSearchPos);

    }

    private void ImportNow() {
        btnOK.disable();
        importRuns = true;

        isCanceld = false;
        // disable UI
        dis = new ImportAnimation(box);
        dis.setBackground(getBackground());
        box.addChild(dis, false);

        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());
        Config.AcceptChanges();

        Date tmpDate;
        try {
            tmpDate = simpleDateFormat.parse(edtDate.getText());
        } catch (Exception ex) {
            tmpDate = new Date();
        }
        final Date publishDate = tmpDate;

        // category.GpxFilename == edtCategory.getText()
        //.resultWithImages(30)
        // Thread abgebrochen!
        // Notify Map
        Thread thread = new Thread(() -> {
            boolean threadCanceled = false;

            try {
                if (actSearchPos != null) {
                    Category category = CoreSettingsForward.Categories.getCategory(edtCategory.getText());
                    GpxFilename gpxFilename = category.addGpxFilename(category.GpxFilename); // category.GpxFilename == edtCategory.getText()
                    if (gpxFilename != null) {
                        Query q = new Query()
                                .resultWithFullFields()
                                //.resultWithImages(30)
                                ;
                        if (!btnBeforeAfterEqual.getText().equals("X")) {
                            q.publishedDate(publishDate, btnBeforeAfterEqual.getText());
                        }
                        if (CB_Core_Settings.numberOfLogs.getValue() > 0) {
                            q.resultWithLogs(CB_Core_Settings.numberOfLogs.getValue());
                        }
                        if (txtRadius.getText().trim().length() > 0) {
                            int radius;
                            try {
                                radius = Integer.parseInt(txtRadius.getText());
                                if (Config.ImperialUnits.getValue()) radius = UnitFormatter.getKilometer(radius);
                                Config.lastSearchRadius.setValue(radius);
                                Config.AcceptChanges();
                                q.searchInCircle(actSearchPos, radius * 1000);
                            } catch (NumberFormatException nex) {
                                q.searchInCircle(actSearchPos, Config.lastSearchRadius.getValue() * 1000);
                            }
                        }
                        if (edtOwner.getText().trim().length() > 0) q.searchForOwner(edtOwner.getText().trim());
                        if (edtCacheName.getText().trim().length() > 0) q.searchForTitle(edtCacheName.getText().trim());

                        if (Config.SearchWithoutFounds.getValue()) q.excludeFinds();
                        if (Config.SearchWithoutOwns.getValue()) q.excludeOwn();
                        if (Config.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

                        int importLimit;
                        try {
                            importLimit = Integer.parseInt(edtImportLimit.getText());
                        } catch (Exception ex) {
                            importLimit = Config.ImportLimit.getDefaultValue();
                        }
                        q.setMaxToFetch(importLimit);
                        Config.ImportLimit.setValue(importLimit);

                        dis.setAnimationType(AnimationType.Download);
                        ArrayList<GeoCacheRelated> geoCacheRelateds = searchGeoCaches(q);
                        dis.setAnimationType(AnimationType.Work);

                        if (geoCacheRelateds.size() > 0) {
                            try {
                                WriteIntoDB.CachesAndLogsAndImagesIntoDB(geoCacheRelateds, gpxFilename);
                            } catch (InterruptedException e) {
                                Log.err(log, "WriteIntoDB.CachesAndLogsAndImagesIntoDB", e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Thread abgebrochen!
                threadCanceled = true;
            }

            if (!threadCanceled) {
                CacheListChangedListeners.getInstance().cacheListChanged();
                if (dis != null) {
                    removeChildsDirekt(dis);
                    dis.dispose();
                    dis = null;
                }
                btnOK.enable();
                finish();
            } else {
                // Notify Map
                CB_Action_ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);
                if (dis != null) {
                    ImportGCPosition.this.removeChildsDirekt(dis);
                    dis.dispose();
                    dis = null;
                }
                btnOK.enable();
            }
            importRuns = false;
        });

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

    }

    @Override
    public void dispose() {
        dispose(btnOK);
        dispose(btnCancel);
        dispose(btnBeforeAfterEqual);
        dispose(btnPlus);
        dispose(btnMinus);
        dispose(lblHeader);
        dispose(lblRadius);
        dispose(lblRadiusEinheit);
        dispose(lblExcludeFounds);
        dispose(lblOnlyAvailable);
        dispose(lblExcludeHides);
        dispose(lblImportLimit);
        dispose(edtImportLimit);
        dispose(lblCacheName);
        dispose(edtCacheName);
        dispose(lblOwner);
        dispose(edtOwner);
        dispose(gsLogo);
        dispose(coordinateButton);
        dispose(checkBoxExcludeFounds);
        dispose(checkBoxOnlyAvailable);
        dispose(checkBoxExcludeHides);
        dispose(txtRadius);
        dispose(tglBtnGPS);
        dispose(tglBtnMap);
        dispose(dis);
        dispose(box);
        dispose(lblPublished);
        dispose(edtDate);
        dispose(lblCategory);
        dispose(edtCategory);

        actSearchPos = null;

        super.dispose();

    }

    private void dispose(CB_View_Base control) {
        if (control != null)
            control.dispose();
        control = null;
    }

}

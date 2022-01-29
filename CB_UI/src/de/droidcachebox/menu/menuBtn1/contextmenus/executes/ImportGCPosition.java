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
package de.droidcachebox.menu.menuBtn1.contextmenus.executes;

import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.Query;
import static de.droidcachebox.core.GroundspeakAPI.searchGeoCaches;
import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;
import static de.droidcachebox.menu.Action.ShowMap;

import android.text.InputType;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.activities.SearchCoordinates;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CoordinateButton;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ImportAnimation;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.MultiToggleButton;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

public class ImportGCPosition extends ActivityBase implements KeyboardFocusChangedEventList.KeyboardFocusChangedEvent {
    private static final String sClass = "ImportGCPosition";
    private final CB_Button btnOK;
    private final CB_Button btnCancel;
    private final CoordinateButton coordinateButton;
    private final Box box;
    private final ScrollBox scrollBox;
    private final SimpleDateFormat simpleDateFormat;
    private final AtomicBoolean isCanceled;
    private CB_Button btnPlus;
    private CB_Button btnMinus;
    private CB_CheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private EditTextField txtRadius;
    private MultiToggleButton tglBtnGPS, tglBtnMap, tglBtnWeb;
    private Coordinate actSearchPos;
    private ImportAnimation dis;
    private boolean importStarted = false;
    private int searchState = 0; // 0=GPS, 1=Map, 2=manuel
    private CB_Button btnBeforeAfterEqual;
    private EditTextField edtDate;
    private EditTextField edtImportLimit;
    private EditTextField edtCacheName;
    private EditTextField edtOwner;
    private EditTextField edtCategory;
    private SearchCoordinates searchCoordinates;

    public ImportGCPosition() {
        super("searchOverPosActivity");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        this.removeChildren();

        // add to this
        // createHeaderLine();
        btnOK = new CB_Button(Translation.get("import"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        this.initRow(BOTTOMUp);
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
        isCanceled = new AtomicBoolean();

    }

    private void createHeaderLine() {
        CB_Label lblHeader = new CB_Label(Translation.get("importCachesOverPosition"));
        lblHeader.setFont(Fonts.getBig()).setHAlignment(CB_Label.HAlignment.CENTER);
        float lineHeight = lblHeader.getHeight();
        Image gsLogo = new Image(0, 0, lineHeight, lineHeight, "", false);
        gsLogo.setDrawable(new SpriteDrawable(Sprites.getSprite(Sprites.IconName.dayGcLiveIcon.name())));
        this.addNext(lblHeader); // sets the width
        this.addLast(gsLogo, FIXED);
        lblHeader.setWrapType(WrapType.WRAPPED);
        lblHeader.updateHeight(this, true);
    }

    private void createRadiusLine() {
        String sRadius = Translation.get("Radius");
        String sUnit = Settings.ImperialUnits.getValue() ? "mi" : "km";

        float wRadius = Fonts.measure(sRadius).width;
        float wUnit = Fonts.measure(sUnit).width;

        CB_Label lblRadius = new CB_Label(sRadius);
        lblRadius.setWidth(wRadius);
        //box.addNext(lblRadius, FIXED);
        box.addNext(lblRadius);

        txtRadius = new EditTextField(this, "*" + Translation.get("Radius"));
        txtRadius.setInputType(InputType.TYPE_CLASS_NUMBER);
        //box.addNext(txtRadius);
        box.addNext(txtRadius);

        CB_Label lblRadiusUnit = new CB_Label(sUnit);
        lblRadiusUnit.setWidth(wUnit);
        box.addNext(lblRadiusUnit, FIXED);

        btnMinus = new CB_Button("-");
        //box.addNext(btnMinus);
        box.addNext(btnMinus);

        btnPlus = new CB_Button("+");
        box.addLast(btnPlus);

    }

    private void createCategoryLine() {
        CB_Label lblCategory = new CB_Label(Translation.get("category"));
        lblCategory.setWidth(Fonts.measure(lblCategory.getText()).width);
        box.addNext(lblCategory, FIXED);
        edtCategory = new EditTextField(this, "*" + Translation.get("category"));
        box.addLast(edtCategory);
    }

    private void createImportLimitLine() {
        CB_Label lblImportLimit = new CB_Label(Translation.get("ImportLimit"));
        lblImportLimit.setWidth(Fonts.measure(lblImportLimit.getText()).width);
        box.addNext(lblImportLimit, FIXED);
        edtImportLimit = new EditTextField(this, "*" + Translation.get("ImportLimit"));
        edtImportLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addLast(edtImportLimit);
    }

    private void createCacheNameLine() {
        CB_Label lblCacheName = new CB_Label(Translation.get("Title"));
        lblCacheName.setWidth(Fonts.measure(lblCacheName.getText()).width);
        box.addNext(lblCacheName, FIXED);
        edtCacheName = new EditTextField(this, "*" + Translation.get("Title"));
        box.addLast(edtCacheName);
    }

    private void createOwnerLine() {
        CB_Label lblOwner = new CB_Label(Translation.get("Owner"));
        lblOwner.setWidth(Fonts.measure(lblOwner.getText()).width);
        box.addNext(lblOwner, FIXED);
        edtOwner = new EditTextField(this, "*" + Translation.get("Owner"));
        box.addLast(edtOwner);
    }

    private void createPublishedLine() {
        CB_Label lblPublished = new CB_Label(Translation.get("published"));
        box.addNext(lblPublished);
        btnBeforeAfterEqual = new CB_Button("<=");
        btnBeforeAfterEqual.setWidth(Fonts.measure(". <= .").width);
        box.addNext(btnBeforeAfterEqual, FIXED);
        edtDate = new EditTextField(this, "*" + Translation.get("published"));
        edtDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        edtDate.setWidth(Fonts.measure(". 2018-12-31 .").width);
        edtDate.setText(simpleDateFormat.format(new Date()));
        box.addLast(edtDate, FIXED);
    }

    private void createChkBoxLines() {

        checkBoxOnlyAvailable = new CB_CheckBox();
        box.addNext(checkBoxOnlyAvailable, FIXED);
        CB_Label lblOnlyAvailable = new CB_Label(Translation.get("SearchOnlyAvailable"));
        box.addLast(lblOnlyAvailable);

        checkBoxExcludeHides = new CB_CheckBox();
        box.addNext(checkBoxExcludeHides, FIXED);
        CB_Label lblExcludeHides = new CB_Label(Translation.get("SearchWithoutOwns"));
        box.addLast(lblExcludeHides);

        checkBoxExcludeFounds = new CB_CheckBox();
        box.addNext(checkBoxExcludeFounds, FIXED);
        CB_Label lblExcludeFounds = new CB_Label(Translation.get("SearchWithoutFounds"));
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

        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            importNow();
            return true;
        });

        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (importStarted) {
                // or add messagebox to confirm cancellation
                isCanceled.set(true);
            } else {
                finish();
            }
            return true;
        });

        btnPlus.setClickHandler((v, x, y, pointer, button) -> {
            incrementRadius(1);
            return true;
        });

        btnMinus.setClickHandler((v, x, y, pointer, button) -> {
            incrementRadius(-1);
            return true;
        });

        tglBtnGPS.setClickHandler((v, x, y, pointer, button) -> {
            actSearchPos = Locator.getInstance().getMyPosition();
            setToggleBtnState(0);
            return true;
        });

        tglBtnMap.setClickHandler((v, x, y, pointer, button) -> {
            actSearchPos = ((ShowMap) ShowMap.action).normalMapView.center;
            setToggleBtnState(1);
            return true;
        });

        tglBtnWeb.setClickHandler((v, x, y, pointer, button) -> {
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

        btnBeforeAfterEqual.setClickHandler((v, x, y, pointer, button) -> {
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

        if (((ShowMap) ShowMap.action).normalMapView.isVisible()) {
            actSearchPos = ((ShowMap) ShowMap.action).normalMapView.center;
            searchState = 1;
        } else {
            actSearchPos = Locator.getInstance().getMyPosition();
            searchState = 0;
        }
        setToggleBtnState(searchState);

        checkBoxExcludeFounds.setChecked(Settings.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Settings.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Settings.SearchWithoutOwns.getValue());
        txtRadius.setText(String.valueOf(Settings.lastSearchRadius.getValue()));

        edtCategory.setText("API-Import");
        if (GlobalCore.isSetSelectedCache()) {
            long id = GlobalCore.getSelectedCache().getGPXFilename_ID();
            Category c = CoreData.categories.getCategoryByGpxFilenameId(id);
            if (c != null)
                edtCategory.setText(c.gpxFileName);
        }
        edtCategory.setCursorPosition(0);

        Category category = CoreData.categories.getCategory(edtCategory.getText());
        edtDate.setText(simpleDateFormat.format(category.LastImported()));
        btnBeforeAfterEqual.setText("X");
        edtImportLimit.setText("" + Settings.ImportLimit.getValue());
    }

    @Override
    public void keyboardFocusChanged(EditTextField editTextField) {
        if (editTextField != null) {
            scrollToY(editTextField);
            editTextField.setCursorPosition(editTextField.getText().length());
        } else {
            if (GL.that.getFocusedEditTextField().equals(edtCategory)) {
                Category category = CoreData.categories.getCategory(edtCategory.getText());
                edtDate.setText(simpleDateFormat.format(category.LastImported()));
                btnBeforeAfterEqual.setText("X");
            }
        }
    }

    @Override
    public void onShow() {
        KeyboardFocusChangedEventList.add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.remove(this);
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

    private void setToggleBtnState(int value) {
        // 0=GPS, 1=Map, 2=Web, 3=manuel
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

    private void importNow() {
        btnOK.disable();
        importStarted = true;

        isCanceled.set(false);

        // disable UI
        dis = new ImportAnimation(box);
        dis.setBackground(getBackground());
        box.addChild(dis, false);

        Settings.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Settings.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Settings.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());
        Settings.getInstance().acceptChanges();

        Date tmpDate;
        try {
            tmpDate = simpleDateFormat.parse(edtDate.getText());
        } catch (Exception ex) {
            tmpDate = new Date();
        }
        final Date publishDate = tmpDate;

        Thread thread = new Thread(() -> {
            boolean threadCanceled = false;

            try {
                if (actSearchPos != null) {
                    Category category = CoreData.categories.getCategory(edtCategory.getText());
                    GpxFilename gpxFilename = category.addGpxFilename(category.gpxFileName); // category.GpxFilename == edtCategory.getText()
                    if (gpxFilename != null) {
                        Query q = new Query()
                                .resultWithFullFields()
                                //.resultWithImages(30)
                                ;
                        if (!btnBeforeAfterEqual.getText().equals("X")) {
                            q.publishedDate(publishDate, btnBeforeAfterEqual.getText());
                        }
                        if (Settings.numberOfLogs.getValue() > 0) {
                            q.resultWithLogs(Settings.numberOfLogs.getValue());
                        }
                        if (txtRadius.getText().trim().length() > 0) {
                            int radius;
                            try {
                                radius = Integer.parseInt(txtRadius.getText());
                                if (Settings.ImperialUnits.getValue())
                                    radius = UnitFormatter.getKilometer(radius);
                                Settings.lastSearchRadius.setValue(radius);
                                Settings.getInstance().acceptChanges();
                                q.searchInCircle(actSearchPos, radius * 1000);
                            } catch (NumberFormatException nex) {
                                q.searchInCircle(actSearchPos, Settings.lastSearchRadius.getValue() * 1000);
                            }
                        }
                        if (edtOwner.getText().trim().length() > 0)
                            q.searchForOwner(edtOwner.getText().trim());
                        if (edtCacheName.getText().trim().length() > 0)
                            q.searchForTitle(edtCacheName.getText().trim());

                        if (Settings.SearchWithoutFounds.getValue()) q.excludeFinds();
                        if (Settings.SearchWithoutOwns.getValue()) q.excludeOwn();
                        if (Settings.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

                        int importLimit;
                        try {
                            importLimit = Integer.parseInt(edtImportLimit.getText());
                        } catch (Exception ex) {
                            importLimit = Settings.ImportLimit.getDefaultValue();
                        }
                        q.setMaxToFetch(importLimit);
                        Settings.ImportLimit.setValue(importLimit);

                        dis.setAnimationType(AnimationType.Download);
                        ArrayList<GeoCacheRelated> geoCacheRelateds = searchGeoCaches(q);
                        dis.setAnimationType(AnimationType.Work);

                        if (geoCacheRelateds.size() > 0) {
                            try {
                                new CachesDAO().writeCachesAndLogsAndImagesIntoDB(geoCacheRelateds, gpxFilename);
                            } catch (InterruptedException e) {
                                Log.err(sClass, "WriteIntoDB.writeCachesAndLogsAndImagesIntoDB", e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                threadCanceled = true;
            }

            if (!threadCanceled) {
                CacheListChangedListeners.getInstance().fire();
                if (dis != null) {
                    removeChildDirect(dis);
                    dis.dispose();
                    dis = null;
                }
                btnOK.enable();
                finish();
            } else {
                // Notify Map
                ((ShowMap) ShowMap.action).normalMapView.setNewSettings(INITIAL_WP_LIST);
                if (dis != null) {
                    ImportGCPosition.this.removeChildDirect(dis);
                    dis.dispose();
                    dis = null;
                }
                btnOK.enable();
            }
            importStarted = false;
        });

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

    }

}

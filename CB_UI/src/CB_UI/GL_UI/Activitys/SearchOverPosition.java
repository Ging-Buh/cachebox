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

import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI.GL_UI.Views.MapView;
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
import android.text.InputType;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static CB_Core.Api.GroundspeakAPI.*;

public class SearchOverPosition extends ActivityBase implements KeyboardFocusChangedEvent {
    private static final String log = "SearchOverPosition";
    private Button bOK, bCancel, btnPlus, btnMinus;
    private Label lblHeader, lblRadius, lblRadiusEinheit, lblExcludeFounds, lblOnlyAvailable, lblExcludeHides;
    private Image gsLogo;
    private CoordinateButton coordBtn;
    private ChkBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private EditTextField Radius;
    private MultiToggleButton tglBtnGPS, tglBtnMap;
    private Coordinate actSearchPos;
    private volatile Thread thread;
    private ImportAnimation dis;
    private Box box;
    private ScrollBox scrollBox;
    private boolean importRuns = false;
    private int searcheState = 0; // 0=GPS, 1= Map, 2= Manuell
    private boolean isCanceld = false;
    ICancel icancel = () -> isCanceld;
    private Label lblPublished;
    private Button btnBeforeAfterEqual;
    private EditTextField edtDate;
    private Label lblImportLimit;
    private EditTextField edtImportLimit;
    private Label lblCacheName;
    private EditTextField edtCacheName;
    private Label lblOwner;
    private EditTextField edtOwner;
    private Label lblCategory;
    private EditTextField edtCategory;
    private SimpleDateFormat simpleDateFormat;

    public SearchOverPosition() {
        super(ActivityRec(), "searchOverPosActivity");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.removeChilds();

        // add to this
        // createHeaderLine();
        bOK = new Button(Translation.Get("import"));
        bCancel = new Button(Translation.Get("cancel"));
        this.initRow(BOTTOMUP);
        this.addNext(bOK);
        this.addLast(bCancel);
        scrollBox = new ScrollBox(0, this.getAvailableHeight());
        scrollBox.setBackground(this.getBackground());
        this.addLast(scrollBox);
        box = new Box(scrollBox.getInnerWidth(), 0); // height will be adjusted after containing all controls
        // add to box
        coordBtn = new CoordinateButton("");
        box.addLast(coordBtn);
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
        lblHeader = new Label(Translation.Get("importCachesOverPosition"));
        lblHeader.setFont(Fonts.getBig()).setHAlignment(Label.HAlignment.CENTER);
        float lineHeight = lblHeader.getHeight();
        gsLogo = new Image(0, 0, lineHeight, lineHeight, "", false);
        gsLogo.setDrawable(new SpriteDrawable(Sprites.getSprite(Sprites.IconName.dayGcLiveIcon.name())));
        this.addNext(lblHeader); // sets the width
        this.addLast(gsLogo, FIXED);
        lblHeader.setWrapType(WrapType.WRAPPED);
        lblHeader.updateHeight(this, true);
    }

    private void createRadiusLine() {
        String sRadius = Translation.Get("Radius");
        String sEinheit = Config.ImperialUnits.getValue() ? "mi" : "km";

        float wRadius = Fonts.Measure(sRadius).width;
        float wEinheit = Fonts.Measure(sEinheit).width;

        lblRadius = new Label(sRadius);
        lblRadius.setWidth(wRadius);
        box.addNext(lblRadius, FIXED);

        Radius = new EditTextField(this, "Radius");
        Radius.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addNext(Radius);

        lblRadiusEinheit = new Label(sEinheit);
        lblRadiusEinheit.setWidth(wEinheit);
        box.addNext(lblRadiusEinheit, FIXED);

        btnMinus = new Button("-");
        box.addNext(btnMinus);

        btnPlus = new Button("+");
        box.addLast(btnPlus);

    }

    private void createCategoryLine() {
        lblCategory = new Label(Translation.Get("category"));
        lblCategory.setWidth(Fonts.Measure(lblCategory.getText()).width);
        box.addNext(lblCategory, FIXED);
        edtCategory = new EditTextField(this, "edtCategory");
        box.addLast(edtCategory);
    }

    private void createImportLimitLine() {
        lblImportLimit = new Label(Translation.Get("ImportLimit"));
        lblImportLimit.setWidth(Fonts.Measure(lblImportLimit.getText()).width);
        box.addNext(lblImportLimit, FIXED);
        edtImportLimit = new EditTextField(this, "edtImportLimit");
        edtImportLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addLast(edtImportLimit);
    }

    private void createCacheNameLine() {
        lblCacheName = new Label(Translation.Get("Title"));
        lblCacheName.setWidth(Fonts.Measure(lblCacheName.getText()).width);
        box.addNext(lblCacheName, FIXED);
        edtCacheName = new EditTextField(this, "edtCacheName");
        box.addLast(edtCacheName);
    }

    private void createOwnerLine() {
        lblOwner = new Label(Translation.Get("Owner"));
        lblOwner.setWidth(Fonts.Measure(lblOwner.getText()).width);
        box.addNext(lblOwner, FIXED);
        edtOwner = new EditTextField(this, "edtOwner");
        box.addLast(edtOwner);
    }

    private void createPublishedLine() {
        lblPublished = new Label(Translation.Get("published"));
        box.addNext(lblPublished);
        btnBeforeAfterEqual = new Button("<=");
        btnBeforeAfterEqual.setWidth(Fonts.Measure(". <= .").width);
        box.addNext(btnBeforeAfterEqual, FIXED);
        edtDate = new EditTextField(this, "edtDate");
        edtDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        edtDate.setWidth(Fonts.Measure(". 2018-12-31 .").width);
        edtDate.setText(simpleDateFormat.format(new Date()));
        box.addLast(edtDate, FIXED);
    }

    private void createChkBoxLines() {

        checkBoxOnlyAvailable = new ChkBox("");
        box.addNext(checkBoxOnlyAvailable, FIXED);
        lblOnlyAvailable = new Label(Translation.Get("SearchOnlyAvailable"));
        box.addLast(lblOnlyAvailable);

        checkBoxExcludeHides = new ChkBox("");
        box.addNext(checkBoxExcludeHides, FIXED);
        lblExcludeHides = new Label(Translation.Get("SearchWithoutOwns"));
        box.addLast(lblExcludeHides);

        checkBoxExcludeFounds = new ChkBox("");
        box.addNext(checkBoxExcludeFounds, FIXED);
        lblExcludeFounds = new Label(Translation.Get("SearchWithoutFounds"));
        box.addLast(lblExcludeFounds);

    }

    private void createToggleButtonLine() {

        tglBtnGPS = new MultiToggleButton("");
        tglBtnMap = new MultiToggleButton("");

        tglBtnGPS.setFont(Fonts.getSmall());
        tglBtnMap.setFont(Fonts.getSmall());

        tglBtnGPS.initialOn_Off_ToggleStates(Translation.Get("FromGps"), Translation.Get("FromGps"));
        tglBtnMap.initialOn_Off_ToggleStates(Translation.Get("FromMap"), Translation.Get("FromMap"));

        box.addNext(tglBtnGPS);
        box.addLast(tglBtnMap);

        if (MapView.that == null)
            tglBtnMap.disable();

    }

    private void initClickHandlersAndContent() {

        bOK.setOnClickListener((v, x, y, pointer, button) -> {
            ImportNow();
            return true;
        });

        bCancel.setOnClickListener((v, x, y, pointer, button) -> {
            if (importRuns) {
                isCanceld = true;
            } else {
                finish();
            }
            return true;
        });

        btnPlus.setOnClickListener((v, x, y, pointer, button) -> {
            incrementRadius(1);
            return true;
        });

        btnMinus.setOnClickListener((v, x, y, pointer, button) -> {
            incrementRadius(-1);
            return true;
        });

        tglBtnGPS.setOnClickListener((v, x, y, pointer, button) -> {
            actSearchPos = Locator.getCoordinate();
            setToggleBtnState(0);
            return true;
        });

        tglBtnMap.setOnClickListener((v, x, y, pointer, button) -> {
            if (MapView.that == null) {
                actSearchPos = new CoordinateGPS(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());
            } else {
                actSearchPos = MapView.that.center;
            }

            setToggleBtnState(1);
            return true;
        });

        coordBtn.setCoordinateChangedListener(coord -> {
            if (coord != null) {
                actSearchPos = coord;
                setToggleBtnState(2);
            }
            SearchOverPosition.this.show();
        });

        btnBeforeAfterEqual.setOnClickListener((v, x, y, pointer, button) -> {
            if (btnBeforeAfterEqual.getText().equals("<="))
                btnBeforeAfterEqual.setText("=");
            else if (btnBeforeAfterEqual.getText().equals("="))
                btnBeforeAfterEqual.setText(">=");
            else btnBeforeAfterEqual.setText("<=");
            return true;
        });

        if (MapView.that != null && MapView.that.isVisible()) {
            actSearchPos = MapView.that.center;
            searcheState = 1;
        } else {
            actSearchPos = Locator.getCoordinate();
            searcheState = 0;
        }
        setToggleBtnState();

        checkBoxExcludeFounds.setChecked(Config.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Config.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Config.SearchWithoutOwns.getValue());
        Radius.setText(String.valueOf(Config.lastSearchRadius.getValue()));

        if (GlobalCore.isSetSelectedCache()) {
            Category c = CoreSettingsForward.Categories.getCategoryByGpxFilenameId(GlobalCore.getSelectedCache().getGPXFilename_ID());
            edtCategory.setText(c.GpxFilename);
        } else {
            edtCategory.setText("API-Import");
        }
        edtCategory.setCursorPosition(0);

        Category category = CoreSettingsForward.Categories.getCategory(edtCategory.getText());
        edtDate.setText(simpleDateFormat.format(category.LastImported()));
        if (category.size() == 0)
            btnBeforeAfterEqual.setText("<=");
        else
            btnBeforeAfterEqual.setText(">=");

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
                if (category.size() == 0)
                    btnBeforeAfterEqual.setText("<=");
                else
                    btnBeforeAfterEqual.setText(">=");
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

    private void incrementRadius(int value) {
        try {
            int ist = Integer.parseInt(Radius.getText());
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
            radius = Integer.parseInt(Radius.getText());
        } catch (NumberFormatException ignore) {
        }

        if (radius != 0)
            Config.lastSearchRadius.setValue(radius);

        Config.AcceptChanges();

        bOK.disable();

        // disable UI
        dis = new ImportAnimation(box);
        dis.setBackground(getBackground());

        this.addChild(dis, false);

        Date tmpDate;
        try {
            tmpDate = simpleDateFormat.parse(edtDate.getText());
        } catch (Exception ex) {
            tmpDate = new Date();
        }
        final Date publishDate = tmpDate;

        importRuns = true;
        thread = new Thread(() -> {
            boolean threadCanceled = false;

            try {
                if (actSearchPos != null) {
                    Category category = CoreSettingsForward.Categories.getCategory(edtCategory.getText());
                    GpxFilename gpxFilename = category.addGpxFilename(category.GpxFilename); // category.GpxFilename == edtCategory.getText()
                    if (gpxFilename != null) {
                        Query q = new Query()
                                .resultWithFullFields()
                                .resultWithLogs(30)
                                //.resultWithImages(30)
                                .publishedDate(publishDate, btnBeforeAfterEqual.getText());
                        if (Radius.getText().trim().length() > 0)
                            q.searchInCircle(actSearchPos, Config.lastSearchRadius.getValue() * 1000);
                        if (edtOwner.getText().trim().length() > 0) q.searchForOwner(edtOwner.getText().trim());
                        if (edtCacheName.getText().trim().length() > 0) q.searchForTitle(edtCacheName.getText().trim());

                        if (Config.SearchWithoutFounds.getValue()) q.excludeFinds();
                        if (Config.SearchWithoutOwns.getValue()) q.excludeOwn();
                        if (Config.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();


                        int importLimit;
                        try {
                            importLimit = Integer.parseInt(Radius.getText());
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
        });

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

    }

    @Override
    public void dispose() {
        dispose(bOK);
        dispose(bCancel);
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
        dispose(coordBtn);
        dispose(checkBoxExcludeFounds);
        dispose(checkBoxOnlyAvailable);
        dispose(checkBoxExcludeHides);
        dispose(Radius);
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

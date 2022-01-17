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
import static de.droidcachebox.core.GroundspeakAPI.fetchGeoCache;
import static de.droidcachebox.core.GroundspeakAPI.fetchGeoCaches;
import static de.droidcachebox.core.GroundspeakAPI.searchGeoCaches;
import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextField.TextFieldListener;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.ImportAnimation;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.MultiToggleButton;
import de.droidcachebox.gdx.controls.popups.SearchDialog;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

public class SearchOverNameOwnerGcCode extends ActivityBase {
    private static final String sClass = "SearchOverNameOwnerGcCode";
    private final float lineHeight;
    private CB_Button btnImport, btnCancel;
    private CB_Label lblTitle, lblExcludeFounds, lblOnlyAvailable, lblExcludeHides;
    private Image gsLogo;
    private CB_CheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private EditTextField editTextField;
    private volatile Thread thread;
    private ImportAnimation importAnimation;
    private Box box;
    private boolean importRuns = false;
    private SearchDialog.SearchMode actSearchType = null;
    private MultiToggleButton mTglBtnTitle, mTglBtnGc, mTglBtnOwner;

    public SearchOverNameOwnerGcCode() {
        super("searchOverPosActivity");

        lineHeight = UiSizes.getInstance().getButtonHeight();

        createOkCancelBtn();
        createBox();
        createTitleLine();
        createChkBoxLines();
        createtoggleButtonLine();
        initialContent();
    }

    public static void showInstance() {
        new SearchOverNameOwnerGcCode().show();
    }

    @Override
    public void onShow() {
        textBox_TextChanged();
    }

    private void createOkCancelBtn() {
        btnImport = new CB_Button(leftBorder, leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");
        btnCancel = new CB_Button(btnImport.getMaxX(), leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        // Translations
        btnImport.setText(Translation.get("import"));
        btnCancel.setText(Translation.get("cancel"));

        addChild(btnImport);
        btnImport.setClickHandler((v, x, y, pointer, button) -> {
            GL.that.postAsync(this::importNow);
            return true;
        });

        addChild(btnCancel);
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (importRuns) {

                cancelImport();

            } else {
                finish();
            }
            return true;
        });

    }

    private void cancelImport() {
        if (thread != null)
            thread.interrupt();
        importRuns = false;
        removeChildDirect(importAnimation);
        importAnimation.dispose();
        importAnimation = null;
    }

    private void createBox() {
        box = new Box(this, "ScrollBox");
        addChild(box);
        box.setHeight(getHeight() - lineHeight - btnImport.getMaxY() - margin - margin);
        box.setY(btnImport.getMaxY() + margin);
        box.setBackground(getBackground());
    }

    private void createTitleLine() {

        float sideLength = UiSizes.getInstance().getButtonHeight() * 0.75f;
        CB_RectF rec = new CB_RectF(innerWidth - margin - sideLength, getHeight() - getTopHeight() - sideLength - margin, sideLength);
        gsLogo = new Image(rec, "", false);
        gsLogo.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.dayGcLiveIcon.name())));
        addChild(gsLogo);

        lblTitle = new CB_Label(name + " lblTitle", leftBorder + margin, getHeight() - getTopHeight() - sideLength - margin, innerWidth - (margin * 4) - gsLogo.getWidth(), sideLength);
        lblTitle.setFont(Fonts.getBig());
        lblTitle.setWrappedText(Translation.get("API_IMPORT_NAME_OWNER_CODE"));
        addChild(lblTitle);

    }

    private void createChkBoxLines() {
        checkBoxOnlyAvailable = new CB_CheckBox();
        checkBoxOnlyAvailable.setPos(margin, box.getHeight() - margin - checkBoxOnlyAvailable.getHeight());
        box.addChild(checkBoxOnlyAvailable);

        checkBoxExcludeHides = new CB_CheckBox();
        checkBoxExcludeHides.setPos(margin, checkBoxOnlyAvailable.getY() - margin - checkBoxExcludeHides.getHeight());
        box.addChild(checkBoxExcludeHides);

        checkBoxExcludeFounds = new CB_CheckBox();
        checkBoxExcludeFounds.setPos(margin, checkBoxExcludeHides.getY() - margin - checkBoxExcludeFounds.getHeight());
        box.addChild(checkBoxExcludeFounds);

        lblOnlyAvailable = new CB_Label(name + " lblOnlyAvailable", checkBoxOnlyAvailable, Translation.get("SearchOnlyAvailable"));
        lblOnlyAvailable.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblOnlyAvailable.setWidth(getWidth() - margin - checkBoxOnlyAvailable.getMaxX() - margin);
        box.addChild(lblOnlyAvailable);

        lblExcludeHides = new CB_Label(name + " lblExcludeHides", checkBoxExcludeHides, Translation.get("SearchWithoutOwns"));
        lblExcludeHides.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblExcludeHides.setWidth(getWidth() - margin - checkBoxExcludeHides.getMaxX() - margin);
        box.addChild(lblExcludeHides);

        lblExcludeFounds = new CB_Label(name + " lblExcludeFounds", checkBoxExcludeFounds, Translation.get("SearchWithoutFounds"));
        lblExcludeFounds.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblExcludeFounds.setWidth(getWidth() - margin - checkBoxExcludeFounds.getMaxX() - margin);
        box.addChild(lblExcludeFounds);

    }

    private void createtoggleButtonLine() {
        CB_RectF rec = new CB_RectF(0, 0, box.getWidth() - (margin * 2), UiSizes.getInstance().getButtonHeight());

        Box line = new Box(rec, "ToggLeButtonLine");

        line.setHeight(UiSizes.getInstance().getButtonHeight() * 2 + margin);

        mTglBtnTitle = new MultiToggleButton(rec, "mTglBtnTitle");
        mTglBtnGc = new MultiToggleButton(rec, "mTglBtnGc");
        mTglBtnOwner = new MultiToggleButton(rec, "mTglBtnOwner");

        mTglBtnTitle.initialOn_Off_ToggleStates(Translation.get("Title"), Translation.get("Title"));
        mTglBtnGc.initialOn_Off_ToggleStates(Translation.get("GCCode"), Translation.get("GCCode"));
        mTglBtnOwner.initialOn_Off_ToggleStates(Translation.get("Owner"), Translation.get("Owner"));

        line.initRow(true);
        line.addNext(mTglBtnTitle);
        line.addNext(mTglBtnGc);
        line.addLast(mTglBtnOwner);

        line.setY(checkBoxExcludeFounds.getY() - margin - line.getHeight());
        line.setX(margin);

        editTextField = new EditTextField(rec, this, "mEingabe", WrapType.SINGLELINE);

        editTextField.setTextFieldListener(new TextFieldListener() {

            @Override
            public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {

            }

            @Override
            public void keyTyped(EditTextField textField, char key) {
                textBox_TextChanged();
            }
        });
        editTextField.setText("");

        line.addLast(editTextField);

        box.addChild(line);
    }

    private void initialContent() {
        textBox_TextChanged();
        switchSearcheMode(0);
        checkBoxExcludeFounds.setChecked(Settings.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Settings.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Settings.SearchWithoutOwns.getValue());

        mTglBtnTitle.setClickHandler((v, x, y, pointer, button) -> {
            switchSearcheMode(0);
            return true;
        });

        mTglBtnGc.setClickHandler((v, x, y, pointer, button) -> {
            switchSearcheMode(1);
            return true;
        });

        mTglBtnOwner.setClickHandler((v, x, y, pointer, button) -> {
            switchSearcheMode(2);
            return true;
        });
    }

    private void importNow() {

        Settings.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Settings.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Settings.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

        Settings.getInstance().acceptChanges();

        btnImport.disable();

        // disable UI
        importAnimation = new ImportAnimation(box);
        importAnimation.setBackground(getBackground());

        addChild(importAnimation, false);

        importRuns = true;
        thread = new Thread(() -> {
            boolean threadCanceld = false;
            try {
                Thread.sleep(200);
                if (actSearchType != null) {

                    // alle per API importierten Caches landen in der Category und GpxFilename API-Import
                    // Category suchen, die dazu gehört
                    Category category = CoreData.categories.getCategory("API-Import");
                    if (category != null) // should not happen!!!
                    {
                        GpxFilename gpxFilename = category.addGpxFilename("API-Import");
                        if (gpxFilename != null) {

                            String searchPattern = editTextField.getText().trim();

                            Coordinate searchCoord;
                            if (ShowMap.getInstance().normalMapView.isVisible()) {
                                searchCoord = ShowMap.getInstance().normalMapView.center;
                            } else {
                                searchCoord = Locator.getInstance().getMyPosition();
                            }
                            if (searchCoord == null) {
                                return;
                            }

                            Query q = new Query()
                                    .setMaxToFetch(50)
                                    .resultWithFullFields()
                                    //.resultWithImages(30)
                                    ;
                            if (Settings.numberOfLogs.getValue() > 0) {
                                q.resultWithLogs(Settings.numberOfLogs.getValue());
                            }
                            if (Settings.SearchWithoutFounds.getValue()) q.excludeFinds();
                            if (Settings.SearchWithoutOwns.getValue()) q.excludeOwn();
                            if (Settings.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

                            ArrayList<GeoCacheRelated> geoCacheRelateds;
                            importAnimation.setAnimationType(AnimationType.Download);
                            switch (actSearchType) {
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
                                try {
                                    importAnimation.setAnimationType(AnimationType.Work);
                                    CacheDAO.getInstance().writeCachesAndLogsAndImagesIntoDB(geoCacheRelateds, gpxFilename);
                                } catch (InterruptedException e) {
                                    Log.err(sClass, "WriteIntoDB.writeCachesAndLogsAndImagesIntoDB", e);
                                }
                            }

                        }
                    }
                }
            } catch (InterruptedException e) {
                threadCanceld = true;
            }

            // Delete all LongDescription from cacheList! LongDescription is Loading by showing DescriptionView direct from DB
            // for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++)
            // {
            // Cache cache = Database.Data.cacheList.get(i);
            // cache.longDescription = "";
            // }

            if (!threadCanceld) {
                CacheListChangedListeners.getInstance().cacheListChanged();
                cancelImport();
                finish();
            } else {

                // Notify Map
                ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);

                btnImport.enable();
            }
            importRuns = false;
        });

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();

    }

    private void switchSearcheMode(int state) {

        if (state == 0) {
            mTglBtnTitle.setState(1);
            mTglBtnGc.setState(0);
            mTglBtnOwner.setState(0);
            actSearchType = SearchDialog.SearchMode.Title;
        }
        if (state == 1) {
            mTglBtnTitle.setState(0);
            mTglBtnGc.setState(1);
            mTglBtnOwner.setState(0);
            actSearchType = SearchDialog.SearchMode.GcCode;
        }
        if (state == 2) {
            mTglBtnTitle.setState(0);
            mTglBtnGc.setState(0);
            mTglBtnOwner.setState(1);
            actSearchType = SearchDialog.SearchMode.Owner;
        }

    }

    private void textBox_TextChanged() {
        boolean isText = editTextField.getText().length() != 0;
        btnImport.setEnable(isText);
    }

    @Override
    public void dispose() {
        if (btnImport != null)
            btnImport.dispose();
        btnImport = null;
        if (btnCancel != null)
            btnCancel.dispose();
        btnCancel = null;
        if (lblTitle != null)
            lblTitle.dispose();
        lblTitle = null;
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
        if (checkBoxExcludeFounds != null)
            checkBoxExcludeFounds.dispose();
        checkBoxExcludeFounds = null;
        if (checkBoxOnlyAvailable != null)
            checkBoxOnlyAvailable.dispose();
        checkBoxOnlyAvailable = null;
        if (checkBoxExcludeHides != null)
            checkBoxExcludeHides.dispose();
        checkBoxExcludeHides = null;
        if (editTextField != null)
            editTextField.dispose();
        editTextField = null;
        if (importAnimation != null)
            importAnimation.dispose();
        importAnimation = null;
        if (box != null)
            box.dispose();
        box = null;
        if (mTglBtnTitle != null)
            mTglBtnTitle.dispose();
        mTglBtnTitle = null;
        if (mTglBtnGc != null)
            mTglBtnGc.dispose();
        mTglBtnGc = null;
        if (mTglBtnOwner != null)
            mTglBtnOwner.dispose();
        mTglBtnOwner = null;

        actSearchType = null;
        super.dispose();
    }

}

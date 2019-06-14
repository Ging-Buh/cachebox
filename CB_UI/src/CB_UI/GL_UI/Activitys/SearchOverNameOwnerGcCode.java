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
import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_UI.WriteIntoDB;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Log.Log;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

import static CB_Core.Api.GroundspeakAPI.*;
import static CB_Locator.Map.MapViewBase.INITIAL_WP_LIST;

public class SearchOverNameOwnerGcCode extends ActivityBase {
    private static final String log = "SearchOverNameOwnerGcCode";
    private final float lineHeight;
    private Button bImport, bCancel;
    private Label lblTitle, lblExcludeFounds, lblOnlyAvailable, lblExcludeHides;
    private Image gsLogo;
    private ChkBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    /**
     * Such Eingabe Feld
     */
    private EditTextField mEingabe;
    private volatile Thread thread;
    private ImportAnimation dis;
    private Box box;
    private boolean importRuns = false;
    private SearchDialog.SearchMode actSearchType = null;

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

    public SearchOverNameOwnerGcCode() {
        super(ActivityRec(), "searchOverPosActivity");

        lineHeight = UI_Size_Base.that.getButtonHeight();

        createOkCancelBtn();
        createBox();
        createTitleLine();
        createChkBoxLines();
        createtoggleButtonLine();
        initialContent();
    }

    public static void ShowInstanz() {
        new SearchOverNameOwnerGcCode().show();
    }

    @Override
    public void onShow() {
        textBox_TextChanged();
    }

    private void createOkCancelBtn() {
        bImport = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
        bCancel = new Button(bImport.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        // Translations
        bImport.setText(Translation.get("import"));
        bCancel.setText(Translation.get("cancel"));

        this.addChild(bImport);
        bImport.setOnClickListener((v, x, y, pointer, button) -> {
            ImportNow();
            return true;
        });

        this.addChild(bCancel);
        bCancel.setOnClickListener((v, x, y, pointer, button) -> {
            if (importRuns) {

                cancelImport();

            } else {
                finish();
            }
            return true;
        });

    }

    private void cancelImport() {

        // breche den Import Thread ab
        if (thread != null)
            thread.interrupt();

        importRuns = false;
        this.removeChildsDirekt(dis);
        dis.dispose();
        dis = null;

    }

    private void createBox() {
        box = new Box(ActivityRec(), "ScrollBox");
        this.addChild(box);
        box.setHeight(this.getHeight() - lineHeight - bImport.getMaxY() - margin - margin);
        box.setY(bImport.getMaxY() + margin);
        box.setBackground(this.getBackground());
    }

    private void createTitleLine() {

        float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

        gsLogo = new Image(innerWidth - margin - lineHeight, this.getHeight() - this.getTopHeight() - lineHeight - margin, lineHeight, lineHeight, "", false);
        gsLogo.setDrawable(new SpriteDrawable(Sprites.getSprite(IconName.dayGcLiveIcon.name())));
        this.addChild(gsLogo);

        lblTitle = new Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - (margin * 4) - gsLogo.getWidth(), lineHeight);
        lblTitle.setFont(Fonts.getBig());
        lblTitle.setWrappedText(Translation.get("API_IMPORT_NAME_OWNER_CODE"));
        this.addChild(lblTitle);

    }

    private void createChkBoxLines() {
        checkBoxOnlyAvailable = new ChkBox("");
        checkBoxOnlyAvailable.setPos(margin, box.getHeight() - margin - checkBoxOnlyAvailable.getHeight());
        box.addChild(checkBoxOnlyAvailable);

        checkBoxExcludeHides = new ChkBox("");
        checkBoxExcludeHides.setPos(margin, checkBoxOnlyAvailable.getY() - margin - checkBoxExcludeHides.getHeight());
        box.addChild(checkBoxExcludeHides);

        checkBoxExcludeFounds = new ChkBox("");
        checkBoxExcludeFounds.setPos(margin, checkBoxExcludeHides.getY() - margin - checkBoxExcludeFounds.getHeight());
        box.addChild(checkBoxExcludeFounds);

        lblOnlyAvailable = new Label(this.name + " lblOnlyAvailable", checkBoxOnlyAvailable, Translation.get("SearchOnlyAvailable"));
        lblOnlyAvailable.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblOnlyAvailable.setWidth(this.getWidth() - margin - checkBoxOnlyAvailable.getMaxX() - margin);
        box.addChild(lblOnlyAvailable);

        lblExcludeHides = new Label(this.name + " lblExcludeHides", checkBoxExcludeHides, Translation.get("SearchWithoutOwns"));
        lblExcludeHides.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblExcludeHides.setWidth(this.getWidth() - margin - checkBoxExcludeHides.getMaxX() - margin);
        box.addChild(lblExcludeHides);

        lblExcludeFounds = new Label(this.name + " lblExcludeFounds", checkBoxExcludeFounds, Translation.get("SearchWithoutFounds"));
        lblExcludeFounds.setX(checkBoxOnlyAvailable.getMaxX() + margin);
        lblExcludeFounds.setWidth(this.getWidth() - margin - checkBoxExcludeFounds.getMaxX() - margin);
        box.addChild(lblExcludeFounds);

    }

    private void createtoggleButtonLine() {
        CB_RectF rec = new CB_RectF(0, 0, box.getWidth() - (margin * 2), UI_Size_Base.that.getButtonHeight());

        Box line = new Box(rec, "ToggLeButtonLine");

        line.setHeight(UI_Size_Base.that.getButtonHeight() * 2 + margin);

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

        line.addLast(mEingabe);

        box.addChild(line);
    }

    private void initialContent() {
        textBox_TextChanged();
        switchSearcheMode(0);
        checkBoxExcludeFounds.setChecked(Config.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Config.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Config.SearchWithoutOwns.getValue());

        mTglBtnTitle.setOnClickListener((v, x, y, pointer, button) -> {
            switchSearcheMode(0);
            return true;
        });

        mTglBtnGc.setOnClickListener((v, x, y, pointer, button) -> {
            switchSearcheMode(1);
            return true;
        });

        mTglBtnOwner.setOnClickListener((v, x, y, pointer, button) -> {
            switchSearcheMode(2);
            return true;
        });
    }

    private void ImportNow() {

        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

        Config.AcceptChanges();

        bImport.disable();

        // disable UI
        dis = new ImportAnimation(box);
        dis.setBackground(getBackground());

        this.addChild(dis, false);

        importRuns = true;
        thread = new Thread(() -> {
            boolean threadCanceld = false;

            try {
                Thread.sleep(200);

                if (actSearchType != null) {

                    // alle per API importierten Caches landen in der Category und
                    // GpxFilename
                    // API-Import
                    // Category suchen, die dazu gehört
                    Category category = CoreSettingsForward.Categories.getCategory("API-Import");
                    if (category != null) // should not happen!!!
                    {
                        GpxFilename gpxFilename = category.addGpxFilename("API-Import");
                        if (gpxFilename != null) {

                            String searchPattern = mEingabe.getText().trim();

                            Coordinate searchCoord;
                            if (CB_Action_ShowMap.getInstance().normalMapView.isVisible()) {
                                searchCoord = CB_Action_ShowMap.getInstance().normalMapView.center;
                            } else {
                                searchCoord = Locator.getCoordinate();
                            }
                            if (searchCoord == null) {
                                return;
                            }

                            Query q = new Query()
                                    .setMaxToFetch(50)
                                    .resultWithFullFields()
                                    //.resultWithImages(30)
                                    ;
                            if (CB_Core_Settings.numberOfLogs.getValue() > 0) {
                                q.resultWithLogs(CB_Core_Settings.numberOfLogs.getValue());
                            }
                            if (Config.SearchWithoutFounds.getValue()) q.excludeFinds();
                            if (Config.SearchWithoutOwns.getValue()) q.excludeOwn();
                            if (Config.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

                            ArrayList<GeoCacheRelated> geoCacheRelateds;
                            dis.setAnimationType(AnimationType.Download);
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

                            dis.setAnimationType(AnimationType.Work);
                            if (geoCacheRelateds.size() > 0) {
                                try {
                                    dis.setAnimationType(AnimationType.Work);
                                    WriteIntoDB.CachesAndLogsAndImagesIntoDB(geoCacheRelateds, gpxFilename);
                                } catch (InterruptedException e) {
                                    Log.err(log, "WriteIntoDB.CachesAndLogsAndImagesIntoDB", e);
                                }
                            }

                        }
                    }
                }
            } catch (InterruptedException e) {
                // Thread abgebrochen!
                threadCanceld = true;
            }

            // Delete all LongDescription from cacheList! LongDescription is Loading by showing DescriptionView direct from DB
            // for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++)
            // {
            // Cache cache = Database.Data.cacheList.get(i);
            // cache.longDescription = "";
            // }

            if (!threadCanceld) {
                CacheListChangedEventList.Call();
                cancelImport();
                finish();
            } else {

                // Notify Map
                CB_Action_ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);

                bImport.enable();
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
        boolean isText = mEingabe.getText().length() != 0;
        bImport.setEnable(isText);
    }

    @Override
    public void dispose() {
        if (bImport != null)
            bImport.dispose();
        bImport = null;
        if (bCancel != null)
            bCancel.dispose();
        bCancel = null;
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
        if (mEingabe != null)
            mEingabe.dispose();
        mEingabe = null;
        if (dis != null)
            dis.dispose();
        dis = null;
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

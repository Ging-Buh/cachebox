package de.droidcachebox.gdx.activities;

import static de.droidcachebox.settings.Config_Core.br;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.Date;
import java.util.Locale;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.Platform;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheSize;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CoordinateButton;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextField.TextFieldStyle;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.Spinner;
import de.droidcachebox.gdx.controls.Spinner.ISpinnerSelectionChanged;
import de.droidcachebox.gdx.controls.SpinnerAdapter;
import de.droidcachebox.menu.menuBtn1.executes.GeoCaches;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.translation.Translation;

public class EditCache extends ActivityBase implements KeyboardFocusChangedEventList.KeyboardFocusChangedEvent {
    // Allgemein
    private final GeoCacheType[] geoCacheTypNumbers = GeoCacheType.caches();
    private final GeoCacheSize[] geoCacheSizeNumbers = new GeoCacheSize[]{GeoCacheSize.other, // 0
            GeoCacheSize.micro, // 1
            GeoCacheSize.small, // 2
            GeoCacheSize.regular, // 3
            GeoCacheSize.large // 4
    };
    private Cache cache;
    private Cache newValues;
    private final ScrollBox mainPanel;
    private final CB_Button btnOK;
    private final CB_Button btnCancel;
    private final Spinner cacheTyp;
    private final Spinner cacheSize;
    private final Spinner cacheDifficulty;
    private final Spinner cacheTerrain;
    private final CoordinateButton cacheCoords;
    private final EditTextField cacheCode; // SingleLine
    private final EditTextField cacheTitle; // WRAPPED
    private final EditTextField cacheOwner; // SingleLine
    private final EditTextField cacheCountry; // SingleLine
    private final EditTextField cacheState; // SingleLine
    private final CB_Button noHtml;
    private final CB_Button toTop;
    private EditTextField cacheDescription; // WRAPPED

    // ctor
    public EditCache() {
        super("EditCache");
        // das übliche
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        initRow(BOTTOMUp);
        addNext(btnOK);
        addLast(btnCancel);
        mainPanel = new ScrollBox(0, getAvailableHeight());
        mainPanel.setBackground(getBackground());
        addLast(mainPanel);
        TextFieldStyle s;
        cacheTitle = new EditTextField(this, "cacheTitle").setWrapType(WrapType.WRAPPED);
        s = cacheTitle.getStyle();
        s.font = Fonts.getCompass();
        cacheTitle.setStyle(s);
        cacheTitle.setWidth(mainPanel.getWidth());
        cacheCode = new EditTextField(this, "cacheCode");
        s = cacheCode.getStyle();
        s.font = Fonts.getCompass();
        cacheCode.setStyle(s);
        cacheDifficulty = new Spinner("EditCacheDifficulty", cacheDifficultyList(), cacheDifficultySelection());
        cacheTyp = new Spinner("EditCacheType", cacheTypList(), cacheTypSelection());
        cacheTerrain = new Spinner("EditCacheTerrain", cacheTerrainList(), index -> {
            activityBase.show();
            newValues.setTerrain((index + 2.0f) / 2.0f);
        });
        cacheSize = new Spinner("EditCacheSize", cacheSizeList(), index -> {
            activityBase.show();
            newValues.geoCacheSize = geoCacheSizeNumbers[index];
        });
        cacheCoords = new CoordinateButton("cacheCoords");
        cacheOwner = new EditTextField(this, "cacheOwner");
        cacheState = new EditTextField(this, "cacheState");
        cacheCountry = new EditTextField(this, "cacheCountry");

        noHtml = new CB_Button(Translation.get("remove") + " < />");
        noHtml.setClickHandler((view, x, y, pointer, button) -> {
            cacheDescription.setText(Platform.removeHtmlEntyties(cacheDescription.getText()));
            layout();
            return true;
        });
        toTop = new CB_Button(" ^ ");
        toTop.setClickHandler((view, x, y, pointer, button) -> {
            cacheDescription.setCursorPosition(0);
            GL.that.setFocusedEditTextField(null);
            mainPanel.scrollTo(0);
            return true;
        });
        cacheDescription = new EditTextField(this, "cacheDescription").setWrapType(WrapType.WRAPPED);
        cacheDescription.setWidth(mainPanel.getWidth());
        // cacheDescription.setSize(mainPanel.getWidth(),mainPanel.getHeight() / 2);

        btnOKClickHandler();
        btnCancelClickHandler();
        setCacheCoordsChangeListener();

    }

    private void layout() {
        Box mainContent = new Box(mainPanel.getInnerWidth(), 0); // height will be adjusted after containing all controls
        mainContent.addLast(cacheCode);
        mainContent.addNext(cacheTyp);
        mainContent.addLast(cacheDifficulty, 0.3f);
        mainContent.addNext(cacheSize);
        mainContent.addLast(cacheTerrain, 0.3f);
        cacheTitle.setHeight(cacheTitle.getTextHeight());
        mainContent.addLast(cacheTitle);
        cacheTitle.setCursorPosition(0);
        mainContent.addLast(cacheCoords);
        mainContent.addLast(cacheOwner);
        mainContent.addLast(cacheCountry);
        mainContent.addLast(cacheState);
        cacheDescription.setHeight(Math.min(mainPanel.getHeight() / 2, cacheDescription.getTextHeight()));
        mainContent.addLast(cacheDescription);
        mainContent.addNext(toTop);
        mainContent.addLast(noHtml);
        mainContent.adjustHeight();

        mainPanel.removeChildrenDirect();
        mainPanel.setVirtualHeight(mainContent.getHeight());
        mainPanel.addChild(mainContent);

    }

    @Override
    public void keyboardFocusChanged(EditTextField editTextField) {
        if (editTextField != null) {
            scrollToY(editTextField);
            editTextField.setCursorPosition(editTextField.getText().length());
        }
    }

    public void update(Cache updateCache) {
        newValues = new Cache(true);
        newValues.copyFrom(updateCache);
        newValues.setShortDescription("");
        newValues.setLongDescription(new CacheDAO().getDescription(updateCache));
        updateCache.setLongDescription(newValues.getLongDescription());
        cache = updateCache;
        doShow();
    }

    public void create() {
        newValues = new Cache(true);
        newValues.setGeoCacheType(GeoCacheType.Traditional);
        newValues.geoCacheSize = GeoCacheSize.micro;
        newValues.setDifficulty(1);
        newValues.setTerrain(1);
        newValues.setCoordinate(ShowMap.getInstance().normalMapView.center);
        if (!newValues.getCoordinate().isValid())
            newValues.setCoordinate(GlobalCore.getSelectedCoordinate());
        // GC - Code bestimmen für freies CWxxxx = CustomWaypint
        String prefix = "CW";
        int count = 0;
        do {
            count++;
            newValues.setGeoCacheCode(prefix + String.format(Locale.US, "%04d", count));
        } while (CBDB.getInstance().cacheList.getCacheByIdFromCacheList(Cache.generateCacheId(newValues.getGeoCacheCode())) != null);
        newValues.setGeoCacheName(newValues.getGeoCacheCode());
        newValues.setOwner("Unbekannt");
        newValues.setState("");
        newValues.setCountry("");
        newValues.setDateHidden(new Date());
        newValues.setArchived(false);
        newValues.setAvailable(true);
        newValues.setFound(false);
        newValues.numTravelbugs = 0;
        newValues.setShortDescription("");
        newValues.setLongDescription("");
        cache = newValues;
        doShow();
    }

    private void doShow() {
        cacheCode.setText(cache.getGeoCacheCode());
        cacheTyp.setSelection(0);
        for (int i = 0; i < geoCacheTypNumbers.length; i++) {
            if (geoCacheTypNumbers[i] == cache.getGeoCacheType()) {
                cacheTyp.setSelection(i);
            }
        }
        cacheSize.setSelection(0);
        for (int i = 0; i < geoCacheSizeNumbers.length; i++) {
            if (geoCacheSizeNumbers[i] == cache.geoCacheSize) {
                cacheSize.setSelection(i);
            }
        }
        cacheDifficulty.setSelection((int) (cache.getDifficulty() * 2 - 2));
        cacheTerrain.setSelection((int) (cache.getTerrain() * 2 - 2));
        cacheCoords.setCoordinate(cache.getCoordinate());
        cacheTitle.setText(cache.getGeoCacheName());
        cacheOwner.setText(cache.getOwner());
        cacheState.setText(cache.getState());
        cacheCountry.setText(cache.getCountry());
        if (cache.getLongDescription().equals(br))
            cache.setLongDescription("");
        cacheDescription.setText(cache.getLongDescription());
        cacheDescription.setCursorPosition(0);
        layout();
        show();
    }

    private void btnOKClickHandler() {
        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            boolean update = false;
            CacheDAO cacheDAO = new CacheDAO();
            String gcc = cacheCode.getText().toUpperCase(); // nur wenn kein Label
            cache.generatedId = Cache.generateCacheId(gcc);

            Cache cl = CBDB.getInstance().cacheList.getCacheByIdFromCacheList(cache.generatedId);

            if (cl != null) {
                update = true;
                if (newValues.getGeoCacheType() == GeoCacheType.Mystery) {
                    if (!(cache.getCoordinate().equals(newValues.getCoordinate()))) {
                        cache.setHasCorrectedCoordinates(true);
                    }
                }
            }

            cache.setGeoCacheCode(gcc);
            cache.setGeoCacheType(newValues.getGeoCacheType());
            cache.geoCacheSize = newValues.geoCacheSize;
            cache.setDifficulty(newValues.getDifficulty());
            cache.setTerrain(newValues.getTerrain());
            cache.setCoordinate(newValues.getCoordinate());
            cache.setGeoCacheName(cacheTitle.getText());
            cache.setOwner(cacheOwner.getText());
            cache.setState(cacheState.getText());
            cache.setCountry(cacheCountry.getText());
            cache.setLongDescription(cacheDescription.getText());
            if (update) {
                cacheDAO.updateDatabase(cache);
                CacheListChangedListeners.getInstance().cacheListChanged();
            } else {
                CBDB.getInstance().cacheList.add(cache);
                cacheDAO.writeToDatabase(cache);
                GlobalCore.setSelectedCache(cache);
                CacheListChangedListeners.getInstance().cacheListChanged();
                GeoCaches.getInstance().setSelectedCacheVisible();
            }

            // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
            cache.setLongDescription("");
            GL.that.runOnGL(this::finish);
            return true;
        });
    }

    private void btnCancelClickHandler() {
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            finish();
            return true;
        });
    }

    private SpinnerAdapter cacheTypList() {
        return new SpinnerAdapter() {
            @Override
            public String getText(int index) {
                return geoCacheTypNumbers[index].name();
                // return "";nur Icons geht nicht
            }

            @Override
            public Drawable getIcon(int index) {
                return new SpriteDrawable(Sprites.getSprite("big" + geoCacheTypNumbers[index].name()));
            }

            @Override
            public int getCount() {
                return geoCacheTypNumbers.length;
            }
        };
    }

    private ISpinnerSelectionChanged cacheTypSelection() {
        return index -> {
            activityBase.show();
            newValues.setGeoCacheType(geoCacheTypNumbers[index]);
        };
    }

    private SpinnerAdapter cacheSizeList() {
        return new SpinnerAdapter() {
            @Override
            public String getText(int index) {
                return geoCacheSizeNumbers[index].name();
            }

            @Override
            public Drawable getIcon(int index) {
                return null;
                // Die folgenden Icons sind zu klein
                // return new SpriteDrawable(Sprites.SizesIcons.get(CacheSizeNumbers[index].ordinal()));
            }

            @Override
            public int getCount() {
                return geoCacheSizeNumbers.length;
            }
        };
    }

    private void setCacheCoordsChangeListener() {
        cacheCoords.setCoordinateChangedListener(coord -> {
            activityBase.show();
            newValues.setCoordinate(coord); // oder = cacheCoords.getMyPosition()
        });
    }

    private SpinnerAdapter cacheDifficultyList() {
        return new SpinnerAdapter() {
            @Override
            public String getText(int index) {
                return "D:" + ((index + 2.0f) / 2.0f);
            }

            @Override
            public Drawable getIcon(int index) {
                return null;
            }

            @Override
            public int getCount() {
                return 9;
            }
        };
    }

    private ISpinnerSelectionChanged cacheDifficultySelection() {
        return index -> {
            activityBase.show();
            newValues.setDifficulty((index + 2.0f) / 2.0f);
        };
    }

    private SpinnerAdapter cacheTerrainList() {
        return new SpinnerAdapter() {
            @Override
            public String getText(int index) {
                return "T:" + ((index + 2.0f) / 2.0f);
            }

            @Override
            public Drawable getIcon(int index) {
                return null;
            }

            @Override
            public int getCount() {
                return 9;
            }
        };
    }

    private void scrollToY(final EditTextField editTextField) {
        mainPanel.scrollTo(-mainPanel.getVirtualHeight() + editTextField.getY() + editTextField.getHeight());
    }

    @Override
    public void onShow() {
        KeyboardFocusChangedEventList.add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.remove(this);
    }

}

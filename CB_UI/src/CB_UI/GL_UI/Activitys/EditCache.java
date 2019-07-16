package CB_UI.GL_UI.Activitys;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheSizes;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheDAO;
import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI.GL_UI.Controls.CoordinateButton.ICoordinateChangedListener;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldStyle;
import CB_UI_Base.GL_UI.Controls.Spinner.ISelectionChangedListener;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.Date;

public class EditCache extends ActivityBase implements KeyboardFocusChangedEvent {
    // Allgemein
    private final CacheTypes[] CacheTypNumbers = CacheTypes.caches();
    private final CacheSizes[] CacheSizeNumbers = new CacheSizes[]{CacheSizes.other, // 0
            CacheSizes.micro, // 1
            CacheSizes.small, // 2
            CacheSizes.regular, // 3
            CacheSizes.large // 4
    };
    private Cache cache;
    private Cache newValues;
    private ScrollBox mainPanel;
    private CB_Button btnOK;
    private CB_Button btnCancel;
    private Spinner cacheTyp;
    private Spinner cacheSize;
    private Spinner cacheDifficulty;
    private Spinner cacheTerrain;
    private CoordinateButton cacheCoords;
    private EditTextField cacheCode; // SingleLine
    private EditTextField cacheTitle; // MultiLine
    private EditTextField cacheOwner; // SingleLine
    private EditTextField cacheCountry; // SingleLine
    private EditTextField cacheState; // SingleLine
    private EditTextField cacheDescription; // MultiLineWrapped

    // ctor
    public EditCache() {
        super(ActivityBase.ActivityRec(), "EditCache");
        // das übliche
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        this.initRow(BOTTOMUP);
        this.addNext(btnOK);
        this.addLast(btnCancel);
        mainPanel = new ScrollBox(0, getAvailableHeight());
        mainPanel.setBackground(this.getBackground());
        this.addLast(mainPanel);
        Box box = new Box(mainPanel.getInnerWidth(), 0); // height will be adjusted after containing all controls
        mainPanel.addChild(box);

        cacheTitle = (new EditTextField(this, "cacheTitle")).setWrapType(WrapType.MULTILINE);
        TextFieldStyle s = cacheTitle.getStyle();
        s.font = Fonts.getBig();
        cacheTitle.setStyle(s);
        cacheCode = new EditTextField(this, "cacheCode");
        s.font = Fonts.getCompass();
        cacheCode.setStyle(s);
        cacheDifficulty = new Spinner("EditCacheDifficulty", cacheDifficultyList(), cacheDifficultySelection());
        cacheTyp = new Spinner("EditCacheType", cacheTypList(), cacheTypSelection());
        cacheTerrain = new Spinner("EditCacheTerrain", cacheTerrainList(), cacheTerrainSelection());
        cacheSize = new Spinner("EditCacheSize", cacheSizeList(), cacheSizeSelection());
        cacheCoords = new CoordinateButton("cacheCoords");
        cacheOwner = new EditTextField(this, "cacheOwner");
        cacheState = new EditTextField(this, "cacheState");
        cacheCountry = new EditTextField(this, "cacheCountry");
        // layout
        box.addLast(cacheCode);
        box.addNext(cacheTyp);
        box.addLast(cacheDifficulty, 0.3f);
        box.addNext(cacheSize);
        box.addLast(cacheTerrain, 0.3f);
        box.addLast(cacheTitle);
        box.addLast(cacheCoords);
        box.addLast(cacheOwner);
        box.addLast(cacheCountry);
        box.addLast(cacheState);
        cacheDescription = new EditTextField(this, "cacheDescription").setWrapType(WrapType.WRAPPED);
        cacheDescription.setHeight(mainPanel.getAvailableHeight() / 2);
        box.addLast(cacheDescription);
        box.adjustHeight();
        mainPanel.setVirtualHeight(box.getHeight());

        btnOKClickHandler();
        btnCancelClickHandler();
        setCacheCoordsChangeListener();

    }

    @Override
    public void KeyboardFocusChanged(EditTextField editTextField) {
        if (editTextField != null) {
            scrollToY(editTextField);
            editTextField.setCursorPosition(editTextField.getText().length());
        }
    }

    public void update(Cache cache) {
        newValues = new Cache(true);
        newValues.copyFrom(cache);
        newValues.setShortDescription("");
        newValues.setLongDescription(Database.GetDescription(cache));
        cache.setLongDescription(newValues.getLongDescription());
        this.cache = cache;
        doShow();
    }

    public void create() {
        newValues = new Cache(true);
        newValues.Type = CacheTypes.Traditional;
        newValues.Size = CacheSizes.micro;
        newValues.setDifficulty(1);
        newValues.setTerrain(1);
        newValues.Pos = CB_Action_ShowMap.getInstance().normalMapView.center;
        if (!newValues.Pos.isValid())
            newValues.Pos = GlobalCore.getSelectedCoord();
        // GC - Code bestimmen für freies CWxxxx = CustomWaypint
        String prefix = "CW";
        int count = 0;
        do {
            count++;
            newValues.setGcCode(prefix + String.format("%04d", count));
        } while (Database.Data.cacheList.GetCacheById(Cache.GenerateCacheId(newValues.getGcCode())) != null);
        newValues.setName(newValues.getGcCode());
        newValues.setOwner("Unbekannt");
        newValues.setState("");
        newValues.setCountry("");
        newValues.setDateHidden(new Date());
        newValues.setArchived(false);
        newValues.setAvailable(true);
        newValues.setFound(false);
        newValues.NumTravelbugs = 0;
        newValues.setShortDescription("");
        newValues.setLongDescription("");
        this.cache = newValues;
        doShow();
    }

    private void doShow() {
        cacheCode.setText(cache.getGcCode());
        cacheTyp.setSelection(0);
        for (int i = 0; i < CacheTypNumbers.length; i++) {
            if (CacheTypNumbers[i] == cache.Type) {
                cacheTyp.setSelection(i);
            }
        }
        cacheSize.setSelection(0);
        for (int i = 0; i < CacheSizeNumbers.length; i++) {
            if (CacheSizeNumbers[i] == cache.Size) {
                cacheSize.setSelection(i);
            }
        }
        cacheDifficulty.setSelection((int) (cache.getDifficulty() * 2 - 2));
        cacheTerrain.setSelection((int) (cache.getTerrain() * 2 - 2));
        cacheCoords.setCoordinate(cache.Pos);
        cacheTitle.setText(cache.getName());
        cacheOwner.setText(cache.getOwner());
        cacheState.setText(cache.getState());
        cacheCountry.setText(cache.getCountry());
        if (cache.getLongDescription().equals(GlobalCore.br))
            cache.setLongDescription("");
        cacheDescription.setText(cache.getLongDescription());
        cacheDescription.setCursorPosition(0);
        this.show();
    }

    private void btnOKClickHandler() {
        btnOK.setOnClickListener((v, x, y, pointer, button) -> {
            boolean update = false;
            CacheDAO cacheDAO = new CacheDAO();
            String gcc = cacheCode.getText().toUpperCase(); // nur wenn kein Label
            cache.Id = Cache.GenerateCacheId(gcc);

            Cache cl = Database.Data.cacheList.GetCacheById(cache.Id);

            if (cl != null) {
                update = true;
                if (newValues.Type == CacheTypes.Mystery) {
                    if (!(cache.Pos.equals(newValues.Pos))) {
                        cache.setHasCorrectedCoordinates(true);
                    }
                }
            }

            cache.setGcCode(gcc);
            cache.Type = newValues.Type;
            cache.Size = newValues.Size;
            cache.setDifficulty(newValues.getDifficulty());
            cache.setTerrain(newValues.getTerrain());
            cache.Pos = newValues.Pos;
            cache.setName(cacheTitle.getText());
            cache.setOwner(cacheOwner.getText());
            cache.setState(cacheState.getText());
            cache.setCountry(cacheCountry.getText());
            cache.setLongDescription(cacheDescription.getText());
            if (update) {
                cacheDAO.UpdateDatabase(cache);
                CacheListChangedEventList.Call();
            } else {
                Database.Data.cacheList.add(cache);
                cacheDAO.WriteToDatabase(cache);
                CacheListChangedEventList.Call();
                GlobalCore.setSelectedCache(cache);
                CacheListView.getInstance().setSelectedCacheVisible();
            }

            // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
            cache.setLongDescription("");
            GL.that.RunOnGL(() -> finish());
            return true;
        });
    }

    private void btnCancelClickHandler() {
        this.btnCancel.setOnClickListener((v, x, y, pointer, button) -> {
            finish();
            return true;
        });
    }

    public SpinnerAdapter cacheTypList() {
        return new SpinnerAdapter() {
            @Override
            public String getText(int index) {
                return CacheTypNumbers[index].name();
                // return "";nur Icons geht nicht
            }

            @Override
            public Drawable getIcon(int index) {
                return new SpriteDrawable(Sprites.getSprite("big" + CacheTypNumbers[index].name()));
            }

            @Override
            public int getCount() {
                return CacheTypNumbers.length;
            }
        };
    }

    private ISelectionChangedListener cacheTypSelection() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(int index) {
                EditCache.this.show();
                newValues.Type = CacheTypNumbers[index];
            }
        };
    }

    public SpinnerAdapter cacheSizeList() {
        return new SpinnerAdapter() {
            @Override
            public String getText(int index) {
                return CacheSizeNumbers[index].name();
            }

            @Override
            public Drawable getIcon(int index) {
                return null;
                // Die folgenden Icons sind zu klein
                // return new SpriteDrawable(Sprites.SizesIcons.get(CacheSizeNumbers[index].ordinal()));
            }

            @Override
            public int getCount() {
                return CacheSizeNumbers.length;
            }
        };
    }

    private ISelectionChangedListener cacheSizeSelection() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(int index) {
                EditCache.this.show();
                newValues.Size = CacheSizeNumbers[index];
            }
        };
    }

    private void setCacheCoordsChangeListener() {
        cacheCoords.setCoordinateChangedListener(new ICoordinateChangedListener() {
            @Override
            public void coordinateChanged(Coordinate coord) {
                EditCache.this.show();
                newValues.Pos = coord; // oder = cacheCoords.getCoordinate()
            }
        });
    }

    public SpinnerAdapter cacheDifficultyList() {
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

    private ISelectionChangedListener cacheDifficultySelection() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(int index) {
                EditCache.this.show();
                newValues.setDifficulty((index + 2.0f) / 2.0f);
            }
        };
    }

    public SpinnerAdapter cacheTerrainList() {
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

    private ISelectionChangedListener cacheTerrainSelection() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(int index) {
                EditCache.this.show();
                newValues.setTerrain((index + 2.0f) / 2.0f);
            }
        };
    }

    private void scrollToY(final EditTextField editTextField) {
        mainPanel.scrollTo(-mainPanel.getVirtualHeight() + editTextField.getY() + editTextField.getHeight());
    }

    @Override
    public void onShow() {
        KeyboardFocusChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.Remove(this);
    }

}

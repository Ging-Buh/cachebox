package CB_UI.GL_UI.Activitys;

import java.util.ArrayList;
import java.util.Date;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheSizes;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.DAO.CacheDAO;
import CB_Core.Types.Cache;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI.GL_UI.Controls.CoordinateButton.CoordinateChangeListner;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldStyle;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.Spinner;
import CB_UI_Base.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_UI_Base.GL_UI.Controls.SpinnerAdapter;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

public class EditCache extends ActivityBase
// implements KeyboardFocusChangedEvent
{
    // Allgemein
    private final CacheTypes[] CacheTypNumbers = new CacheTypes[] { CacheTypes.Traditional, // = 0,
	    CacheTypes.Multi, // = 1,
	    CacheTypes.Mystery, // = 2,
	    CacheTypes.Camera, // = 3,
	    CacheTypes.Earth, // = 4,
	    CacheTypes.Event, // = 5,
	    CacheTypes.MegaEvent, // = 6,
	    CacheTypes.CITO, // = 7,
	    CacheTypes.Virtual, // = 8,
	    CacheTypes.Letterbox, // = 9,
	    CacheTypes.Wherigo }; // = 10,
    private final CacheSizes[] CacheSizeNumbers = new CacheSizes[] { CacheSizes.other, // 0
	    CacheSizes.micro, // 1
	    CacheSizes.small, // 2
	    CacheSizes.regular, // 3
	    CacheSizes.large // 4
    };

    private Cache cache;
    private Cache newValues;
    private ScrollBox mainPanel;
    private Button btnOK;
    private Button btnCancel;
    private Spinner cacheTyp;
    private Spinner cacheSize;
    private Spinner cacheDifficulty;
    private Spinner cacheTerrain;
    private CoordinateButton cacheCoords;
    private EditTextField cacheCode; // SingleLine
    private EditTextField cacheTitle; // MultiLine
    private EditTextField cacheOwner; // SingleLine
    private EditTextField cacheDescription; // MultiLineWraped

    // ctor
    public EditCache(CB_RectF rec, String Name) {
	super(rec, Name);
	// das übliche
	btnOK = new Button(Translation.Get("ok"));
	btnOKClickHandler();
	btnCancel = new Button(Translation.Get("cancel"));
	btnCancelClickHandler();
	this.initRow(BOTTOMUP);
	this.addNext(btnOK);
	this.addLast(btnCancel);
	mainPanel = new ScrollBox(innerWidth, getAvailableHeight()); // (innerWidth, getAvailableHeight(), "mainPanel");
	this.addLast(mainPanel);
	mainPanel.initRow(BOTTOMUP);
	// --- Description
	cacheDescription = new EditTextField(this, this.name + " cacheDescription").setWrapType(WrapType.WRAPPED);
	cacheDescription.setHeight(mainPanel.getAvailableHeight() / 2);
	mainPanel.addLast(cacheDescription);
	registerTextField(cacheDescription);
	// --- Hint
	// --- Notes
	// --- Status
	// --- versteckt am
	// --- Owner
	cacheOwner = new EditTextField(this, this.name + " cacheOwner");
	mainPanel.addLast(cacheOwner);
	registerTextField(cacheOwner);
	// --- Coords
	cacheCoords = new CoordinateButton("cacheCoords");
	setCacheCoordsChangeListner();
	mainPanel.addLast(cacheCoords);
	// --- Title
	cacheTitle = (new EditTextField(this, this.name + " cacheTitle")).setWrapType(WrapType.MULTILINE);
	TextFieldStyle s = cacheTitle.getStyle();
	s.font = Fonts.getBig();
	cacheTitle.setStyle(s);
	mainPanel.addLast(cacheTitle);
	registerTextField(cacheTitle);
	// --- Size
	cacheSize = new Spinner("cacheSize", cacheSizeList(), cacheSizeSelection());
	mainPanel.addNext(cacheSize);
	// --- Terrain
	cacheTerrain = new Spinner("cacheTerrain", cacheTerrainList(), cacheTerrainSelection());
	mainPanel.addLast(cacheTerrain, 0.3f);
	// --- Type
	// Label lblType = new Label("lblType");
	// mainPanel.addNext(lblType, 0.2f);
	// lblType.setText(tl.Get("type"));
	cacheTyp = new Spinner("cacheTyp", cacheTypList(), cacheTypSelection());
	mainPanel.addNext(cacheTyp);
	// --- Difficulty
	cacheDifficulty = new Spinner("cacheDifficulty", cacheDifficultyList(), cacheDifficultySelection());
	mainPanel.addLast(cacheDifficulty, 0.3f);
	// --- Code
	cacheCode = new EditTextField(this.name + " cacheCode");
	s.font = Fonts.getCompass();
	cacheCode.setStyle(s);
	mainPanel.addLast(cacheCode);
	registerTextField(cacheCode);

	mainPanel.setVirtualHeight(mainPanel.getHeightFromBottom());

	this.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		for (EditTextField tmp : allTextFields) {
		    tmp.getOnscreenKeyboard().show(false);
		    tmp.setFocus(false);
		}
		return true;
	    }
	});

    }

    public void Update(Cache cache) {
	newValues = new Cache(true);
	newValues.copyFrom(cache);
	newValues.setShortDescription("");
	newValues.setLongDescription(Database.GetDescription(cache));
	cache.setLongDescription(newValues.getLongDescription());
	this.cache = cache;
	doShow();
    }

    public void Create() {
	newValues = new Cache(true);
	newValues.Type = CacheTypes.Traditional;
	newValues.Size = CacheSizes.micro;
	newValues.setDifficulty(1);
	newValues.setTerrain(1);
	newValues.Pos = Locator.getLocation().toCordinate();
	if (!newValues.Pos.isValid())
	    newValues.Pos = GlobalCore.getSelectedCoord();
	// GC - Code bestimmen für freies CWxxxx = CustomWaypint
	String prefix = "CW";
	int count = 0;
	do {
	    count++;
	    newValues.setGcCode(prefix + String.format("%04d", count));
	} while (Database.Data.Query.GetCacheById(Cache.GenerateCacheId(newValues.getGcCode())) != null);
	newValues.setName(newValues.getGcCode());
	newValues.setOwner("Unbekannt");
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
	if (cache.getLongDescription().equals(GlobalCore.br))
	    cache.setLongDescription("");
	cacheDescription.setText(cache.getLongDescription());
	this.show();
    }

    private void btnOKClickHandler() {
	this.btnOK.setOnClickListener(new OnClickListener() {
	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		boolean update = false;
		CacheDAO cacheDAO = new CacheDAO();
		String gcc = cacheCode.getText().toUpperCase(); // nur wenn kein Label
		cache.Id = Cache.GenerateCacheId(gcc);

		Cache cl = Database.Data.Query.GetCacheById(cache.Id);

		if (cl != null) {
		    update = true;
		    if (newValues.Type == CacheTypes.Mystery) {
			if (!(cache.Pos.equals(newValues.Pos))) {
			    cache.setCorrectedCoordinates(true);
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
		cache.setLongDescription(cacheDescription.getText());
		if (update) {
		    cacheDAO.UpdateDatabase(cache);
		    CacheListChangedEventList.Call();
		} else {
		    Database.Data.Query.add(cache);
		    cacheDAO.WriteToDatabase(cache);
		    CacheListChangedEventList.Call();
		    GlobalCore.setSelectedCache(cache);
		    if (TabMainView.cacheListView != null)
			TabMainView.cacheListView.setSelectedCacheVisible();
		}

		// Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
		cache.setLongDescription("");
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {
			finish();
		    }
		});

		return true;
	    }
	});
    }

    private void btnCancelClickHandler() {
	this.btnCancel.setOnClickListener(new OnClickListener() {

	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		finish();
		return true;
	    }
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
		return new SpriteDrawable(SpriteCacheBase.BigIcons.get(CacheTypNumbers[index].ordinal()));
	    }

	    @Override
	    public int getCount() {
		return CacheTypNumbers.length;
	    }
	};
    }

    private selectionChangedListner cacheTypSelection() {
	return new selectionChangedListner() {
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
		// return new SpriteDrawable(SpriteCache.SizesIcons.get(CacheSizeNumbers[index].ordinal()));
	    }

	    @Override
	    public int getCount() {
		return CacheSizeNumbers.length;
	    }
	};
    }

    private selectionChangedListner cacheSizeSelection() {
	return new selectionChangedListner() {
	    @Override
	    public void selectionChanged(int index) {
		EditCache.this.show();
		newValues.Size = CacheSizeNumbers[index];
	    }
	};
    }

    private void setCacheCoordsChangeListner() {
	cacheCoords.setCoordinateChangedListner(new CoordinateChangeListner() {
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

    private selectionChangedListner cacheDifficultySelection() {
	return new selectionChangedListner() {
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

    private selectionChangedListner cacheTerrainSelection() {
	return new selectionChangedListner() {
	    @Override
	    public void selectionChanged(int index) {
		EditCache.this.show();
		newValues.setTerrain((index + 2.0f) / 2.0f);
	    }
	};
    }

    private ArrayList<EditTextField> allTextFields = new ArrayList<EditTextField>();

    public void registerTextField(final EditTextField textField) {
	textField.setOnscreenKeyboard(new OnscreenKeyboard() {
	    @Override
	    public void show(boolean arg0) {
		scrollToY(textField);
		textField.setCursorPosition(textField.getText().length());
	    }
	});
	allTextFields.add(textField);
    }

    private void scrollToY(final EditTextField textField) {
	mainPanel.scrollTo(-mainPanel.getVirtualHeight() + textField.getY() + textField.getHeight());
    }

}

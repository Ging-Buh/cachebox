package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;
import java.util.Date;

import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CoordinateButton;
import CB_Core.GL_UI.Controls.CoordinateButton.CoordinateChangeListner;
import CB_Core.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditTextFieldBase.TextFieldStyle;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_Core.GL_UI.Controls.SpinnerAdapter;
import CB_Core.GL_UI.Views.CacheListView;
import CB_Core.Math.CB_RectF;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Locator.Coordinate;
import CB_Locator.Locator;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class EditCache extends ActivityBase
// implements KeyboardFocusChangedEvent
{
	// Allgemein
	private final CacheTypes[] CacheTypNumbers = new CacheTypes[]
		{ CacheTypes.Traditional, // = 0,
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
	private final CacheSizes[] CacheSizeNumbers = new CacheSizes[]
		{ CacheSizes.other, // 0
				CacheSizes.micro, // 1
				CacheSizes.small, // 2
				CacheSizes.regular, // 3
				CacheSizes.large // 4
		};

	private Cache cache;
	private Cache newValues;
	private Box mainPanel;
	private float mainPanel_Y;
	private Button btnOK;
	private Button btnCancel;
	private Spinner cacheTyp;
	private Spinner cacheSize;
	private Spinner cacheDifficulty;
	private Spinner cacheTerrain;
	private CoordinateButton cacheCoords;
	private EditWrapedTextField cacheCode; // SingleLine
	private EditWrapedTextField cacheTitle; // MultiLine
	private EditWrapedTextField cacheOwner; // SingleLine
	private EditWrapedTextField cacheDescription; // MultiLineWraped

	// ctor
	public EditCache(CB_RectF rec, String Name)
	{
		super(rec, Name);
		// das übliche
		btnOK = new Button("btnOK");
		btnOKClickHandler();
		btnCancel = new Button("btnCancel");
		btnCancelClickHandler();
		this.initRow(false);
		this.addNext(btnOK);
		this.addLast(btnCancel);
		btnCancel.setText(Translation.Get("cancel"));
		btnOK.setText(Translation.Get("ok"));
		mainPanel = new Box(this.getAvailableWidth(), this.getAvailableHeight(), "mainPanel");
		this.addLast(mainPanel);
		mainPanel_Y = mainPanel.getY();
		// --- Code
		cacheCode = new EditWrapedTextField("cacheCode", TextFieldType.SingleLine);
		TextFieldStyle s = cacheCode.getStyle();
		s.font = Fonts.getCompass();
		cacheCode.setStyle(s);
		mainPanel.addLast(cacheCode);
		registerTextField(cacheCode);
		// --- Type
		// Label lblType = new Label("lblType");
		// mainPanel.addNext(lblType, 0.2f);
		// lblType.setText(tl.Get("type"));
		cacheTyp = new Spinner("cacheTyp", cacheTypList(), cacheTypSelection());
		mainPanel.addNext(cacheTyp);
		// --- Difficulty
		cacheDifficulty = new Spinner("cacheDifficulty", cacheDifficultyList(), cacheDifficultySelection());
		mainPanel.addLast(cacheDifficulty, 0.3f);
		// --- Size
		cacheSize = new Spinner("cacheSize", cacheSizeList(), cacheSizeSelection());
		mainPanel.addNext(cacheSize);
		// --- Terrain
		cacheTerrain = new Spinner("cacheTerrain", cacheTerrainList(), cacheTerrainSelection());
		mainPanel.addLast(cacheTerrain, 0.3f);
		// --- Title
		cacheTitle = new EditWrapedTextField(this, cacheTyp.copy(), TextFieldType.MultiLine, "cacheTitle");
		s = cacheTitle.getStyle();
		s.font = Fonts.getBig();
		cacheTitle.setStyle(s);
		mainPanel.addLast(cacheTitle);
		registerTextField(cacheTitle);
		// --- Coords
		cacheCoords = new CoordinateButton("cacheCoords");
		setCacheCoordsChangeListner();
		mainPanel.addLast(cacheCoords);
		// --- Owner
		cacheOwner = new EditWrapedTextField(this, cacheTyp.copy(), TextFieldType.SingleLine, "cacheOwner");
		mainPanel.addLast(cacheOwner);
		registerTextField(cacheOwner);
		// --- versteckt am
		// --- Status
		// --- Notes
		// --- Hint
		// --- Description
		cacheDescription = new EditWrapedTextField(this, cacheTyp.copy(), TextFieldType.MultiLineWraped, "cacheDescription");
		cacheDescription.setHeight(mainPanel.getAvailableHeight());
		mainPanel.addLast(cacheDescription);
		registerTextField(cacheDescription);

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				for (EditWrapedTextField tmp : allTextFields)
				{
					tmp.getOnscreenKeyboard().show(false);
					tmp.resetFocus();
				}
				mainPanel.setY(mainPanel_Y);
				return true;
			}
		});

	}

	public void Update(Cache cache)
	{
		newValues = new Cache();
		newValues.copyFrom(cache);
		newValues.shortDescription = "";
		newValues.longDescription = Database.GetDescription(cache);
		cache.longDescription = newValues.longDescription;
		this.cache = cache;
		doShow();
	}

	public void Create()
	{
		newValues = new Cache();
		newValues.Type = CacheTypes.Traditional;
		newValues.Size = CacheSizes.micro;
		newValues.Difficulty = 1;
		newValues.Terrain = 1;
		newValues.Pos = Locator.getLocation().toCordinate();
		if (!newValues.Pos.isValid()) newValues.Pos = GlobalCore.getSelectedCoord();
		// GC - Code bestimmen für freies CWxxxx = CustomWaypint
		String prefix = "CW";
		int count = 0;
		do
		{
			count++;
			newValues.GcCode = prefix + String.format("%04d", count);
		}
		while (Database.Data.Query.GetCacheById(Cache.GenerateCacheId(newValues.GcCode)) != null);
		newValues.Name = newValues.GcCode;
		newValues.Owner = "Unbekannt";
		newValues.DateHidden = new Date();
		newValues.Archived = false;
		newValues.Available = true;
		newValues.Found = false;
		newValues.NumTravelbugs = 0;
		newValues.shortDescription = "";
		newValues.longDescription = "";
		this.cache = newValues;
		doShow();
	}

	private void doShow()
	{
		cacheCode.setText(cache.GcCode);
		cacheTyp.setSelection(0);
		for (int i = 0; i < CacheTypNumbers.length; i++)
		{
			if (CacheTypNumbers[i] == cache.Type)
			{
				cacheTyp.setSelection(i);
			}
		}
		cacheSize.setSelection(0);
		for (int i = 0; i < CacheSizeNumbers.length; i++)
		{
			if (CacheSizeNumbers[i] == cache.Size)
			{
				cacheSize.setSelection(i);
			}
		}
		cacheDifficulty.setSelection((int) (cache.Difficulty * 2 - 2));
		cacheTerrain.setSelection((int) (cache.Terrain * 2 - 2));
		cacheCoords.setCoordinate(cache.Pos);
		cacheTitle.setText(cache.Name);
		cacheOwner.setText(cache.Owner);
		if (cache.longDescription.equals(GlobalCore.br)) cache.longDescription = "";
		cacheDescription.setText(cache.longDescription);
		this.show();
	}

	private void btnOKClickHandler()
	{
		this.btnOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				boolean update = false;
				CacheDAO cacheDAO = new CacheDAO();
				String gcc = cacheCode.getText().toUpperCase(); // nur wenn kein Label
				cache.Id = Cache.GenerateCacheId(gcc);
				Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);
				if (aktCache != null)
				{
					cache = aktCache;
					update = true;
					if (newValues.Type == CacheTypes.Mystery)
					{
						if (!(cache.Pos.equals(newValues.Pos)))
						{
							cache.setCorrectedCoordinates(true);
						}
					}
				}
				cache.GcCode = gcc;
				cache.Type = newValues.Type;
				cache.Size = newValues.Size;
				cache.Difficulty = newValues.Difficulty;
				cache.Terrain = newValues.Terrain;
				cache.Pos = newValues.Pos;
				cache.Name = cacheTitle.getText();
				cache.Owner = cacheOwner.getText();
				cache.longDescription = cacheDescription.getText();
				if (update)
				{
					cacheDAO.UpdateDatabase(cache);
					CachListChangedEventList.Call();
				}
				else
				{
					Database.Data.Query.add(cache);
					cacheDAO.WriteToDatabase(cache);
					CachListChangedEventList.Call();
					GlobalCore.setSelectedCache(cache);
					CacheListView.that.setSelectedCacheVisible();
				}
				finish();
				return true;
			}
		});
	}

	private void btnCancelClickHandler()
	{
		this.btnCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				finish();
				return true;
			}
		});
	}

	public SpinnerAdapter cacheTypList()
	{
		return new SpinnerAdapter()
		{
			@Override
			public String getText(int index)
			{
				return CacheTypNumbers[index].name();
				// return "";nur Icons geht nicht
			}

			@Override
			public Drawable getIcon(int index)
			{
				return new SpriteDrawable(SpriteCache.BigIcons.get(CacheTypNumbers[index].ordinal()));
			}

			@Override
			public int getCount()
			{
				return CacheTypNumbers.length;
			}
		};
	}

	private selectionChangedListner cacheTypSelection()
	{
		return new selectionChangedListner()
		{
			@Override
			public void selectionChanged(int index)
			{
				that.show();
				newValues.Type = CacheTypNumbers[index];
			}
		};
	}

	public SpinnerAdapter cacheSizeList()
	{
		return new SpinnerAdapter()
		{
			@Override
			public String getText(int index)
			{
				return CacheSizeNumbers[index].name();
			}

			@Override
			public Drawable getIcon(int index)
			{
				return null;
				// Die folgenden Icons sind zu klein
				// return new SpriteDrawable(SpriteCache.SizesIcons.get(CacheSizeNumbers[index].ordinal()));
			}

			@Override
			public int getCount()
			{
				return CacheSizeNumbers.length;
			}
		};
	}

	private selectionChangedListner cacheSizeSelection()
	{
		return new selectionChangedListner()
		{
			@Override
			public void selectionChanged(int index)
			{
				that.show();
				newValues.Size = CacheSizeNumbers[index];
			}
		};
	}

	private void setCacheCoordsChangeListner()
	{
		cacheCoords.setCoordinateChangedListner(new CoordinateChangeListner()
		{
			@Override
			public void coordinateChanged(Coordinate coord)
			{
				that.show();
				newValues.Pos = coord; // oder = cacheCoords.getCoordinate()
			}
		});
	}

	public SpinnerAdapter cacheDifficultyList()
	{
		return new SpinnerAdapter()
		{
			@Override
			public String getText(int index)
			{
				return "D:" + ((index + 2.0f) / 2.0f);
			}

			@Override
			public Drawable getIcon(int index)
			{
				return null;
			}

			@Override
			public int getCount()
			{
				return 9;
			}
		};
	}

	private selectionChangedListner cacheDifficultySelection()
	{
		return new selectionChangedListner()
		{
			@Override
			public void selectionChanged(int index)
			{
				that.show();
				newValues.Difficulty = (index + 2.0f) / 2.0f;
			}
		};
	}

	public SpinnerAdapter cacheTerrainList()
	{
		return new SpinnerAdapter()
		{
			@Override
			public String getText(int index)
			{
				return "T:" + ((index + 2.0f) / 2.0f);
			}

			@Override
			public Drawable getIcon(int index)
			{
				return null;
			}

			@Override
			public int getCount()
			{
				return 9;
			}
		};
	}

	private selectionChangedListner cacheTerrainSelection()
	{
		return new selectionChangedListner()
		{
			@Override
			public void selectionChanged(int index)
			{
				that.show();
				newValues.Terrain = (index + 2.0f) / 2.0f;
			}
		};
	}

	private ArrayList<EditWrapedTextField> allTextFields = new ArrayList<EditWrapedTextField>();

	public void registerTextField(final EditWrapedTextField textField)
	{
		textField.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean arg0)
			{
				scrollToY(textField);
				textField.setCursorPosition(textField.getText().length());
			}
		});
		allTextFields.add(textField);
	}

	private void scrollToY(final EditWrapedTextField textField)
	{
		mainPanel.setY(mainPanel_Y + mainPanel.getHeight() - textField.getY() - textField.getHeight());
	}

}

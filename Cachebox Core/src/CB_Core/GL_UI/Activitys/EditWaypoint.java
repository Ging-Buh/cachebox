package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.KeyboardFocusChangedEvent;
import CB_Core.Events.KeyboardFocusChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CoordinateButton;
import CB_Core.GL_UI.Controls.CoordinateButton.CoordinateChangeListner;
import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.GL_UI.Controls.EditTextFieldBase.DefaultOnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_Core.GL_UI.Controls.SpinnerAdapter;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.TranslationEngine.LangStrings;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class EditWaypoint extends ActivityBase implements KeyboardFocusChangedEvent
{

	private Waypoint waypoint;
	private CoordinateButton bCoord = null;
	private Spinner sType = null;
	private Button bOK = null;
	private Button bCancel = null;
	private Label tvCacheName = null;
	private Label tvTyp = null;
	private Label tvTitle = null;
	private EditWrapedTextField etTitle = null;
	private Label tvDescription = null;
	private EditWrapedTextField etDescription = null;
	private Label tvClue = null;
	private EditWrapedTextField etClue = null;
	private Boolean firstShow = true;
	// damit kann festgelegt werden, ob beim Start des WaypointDialogs gleich der Coordinaten-Dialog gezeigt werden soll oder nicht.
	private Boolean showCoordinateDialog = false;

	private Box scrollBox;

	public interface ReturnListner
	{
		public void returnedWP(Waypoint wp);
	}

	private ReturnListner mReturnListner;

	public EditWaypoint(CB_RectF rec, String Name, Waypoint waypoint, ReturnListner listner, boolean showCoordinateDialog)
	{
		super(rec, Name);
		scrollBox = new Box(rec, Name);
		this.addChild(scrollBox);
		this.waypoint = waypoint;
		this.mReturnListner = listner;
		this.showCoordinateDialog = showCoordinateDialog;

		iniCacheNameLabel();
		iniCoordButton();
		iniLabelTyp();
		iniTypeSpinner();
		iniLabelTitle();
		iniTitleTextField();
		iniLabelDesc();
		iniTitleTextDesc();
		iniLabelClue();
		iniTitleTextClue();
		iniOkCancel();
		iniTextfieldFocus();

		layoutTextFields();

		this.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				for (EditWrapedTextField tmp : allTextFields)
				{
					tmp.resetFocus();
				}

				keyboard.show(false);
				scrollToY(that.getHeight(), that.getHeight());
				return true;
			}
		});

	}

	@Override
	protected void Initial()
	{

	}

	private void iniCacheNameLabel()
	{
		tvCacheName = new Label(this.getLeftWidth() + margin, height - this.getTopHeight() - MeasuredLabelHeight, width
				- this.getLeftWidth() - this.getRightWidth() - margin, MeasuredLabelHeight, "CacheNameLabel");
		tvCacheName.setFont(Fonts.getBubbleNormal());
		tvCacheName.setText(GlobalCore.getSelectedCache().Name);
		scrollBox.addChild(tvCacheName);
	}

	private void iniCoordButton()
	{
		CB_RectF rec = new CB_RectF(this.getLeftWidth(), tvCacheName.getY() - UiSizes.getButtonHeight(), width - this.getLeftWidth()
				- this.getRightWidth(), UiSizes.getButtonHeight());
		bCoord = new CoordinateButton(rec, "CoordButton", waypoint.Pos);

		bCoord.setCoordinateChangedListner(new CoordinateChangeListner()
		{

			@Override
			public void coordinateChanged(Coordinate coord)
			{
				that.show();
			}
		});

		scrollBox.addChild(bCoord);
	}

	private void iniLabelTyp()
	{
		tvTyp = new Label(this.getLeftWidth() + margin, bCoord.getY() - margin - MeasuredLabelHeight, width - this.getLeftWidth()
				- this.getRightWidth() - margin, MeasuredLabelHeight, "TypeLabel");
		tvTyp.setFont(Fonts.getBubbleNormal());
		tvTyp.setText(GlobalCore.Translations.Get("type"));
		scrollBox.addChild(tvTyp);

	}

	private void iniTypeSpinner()
	{
		CB_RectF rec = new CB_RectF(this.getLeftWidth(), tvTyp.getY() - UiSizes.getButtonHeight(), width - this.getLeftWidth()
				- this.getRightWidth(), UiSizes.getButtonHeight());
		sType = new Spinner(rec, "CoordButton", getSpinerAdapter(), new selectionChangedListner()
		{

			@Override
			public void selectionChanged(int index)
			{
				that.show();
				switch (index)
				{
				case 0:
					waypoint.Type = CacheTypes.ReferencePoint;
					break;
				case 1:
					waypoint.Type = CacheTypes.MultiStage;
					break;
				case 2:
					waypoint.Type = CacheTypes.MultiQuestion;
					break;
				case 3:
					waypoint.Type = CacheTypes.Trailhead;
					break;
				case 4:
					waypoint.Type = CacheTypes.ParkingArea;
					break;
				case 5:
					waypoint.Type = CacheTypes.Final;
					break;
				}

			}
		});

		// Spinner initialisieren
		switch (waypoint.Type)
		{
		case ReferencePoint:
			sType.setSelection(0);
			break;
		case MultiStage:
			sType.setSelection(1);
			break;
		case MultiQuestion:
			sType.setSelection(2);
			break;
		case Trailhead:
			sType.setSelection(3);
			break;
		case ParkingArea:
			sType.setSelection(4);
			break;
		case Final:
			sType.setSelection(5);
			break;
		default:
			sType.setSelection(0);
		}

		scrollBox.addChild(sType);
	}

	private SpinnerAdapter getSpinerAdapter()
	{
		final LangStrings ls = GlobalCore.Translations;

		final String[] names = new String[]
			{ ls.Get("Reference"), ls.Get("StageofMulti"), ls.Get("Question2Answer"), ls.Get("Trailhead"), ls.Get("Parking"),
					ls.Get("Final") };

		SpinnerAdapter adapter = new SpinnerAdapter()
		{

			@Override
			public String getText(int position)
			{
				return names[position];
			}

			@Override
			public Drawable getIcon(int Position)
			{
				switch (Position)
				{
				case 0:
					return new SpriteDrawable(SpriteCache.BigIcons.get(CacheTypes.ReferencePoint.ordinal()));
				case 1:
					return new SpriteDrawable(SpriteCache.BigIcons.get(CacheTypes.MultiStage.ordinal()));
				case 2:
					return new SpriteDrawable(SpriteCache.BigIcons.get(CacheTypes.MultiQuestion.ordinal()));
				case 3:
					return new SpriteDrawable(SpriteCache.BigIcons.get(CacheTypes.Trailhead.ordinal()));
				case 4:
					return new SpriteDrawable(SpriteCache.BigIcons.get(CacheTypes.ParkingArea.ordinal()));
				case 5:
					return new SpriteDrawable(SpriteCache.BigIcons.get(CacheTypes.Final.ordinal()));

				}

				return null;
			}

			@Override
			public int getCount()
			{
				return names.length;
			}
		};

		return adapter;

	}

	private void iniLabelTitle()
	{
		tvTitle = new Label(this.getLeftWidth() + margin, sType.getY() - margin - MeasuredLabelHeight, width - this.getLeftWidth()
				- this.getRightWidth() - margin, MeasuredLabelHeight, "TitleLabel");
		tvTitle.setFont(Fonts.getBubbleNormal());
		tvTitle.setText(GlobalCore.Translations.Get("Title"));
		scrollBox.addChild(tvTitle);
	}

	private void iniTitleTextField()
	{
		CB_RectF rec = new CB_RectF(this.getLeftWidth(), tvTitle.getY() - UiSizes.getButtonHeight(), width - this.getLeftWidth()
				- this.getRightWidth(), UiSizes.getButtonHeight());
		etTitle = new EditWrapedTextField(this, rec, "TitleTextField");

		String txt = (waypoint.Title == null) ? "" : waypoint.Title;

		etTitle.setText(txt);
		scrollBox.addChild(etTitle);
	}

	private void iniLabelDesc()
	{
		tvDescription = new Label(this.getLeftWidth() + margin, etTitle.getY() - margin - MeasuredLabelHeight, width - this.getLeftWidth()
				- this.getRightWidth() - margin, MeasuredLabelHeight, "DescLabel");
		tvDescription.setFont(Fonts.getBubbleNormal());
		tvDescription.setText(GlobalCore.Translations.Get("Description"));
		scrollBox.addChild(tvDescription);
	}

	private void iniTitleTextDesc()
	{
		CB_RectF rec = new CB_RectF(this.getLeftWidth(), tvDescription.getY() - UiSizes.getButtonHeight(), width - this.getLeftWidth()
				- this.getRightWidth(), UiSizes.getButtonHeight());
		etDescription = new EditWrapedTextField(this, rec, TextFieldType.MultiLineWraped, "DescTextField");

		String txt = (waypoint.Description == null) ? "" : waypoint.Description;

		etDescription.setText(txt);

		etDescription.setTextFieldListener(new TextFieldListener()
		{

			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{

			}

			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{
				layoutTextFields();
			}
		});

		scrollBox.addChild(etDescription);
	}

	private void iniLabelClue()
	{
		tvClue = new Label(this.getLeftWidth() + margin, etDescription.getY() - margin - MeasuredLabelHeight, width - this.getLeftWidth()
				- this.getRightWidth() - margin, MeasuredLabelHeight, "ClueLabel");
		tvClue.setFont(Fonts.getBubbleNormal());
		tvClue.setText(GlobalCore.Translations.Get("Clue"));
		scrollBox.addChild(tvClue);
	}

	private void iniTitleTextClue()
	{
		CB_RectF rec = new CB_RectF(this.getLeftWidth(), tvClue.getY() - UiSizes.getButtonHeight(), width - this.getLeftWidth()
				- this.getRightWidth(), UiSizes.getButtonHeight());
		etClue = new EditWrapedTextField(this, rec, TextFieldType.MultiLineWraped, "ClueTextField");

		String txt = (waypoint.Clue == null) ? "" : waypoint.Clue;

		etClue.setText(txt);

		etClue.setTextFieldListener(new TextFieldListener()
		{
			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{

			}

			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{
				layoutTextFields();
			}
		});

		scrollBox.addChild(etClue);
	}

	private void iniOkCancel()
	{
		CB_RectF btnRec = new CB_RectF(this.getLeftWidth(), this.getBottomHeight(),
				(this.width - this.getLeftWidth() - this.getRightWidth()) / 2, UiSizes.getButtonHeight());
		bOK = new Button(btnRec, "OkButton");

		btnRec.setX(bOK.getMaxX());
		bCancel = new Button(btnRec, "CancelButton");

		bOK.setText(GlobalCore.Translations.Get("ok"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(bOK);
		this.addChild(bCancel);

		bOK.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null)
				{
					waypoint.Pos = bCoord.getCoordinate();
					waypoint.Title = etTitle.getText();
					waypoint.Description = etDescription.getText();
					waypoint.Clue = etClue.getText();
					mReturnListner.returnedWP(waypoint);
				}

				// Änderungen auch an die MapView melden
				if (MapView.that != null) MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);

				finish();
				return true;
			}
		});

		bCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null) mReturnListner.returnedWP(null);
				finish();
				return true;
			}
		});

	}

	private void iniTextfieldFocus()
	{
		registerTextField(etTitle);
		registerTextField(etDescription);
		registerTextField(etClue);
	}

	private ArrayList<EditWrapedTextField> allTextFields = new ArrayList<EditWrapedTextField>();

	private OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();

	public void registerTextField(final EditWrapedTextField textField)
	{
		textField.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean arg0)
			{
				textField.setFocus(true);
				scrollToY(textField.getY(), textField.getMaxY());
			}
		});

		allTextFields.add(textField);
	}

	private void scrollToY(float y, float maxY)
	{
		if (y < this.halfHeight)// wird von softKeyboard verdeckt
		{
			scrollBox.setY(this.height - maxY - MeasuredLabelHeight);
		}
		else
		{
			scrollBox.setY(0);
		}
	}

	private void layoutTextFields()
	{
		float maxTextFieldHeight = this.height / 2.3f;
		float rand = etClue.getStyle().background.getBottomHeight() + etClue.getStyle().background.getTopHeight();
		float descriptionHeight = Math.min(maxTextFieldHeight, etDescription.getMeasuredHeight() + rand);
		float clueHeight = Math.min(maxTextFieldHeight, etClue.getMeasuredHeight() + rand);

		descriptionHeight = Math.max(descriptionHeight, UiSizes.getButtonHeight());
		clueHeight = Math.max(clueHeight, UiSizes.getButtonHeight());

		etDescription.setHeight(descriptionHeight);
		etClue.setHeight(clueHeight);

		etDescription.setY(tvDescription.getY() - descriptionHeight);

		tvClue.setY(etDescription.getY() - margin - MeasuredLabelHeight);

		etClue.setY(tvClue.getY() - clueHeight);

	}

	@Override
	public void onShow()
	{
		// onShow switch to editCoord Dialog if this the first show
		if (firstShow && showCoordinateDialog) bCoord.performClick();
		firstShow = false;
		KeyboardFocusChangedEventList.Add(this);
	}

	@Override
	public void onHide()
	{
		KeyboardFocusChangedEventList.Remove(this);
	}

	@Override
	public void KeyboardFocusChanged(EditTextFieldBase focus)
	{
		if (focus == null)
		{
			scrollBox.setY(0);
		}
	}

}

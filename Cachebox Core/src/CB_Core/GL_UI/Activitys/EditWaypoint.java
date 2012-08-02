package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.Enums.CacheTypes;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CoordinateButton;
import CB_Core.GL_UI.Controls.CoordinateButton.CoordinateChangeListner;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Spinner;
import CB_Core.GL_UI.Controls.Spinner.selectionChangedListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.CB_TextField;
import CB_Core.GL_UI.libGdx_Controls.CB_WrappedTextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.TranslationEngine.LangStrings;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.scenes.scene2d.ui.TextField.DefaultOnscreenKeyboard;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.OnscreenKeyboard;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;

public class EditWaypoint extends ActivityBase
{

	private Waypoint waypoint;
	private CoordinateButton bCoord = null;
	private Spinner sType = null;
	private Button bOK = null;
	private Button bCancel = null;
	private Label tvCacheName = null;
	private Label tvTyp = null;
	private Label tvTitle = null;
	private CB_TextField etTitle = null;
	private Label tvDescription = null;
	private CB_WrappedTextField etDescription = null;
	private Label tvClue = null;
	private CB_WrappedTextField etClue = null;
	private Boolean firstShow = true;

	private Box scrollBox;

	public interface ReturnListner
	{
		public void returnedWP(Waypoint wp);
	}

	private ReturnListner mReturnListner;

	public EditWaypoint(CB_RectF rec, String Name, Waypoint waypoint, ReturnListner listner)
	{
		super(rec, Name);
		scrollBox = new Box(rec, Name);
		this.addChild(scrollBox);
		this.waypoint = waypoint;
		this.mReturnListner = listner;

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
				for (CB_TextField tmp : allTextFields)
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
		GL_Listener.glListener.renderForTextField(etTitle);
	}

	private void iniCacheNameLabel()
	{
		tvCacheName = new Label(Left + margin, height - Top - MesuredLabelHeight, width - Left - Right - margin, MesuredLabelHeight,
				"CacheNameLabel");
		tvCacheName.setFont(Fonts.getBubbleNormal());
		tvCacheName.setText(GlobalCore.SelectedCache().Name);
		scrollBox.addChild(tvCacheName);
	}

	private void iniCoordButton()
	{
		CB_RectF rec = new CB_RectF(Left, tvCacheName.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
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
		tvTyp = new Label(Left + margin, bCoord.getY() - margin - MesuredLabelHeight, width - Left - Right - margin, MesuredLabelHeight,
				"TypeLabel");
		tvTyp.setFont(Fonts.getBubbleNormal());
		tvTyp.setText(GlobalCore.Translations.Get("type"));
		scrollBox.addChild(tvTyp);

	}

	private void iniTypeSpinner()
	{
		CB_RectF rec = new CB_RectF(Left, tvTyp.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
		sType = new Spinner(rec, "CoordButton", getWaypointTypes(), new selectionChangedListner()
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

	private String[] getWaypointTypes()
	{
		final LangStrings ls = GlobalCore.Translations;
		return new String[]
			{ ls.Get("Reference"), ls.Get("StageofMulti"), ls.Get("Question2Answer"), ls.Get("Trailhead"), ls.Get("Parking"),
					ls.Get("Final") };
	}

	private void iniLabelTitle()
	{
		tvTitle = new Label(Left + margin, sType.getY() - margin - MesuredLabelHeight, width - Left - Right - margin, MesuredLabelHeight,
				"TitleLabel");
		tvTitle.setFont(Fonts.getBubbleNormal());
		tvTitle.setText(GlobalCore.Translations.Get("Title"));
		scrollBox.addChild(tvTitle);
	}

	private void iniTitleTextField()
	{
		CB_RectF rec = new CB_RectF(Left, tvTitle.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
		etTitle = new CB_TextField(rec, "TitleTextField");

		String txt = (waypoint.Title == null) ? "" : waypoint.Title;

		etTitle.setText(txt);
		scrollBox.addChild(etTitle);
	}

	private void iniLabelDesc()
	{
		tvDescription = new Label(Left + margin, etTitle.getY() - margin - MesuredLabelHeight, width - Left - Right - margin,
				MesuredLabelHeight, "DescLabel");
		tvDescription.setFont(Fonts.getBubbleNormal());
		tvDescription.setText(GlobalCore.Translations.Get("Description"));
		scrollBox.addChild(tvDescription);
	}

	private void iniTitleTextDesc()
	{
		CB_RectF rec = new CB_RectF(Left, tvDescription.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
		etDescription = new CB_WrappedTextField(rec, "DescTextField");

		String txt = (waypoint.Description == null) ? "" : waypoint.Description;

		etDescription.setText(txt);

		etDescription.setTextChangedListner(new TextFieldListener()
		{

			@Override
			public void keyTyped(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char key)
			{
				layoutTextFields();
			}
		});

		scrollBox.addChild(etDescription);
	}

	private void iniLabelClue()
	{
		tvClue = new Label(Left + margin, etDescription.getY() - margin - MesuredLabelHeight, width - Left - Right - margin,
				MesuredLabelHeight, "ClueLabel");
		tvClue.setFont(Fonts.getBubbleNormal());
		tvClue.setText(GlobalCore.Translations.Get("Clue"));
		scrollBox.addChild(tvClue);
	}

	private void iniTitleTextClue()
	{
		CB_RectF rec = new CB_RectF(Left, tvClue.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
		etClue = new CB_WrappedTextField(rec, "ClueTextField");

		String txt = (waypoint.Clue == null) ? "" : waypoint.Clue;

		etClue.setText(txt);

		etClue.setTextChangedListner(new TextFieldListener()
		{

			@Override
			public void keyTyped(com.badlogic.gdx.scenes.scene2d.ui.TextField textField, char key)
			{
				layoutTextFields();
			}
		});

		scrollBox.addChild(etClue);
	}

	private void iniOkCancel()
	{
		CB_RectF btnRec = new CB_RectF(Left, Bottom, (width - Left - Right) / 2, UiSizes.getButtonHeight());
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

	private ArrayList<CB_TextField> allTextFields = new ArrayList<CB_TextField>();

	private OnscreenKeyboard keyboard = new DefaultOnscreenKeyboard();

	public void registerTextField(final CB_TextField textField)
	{
		textField.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean arg0)
			{

				for (CB_TextField tmp : allTextFields)
				{
					tmp.resetFocus();
				}

				textField.setFocus(true);
				keyboard.show(true);
				scrollToY(textField.getY(), textField.getMaxY());
			}
		});

		allTextFields.add(textField);
	}

	private void scrollToY(float y, float maxY)
	{
		if (y < this.halfHeight)// wird von softKeyboard verdeckt
		{
			scrollBox.setY(this.height - maxY - MesuredLabelHeight);
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
		float descriptionHeight = Math.min(maxTextFieldHeight, etDescription.getmesuredHeight() + rand);
		float clueHeight = Math.min(maxTextFieldHeight, etClue.getmesuredHeight() + rand);

		descriptionHeight = Math.max(descriptionHeight, UiSizes.getButtonHeight());
		clueHeight = Math.max(clueHeight, UiSizes.getButtonHeight());

		etDescription.setHeight(descriptionHeight);
		etClue.setHeight(clueHeight);

		etDescription.setY(tvDescription.getY() - descriptionHeight);

		tvClue.setY(etDescription.getY() - margin - MesuredLabelHeight);

		etClue.setY(tvClue.getY() - clueHeight);

	}

	@Override
	public void onShow()
	{
		// onShow switch to editCoord Dialog if this the first show
		if (firstShow) bCoord.performClick();
		firstShow = false;
	}

}

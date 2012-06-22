package CB_Core.GL_UI.Activitys;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CoordinateButton;
import CB_Core.GL_UI.Controls.CoordinateButton.CoordinateChangeListner;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.NumPad;
import CB_Core.GL_UI.Controls.NumPad.keyEventListner;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.scenes.scene2d.ui.TextField.OnscreenKeyboard;

public class ProjectionCoordinate extends ActivityBase
{
	private Coordinate cancelCoord;
	private Coordinate coord;
	private Coordinate projCoord;
	private TextField valueBearing = null;
	private Label lblBearing = null;
	private Label lblBearingUnit = null;
	private TextField valueDistance = null;
	private Label lblDistance = null;
	private Label lblDistanceUnit = null;
	private ReturnListner mReturnListner;
	private Label Title = null;
	private CoordinateButton bCoord = null;
	private Button bOK = null;
	private Button bCancel = null;
	private Boolean radius = false;
	private boolean ImperialUnits = false;
	private NumPad numPad;

	public interface ReturnListner
	{
		public void returnCoord(Coordinate coord);
	}

	public ProjectionCoordinate(CB_RectF rec, String Name, Coordinate Coord, ReturnListner listner, Boolean Radius)
	{
		super(rec, Name);
		coord = Coord;
		cancelCoord = Coord.copy();
		radius = Radius;

		ImperialUnits = Config.settings.ImperialUnits.getValue();

		iniCacheNameLabel();
		iniCoordButton();
		iniTextFields();
		iniOkCancel();
		iniNumPad();
	}

	private void iniCacheNameLabel()
	{
		CB_RectF rec = new CB_RectF(Left + margin, height - Top - MesuredLabelHeight, width - Left - Right - margin, MesuredLabelHeight);

		Title = new Label(rec, "CacheNameLabel");
		Title.setText(this.name);
		this.addChild(Title);
	}

	private void iniCoordButton()
	{
		CB_RectF rec = new CB_RectF(Left, Title.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
		bCoord = new CoordinateButton(rec, "CoordButton", coord);

		bCoord.setCoordinateChangedListner(new CoordinateChangeListner()
		{

			@Override
			public void coordinateChanged(Coordinate Coord)
			{
				that.show();
				coord = Coord;
			}
		});

		this.addChild(bCoord);
	}

	private void iniTextFields()
	{
		// mesure label width
		String sBearing = GlobalCore.Translations.Get("Bearing");
		String sDistance = radius ? "Radius" : GlobalCore.Translations.Get("Distance");
		String sUnit = ImperialUnits ? "yd" : "m";

		float wB = Fonts.Mesure(sBearing).width;
		float wD = Fonts.Mesure(sDistance).width;
		float wMax = Math.max(wB, wD);

		float y = bCoord.getY() - ButtonHeight;
		float eWidth = Fonts.Mesure(sUnit).width;
		CB_RectF labelRec = new CB_RectF(Left, y, wMax, ButtonHeight);
		CB_RectF textFieldRec = new CB_RectF(labelRec.getMaxX(), y, width - Left - Right - labelRec.getWidth() - eWidth - (margin * 2),
				ButtonHeight);
		CB_RectF UnitRec = new CB_RectF(textFieldRec.getMaxX(), y, eWidth, ButtonHeight);

		lblBearing = new Label(labelRec, "lblBearing");
		valueBearing = new TextField(textFieldRec, "valueBearing");
		lblBearingUnit = new Label(UnitRec, "lblBearingEinheit");

		labelRec.setY(lblBearing.getY() - ButtonHeight);
		textFieldRec.setY(lblBearing.getY() - ButtonHeight);
		UnitRec.setY(lblBearing.getY() - ButtonHeight);

		lblDistance = new Label(labelRec, "lblBearing");
		valueDistance = new TextField(textFieldRec, "valueBearing");
		lblDistanceUnit = new Label(UnitRec, "lblBearingEinheit");

		lblBearing.setText(sBearing);
		lblDistance.setText(sDistance);
		valueDistance.setText("0");
		valueBearing.setText("0");

		lblBearingUnit.setText("°");
		lblDistanceUnit.setText(sUnit);

		this.addChild(lblBearing);
		this.addChild(lblDistance);
		this.addChild(valueDistance);
		this.addChild(valueBearing);
		this.addChild(lblBearingUnit);
		this.addChild(lblDistanceUnit);

		setKeyboardHandling(valueDistance);
		setKeyboardHandling(valueBearing);

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
				if (!parseView()) return true;
				if (mReturnListner != null) mReturnListner.returnCoord(projCoord);
				finish();
				return true;
			}
		});

		bCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null) mReturnListner.returnCoord(cancelCoord);
				finish();
				return true;
			}
		});

	}

	private void iniNumPad()
	{
		CB_RectF numRec = new CB_RectF(Left, bOK.getMaxY(), width - Left - Right, lblDistance.getY() - bOK.getMaxY());
		numPad = new NumPad(numRec, "numPad", NumPad.Type.withDot, keyListner);

		this.addChild(numPad);
	}

	private boolean parseView()
	{

		double Bearing = Double.parseDouble(valueBearing.getText().toString());
		double Distance = Double.parseDouble(valueDistance.getText().toString());

		if (ImperialUnits) Distance *= 0.9144f;

		Coordinate newCoord = Coordinate.Project(coord.Latitude, coord.Longitude, Bearing, Distance);

		if (newCoord.Valid)
		{
			projCoord = newCoord;
			return true;
		}
		else
			return false;
	}

	private ArrayList<TextField> allTextFields = new ArrayList<TextField>();
	private TextField focusedTextField = null;

	private void setKeyboardHandling(final TextField textField)
	{
		textField.setOnscreenKeyboard(new OnscreenKeyboard()
		{
			@Override
			public void show(boolean arg0)
			{

				for (TextField tmp : allTextFields)
				{
					tmp.resetFocus();
				}

				textField.setFocus(true);
				focusedTextField = textField;
			}
		});

		allTextFields.add(textField);
	}

	keyEventListner keyListner = new keyEventListner()
	{

		@Override
		public void KeyPressed(String value)
		{
			if (focusedTextField == null) return;

			int cursorPos = focusedTextField.getCursorPosition();

			if (value.equals("O"))
			{
				// sollte nicht passieren, da der Button nicht sichtbar ist
			}
			else if (value.equals("C"))
			{
				// sollte nicht passieren, da der Button nicht sichtbar ist
			}
			else if (value.equals("<"))
			{
				if (cursorPos == 0) cursorPos = 1; // cursorPos darf nicht 0 sein
				focusedTextField.setCursorPosition(cursorPos - 1);
			}
			else if (value.equals(">"))
			{
				focusedTextField.setCursorPosition(cursorPos + 1);
			}
			else if (value.equals("D"))
			{
				if (cursorPos > 0)
				{
					String text2 = focusedTextField.getText().substring(cursorPos);
					String text1 = focusedTextField.getText().substring(0, cursorPos - 1);

					focusedTextField.setText(text1 + text2);
					focusedTextField.setCursorPosition(cursorPos + -1);
				}
			}
			else
			{
				String text2 = focusedTextField.getText().substring(cursorPos);
				String text1 = focusedTextField.getText().substring(0, cursorPos);

				focusedTextField.setText(text1 + value + text2);
				focusedTextField.setCursorPosition(cursorPos + value.length());
			}

		}
	};

}

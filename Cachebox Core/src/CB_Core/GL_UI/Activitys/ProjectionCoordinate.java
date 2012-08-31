package CB_Core.GL_UI.Activitys;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.CoordinateButton;
import CB_Core.GL_UI.Controls.CoordinateButton.CoordinateChangeListner;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.NumPad;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;

public class ProjectionCoordinate extends ActivityBase
{
	private Coordinate cancelCoord;
	private Coordinate coord;
	private Coordinate projCoord;

	private double Bearing;
	private double Distance;

	private EditWrapedTextField valueBearing = null;
	private Label lblBearing = null;
	private Label lblP2P = null;
	private Label lblBearingUnit = null;
	private EditWrapedTextField valueDistance = null;
	private Label lblDistance = null;
	private Label lblDistanceUnit = null;

	private Label Title = null;
	private CoordinateButton bCoord = null;
	private CoordinateButton bCoord2 = null;
	private Button bOK = null;
	private Button bCancel = null;
	private Boolean radius = false;
	private Boolean p2p = false;
	private boolean ImperialUnits = false;
	private NumPad numPad;

	private ReturnListner mReturnListner;

	public enum Type
	{
		projetion, circle, p2p
	}

	public interface ReturnListner
	{
		/**
		 * Return from ProjectionCoordinate Dialog
		 * 
		 * @param targetCoord
		 * @param startCoord
		 * @param Bearing
		 * @param distance
		 */
		public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance);
	}

	public ProjectionCoordinate(CB_RectF rec, String Name, Coordinate Coord, ReturnListner listner, Type type)
	{
		super(rec, Name);
		coord = Coord;
		cancelCoord = Coord.copy();
		radius = (type == Type.circle);
		p2p = (type == Type.p2p);
		mReturnListner = listner;
		ImperialUnits = Config.settings.ImperialUnits.getValue();

		if (p2p) projCoord = Coord.copy();

		iniCacheNameLabel();
		iniCoordButton();
		if (p2p) iniCoordButton2();
		if (!p2p) iniTextFields();
		iniOkCancel();
		if (!p2p) iniNumPad();
	}

	@Override
	protected void Initial()
	{
		GL.that.renderForTextField(valueDistance);
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

	private void iniCoordButton2()
	{

		CB_RectF labelRec = new CB_RectF(Left + margin, bCoord.getY() - ButtonHeight - MesuredLabelHeight, this.width - Left - Right,
				MesuredLabelHeight);

		lblP2P = new Label(labelRec, "lblBearing");
		lblP2P.setText(GlobalCore.Translations.Get("toPoint"));
		this.addChild(lblP2P);

		CB_RectF rec = new CB_RectF(Left, lblP2P.getY() - UiSizes.getButtonHeight(), width - Left - Right, UiSizes.getButtonHeight());
		bCoord2 = new CoordinateButton(rec, "CoordButton2", projCoord);

		bCoord2.setCoordinateChangedListner(new CoordinateChangeListner()
		{

			@Override
			public void coordinateChanged(Coordinate Coord)
			{
				that.show();
				projCoord = Coord;
			}
		});

		this.addChild(bCoord2);
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
		valueBearing = new EditWrapedTextField(this, textFieldRec, "valueBearing");
		lblBearingUnit = new Label(UnitRec, "lblBearingEinheit");

		labelRec.setY(lblBearing.getY() - ButtonHeight);
		textFieldRec.setY(lblBearing.getY() - ButtonHeight);
		UnitRec.setY(lblBearing.getY() - ButtonHeight);

		lblDistance = new Label(labelRec, "lblBearing");
		valueDistance = new EditWrapedTextField(this, textFieldRec, "valueBearing");
		lblDistanceUnit = new Label(UnitRec, "lblBearingEinheit");

		lblBearing.setText(sBearing);
		lblDistance.setText(sDistance);
		valueDistance.setText("0");
		valueBearing.setText("0");

		lblBearingUnit.setText("°");
		lblDistanceUnit.setText(sUnit);

		if (!radius) this.addChild(lblBearing);
		this.addChild(lblDistance);
		this.addChild(valueDistance);
		if (!radius) this.addChild(valueBearing);
		if (!radius) this.addChild(lblBearingUnit);
		this.addChild(lblDistanceUnit);

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
				if (mReturnListner != null) mReturnListner.returnCoord(projCoord, coord, Bearing, Distance);
				finish();
				return true;
			}
		});

		bCancel.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (mReturnListner != null) mReturnListner.returnCoord(null, null, 0, 0);
				finish();
				return true;
			}
		});

	}

	private void iniNumPad()
	{
		CB_RectF numRec = new CB_RectF(Left, bOK.getMaxY(), width - Left - Right, lblDistance.getY() - bOK.getMaxY());
		numPad = new NumPad(numRec, "numPad", NumPad.Type.withDot);

		numPad.registerTextField(valueDistance);
		numPad.registerTextField(valueBearing);

		this.addChild(numPad);
	}

	private boolean parseView()
	{

		if (p2p)
		{
			try
			{
				Distance = coord.Distance(projCoord);
				Bearing = coord.bearingTo(projCoord); // TODO chk -180°
				return true;
			}
			catch (Exception e)
			{
				return false;
			}
		}
		else
		{
			Bearing = Double.parseDouble(valueBearing.getText().toString());
			Distance = Double.parseDouble(valueDistance.getText().toString());

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

	}
}

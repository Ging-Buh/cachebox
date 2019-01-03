package CB_UI.GL_UI.Activitys;

import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.CB_UI_Settings;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI.GL_UI.Controls.CoordinateButton.ICoordinateChangedListener;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.IBecomesFocus;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.NumPad;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.MathUtils.CalculationType;

public class ProjectionCoordinate extends ActivityBase {
    private final String wpName;
    private final ICoordReturnListener mCoordReturnListener;
    private Coordinate coord;
    private Coordinate projCoord;
    private double Bearing;
    private double Distance;
    private EditTextField valueBearing = null;
    private Label lblBearing = null;
    private Label lblP2P = null;
    private Label lblBearingUnit = null;
    private EditTextField valueDistance = null;
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

    public ProjectionCoordinate(CB_RectF rec, String Name, Coordinate coord2, ICoordReturnListener listener, Type type, String WP_Name) {
        super(rec, Name);
        coord = coord2;
        wpName = WP_Name;
        radius = (type == Type.circle);
        p2p = (type == Type.p2p);
        mCoordReturnListener = listener;
        ImperialUnits = CB_UI_Settings.ImperialUnits.getValue();

        if (p2p)
            projCoord = coord2.copy();

        iniCacheNameLabel();
        iniCoordButton();
        if (p2p)
            iniCoordButton2();
        if (!p2p)
            iniTextFields();
        iniOkCancel();
        if (!p2p)
            iniNumPad();
    }

    private void iniCacheNameLabel() {
        CB_RectF rec = new CB_RectF(leftBorder + margin, getHeight() - this.getTopHeight() - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        Title = new Label(this.name + " Title", rec);
        this.addChild(Title);
    }

    @Override
    public void onShow() {
        if (valueBearing == null) {
            if (!p2p) {
                iniTextFields();
                valueBearing.setFocus(true);
            }
        } else {
            valueBearing.setFocus(true);
        }

    }

    private void iniCoordButton() {
        CB_RectF rec = new CB_RectF(leftBorder, Title.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth, UI_Size_Base.that.getButtonHeight());
        bCoord = new CoordinateButton(rec, "CoordButton", coord, wpName);

        bCoord.setCoordinateChangedListener(new ICoordinateChangedListener() {

            @Override
            public void coordinateChanged(Coordinate Coord) {
                ProjectionCoordinate.this.show();
                coord = Coord;
            }
        });

        this.addChild(bCoord);
    }

    private void iniCoordButton2() {

        CB_RectF labelRec = new CB_RectF(leftBorder + margin, bCoord.getY() - ButtonHeight - MeasuredLabelHeight, innerWidth, MeasuredLabelHeight);

        lblP2P = new Label(this.name + " lblP2P", labelRec, Translation.Get("toPoint"));
        this.addChild(lblP2P);

        CB_RectF rec = new CB_RectF(leftBorder, lblP2P.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth, UI_Size_Base.that.getButtonHeight());
        bCoord2 = new CoordinateButton(rec, "CoordButton2", projCoord, null);

        bCoord2.setCoordinateChangedListener(new ICoordinateChangedListener() {

            @Override
            public void coordinateChanged(Coordinate Coord) {
                ProjectionCoordinate.this.show();
                projCoord = Coord;
            }
        });

        this.addChild(bCoord2);
    }

    private void iniTextFields() {
        // measure label width
        String sBearing = Translation.Get("Bearing");
        String sDistance = radius ? "Radius" : Translation.Get("Distance");
        String sUnit = ImperialUnits ? "yd" : "m";

        float wB = Fonts.Measure(sBearing).width;
        float wD = Fonts.Measure(sDistance).width;
        float wMax = Math.max(wB, wD);

        float y = bCoord.getY() - ButtonHeight;
        float eWidth = Fonts.Measure(sUnit).width;
        CB_RectF labelRec = new CB_RectF(leftBorder, y, wMax, ButtonHeight);
        CB_RectF textFieldRec = new CB_RectF(labelRec.getMaxX(), y, innerWidth - labelRec.getWidth() - eWidth - (margin * 2), ButtonHeight);
        CB_RectF UnitRec = new CB_RectF(textFieldRec.getMaxX(), y, eWidth, ButtonHeight);

        lblBearing = new Label(this.name + " lblBearing", labelRec, sBearing);
        valueBearing = new EditTextField(textFieldRec, this, "*" + sBearing);
        valueBearing.disableKeyboardPopup();
        lblBearingUnit = new Label(this.name + " lblBearingUnit", UnitRec, "°");

        labelRec.setY(lblBearing.getY() - ButtonHeight);
        textFieldRec.setY(lblBearing.getY() - ButtonHeight);
        UnitRec.setY(lblBearing.getY() - ButtonHeight);

        lblDistance = new Label(this.name + " lblDistance", labelRec, sDistance);
        valueDistance = new EditTextField(textFieldRec, this, "*" + sDistance);
        valueDistance.disableKeyboardPopup();
        lblDistanceUnit = new Label(this.name + " lblDistanceUnit", UnitRec, sUnit);

        valueDistance.setText("0");
        valueBearing.setText("0");

        if (!radius)
            this.addChild(lblBearing);
        this.addChild(lblDistance);
        this.addChild(valueDistance);
        if (!radius)
            this.addChild(valueBearing);
        if (!radius)
            this.addChild(lblBearingUnit);
        this.addChild(lblDistanceUnit);

        valueDistance.setBecomesFocusListener(new IBecomesFocus() {

            @Override
            public void becomesFocus() {
                numPad.registerTextField(valueDistance);
                GL.that.RunOnGL(new IRunOnGL() {
                    @Override
                    public void run() {
                        int textLength = valueDistance.getText().length();
                        valueDistance.setSelection(0, textLength);
                    }
                });
            }
        });

        valueBearing.setBecomesFocusListener(new IBecomesFocus() {

            @Override
            public void becomesFocus() {
                if (numPad != null)
                    numPad.registerTextField(valueBearing);
                GL.that.RunOnGL(new IRunOnGL() {

                    @Override
                    public void run() {
                        int textLength = valueBearing.getText().length();
                        valueBearing.setSelection(0, textLength);
                    }
                });

            }
        });

    }

    private void iniOkCancel() {
        CB_RectF btnRec = new CB_RectF(leftBorder, this.getBottomHeight(), innerWidth / 2, UI_Size_Base.that.getButtonHeight());
        bOK = new Button(btnRec, "OkButton");

        btnRec.setX(bOK.getMaxX());
        bCancel = new Button(btnRec, "CancelButton");

        bOK.setText(Translation.Get("ok"));
        bCancel.setText(Translation.Get("cancel"));

        this.addChild(bOK);
        this.addChild(bCancel);

        bOK.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (!parseView())
                    return true;
                if (mCoordReturnListener != null)
                    mCoordReturnListener.returnCoord(projCoord, coord, Bearing, Distance);
                finish();
                return true;
            }
        });

        bCancel.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mCoordReturnListener != null)
                    mCoordReturnListener.returnCoord(null, null, 0, 0);
                finish();
                return true;
            }
        });

    }

    private void iniNumPad() {
        CB_RectF numRec = new CB_RectF(leftBorder, bOK.getMaxY(), innerWidth, lblDistance.getY() - bOK.getMaxY());
        numPad = new NumPad(numRec, "numPad", NumPad.NumPadType.withDot);
        this.addChild(numPad);
    }

    private boolean parseView() {

        if (p2p) {
            try {
                Distance = coord.Distance(projCoord, CalculationType.ACCURATE);
                Bearing = coord.bearingTo(projCoord, CalculationType.ACCURATE);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            Bearing = Double.parseDouble(valueBearing.getText().toString());
            Distance = Double.parseDouble(valueDistance.getText().toString());

            if (ImperialUnits)
                Distance *= 0.9144f;

            Coordinate newCoord = Coordinate.Project(coord.getLatitude(), coord.getLongitude(), Bearing, Distance);

            if (newCoord.isValid()) {
                projCoord = newCoord;
                return true;
            } else
                return false;
        }

    }

    public enum Type {
        projetion, circle, p2p
    }

    public interface ICoordReturnListener {
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
}

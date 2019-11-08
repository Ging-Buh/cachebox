package de.droidcachebox.gdx.activities;

import de.droidcachebox.CB_UI_Settings;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils.CalculationType;

public class ProjectionCoordinate extends ActivityBase {
    private final String wpName;
    private final ICoordReturnListener mCoordReturnListener;
    private Coordinate coord;
    private Coordinate projCoord;
    private double Bearing;
    private double Distance;
    private EditTextField valueBearing = null;
    private EditTextField valueDistance = null;
    private CB_Label lblDistance = null;
    private CB_Label Title = null;
    private CoordinateButton bCoord = null;
    private CB_Button bOK = null;
    private Boolean radius;
    private Boolean p2p;
    private boolean ImperialUnits;
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
        Title = new CB_Label(rec);
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
        CB_RectF rec = new CB_RectF(leftBorder, Title.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        bCoord = new CoordinateButton(rec, "CoordButton", coord, wpName);

        bCoord.setCoordinateChangedListener(Coord -> {
            ProjectionCoordinate.this.show();
            coord = Coord;
        });

        this.addChild(bCoord);
    }

    private void iniCoordButton2() {

        CB_RectF labelRec = new CB_RectF(leftBorder + margin, bCoord.getY() - ButtonHeight - MeasuredLabelHeight, innerWidth, MeasuredLabelHeight);

        CB_Label lblP2P = new CB_Label(this.name + " lblP2P", labelRec, Translation.get("toPoint"));
        this.addChild(lblP2P);

        CB_RectF rec = new CB_RectF(leftBorder, lblP2P.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        CoordinateButton bCoord2 = new CoordinateButton(rec, "CoordButton2", projCoord, null);

        bCoord2.setCoordinateChangedListener(Coord -> {
            ProjectionCoordinate.this.show();
            projCoord = Coord;
        });

        this.addChild(bCoord2);
    }

    private void iniTextFields() {
        // measure label width
        String sBearing = Translation.get("Bearing");
        String sDistance = radius ? "Radius" : Translation.get("Distance");
        String sUnit = ImperialUnits ? "yd" : "m";

        float wB = Fonts.Measure(sBearing).width;
        float wD = Fonts.Measure(sDistance).width;
        float wMax = Math.max(wB, wD);

        float y = bCoord.getY() - ButtonHeight;
        float eWidth = Fonts.Measure(sUnit).width;
        CB_RectF labelRec = new CB_RectF(leftBorder, y, wMax, ButtonHeight);
        CB_RectF textFieldRec = new CB_RectF(labelRec.getMaxX(), y, innerWidth - labelRec.getWidth() - eWidth - (margin * 2), ButtonHeight);
        CB_RectF UnitRec = new CB_RectF(textFieldRec.getMaxX(), y, eWidth, ButtonHeight);

        CB_Label lblBearing = new CB_Label(this.name + " lblBearing", labelRec, sBearing);
        valueBearing = new EditTextField(textFieldRec, this, "*" + sBearing);
        valueBearing.disableKeyboardPopup();
        CB_Label lblBearingUnit = new CB_Label(this.name + " lblBearingUnit", UnitRec, "°");

        labelRec.setY(lblBearing.getY() - ButtonHeight);
        textFieldRec.setY(lblBearing.getY() - ButtonHeight);
        UnitRec.setY(lblBearing.getY() - ButtonHeight);

        lblDistance = new CB_Label(this.name + " lblDistance", labelRec, sDistance);
        valueDistance = new EditTextField(textFieldRec, this, "*" + sDistance);
        valueDistance.disableKeyboardPopup();
        CB_Label lblDistanceUnit = new CB_Label(this.name + " lblDistanceUnit", UnitRec, sUnit);

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

        valueDistance.setBecomesFocusListener(() -> {
            numPad.registerTextField(valueDistance);
            GL.that.RunOnGL(() -> {
                int textLength = valueDistance.getText().length();
                valueDistance.setSelection(0, textLength);
            });
        });

        valueBearing.setBecomesFocusListener(() -> {
            if (numPad != null)
                numPad.registerTextField(valueBearing);
            GL.that.RunOnGL(() -> {
                int textLength = valueBearing.getText().length();
                valueBearing.setSelection(0, textLength);
            });

        });

    }

    private void iniOkCancel() {
        CB_RectF btnRec = new CB_RectF(leftBorder, this.getBottomHeight(), innerWidth / 2, UiSizes.getInstance().getButtonHeight());
        bOK = new CB_Button(btnRec, "OkButton");

        btnRec.setX(bOK.getMaxX());
        CB_Button bCancel = new CB_Button(btnRec, "CancelButton");

        bOK.setText(Translation.get("ok"));
        bCancel.setText(Translation.get("cancel"));

        this.addChild(bOK);
        this.addChild(bCancel);

        bOK.addClickHandler((v, x, y, pointer, button) -> {
            if (!parseView())
                return true;
            if (mCoordReturnListener != null)
                mCoordReturnListener.returnCoord(projCoord, coord, Bearing, Distance);
            finish();
            return true;
        });

        bCancel.addClickHandler((v, x, y, pointer, button) -> {
            if (mCoordReturnListener != null)
                mCoordReturnListener.returnCoord(null, null, 0, 0);
            finish();
            return true;
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
            Bearing = Double.parseDouble(valueBearing.getText());
            Distance = Double.parseDouble(valueDistance.getText());

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
         * @param targetCoord targetCoord
         * @param startCoord  startCoord
         * @param Bearing     Bearing
         * @param distance    distance
         */
        void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance);
    }
}
package de.droidcachebox.gdx.activities;

import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CoordinateButton;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.NumPad;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils.CalculationType;

/**
 * input gui for different ProjectionTypes
 */
public class ProjectionCoordinate extends ActivityBase {
    private final String coordinateButtonText;
    private final ICoordReturnListener coordinateReturnListener;
    private ProjectionType projectionType;
    private Coordinate fromCoordinate, projectedCoordinate;
    private double bearing;
    private double distance;
    private boolean imperialUnits;
    private NumPad numPad;
    private EditTextField valueBearing = null;
    private EditTextField valueDistance = null;
    private CB_Label lblDistance = null;
    private CB_Label title = null;
    private CoordinateButton btnCoordinate = null;
    private CB_Button btnOK = null;

    public ProjectionCoordinate(String name, Coordinate from, ICoordReturnListener theCoordinateReturnListener, ProjectionType wantedProjectionType, String fromText) {
        super(name);
        fromCoordinate = from;
        coordinateButtonText = fromText;
        projectionType = wantedProjectionType;
        coordinateReturnListener = theCoordinateReturnListener;
        imperialUnits = Settings.ImperialUnits.getValue();
        if (projectionType == ProjectionType.point2point)
            projectedCoordinate = from.copy();
        iniCacheNameLabel();
        iniCoordButton();
        if (projectionType == ProjectionType.point2point)
            iniCoordButton2();
        else
            iniTextFields();
        iniOkCancel();
        if (projectionType != ProjectionType.point2point)
            iniNumPad();
    }

    private void iniCacheNameLabel() {
        CB_RectF rec = new CB_RectF(leftBorder + margin, getHeight() - getTopHeight() - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        title = new CB_Label(rec);
        addChild(title);
    }

    @Override
    public void onShow() {
        if (valueBearing == null) {
            if (projectionType != ProjectionType.point2point) {
                iniTextFields();
                valueBearing.setFocus(true);
            }
        } else {
            valueBearing.setFocus(true);
        }

    }

    private void iniCoordButton() {
        CB_RectF rec = new CB_RectF(leftBorder, title.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        btnCoordinate = new CoordinateButton(rec, "CoordButton", fromCoordinate, coordinateButtonText);

        btnCoordinate.setCoordinateChangedListener(Coord -> {
            activityBase.show();
            fromCoordinate = Coord;
        });

        addChild(btnCoordinate);
    }

    private void iniCoordButton2() {

        CB_RectF labelRec = new CB_RectF(leftBorder + margin, btnCoordinate.getY() - ButtonHeight - MeasuredLabelHeight, innerWidth, MeasuredLabelHeight);

        CB_Label lblP2P = new CB_Label(name + " lblP2P", labelRec, Translation.get("toPoint"));
        addChild(lblP2P);

        CB_RectF rec = new CB_RectF(leftBorder, lblP2P.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        CoordinateButton bCoord2 = new CoordinateButton(rec, "CoordButton2", projectedCoordinate, null);

        bCoord2.setCoordinateChangedListener(Coord -> {
            activityBase.show();
            projectedCoordinate = Coord;
        });

        addChild(bCoord2);
    }

    private void iniTextFields() {
        // measure label width
        String sBearing = Translation.get("Bearing");
        String sDistance = projectionType == ProjectionType.circle ? "Radius" : Translation.get("Distance");
        String sUnit = imperialUnits ? "yd" : "m";

        float wB = Fonts.Measure(sBearing).width;
        float wD = Fonts.Measure(sDistance).width;
        float wMax = Math.max(wB, wD);

        float y = btnCoordinate.getY() - ButtonHeight;
        float eWidth = Fonts.Measure(sUnit).width;
        CB_RectF labelRec = new CB_RectF(leftBorder, y, wMax, ButtonHeight);
        CB_RectF textFieldRec = new CB_RectF(labelRec.getMaxX(), y, innerWidth - labelRec.getWidth() - eWidth - (margin * 2), ButtonHeight);
        CB_RectF UnitRec = new CB_RectF(textFieldRec.getMaxX(), y, eWidth, ButtonHeight);

        CB_Label lblBearing = new CB_Label(name + " lblBearing", labelRec, sBearing);
        valueBearing = new EditTextField(textFieldRec, this, "*" + sBearing);
        valueBearing.disableKeyboardPopup();
        CB_Label lblBearingUnit = new CB_Label(name + " lblBearingUnit", UnitRec, "°");

        labelRec.setY(lblBearing.getY() - ButtonHeight);
        textFieldRec.setY(lblBearing.getY() - ButtonHeight);
        UnitRec.setY(lblBearing.getY() - ButtonHeight);

        lblDistance = new CB_Label(name + " lblDistance", labelRec, sDistance);
        valueDistance = new EditTextField(textFieldRec, this, "*" + sDistance);
        valueDistance.disableKeyboardPopup();
        CB_Label lblDistanceUnit = new CB_Label(name + " lblDistanceUnit", UnitRec, sUnit);

        valueDistance.setText("0");
        valueBearing.setText("0");

        if (projectionType != ProjectionType.circle)
            addChild(lblBearing);
        addChild(lblDistance);
        addChild(valueDistance);
        if (projectionType != ProjectionType.circle)
            addChild(valueBearing);
        if (projectionType != ProjectionType.circle)
            addChild(lblBearingUnit);
        addChild(lblDistanceUnit);

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
        CB_RectF btnRec = new CB_RectF(leftBorder, getBottomHeight(), innerWidth / 2, UiSizes.getInstance().getButtonHeight());
        btnOK = new CB_Button(btnRec, "OkButton");

        btnRec.setX(btnOK.getMaxX());
        CB_Button bCancel = new CB_Button(btnRec, "CancelButton");

        btnOK.setText(Translation.get("ok"));
        bCancel.setText(Translation.get("cancel"));

        addChild(btnOK);
        addChild(bCancel);

        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            if (!parseInput())
                return true;
            if (coordinateReturnListener != null)
                coordinateReturnListener.returnCoord(projectedCoordinate, fromCoordinate, bearing, distance);
            finish();
            return true;
        });

        bCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (coordinateReturnListener != null)
                coordinateReturnListener.returnCoord(null, null, 0, 0);
            finish();
            return true;
        });

    }

    private void iniNumPad() {
        CB_RectF numRec = new CB_RectF(leftBorder, btnOK.getMaxY(), innerWidth, lblDistance.getY() - btnOK.getMaxY());
        numPad = new NumPad(numRec, "numPad", NumPad.NumPadType.withDot);
        addChild(numPad);
    }

    private boolean parseInput() {

        if (projectionType == ProjectionType.point2point) {
            try {
                distance = fromCoordinate.distance(projectedCoordinate, CalculationType.ACCURATE);
                bearing = fromCoordinate.bearingTo(projectedCoordinate, CalculationType.ACCURATE);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            bearing = Double.parseDouble(valueBearing.getText());
            distance = Double.parseDouble(valueDistance.getText());

            if (imperialUnits)
                distance *= 0.9144f;

            Coordinate newCoord = Coordinate.project(fromCoordinate.getLatitude(), fromCoordinate.getLongitude(), bearing, distance);

            if (newCoord.isValid()) {
                projectedCoordinate = newCoord;
                return true;
            } else
                return false;
        }

    }

    public enum ProjectionType {
        projection, circle, point2point
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

package de.droidcachebox.solver;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils.CalculationType;

public class FunctionBearing extends Function {

    private static final long serialVersionUID = -85879423478038052L;

    public FunctionBearing(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Bearing", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncBearing");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescBearing");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 2) {
            return Translation.get("solverErrParamCount", "2", "$solverFuncBearing");
        }
        Coordinate[] coord = new Coordinate[2];
        for (int i = 0; i < 2; i++) {
            coord[i] = new CoordinateGPS(parameter[i]);
            if (!coord[i].isValid())
                return Translation.get("solverErrParamType", "$solverFuncBearing", String.valueOf(i + 1), "$coordinate", "$coordinate", parameter[i]);
        }
        try {
            double bearing = CoordinateGPS.Bearing(CalculationType.ACCURATE, coord[0], coord[1]);
            if (bearing < 0)
                bearing = bearing + 360;
            return String.valueOf(bearing);
        } catch (Exception ex) {
            return Translation.get("StdError", "$solverFuncBearing", ex.getMessage(), coord[0].formatCoordinate() + " -> " + coord[1].formatCoordinate());
        }
    }

    @Override
    public int getAnzParam() {
        return 2;
    }

    @Override
    public boolean needsTextArgument() {
        return false;
    }

    @Override
    public DataType getParamType(int i) {
        switch (i) {
            case 0:
                return DataType.Coordinate;
            case 1:
                return DataType.Coordinate;
            default:
                return DataType.None;
        }
    }

    @Override
    public DataType getReturnType() {
        return DataType.Float;
    }

    @Override
    public String getParamName(int i) {
        return "solverParamCoordinate";
    }

}
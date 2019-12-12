package de.droidcachebox.solver;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.solver.DataTypes.DataType;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;

public class FunctionDistance extends Function {

    private static final long serialVersionUID = -7861925988066369903L;

    public FunctionDistance(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Distance", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncDistance");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescDistance");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 2) {
            return Translation.get("solverErrParamCount", "2", "$solverFuncDistance");
        }
        Coordinate[] coord = new Coordinate[2];
        for (int i = 0; i < 2; i++) {
            coord[i] = new CoordinateGPS(parameter[i]);
            if (!coord[i].isValid())
                return Translation.get("solverErrParamType", "$solverFuncDistance", String.valueOf(i + 1), "$coordinate", "$coordinate", parameter[i]);
        }
        float[] dist = new float[2];
        try {
            MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, coord[0].getLatitude(), coord[0].getLongitude(), coord[1].getLatitude(), coord[1].getLongitude(), dist);
            return String.valueOf(dist[0]);
        } catch (Exception ex) {
            return Translation.get("StdError", "$solverFuncDistance", ex.getMessage(), coord[0].formatCoordinate() + " -> " + coord[1].formatCoordinate());
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
        switch (i) {
            case 0:
            case 1:
                return "solverParamCoordinate";
            default:
                return super.getParamName(i);
        }
    }
}
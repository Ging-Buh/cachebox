package de.droidcachebox.solver;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils.CalculationType;

public class FunctionCrossbearing extends Function {

    private static final long serialVersionUID = 4233730654010706806L;

    public FunctionCrossbearing(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Crossbearing", "en"));
        Names.add(new LocalNames("Kreuzpeilung", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncCrossbearing");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescCrossbearing");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 4) {
            return Translation.get("solverErrParamCount", "4", "$solverFuncBearing");
        }
        Coordinate[] coord = new Coordinate[2];
        double[] angle = new double[2];
        for (int i = 0; i < 2; i++) {
            coord[i] = new CoordinateGPS(parameter[i * 2]);
            if (!coord[i].isValid())
                return Translation.get("solverErrParamType", "$solverFuncCrossbearing", String.valueOf(i * 2 + 1), "$coordinate", "$coordinate", parameter[i * 2]);
            try {
                angle[i] = Double.valueOf(parameter[i * 2 + 1]);
            } catch (Exception ex) {
                return Translation.get("solverErrParamType", "$solverFuncCrossbearing", String.valueOf(i * 2 + 2), "$angle", "$number", parameter[i * 2 + 1]);
            }
        }

        try {
            return CoordinateGPS.Crossbearing(CalculationType.ACCURATE, coord[0], angle[0], coord[1], angle[1]).formatCoordinate();
        } catch (Exception ex) {
            return Translation.get("StdError", "$solverFuncCrossbearing", ex.getMessage(), coord[0].formatCoordinate() + " -> " + coord[1].formatCoordinate());
        }
    }

    @Override
    public int getAnzParam() {
        return 4;
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
                return DataType.Float;
            case 2:
                return DataType.Coordinate;
            case 3:
                return DataType.Float;
            default:
                return DataType.None;
        }
    }

    @Override
    public DataType getReturnType() {
        return DataType.Coordinate;
    }

    @Override
    public String getParamName(int i) {
        switch (i) {
            case 0:
            case 2:
                return "solverParamCoordinate";
            case 1:
            case 3:
                return "solverParamAngle";
            default:
                return super.getParamName(i);
        }
    }
}
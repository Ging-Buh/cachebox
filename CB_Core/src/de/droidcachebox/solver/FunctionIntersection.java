package de.droidcachebox.solver;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;

public class FunctionIntersection extends Function {

    private static final long serialVersionUID = 8472007835430135995L;

    public FunctionIntersection(SolverLines solverLines) {
        super(solverLines);
        Names.add(new LocalNames("Intersection", "en"));
        Names.add(new LocalNames("Schnittpunkt", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncIntersection");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescIntersection");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 4) {
            return Translation.get("solverErrParamCount", "4", "$solverFuncIntersection");
        }
        Coordinate[] coord = new Coordinate[4];
        for (int i = 0; i < 4; i++) {
            coord[i] = new CoordinateGPS(parameter[i]);
            if (!coord[i].isValid())
                return Translation.get("solverErrParamType", "$solverFuncIntersection", String.valueOf(i + 1), "$coordinate", "$coordinate", parameter[i]);
        }
        try {
            return CoordinateGPS.Intersection(coord[0], coord[1], coord[2], coord[3]).formatCoordinate();
        } catch (Exception ex) {
            String s = coord[0].formatCoordinate() + " / " + coord[1].formatCoordinate() + " -> " + coord[2].formatCoordinate() + " / " + coord[3].formatCoordinate();
            return Translation.get("StdError", "$solverFuncIntersection", ex.getMessage(), s);
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
                return DataType.Coordinate;
            case 2:
                return DataType.Coordinate;
            case 3:
                return DataType.Coordinate;
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
            case 1:
            case 2:
            case 3:
                return "solverParamCoordinate";
            default:
                return super.getParamName(i);
        }
    }

}
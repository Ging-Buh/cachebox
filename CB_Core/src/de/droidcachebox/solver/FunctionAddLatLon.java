package de.droidcachebox.solver;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;

public class FunctionAddLatLon extends Function {

    private static final long serialVersionUID = -6013883020785631158L;

    public FunctionAddLatLon(SolverLines solverLines) {
        super(solverLines);
        Names.add(new LocalNames("AddLatLon", "en"));
        Names.add(new LocalNames("AddLatLon", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncAddLatLon");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescAddLatLon");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 3) {
            String s = Translation.get("solverErrParamCount", "3", "$solverFuncAddLatLon");
            return s;
        }
        Coordinate coord = new CoordinateGPS(parameter[0]);
        if (!coord.isValid()) {
            return Translation.get("solverErrParamType", "$solverFuncAddLatLon", "1", "$coordinate", "$coordinate", parameter[0]);
        }
        int lat, lon;
        try {
            lat = Integer.parseInt(parameter[1]);
        } catch (Exception ex) {
            return Translation.get("solverErrParamType", "$solverFuncAddLatLon", "2", "$distance", "$number", parameter[1]);
        }
        try {
            lon = Integer.parseInt(parameter[2]);
        } catch (Exception ex) {
            return Translation.get("solverErrParamType", "$solverFuncAddLatLon", "3", "$angle", "$number", parameter[2]);
        }

        try {
            Coordinate result = new Coordinate(coord.getLatitude()+lat/60000.0, coord.getLongitude()+lon/60000.0);
            if (!result.isValid())
                return Translation.get("InvalidCoordinate", "$solverFuncAddLatLon", "Lat: " + String.valueOf(coord.getLatitude()) + ", Lon: " + String.valueOf(coord.getLongitude()));
            return result.formatCoordinate();
        } catch (Exception e) {
            return Translation.get("InvalidCoordinate", "$solverFuncAddLatLon", ""+coord.getLatitude()+lat +" "+ coord.getLongitude()+lon);
        }

    }

    @Override
    public int getAnzParam() {
        return 3;
    }

    @Override
    public boolean needsTextArgument() {
        return true;
    }

    @Override
    public DataType getParamType(int i) {
        switch (i) {
            case 0:
                return DataType.Coordinate;
            case 1:
            case 2:
                return DataType.Integer;
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
                return "solverParamCoordinate";
            case 1:
            case 2:
                return "solverParamInteger";
            default:
                return super.getParamName(i);
        }
    }
}
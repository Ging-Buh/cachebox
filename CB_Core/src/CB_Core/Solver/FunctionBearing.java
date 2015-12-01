package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_Utils.MathUtils.CalculationType;

public class FunctionBearing extends Function {

    private static final long serialVersionUID = -85879423478038052L;

    public FunctionBearing(Solver solver) {
	super(solver);
	Names.add(new LocalNames("Bearing", "en"));
    }

    @Override
    public String getName() {
	return Translation.Get("solverFuncBearing".hashCode());
    }

    @Override
    public String getDescription() {
	return Translation.Get("solverDescBearing".hashCode());
    }

    @Override
    public String Calculate(String[] parameter) {
	if (parameter.length != 2) {
	    return Translation.Get("solverErrParamCount".hashCode(), "2", "$solverFuncBearing");
	}
	Coordinate[] coord = new Coordinate[2];
	for (int i = 0; i < 2; i++) {
	    coord[i] = new CoordinateGPS(parameter[i]);
	    if (!coord[i].isValid())
		return Translation.Get("solverErrParamType".hashCode(), "$solverFuncBearing", String.valueOf(i + 1), "$coordinate", "$coordinate", parameter[i]);
	}
	try {
	    double bearing = CoordinateGPS.Bearing(CalculationType.ACCURATE, coord[0], coord[1]);
	    if (bearing < 0)
		bearing = bearing + 360;
	    return String.valueOf(bearing);
	} catch (Exception ex) {
	    return Translation.Get("StdError".hashCode(), "$solverFuncBearing", ex.getMessage(), coord[0].FormatCoordinate() + " -> " + coord[1].FormatCoordinate());
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
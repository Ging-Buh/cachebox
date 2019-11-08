package de.droidcachebox.solver;

import de.droidcachebox.solver.DataTypes.DataType;
import de.droidcachebox.translation.Translation;

public class FunctionInt extends Function {
    private static final long serialVersionUID = -4677935521343499858L;

    public FunctionInt(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Int", "en"));
        Names.add(new LocalNames("Ganzzahl", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncInt");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescInt");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncInt");
        }
        double number = 0;
        try {
            number = Double.valueOf(parameter[0].trim());
        } catch (Exception ex) {
            return Translation.get("solverErrParamType", "$solverFuncInt", "1", "$value", "$number", parameter[0]);
        }
        return String.valueOf((int) number);
    }

    @Override
    public int getAnzParam() {
        return 1;
    }

    @Override
    public boolean needsTextArgument() {
        return false;
    }

    @Override
    public DataType getParamType(int i) {
        switch (i) {
            case 0:
                return DataType.Float;
            default:
                return DataType.None;
        }
    }

    @Override
    public DataType getReturnType() {
        return DataType.Integer;
    }

    @Override
    public String getParamName(int i) {
        switch (i) {
            case 0:
                return "solverParamNumber";
            default:
                return super.getParamName(i);
        }
    }
}

package de.droidcachebox.solver;

import de.droidcachebox.solver.DataTypes.DataType;
import de.droidcachebox.translation.Translation;

public class FunctionPrimeIndex extends FunctionPrimeBase {
    private static final long serialVersionUID = -3417894947271978934L;

    public FunctionPrimeIndex(Solver solver) {
        super(solver);
        Names.add(new LocalNames("PrimeIndex", "en"));
        Names.add(new LocalNames("PrimIndex", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncPrimeIndex");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescPrimeIndex");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncPrimeIndex");
        }
        int number = 0;
        try {
            number = Integer.valueOf(parameter[0].trim());
        } catch (Exception ex) {
            return Translation.get("solverErrParamType", "$solverFuncPrimeIndex", "1", "$value", "$number", parameter[0]);
        }
        if (!IsPrimeNumber(number))
            return "0";
        int anz = 0;
        int akt = 0;
        while (number >= akt) {
            if (IsPrimeNumber(akt)) {
                anz++;
            }
            akt++;
        }
        return String.valueOf(anz);
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
                return DataType.Integer;
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
                return "solverParamInteger";
            default:
                return super.getParamName(i);
        }
    }
}

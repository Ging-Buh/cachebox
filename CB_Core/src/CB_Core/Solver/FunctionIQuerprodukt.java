package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionIQuerprodukt extends Function {
    private static final long serialVersionUID = -3859887811865649616L;

    public FunctionIQuerprodukt(Solver solver) {
        super(solver);
        Names.add(new LocalNames("ICrossproduct", "en"));
        Names.add(new LocalNames("IQuerprodukt", "de"));
        Names.add(new LocalNames("ICP", "en"));
        Names.add(new LocalNames("IQP", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncICrossproduct");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescICrossproduct");
    }

    private String Qp(String wert) {
        int result = 1;
        for (char c : wert.toCharArray()) {
            int i = c - 48;
            if ((i >= 0) && (i <= 9))
                result *= i;
        }
        return String.valueOf(result);
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncICrossproduct");
        }
        String wert = parameter[0].trim();
        while (wert.length() > 1) {
            wert = Qp(wert);
        }
        return wert;
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

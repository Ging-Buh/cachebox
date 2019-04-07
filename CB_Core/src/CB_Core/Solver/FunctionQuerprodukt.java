package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionQuerprodukt extends Function {
    private static final long serialVersionUID = 8720582376213442054L;

    public FunctionQuerprodukt(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Crossproduct", "en"));
        Names.add(new LocalNames("Querprodukt", "de"));
        Names.add(new LocalNames("CP", "en"));
        Names.add(new LocalNames("QP", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncCrossproduct");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescCrossprocuct");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncCrossproduct");
        }
        String wert = parameter[0].trim();
        int result = 1;
        for (char c : wert.toCharArray()) {
            int i = c - 48;
            if ((i >= 0) && (i <= 9))
                result *= i;
        }
        return String.valueOf(result);
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

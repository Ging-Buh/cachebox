package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionHandyCode extends Function {

    private static final long serialVersionUID = -270660378762802943L;

    public FunctionHandyCode(Solver solver) {
        super(solver);
        Names.add(new LocalNames("PhoneCode", "en"));
        Names.add(new LocalNames("HandyCode", "de"));
        Names.add(new LocalNames("PC", "en"));
        Names.add(new LocalNames("HC", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncPhoneCode".hashCode());
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescPhoneCode".hashCode());
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount".hashCode(), "1", "$solverFuncPhoneCode");
        }
        String wert = parameter[0].trim().toLowerCase();
        if (wert.length() == 0)
            return "0";
        char c = wert.charAt(0);
        int i = c - ('a') + 1;
        i -= 3;
        if (i <= 0)
            return "2";
        i -= 3;
        if (i <= 0)
            return "3";
        i -= 3;
        if (i <= 0)
            return "4";
        i -= 3;
        if (i <= 0)
            return "5";
        i -= 3;
        if (i <= 0)
            return "6";
        i -= 4;
        if (i <= 0)
            return "7";
        i -= 3;
        if (i <= 0)
            return "8";
        i -= 4;
        if (i <= 0)
            return "9";
        return "0";
    }

    @Override
    public int getAnzParam() {
        return 1;
    }

    @Override
    public boolean needsTextArgument() {
        return true;
    }

    @Override
    public DataType getParamType(int i) {
        switch (i) {
            case 0:
                return DataType.String;
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
                return "solverParamText";
            default:
                return super.getParamName(i);
        }
    }
}

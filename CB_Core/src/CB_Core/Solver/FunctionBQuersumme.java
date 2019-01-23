package CB_Core.Solver;

import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionBQuersumme extends Function{
    private static final long serialVersionUID = -6962880870313633796L;

    public FunctionBQuersumme(Solver solver) {
        super(solver);
        Names.add(new Function.LocalNames("AlphaQS", "en"));
        Names.add(new Function.LocalNames("AQS", "en"));
    }

    @Override
    public String getName() {
        return Translation.Get("solverFuncAlphaQSum".hashCode());
    }

    @Override
    public String getDescription() {
        return Translation.Get("solverDescAlphaQSum".hashCode());
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.Get("solverErrParamCount".hashCode(), "1", "$solverFuncAlphaQSum");
        }
        int result = 0;
        if (parameter[0].length() == 0)
            return "0";
        parameter[0] = parameter[0].toLowerCase();
        for (char c : parameter[0].toCharArray()) {
            if ((c >= 'a') && (c <= 'z')) {
                result += qs(c - ('a') + 1);
            }
        }
        return String.valueOf(result);
    }

    private int qs(int zahl) {
        if (zahl <= 9) return zahl;
        return zahl%10 + qs(zahl/10);
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
    public DataTypes.DataType getParamType(int i) {
        switch (i) {
            case 0:
                return DataTypes.DataType.String;
            default:
                return DataTypes.DataType.None;
        }
    }

    @Override
    public DataTypes.DataType getReturnType() {
        return DataTypes.DataType.Integer;
    }

    @Override
    public String getParamName(int i) {
        return "solverParamText";
    }
}

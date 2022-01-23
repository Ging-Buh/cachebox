package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;

public class FunctionBQuersumme extends Function {
    private static final long serialVersionUID = -6962880870313633796L;

    public FunctionBQuersumme(SolverLines solverLines) {
        super(solverLines);
        Names.add(new Function.LocalNames("AlphaCT", "en"));
        Names.add(new Function.LocalNames("ACT", "en"));
        Names.add(new Function.LocalNames("AlphaQS", "de"));
        Names.add(new Function.LocalNames("AQS", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncAlphaQSum");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescAlphaQSum");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncAlphaQSum");
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
        return zahl % 10 + qs(zahl / 10);
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
        return "solverParamText";
    }
}

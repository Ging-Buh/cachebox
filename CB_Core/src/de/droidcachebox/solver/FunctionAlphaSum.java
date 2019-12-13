package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;

public class FunctionAlphaSum extends Function {
    private static final long serialVersionUID = -6962880870313633795L;

    public FunctionAlphaSum(Solver solver) {
        super(solver);
        Names.add(new LocalNames("AlphaSum", "en"));
        Names.add(new LocalNames("AS", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncAlphaSum");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescAlphaSum");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncAlphaSum");
        }
        int result = 0;
        if (parameter[0].length() == 0)
            return "0";
        parameter[0] = parameter[0].toLowerCase();
        for (char c : parameter[0].toCharArray()) {
            if ((c >= 'a') && (c <= 'z'))
                result += c - ('a') + 1;
            if (c == 'ä') result += 27;
            if (c == 'ö') result += 28;
            if (c == 'ü') result += 29;
            if (c == 'ß') result += 30;
        }
        return String.valueOf(result);
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

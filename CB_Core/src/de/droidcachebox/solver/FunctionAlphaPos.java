package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;

public class FunctionAlphaPos extends Function {
    private static final long serialVersionUID = -2993835599804293184L;

    public FunctionAlphaPos(SolverLines solverLines) {
        super(solverLines);
        Names.add(new LocalNames("AlphaPos", "en"));
        Names.add(new LocalNames("AP", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncAlphaPos");
        // return "AlphaPos";
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescAlphaPos");
        // return "Position des ersten Zeichens im Alphabet";
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncAlphaPos");
            // return "Diese Funktion benötigt %s Parameter".replace("%s", "1");
        }
        String wert = parameter[0].trim().toLowerCase();
        if (wert.length() == 0)
            return "0";
        char c = wert.charAt(0);
        int result = c - ('a') + 1;
        if (result < 0) result = 0;
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

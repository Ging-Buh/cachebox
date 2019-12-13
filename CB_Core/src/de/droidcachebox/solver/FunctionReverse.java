package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;

public class FunctionReverse extends Function {
    private static final long serialVersionUID = 9169402073615894654L;

    public FunctionReverse(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Reverse", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncReverse");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescReverse");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncReverse");
        }
        String result = "";
        for (char c : parameter[0].toCharArray())
            result = c + result;
        return result;
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
        return DataType.String;
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

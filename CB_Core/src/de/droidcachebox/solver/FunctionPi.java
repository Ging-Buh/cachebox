package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;

public class FunctionPi extends Function {
    private static final long serialVersionUID = -5961548229978339692L;

    public FunctionPi(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Pi", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncPi");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescPi");
    }

    @Override
    public String Calculate(String[] parameter) {
        if ((parameter.length != 1) || (parameter[0].trim() != ""))
            return Translation.get("solverErrParamCount", "0", "$solverFuncPi");
        return String.valueOf(Math.PI);
    }

    @Override
    public int getAnzParam() {
        return 0;
    }

    @Override
    public boolean needsTextArgument() {
        return false;
    }

    @Override
    public DataType getParamType(int i) {
        switch (i) {
            default:
                return DataType.None;
        }
    }

    @Override
    public DataType getReturnType() {
        return DataType.Float;
    }

}

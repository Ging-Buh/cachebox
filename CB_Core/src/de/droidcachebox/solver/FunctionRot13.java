package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.UnitFormatter;

public class FunctionRot13 extends Function {
    private static final long serialVersionUID = 172122667088261676L;

    public FunctionRot13(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Rot13", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncRot13");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescRot13");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncRot13");
        }
        return UnitFormatter.Rot13(parameter[0]);
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

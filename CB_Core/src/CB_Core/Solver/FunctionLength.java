package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionLength extends Function {
    private static final long serialVersionUID = -7915834072364391848L;

    public FunctionLength(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Length", "en"));
        Names.add(new LocalNames("Länge", "de"));
        Names.add(new LocalNames("Len", "en"));
        Names.add(new LocalNames("Len", "de"));
    }

    @Override
    public String getName() {
        return Translation.Get("solverFuncLength".hashCode());
    }

    @Override
    public String getDescription() {
        return Translation.Get("solverDescLength".hashCode());
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.Get("solverErrParamCount".hashCode(), "1", "$solverFuncLength");
        }
        return String.valueOf(parameter[0].length());
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

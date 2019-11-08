package de.droidcachebox.solver;

import de.droidcachebox.solver.DataTypes.DataType;
import de.droidcachebox.translation.Translation;

// ************************************************************************
// ********************** Iterierte (einstellige) Quersumme (Iterated CrossTotal) **************************
// ************************************************************************
public class FunctionIQuersumme extends Function {
    private static final long serialVersionUID = -1727934349667230259L;

    public FunctionIQuersumme(Solver solver) {
        super(solver);
        Names.add(new LocalNames("ICrosstotal", "en"));
        Names.add(new LocalNames("IQuersumme", "de"));
        Names.add(new LocalNames("ICT", "en"));
        Names.add(new LocalNames("IQS", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncICrosstotal");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescICrosstotal");
    }

    private String Qs(String wert) {
        int result = 0;
        for (char c : wert.toCharArray()) {
            int i = c - 48;
            if ((i >= 0) && (i <= 9))
                result += i;
        }
        return String.valueOf(result);
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncICrosstotal");
        }
        String wert = parameter[0].trim();
        while (wert.length() > 1) {
            wert = Qs(wert);
        }
        return wert;
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

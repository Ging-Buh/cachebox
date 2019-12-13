package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;

public class FunctionHandySum extends Function {
    private static final long serialVersionUID = -9107479222557989258L;

    public FunctionHandySum(Solver solver) {
        super(solver);
        Names.add(new LocalNames("PhoneSum", "en"));
        Names.add(new LocalNames("HandySum", "de"));
        Names.add(new LocalNames("PS", "en"));
        Names.add(new LocalNames("HS", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncPhoneSum");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescPhoneSum");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncPhoneSum");
        }
        int result = 0;
        String wert = parameter[0].toLowerCase();
        for (char c : wert.toCharArray()) {
            int i = c - ('a') + 1;
            if ((i < 1) || (i > 26))
                continue; // nur Buchstaben!!!
            i -= 3;
            if (i <= 0) {
                result += 2;
                continue;
            }
            i -= 3;
            if (i <= 0) {
                result += 3;
                continue;
            }
            i -= 3;
            if (i <= 0) {
                result += 4;
                continue;
            }
            i -= 3;
            if (i <= 0) {
                result += 5;
                continue;
            }
            i -= 3;
            if (i <= 0) {
                result += 6;
                continue;
            }
            i -= 4;
            if (i <= 0) {
                result += 7;
                continue;
            }
            i -= 3;
            if (i <= 0) {
                result += 8;
                continue;
            }
            i -= 4;
            if (i <= 0) {
                result += 9;
                continue;
            }
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
        switch (i) {
            case 0:
                return "solverParamText";
            default:
                return super.getParamName(i);
        }
    }
}

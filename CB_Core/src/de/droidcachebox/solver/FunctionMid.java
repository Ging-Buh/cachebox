package de.droidcachebox.solver;

import java.io.Serial;

import de.droidcachebox.translation.Translation;

/**
 * Return a substring of a string. Parameter 3 defaults to 1
 * Mid(String; StartPosition; [CharacterCount])
 */
public class FunctionMid extends Function {
    @Serial
    private static final long serialVersionUID = 3727854231542597267L;

    FunctionMid(SolverLines solverLines) {
        super(solverLines);
        Names.add(new LocalNames("Mid", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncMid");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescMid");
    }

    @Override
    public String Calculate(String[] parameter) {
        if ((parameter.length < 2) || (parameter.length > 3)) {
            return Translation.get("solverErrParamCount", "2-3", "$solverFuncMid");
        }
        String sValue = parameter[0].trim();
        int iPos, iCount;
        try {
            iPos = Integer.parseInt(parameter[1].trim());
            if (iPos < 0) iPos = 0; // is an error too
        } catch (Exception ex) {
            return Translation.get("solverErrParamType", "$solverFuncMid", "2", "$Position", "$number", parameter[1]);
        }
        try {
            if (parameter.length == 2)
                iCount = 1;
            else {
                iCount = Integer.parseInt(parameter[2].trim());
                if (iCount < 0) iCount = 1; // is like default one character
            }
        } catch (Exception ex) {
            return Translation.get("solverErrParamType", "$solverFuncMid", "5", "$count", "$number", parameter[2]);
        }
        if (iPos == 0 || iPos > sValue.length() || iPos + iCount - 1 > sValue.length()) {
            return "%" + Translation.get("PosGtLength", "$solverFuncMid", String.valueOf(iPos), sValue);
        }
        return sValue.substring(iPos - 1, iPos - 1 + iCount);
    }

    @Override
    public int getAnzParam() {
        return 3;
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
            case 1:
            case 2:
                return DataType.Integer;
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
            case 1:
            case 2:
                return "solverParamInteger";
            default:
                return super.getParamName(i);
        }
    }
}

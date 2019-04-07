package CB_Core.Solver;

import CB_Core.Solver.DataTypes.DataType;
import CB_Translation_Base.TranslationEngine.Translation;

public class FunctionRom2Dec extends Function {
    private static final long serialVersionUID = 3808926872593878660L;

    public FunctionRom2Dec(Solver solver) {
        super(solver);
        Names.add(new LocalNames("Rom2Dec", "en"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncRom2Dec");
    }

    @Override
    public String getDescription() {
        return Translation.get("solverDescRom2Dec");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 1) {
            return Translation.get("solverErrParamCount", "1", "$solverFuncRom2Dec");
        }
        String wert = parameter[0].trim();
        String ziffern = "IVXLCDM";
        int[] werte = new int[]{1, 5, 10, 50, 100, 500, 1000};
        int result = 0;
        int i, idx0 = 0, idx1 = 0;

        wert = wert.toUpperCase();
        try {
            if (wert.length() > 1) {
                for (i = 0; i < wert.length() - 1; i++) {
                    idx0 = ziffern.indexOf(wert.charAt(i + 0), 0);
                    idx1 = ziffern.indexOf(wert.charAt(i + 1), 0);

                    if (idx0 < idx1) {
                        result -= werte[idx0];
                    } else {
                        result += werte[idx0];
                    }
                }
                result += werte[idx1];
            } else {
                result = werte[ziffern.indexOf(wert.charAt(0), 0)];
            }
        } catch (Exception ex) {
            return Translation.get("$InvalidRomString", parameter[0]);
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
                return "solverParamRomNumber";
            default:
                return super.getParamName(i);
        }
    }

}

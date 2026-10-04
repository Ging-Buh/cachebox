package de.droidcachebox.solver;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.translation.Translation;

public class FunctionReverseWherIgo extends Function {

    private static final long serialVersionUID = -6013883020785631158L;

    public FunctionReverseWherIgo(SolverLines solverLines) {
        super(solverLines);
        Names.add(new LocalNames("ReverseWherIgo", "en"));
        Names.add(new LocalNames("ReverseWherIgo", "de"));
    }

    @Override
    public String getName() {
        return Translation.get("solverFuncRevWherIgo");
    }


    @Override
    public String getDescription() {
        return Translation.get("solverDescRevWherIgo");
    }

    @Override
    public String Calculate(String[] parameter) {
        if (parameter.length != 3) {
            String s = Translation.get("solverErrParamCount", "3", "solverFuncRevWherIgo");
            return s;
        }
        String a = parameter[0];
        if (a.length() != 6) {
            return Translation.get("solverRevWigParamType", "1", parameter[0]);
        }
        String b = parameter[1];
        if (b.length() != 6) {
            return Translation.get("solverRevWigParamType", "2", parameter[1]);
        }
        String c = parameter[2];
        if (c.length() != 6) {
            return Translation.get("solverRevWigParamType", "3", parameter[2]);
        }

        char a4 = a.charAt(3);
        char ns = 'N', ew = 'E';
        if ("1234".contains(a4+""))
        {
            switch (a4) {
                case '1': ns = 'N'; ew = 'E'; break;
                case '2': ns = 'S'; ew = 'E'; break;
                case '3': ns = 'N'; ew = 'W'; break;
                case '4': ns = 'S'; ew = 'W'; break;
            }
        }
        else {
            return Translation.get("solverRevWig1ParamType", parameter[0]);
        }
        char a1 = a.charAt(0);
        char a2 = a.charAt(1);
        char a3 = a.charAt(2);
        char a5 = a.charAt(4);
        char a6 = a.charAt(5);
        char b1 = b.charAt(0);
        char b2 = b.charAt(1);
        //char b3 = b.charAt(2);
        char b4 = b.charAt(3);
        char b5 = b.charAt(4);
        char b6 = b.charAt(5);
        char c1 = c.charAt(0);
        char c2 = c.charAt(1);
        //char c3 = c.charAt(2);
        char c4 = c.charAt(3);
        char c5 = c.charAt(4);
        char c6 = c.charAt(5);
        String revCoord;
        int modu = (Character.getNumericValue(c2)+Character.getNumericValue(c5)) % 2;
        if ((Character.getNumericValue(c2)+Character.getNumericValue(c5)) % 2 == 0)
        {
            revCoord = new String(""+ns+a3+b5+'.'+b2+c4+a1+c5+a6+' '+ew+a2+c1+c6+'.'+b4+b1+a5+c2+b6);
        }
        else {
            revCoord = new String(""+ns+b1+a6+'.'+a3+c1+c4+c5+a1+' '+ew+b5+c6+a5+'.'+a2+b4+b6+c2+b2);
        }
        try {
            Coordinate result = new Coordinate(revCoord);
            if (!result.isValid())
                return Translation.get("InvalidCoordinate", "solverFuncRevWherIgo", revCoord);
            return result.formatCoordinate();
        } catch (Exception e) {
            return Translation.get("InvalidCoordinate", "solverFuncRevWherIgo", revCoord);
        }
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
            case 1:
            case 2:
                return DataType.Integer;
            default:
                return DataType.None;
        }
    }

    @Override
    public DataType getReturnType() {
        return DataType.Coordinate;
    }

    @Override
    public String getParamName(int i) {
        switch (i) {
            case 0:
            case 1:
            case 2:
                return "solverParamInteger";
            default:
                return super.getParamName(i);
        }
    }
}
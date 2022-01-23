package de.droidcachebox.solver;

public abstract class FunctionPrimeBase extends Function {
    private static final long serialVersionUID = 4014109046993845632L;

    public FunctionPrimeBase(SolverLines solverLines) {
        super(solverLines);
    }

    protected boolean IsPrimeNumber(long testNumber) {
        if (testNumber < 2)
            return false;
        if (testNumber == 2)
            return true;
        // 2 explizit testen, da die Schliefe an 3 startet
        if (testNumber % 2 == 0)
            return false;

        long upperBorder = (long) Math.round(Math.sqrt(testNumber));
        // Alle ungeraden Zahlen bis zur Wurzel pruefen
        for (long i = 3; i <= upperBorder; i = i + 2)
            if (testNumber % i == 0)
                return false;
        return true;
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

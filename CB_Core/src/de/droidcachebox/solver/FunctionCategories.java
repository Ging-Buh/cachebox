package de.droidcachebox.solver;

import java.util.TreeMap;

public class FunctionCategories extends TreeMap<String, Functions> {
    private static final long serialVersionUID = -4054421409675901933L;

    public FunctionCategories(SolverLines solverLines) {
        Functions functions = new Functions("solverGroupText");
        functions.add(new FunctionAlphaSum(solverLines));
        functions.add(new FunctionBQuersumme(solverLines));
        functions.add(new FunctionAlphaPos(solverLines));
        functions.add(new FunctionHandyCode(solverLines));
        functions.add(new FunctionHandySum(solverLines));
        functions.add(new FunctionLength(solverLines));
        functions.add(new FunctionReverse(solverLines));
        functions.add(new FunctionRot13(solverLines));
        functions.add(new FunctionMid(solverLines));
        this.put(functions.Name, functions);
        functions = new Functions("solverGroupNumbers");
        functions.add(new FunctionQuersumme(solverLines));
        functions.add(new FunctionIQuersumme(solverLines));
        functions.add(new FunctionQuerprodukt(solverLines));
        functions.add(new FunctionIQuerprodukt(solverLines));
        functions.add(new FunctionRom2Dec(solverLines));
        functions.add(new FunctionPrimenumber(solverLines));
        functions.add(new FunctionInt(solverLines));
        functions.add(new FunctionRound(solverLines));
        functions.add(new FunctionPrimeIndex(solverLines));
        functions.add(new FunctionPi(solverLines));
        this.put(functions.Name, functions);
        functions = new Functions("solverGroupCoordinates");
        functions.add(new FunctionProjection(solverLines));
        functions.add(new FunctionIntersection(solverLines));
        functions.add(new FunctionCrossbearing(solverLines));
        functions.add(new FunctionBearing(solverLines));
        functions.add(new FunctionDistance(solverLines));
        this.put(functions.Name, functions);

    }

    public boolean InsertEntities(TempEntity tEntity, EntityList entities) {
        for (Functions functions : this.values()) {
            if (functions.InsertEntities(tEntity, entities))
                return true;
        }
        return false;
    }

    public boolean isFunction(String s) {
        for (Functions functions : this.values()) {
            if (functions.isFunction(s))
                return true;
        }
        return false;
    }

    public Function getFunction(String s) {
        for (Functions functions : this.values()) {
            Function function = functions.getFunction(s);
            if (function != null)
                return function;
        }
        return null;
    }
}

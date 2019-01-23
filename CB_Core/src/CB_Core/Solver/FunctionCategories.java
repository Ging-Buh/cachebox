package CB_Core.Solver;

import java.util.TreeMap;

public class FunctionCategories extends TreeMap<String, Functions> {
    private static final long serialVersionUID = -4054421409675901933L;

    public FunctionCategories(Solver solver) {
        Functions functions = new Functions("solverGroupText");
        functions.add(new FunctionAlphaSum(solver));
        functions.add(new FunctionBQuersumme(solver));
        functions.add(new FunctionAlphaPos(solver));
        functions.add(new FunctionHandyCode(solver));
        functions.add(new FunctionHandySum(solver));
        functions.add(new FunctionLength(solver));
        functions.add(new FunctionReverse(solver));
        functions.add(new FunctionRot13(solver));
        functions.add(new FunctionMid(solver));
        this.put(functions.Name, functions);
        functions = new Functions("solverGroupNumbers");
        functions.add(new FunctionQuersumme(solver));
        functions.add(new FunctionIQuersumme(solver));
        functions.add(new FunctionQuerprodukt(solver));
        functions.add(new FunctionIQuerprodukt(solver));
        functions.add(new FunctionRom2Dec(solver));
        functions.add(new FunctionPrimenumber(solver));
        functions.add(new FunctionInt(solver));
        functions.add(new FunctionRound(solver));
        functions.add(new FunctionPrimeIndex(solver));
        functions.add(new FunctionPi(solver));
        this.put(functions.Name, functions);
        functions = new Functions("solverGroupCoordinates");
        functions.add(new FunctionProjection(solver));
        functions.add(new FunctionIntersection(solver));
        functions.add(new FunctionCrossbearing(solver));
        functions.add(new FunctionBearing(solver));
        functions.add(new FunctionDistance(solver));
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

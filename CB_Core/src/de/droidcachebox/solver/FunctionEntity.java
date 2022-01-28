package de.droidcachebox.solver;

import java.util.ArrayList;

public class FunctionEntity extends Entity {

    private Function function;
    private Entity entity;

    public FunctionEntity(SolverLines solverLines, int id, Function function, Entity entity) {
        super(solverLines, id);
        this.function = function;
        this.entity = entity;
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
        if (entity == source)
            entity = dest;
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
        list.add(entity);
    }

    @Override
    public String calculate() {
        String[] str;
        if (entity instanceof ParameterEntity) {
            str = ((ParameterEntity) entity).GetParameter();
        } else {
            String argument = entity.calculate();
            str = new String[1];
            str[0] = argument;
        }
        for (String s : str) {
            if (SolverLines.isError(s)) {
                return s; // einer der aufrufenden Parameter ist eine Fehlermeldung -> Fehlermeldung direkt ausgeben!
            }
        }
        return function.Calculate(str);
    }

    @Override
    public String toString() {
        return "F:" + function + "(" + entity.toString() + ")";
    }
}

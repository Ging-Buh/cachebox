package de.droidcachebox.solver;

import java.util.ArrayList;

public class ConstantEntity extends Entity {

    double wert;

    public ConstantEntity(SolverLines solverLines, int id, double wert) {
        super(solverLines, id);
        this.wert = wert;
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
    }

    @Override
    public String calculate() {
        String value = String.valueOf(wert);
        if (Math.round(wert) == wert) {
            long iv = (long) Math.round(wert);
            value = String.valueOf(iv);
        }
        return value;
    }

    @Override
    public String toString() {
        String value = String.valueOf(wert);
        if (Math.round(wert) == wert)
            value = String.valueOf((int) (Math.round(wert)));
        return "C" + entityId + ":(" + value + ")";
    }

}

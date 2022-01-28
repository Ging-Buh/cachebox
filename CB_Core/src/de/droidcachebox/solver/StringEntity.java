package de.droidcachebox.solver;

import java.util.ArrayList;

public class StringEntity extends Entity {
    String Wert;

    public StringEntity(SolverLines solverLines, int id, String wert) {
        super(solverLines, id);
        this.Wert = wert;
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
    }

    @Override
    public String calculate() {
        return Wert;
    }

    @Override
    public String toString() {
        return "S:" + entityId + ":(" + Wert + ")";
    }
}

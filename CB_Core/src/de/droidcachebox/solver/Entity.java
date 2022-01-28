package de.droidcachebox.solver;

import java.util.ArrayList;

public class Entity {
    protected SolverLines solverLines;
    protected int entityId;
    protected boolean isLeftPartOfAssign; // becomes true if this is to the left of the =.

    public Entity(SolverLines solverLines, int entityId) {
        this.solverLines = solverLines;
        this.entityId = entityId;
        isLeftPartOfAssign = false;
    }

    // replace all occurrences of source with dest because source is just a reference to dest!
    public void replaceTemp(Entity source, Entity dest) {
    }

    // publish all entities contained in this one
    public void getAllEntities(ArrayList<Entity> list) {
    }

    public String calculate() {
        return "";
    }

    public String toString() {
        return "";
    }
}

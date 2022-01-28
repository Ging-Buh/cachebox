package de.droidcachebox.solver;

import java.util.ArrayList;

public class VariableEntity extends Entity {
    // Speichert einen Wert in eine Variable
    String Name;

    public VariableEntity(SolverLines solverLines, int id, String name) {
        super(solverLines, id);
        this.Name = name;
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
    }

    @Override
    public String calculate() {
        if (solverLines.Variablen.containsKey(Name.toLowerCase())) {
            return solverLines.Variablen.get(Name.toLowerCase());
        } else
            return "Fehler";
    }

    @Override
    public String toString() {
        return "V" + entityId + ":(" + Name + ")";
    }
}

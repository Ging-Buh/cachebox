package de.droidcachebox.solver;

import java.util.ArrayList;

class ParameterEntity extends Entity {
    ArrayList<Entity> Liste = new ArrayList<Entity>();

    ParameterEntity(SolverLines solverLines, int id) {
        super(solverLines, id);
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
        for (int i = 0; i < Liste.size(); i++) {
            Entity entity = Liste.get(i);
            if (entity == source) {
                Liste.remove(i);
                Liste.add(i, dest);
            }
        }
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
        for (Entity entity : Liste)
            list.add(entity);
    }

    @Override
    public String calculate() {
        String result = "";
        for (Entity entity : Liste) {
            result += entity.calculate();
        }
        return result;
    }

    String[] GetParameter() {
        String[] result = new String[Liste.size()];
        for (int i = 0; i < Liste.size(); i++) {
            Entity entity = Liste.get(i);

            result[i] = entity.calculate();
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "P" + entityId + ":(";
        for (Entity entity : Liste)
            result += entity.toString() + ";";
        result = result.substring(0, result.length() - 1);
        result += ")";
        return result;
    }

}

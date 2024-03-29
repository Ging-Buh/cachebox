package de.droidcachebox.solver;

import java.util.ArrayList;

class OperatorEntity extends Entity {
    private Entity links;
    private Entity rechts;
    private String op;

    OperatorEntity(SolverLines solverLines, int id, Entity links, String op, Entity rechts) {
        super(solverLines, id);
        this.links = links;
        this.op = op;
        this.rechts = rechts;
    }

    private static String DoubleToString(double wert) {
        String value = String.valueOf(wert);
        if (Math.round(wert) == wert)
            value = String.valueOf((int) (Math.round(wert)));
        return value;
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
        if (links == source)
            links = dest;
        if (rechts == source)
            rechts = dest;
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
        list.add(links);
        list.add(rechts);
    }

    @Override
    public String calculate() {
        String lLinks = links.calculate();
        String lRechts = rechts.calculate();
        String result = "";
        try {
            double dLinks = Double.parseDouble(lLinks);
            double dRechts = Double.parseDouble(lRechts);
            if (op.equals("+"))
                result = DoubleToString(dLinks + dRechts);
            else if (op.equals("-"))
                result = DoubleToString(dLinks - dRechts);
            else if (op.equals("*"))
                result = DoubleToString(dLinks * dRechts);
            else if (op.equals("/"))
                result = DoubleToString(dLinks / dRechts);
            else if (op.equals(":")) {
                result = DoubleToString(dLinks);
                while (result.length() < dRechts)
                    result = '0' + result;
            } else if (op.equals("^"))
                result = DoubleToString(Math.pow(dLinks, dRechts));
        } catch (Exception ex) {
            // Fehler ausgeben.
            return ex.getMessage();
        }
        return result;
    }

    @Override
    public String toString() {
        return "O" + entityId + op + "(" + links.toString() + "," + rechts.toString() + ")";
    }

}

package de.droidcachebox.solver;

import java.util.ArrayList;

import de.droidcachebox.translation.Translation;

public class ZuweisungEntity extends Entity {

    private Entity links;
    private Entity rechts;

    public ZuweisungEntity(SolverLines solverLines, int id, Entity links, Entity rechts) {
        super(solverLines, id);
        this.links = links;
        this.rechts = rechts;
    }

    @Override
    public void replaceTemp(Entity source, Entity dest) {
        if (links == source)
            links = dest;
        if (rechts == source)
            rechts = dest;
        links.isLeftPartOfAssign = true;
    }

    @Override
    public void getAllEntities(ArrayList<Entity> list) {
        list.add(links);
        list.add(rechts);
    }

    @Override
    public String calculate() {
        String lLinks = "";
        String lRechts = rechts.calculate();

        // links muss der Name einer Variablen sein (=TempEntity)
        if (links instanceof VariableEntity) {
            lLinks = ((VariableEntity) links).Name.toLowerCase();
            // auf gueltigen Variablennamen ueberpruefen
            boolean ungueltig = false;
            boolean firstChar = true;
            char[] chars = new char[lLinks.length()];
            lLinks.getChars(0, lLinks.length(), chars, 0);
            for (char c : chars) {
                boolean isBuchstabe = ((c >= 'a') || (c <= 'z'));
                boolean isZahl = ((c >= '0') || (c <= '9'));
                if (firstChar && (!isBuchstabe))
                    ungueltig = true;
                if (!(isBuchstabe || isZahl))
                    ungueltig = true;
                firstChar = false;
            }
            if (ungueltig)
                return Translation.get("InvalidVariableName", lLinks);
            // lLinks ist gueltiger Variablenname
            if (!solverLines.Variablen.containsKey(lLinks)) {
                // neue Variable hinzfuegen
                solverLines.Variablen.put(lLinks, lRechts);
            } else {
                // Variable aendern
                solverLines.Variablen.remove(lLinks);
                solverLines.Variablen.put(lLinks, lRechts);
            }
            return lRechts;
        } else if (links instanceof CoordinateEntity) {
            return ((CoordinateEntity) links).setCoordinate(lRechts);
        } else
            return Translation.get("LeftMustBeAVariable", lLinks);
    }

    @Override
    public String toString() {
        return "Z" + entityId + "(" + links + "," + rechts + ")";
    }
}

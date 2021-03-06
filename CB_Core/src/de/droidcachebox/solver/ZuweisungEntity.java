package de.droidcachebox.solver;

import de.droidcachebox.translation.Translation;

import java.util.ArrayList;

public class ZuweisungEntity extends Entity {

    private Entity links;
    private Entity rechts;

    public ZuweisungEntity(Solver solver, int id, Entity links, Entity rechts) {
        super(solver, id);
        this.links = links;
        this.rechts = rechts;
    }

    @Override
    public void ReplaceTemp(Entity source, Entity dest) {
        if (links == source)
            links = dest;
        if (rechts == source)
            rechts = dest;
        links.IsLinks = true;
    }

    @Override
    public void GetAllEntities(ArrayList<Entity> list) {
        list.add(links);
        list.add(rechts);
    }

    @Override
    public String Berechne() {
        String lLinks = "";
        String lRechts = rechts.Berechne();

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
            if (!solver.Variablen.containsKey(lLinks)) {
                // neue Variable hinzfuegen
                solver.Variablen.put(lLinks, lRechts);
            } else {
                // Variable aendern
                solver.Variablen.remove(lLinks);
                solver.Variablen.put(lLinks, lRechts);
            }
            return lRechts;
        } else if (links instanceof CoordinateEntity) {
            return ((CoordinateEntity) links).SetCoordinate(lRechts);
        } else
            return Translation.get("LeftMustBeAVariable", lLinks);
    }

    @Override
    public String ToString() {
        return "Z" + Id + "(" + links + "," + rechts + ")";
    }
}

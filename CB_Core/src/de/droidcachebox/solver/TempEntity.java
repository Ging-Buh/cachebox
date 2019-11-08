package de.droidcachebox.solver;

import java.util.SortedMap;
import java.util.TreeMap;

public class TempEntity extends Entity {

    public String Text;
    SortedMap<Integer, Entity> entities = new TreeMap<Integer, Entity>();

    public TempEntity(Solver solver, int id, String text) {
        super(solver, id);
        this.Text = text.trim();
    }

    @Override
    public void ReplaceTemp(Entity source, Entity dest) {
        this.Text = this.Text.replace("#" + source.Id + "#", "#" + dest.Id + "#");
    }

    @Override
    public String ToString() {
        return "T" + Id + "(" + Text + ")";
    }

    @Override
    public String Berechne() {
        // dies kann eine Zahl, ein String oder eine Variable sein!
        Text = Text.trim();
        try {
            double zahl = Double.valueOf(Text);
            return String.valueOf(zahl);
        } catch (Exception exc) {
            // Exception -> keine Zahl
        }
        if ((Text.length() >= 2) && (Text.substring(0, 1).equals('"')) && (Text.substring(Text.length() - 1).equals('"')))
            return Text.substring(1, Text.length() - 1);
        // text ist keine Zahl und kein String

        // dies ist eine Variable, es koenne aber auch mehrere Variablen und Texte hintereinander stehen
        // -> deren Wert ausgeben
        String[] ss = Text.split(" ");
        String result = "";
        for (String s : ss) {
            if (solver.Variablen.containsKey(s.trim().toLowerCase())) {
                result += solver.Variablen.get(s.trim().toLowerCase());
            } else
                result += s;
        }
        return result;
    }
}

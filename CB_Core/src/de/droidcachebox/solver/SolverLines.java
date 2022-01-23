package de.droidcachebox.solver;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import de.droidcachebox.translation.Translation;

public class SolverLines extends ArrayList<SolverLine> {
    public static final String errorPrefix = Translation.get("solverErrorPrefix");
    private static final long serialVersionUID = 132452345624562L;
    public static SolverCacheInterface solverCacheInterface = null;
    // Liste mit den Operatoren, werden in dieser Reihenfolge abgearbeitet (. vor -)...
    public SortedMap<Integer, ArrayList<String>> operatoren = new TreeMap<>();
    public FunctionCategories functions = new FunctionCategories(this);
    // hier werden die Loesungen aller Variablen gespeichert
    public TreeMap<String, String> Variablen = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public SortedMap<String, Integer> MissingVariables = null;
    String source;

    public SolverLines(String source, SolverCacheInterface sci) {
        solverCacheInterface = sci;
        if (source == null)
            source = "";
        if (operatoren.size() == 0) {
            ArrayList<String> ops = new ArrayList<>();
            ops.add("=");
            operatoren.put(0, ops);

            ops = new ArrayList<>();
            ops.add(":");
            operatoren.put(1, ops);

            ops = new ArrayList<>();
            ops.add("-");
            ops.add("+");
            operatoren.put(2, ops);

            ops = new ArrayList<>();
            ops.add("/");
            ops.add("*");
            operatoren.put(3, ops);

            ops = new ArrayList<>();
            ops.add("^");
            operatoren.put(4, ops);
        }
        this.source = source;
    }

    public static boolean isError(String s) {
        if (s.length() <= errorPrefix.length())
            return false;

        return s.startsWith(errorPrefix);
    }

    public boolean Solve() {
        MissingVariables = null;
        Variablen.clear();
        int pos = 0;
        while (pos < source.length()) {
            int pos2 = source.indexOf("\n", pos);
            if (pos2 < 0)
                break;
            String s = source.substring(pos, pos2);
            int pos3 = s.indexOf('#');
            if (pos3 > 0) // Kommentar entfernen
                s = s.substring(0, pos3 - 1);
            else if (pos3 == 0)
                s = "";
            this.add(new SolverLine(this, s));
            pos = pos2 + "\n".length(); // Environment.NewLine.Length;
        }
        // letzte Zeile auch noch einfuegen
        if (pos < source.length()) {
            String ss = source.substring(pos);
            this.add(new SolverLine(this, ss));
        }
        return parseZeilen();
    }

    private boolean parseZeilen() {
        for (SolverLine zeile : this) {
            if (!zeile.Parse())
                return false;
        }
        return true;
    }

    public String getSolverString() {
        StringBuilder result = new StringBuilder();

        for (SolverLine zeile : this) {
            // wenn die letzte Zeile leer ist dann nicht einfügen
            if ((zeile == this.get(this.size() - 1)) && (zeile.getOrgText().length() == 0))
                break;
            if (result.length() > 0)
                result.append("\n");
            result.append(zeile.getOrgText());
        }

        return result.toString();
    }
}

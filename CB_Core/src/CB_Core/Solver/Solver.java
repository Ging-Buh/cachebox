package CB_Core.Solver;

import CB_Translation_Base.TranslationEngine.Translation;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class Solver extends ArrayList<SolverZeile> {
    public static final String errorPrefix = Translation.Get("solverErrorPrefix".hashCode());
    public static final String errorPostfix = "";
    private static final long serialVersionUID = 132452345624562L;
    public static SolverCacheInterface solverCacheInterface = null;
    // Liste mit den Operatoren, werden in dieser Reihenfolge abgearbeitet (. vor -)...
    public SortedMap<Integer, ArrayList<String>> operatoren = new TreeMap<Integer, ArrayList<String>>();
    public FunctionCategories functions = new FunctionCategories(this);
    // hier werden die Loesungen aller Variablen gespeichert
    public TreeMap<String, String> Variablen = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    public SortedMap<String, Integer> MissingVariables = null;
    String source;

    public Solver(String source, SolverCacheInterface sci) {
        solverCacheInterface = sci;
        if (source == null)
            source = "";
        if (operatoren.size() == 0) {
            ArrayList<String> ops = new ArrayList<String>();
            ops.add("=");
            operatoren.put(0, ops);

            ops = new ArrayList<String>();
            ops.add(":");
            operatoren.put(1, ops);

            ops = new ArrayList<String>();
            ops.add("-");
            ops.add("+");
            operatoren.put(2, ops);

            ops = new ArrayList<String>();
            ops.add("/");
            ops.add("*");
            operatoren.put(3, ops);

            ops = new ArrayList<String>();
            ops.add("^");
            operatoren.put(4, ops);
        }
        this.source = source;
    }

    public static boolean isError(String s) {
        if (s.length() <= errorPrefix.length())
            return false;

        if (s.substring(0, errorPrefix.length()).equals(errorPrefix)
            /* && s.substring(s.length() - errorPostfix.length(), s.length()).equals(errorPostfix) */)
            return true;
        else
            return false;
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
            this.add(new SolverZeile(this, s));
            pos = pos2 + "\n".length(); // Environment.NewLine.Length;
        }
        // letzte Zeile auch noch einfuegen
        if (pos < source.length()) {
            String ss = source.substring(pos, source.length());
            this.add(new SolverZeile(this, ss));
        }
        if (!parseZeilen())
            return false;
        return true;
    }

    private boolean parseZeilen() {
        for (SolverZeile zeile : this) {
            if (!zeile.Parse())
                return false;
        }
        return true;
    }

    public String getSolverString() {
        String result = "";

        for (SolverZeile zeile : this) {
            // wenn die letzte Zeile leer ist dann nicht einfügen
            if ((zeile == this.get(this.size() - 1)) && (zeile.getOrgText().length() == 0))
                break;
            if (result.length() > 0)
                result += "\n";
            result += zeile.getOrgText();
        }

        return result;
    }
}

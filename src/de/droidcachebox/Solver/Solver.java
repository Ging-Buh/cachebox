package de.droidcachebox.Solver;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import android.os.Environment;

import de.droidcachebox.Solver.Functions.FunctionCategories;

public class Solver extends ArrayList<SolverZeile> {
	private static final long serialVersionUID = 1L;

	// Liste mit den Operatoren, werden in dieser Reihenfolge abgearbeitet (. vor -)...
    static SortedMap<Integer, ArrayList<String>> operatoren = new TreeMap<Integer, ArrayList<String>>();
    static FunctionCategories functions = new FunctionCategories();   
    // hier werden die Loesungen aller Variablen gespeichert
    static TreeMap<String, String> Variablen = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    static public SortedMap<String, Integer> MissingVariables = null;
 
    String source;
    public Solver(String source)
    {
      if (operatoren.size() == 0)
      {
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

    public boolean Solve()
    {
      MissingVariables = null;
      Solver.Variablen.clear();
      int pos = 0;
      while (pos < source.length())
      {
        int pos2 = source.indexOf("\n", pos);
        if (pos2 < 0)
          break;
        String s = source.substring(pos, pos2);
        int pos3 = s.indexOf('#');
        if (pos3 > 0)   // Kommentar entfernen
          s = s.substring(0, pos3 - 1);
        else if (pos3 == 0)
          s = "";
        this.add(new SolverZeile(this, s));
        pos = pos2 + "\n".length(); //Environment.NewLine.Length;
      }
      // letzte Zeile auch noch einfuegen
      if (pos < source.length())
      {
        String ss = source.substring(pos, source.length());
        this.add(new SolverZeile(this, ss));
      }
      if (!parseZeilen())
        return false;
      return true;
    }

    private boolean parseZeilen()
    {
      for (SolverZeile zeile : this)
      {
        if (!zeile.Parse())
          return false;
      }
      return true;
    }
}


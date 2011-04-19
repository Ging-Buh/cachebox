package de.droidcachebox.Solver;

import java.util.ArrayList;
import java.util.Collection;

public class SolverZeile {
    private Solver solver;
    private String text;
    private EntityList entities = new EntityList();
    public String Solution = "";
    public SolverZeile(Solver solver, String text)
    {
      this.solver = solver;
      this.text = text;
    }

    public boolean Parse()
    {
      // alle Klammern herausfinden
      while (true)
      {
        // erste innerste Klammer Auf suchen
        if (getFirstKlammer())
        {
          if (nextZu <= firstAuf)
          {
            Solution = "Missing )!";
            return true;
          }
          // ; suchen fuer Trennung zwischen Paramtern...
          String anweisung = text.substring(firstAuf + 1, nextZu);
          String var = entities.Insert(anweisung);
          text = text.substring(0, firstAuf).trim() + var + text.substring(nextZu + 1, text.length()).trim();
        }
        else
        {
          // Rest als tempvar einfuegen
          String var = entities.Insert(text);
          break;
        }
      }
      String debug = entities.ToString();
      searchOperators(true);
      debug = entities.ToString();

      // Funktionsparamter mit ';' getrennt suchen
      searchFunctionParameters();
      debug = entities.ToString();

      // zuerst Strings heraussuchen
      searchStrings();
      debug = entities.ToString();

      // Auflistungen, Variablen, Konstanten und Strings
      searchLists();
      debug = entities.ToString();
/*
      // alle Operatoren herausfinden
      searchOperators(false);

      // Functionen suchen
      searchFunctions();
      
      // Variablen, Konstanten und Strings
      searchVariables();
      
      entities.Pack();

      // Alle Entities holen, die Bestandteil eines anderen sind
      List<Entity> list = new List<Entity>();
      foreach (Entity ent in entities.Values)
      {
        ent.GetAllEntities(list);
      }
      // diese koennen dann geloescht werden
      foreach (Entity ent in list)
      {
        entities.Remove(ent.Id);
      }
      // es sollte nur 1 Entity uebrig bleiben. Dies beinhaltet dann die komplette Formel
      if (entities.Values.Count >= 1)
      {
        solution = entities.Values[0].Berechne();
        entities.Values[0].GetAllEntities(list);
        SortedList<string, int> missingVariables = null;
        foreach (Entity tent in list)
        {
          // store Missing Variables in global List
          // store Missing Variables in local List too (only for this line)
          if (tent is TempEntity)
          {
            if ((tent as TempEntity).Text.Trim() == "")
              continue;
            // store in global list
            if (Solver.MissingVariables == null)
              Solver.MissingVariables = new SortedList<string, int>();
            if (!Solver.MissingVariables.Keys.Contains((tent as TempEntity).Text))
              Solver.MissingVariables.Add((tent as TempEntity).Text, 0);
            // store in local list
            if (missingVariables == null)
              missingVariables = new SortedList<string, int>();
            if (!missingVariables.ContainsKey((tent as TempEntity).Text))
              missingVariables.Add((tent as TempEntity).Text, 0);
          }
        }
        if ((missingVariables != null) && (missingVariables.Count > 0))
        {
          solution = "missing: ";
          bool first = true;
          foreach (string s in missingVariables.Keys)
          {
            if (!first)
              solution += ", ";
            first = false;
            solution += s;
          }
        }
      }
*/
      return true;
    }

    // sucht die erste Klammer mit der maximalen Verschachtelungstiefe
    private int firstAuf; 
    private int nextZu;
    
    private boolean getFirstKlammer()
    {
      firstAuf = -1;
      nextZu = -1;
      int max = 0;
      int tiefe = 0;
      boolean found = false;
      int pos = 0;
      boolean useNextZu = false;
      char[] chars = new char[text.length()];
      text.getChars(0, text.length(), chars, 0);
      for (char c : chars)
      {
        if ((c == '(') || (c == '[') || (c == '{'))
        {
          tiefe++;
          if (tiefe > max)
          {
            // dies ist bisher die erste Klammer mit der Verschachtelungstiefe tiefe
            max = tiefe;
            firstAuf = pos;
            found = true;
            useNextZu = true;
          }
        }
        else if ((c == ')') || (c == ']') || (c == '}'))
        {
          tiefe--;
          if (useNextZu)
          {
            nextZu = pos;
            useNextZu = false;
          }
        }
        pos++;
      }
      return found;
    }

    private void searchOperators(boolean nurGleich)
    {
        int ie = 0;

        for (ie = 0; ie < entities.size(); ie++)
        {
        	Object[] coll = entities.values().toArray();
      	  	Entity entity = (Entity) coll[ie];
        	if (!(entity instanceof TempEntity))
        		continue;
        	TempEntity tEntity = (TempEntity)entity;
        	if (tEntity.Text.equals(""))
        		continue;
        	if (tEntity.Text.substring(0, 1).equals('"')) continue;   // in String mit Anfuehrungszeichen kann kein Operator stecken!

        	for (ArrayList<String> ops : Solver.operatoren.values())
        	{
        		while (true)
        		{
        			int pos = -1;
        			String op = "";
        			for (String top : ops)
        			{
        				if (nurGleich && (!top.equals("=")))
        					continue;
        				// letztes Auftreten eines dieser Operatoren suchen
        				int tpos = tEntity.Text.lastIndexOf(top);
        				if ((tpos >= 0) && (tpos > pos))
        				{
        					pos = tpos;
        					op = top;
        				}
        			}
        			
        			if (pos >= 1)
        			{
        				// OperatorEntity einfuegen
        				TempEntity links = new TempEntity(-1, tEntity.Text.substring(0, pos));
        				entities.Insert(links);
        				TempEntity rechts = new TempEntity(-1, tEntity.Text.substring(pos + op.length(), tEntity.Text.length()));
        				entities.Insert(rechts);
        				Entity oEntity;
        				if (op == "=")
        					oEntity = new ZuweisungEntity(-1, links, rechts);
        				else
        					oEntity = new OperatorEntity(-1, links, op, rechts);
        				String var = entities.Insert(oEntity);
        				tEntity.Text = var;
        			}
        			else
        				break;
        		}
        	}
        }
        entities.Pack();
    }

    private void searchFunctionParameters()
    {
      int ie = 0;

      for (ie = 0; ie < entities.size(); ie++)
      {
    	  Object[] coll = entities.values().toArray();
    	  Entity entity = (Entity) coll[ie];
    	  if (!(entity instanceof TempEntity))
    		  continue;
        TempEntity tEntity = (TempEntity)entity;
        if (tEntity.IsLinks)
          continue;
        String s = tEntity.Text.trim();

        ParameterEntity pEntity = new ParameterEntity(entity.Id);

        while (s.length() > 0)
        {
          s = s.trim();
          // neues Entity bis zum naechsten ";" oder bis zum Ende
          int pos = s.indexOf(';');

          if (pos < 0)
            pos = s.length();
          TempEntity te = new TempEntity(-1, s.substring(0, pos));
          pEntity.Liste.add(te);

          if (pos == s.length())
            s = "";
          else
            s = s.substring(pos + 1, s.length());
        }
        if (pEntity.Liste.size() > 1)
        {
        	// Auflistung nur erstellen, wenn mehr als 1 Eintrag!!!
        	for (Entity eee : entities.values())
        		eee.ReplaceTemp(entity, pEntity);
        	entities.put(entity.Id, pEntity);

        	for (Entity ent : pEntity.Liste)
        		entities.Insert(ent);
        }
      }
      entities.Pack();
    }
/*
    private void searchFunctions()
    {
      // functionen heraussuchen
      int ie = 0;
      for (ie = 0; ie < entities.Count; ie++)
      {
        Entity entity = entities.Values[ie];
        TempEntity tEntity = entity as TempEntity;
        if (tEntity == null) continue;
        if (tEntity.Text == "") continue;
        if (tEntity.Text[0] == '"') continue;   // in String mit Anfuehrungszeichen kann keine Funktion stecken!
        while (true)
        {
          if (!CBSolver.Solver.functions.InsertEntities(tEntity, entities))
            break;
        }
      }
      entities.Pack();
    }
*/
    private void searchStrings()
    {
        int ie = 0;

        for (ie = 0; ie < entities.size(); ie++)
        {
        	Object[] coll = entities.values().toArray();
      	  	Entity entity = (Entity) coll[ie];
        	if (!(entity instanceof TempEntity))
        		continue;
        	TempEntity tEntity = (TempEntity)entity;

      	  	if (tEntity.IsLinks)
      	  		continue;
      	  	String s = tEntity.Text.trim();

      	  	AuflistungEntity aEntity = new AuflistungEntity(entity.Id);
      	  	while (s.length() > 0)
      	  	{
      	  		s = s.trim();
      	  		int pos = s.indexOf('"');
      	  		if (pos < 0) break;
      	  		int pos2 = s.indexOf('"', pos + 1);
      	  		if (pos2 < pos) break;
      	  		// String von pos bis pos2
      	  		if (pos > 0)
      	  		{
      	  			// alles vor dem ersten "" abtrennen
      	  			TempEntity te = new TempEntity(-1, s.substring(0, pos));
      	  			aEntity.Liste.add(te);
      	  			s = s.substring(pos, s.length() - pos);
      	  			pos2 -= pos;
      	  			pos = 0;
      	  		}
      	  		// String abtrennen
      	  		TempEntity te2 = new TempEntity(-1, s.substring(0, pos2 + 1));
      	  		aEntity.Liste.add(te2);
      	  		s = s.substring(pos2 + 1, s.length());
      	  	}
      	  	if ((aEntity.Liste.size() > 1) || ((aEntity.Liste.size() == 1) && (s.length() > 0)))
      	  	{
      	  		if (s.length() > 0)
      	  		{
      	  			// Rest auch noch in die Auflistung aufnehmen
      	  			TempEntity te = new TempEntity(-1, s);
      	  			aEntity.Liste.add(te);
      	  		}
      	  		// Auflistung nur erstellen, wenn mehr als 1 Eintrag!!!
            	for (Entity eee : entities.values())
            		eee.ReplaceTemp(entity, aEntity);
            	entities.put(entity.Id, aEntity);

            	for (Entity ent : aEntity.Liste)
            		entities.Insert(ent);
      	  	}
        }
        entities.Pack();
    }
/*
    internal static bool IsOperator(string s)
    {
      foreach (List<string> olist in CBSolver.Solver.operatoren.Values)
      {
        foreach (string op in olist)
        {
          if (op == s)
            return true;
        }
      }
      return false;
    }
    internal class tmpListEntity
    {
      internal string text;
      internal bool isFunction = false;
      internal bool isOperator = false;
      internal bool isVariable = false;
      internal tmpListEntity(string text)
      {
        this.text = text;
        if (text.Length == 0)
          return;
        this.isFunction = CBSolver.Solver.functions.isFunction(text);
        this.isOperator = IsOperator(text);
        isVariable = ((text[0] == '#') && (text[text.Length - 1] == '#'));
      }
    }
*/
    private void searchLists()
    {
/*        int ie = 0;

        for (ie = 0; ie < entities.size(); ie++)
        {
        	Object[] coll = entities.values().toArray();
      	  	Entity entity = (Entity) coll[ie];
        	if (!(entity instanceof TempEntity))
        		continue;
        	TempEntity tEntity = (TempEntity)entity;
        	if (tEntity.IsLinks)
        		continue;
        	String s = tEntity.Text.trim();
        	if (s == "")
        		continue;
        	if (s.substring(0, 1).equals('"'))
        		continue;    // im String nichts trennen
        	AuflistungEntity aEntity = new AuflistungEntity(entity.Id);

        	ArrayList<tmpListEntity> sList = new ArrayList<tmpListEntity>();
        string tmp = "";
        bool isVariable = false;
        for (int ii = 0; ii < s.Length; ii++)
        {
          if ((s[ii] == ' ') && (!isVariable))
          {
            if (tmp.Length > 0)
              sList.Add(new tmpListEntity(tmp));
            tmp = "";
          }
          else if (s[ii] == '#')
          {
            if (!isVariable)
            {
              if (tmp.Length > 0)
                sList.Add(new tmpListEntity(tmp));
              tmp = "#";
              isVariable = true;  // hier beginnt eine interne Variable
            }
            else
            {
              isVariable = false;  // hier endet die Variable
              sList.Add(new tmpListEntity(tmp + "#"));
              tmp = "";
            }
          }
          else if (IsOperator(s[ii].ToString()))
          {
            if (tmp.Length > 0)
              sList.Add(new tmpListEntity(tmp));
            tmp = s[ii].ToString();
            sList.Add(new tmpListEntity(tmp));
            tmp = "";
          } else
            tmp += s[ii];
        }
        if (tmp.Length > 0)
          sList.Add(new tmpListEntity(tmp));
        tmp = "";
        for (int li = 0; li < sList.Count; li++)
        {
          tmpListEntity tmp1 = sList[li];
          tmp += tmp1.text;
          if (li == sList.Count - 1)
          {
            TempEntity temp = new TempEntity(-1, tmp);
            aEntity.Liste.Add(temp);
            break;
          }
          tmpListEntity tmp2 = sList[li + 1];
          bool trennen = true;
          // nicht trennen, wenn eine der beiden Seiten ein Operator ist
          if ((tmp1.isOperator) || (tmp2.isOperator))
            trennen = false;
          // nicht trennen nach funktionsnamen
          if (tmp1.isFunction)
            trennen = false;
          
          if (trennen)
          {
            TempEntity temp = new TempEntity(-1, tmp);
            aEntity.Liste.Add(temp);
            tmp = "";
          }
        }
        if (aEntity.Liste.Count > 1)
        {
          // Auflistung nur erstellen, wenn mehr als 1 Eintrag!!!
          foreach (Entity eee in entities.Values)
            eee.ReplaceTemp(entities[entities.Keys[ie]], aEntity);
          entities[entities.Keys[ie]] = aEntity;

          foreach (Entity ent in aEntity.Liste)
            entities.Insert(ent);
        }

      }
*/
      entities.Pack();
    }
/*
    private void searchVariables()
    {
      int ie = 0;
      for (ie = 0; ie < entities.Count; ie++)
      {
        Entity entity = entities.Values[ie];
        TempEntity tEntity = entity as TempEntity;
        if (tEntity == null) continue;
        string s = tEntity.Text.Trim();
        if (s == "") break;
        if (s[0] == '$')
        {
          // GC-Koordinate suchen
          CoordinateEntity cEntity = new CoordinateEntity(-1, s.Substring(1, s.Length - 1));
          string var = entities.Insert(cEntity);
          tEntity.Text = var;
          s = "";
        }
        if ((s.Length >= 2) && (s[0] == '"') && (s[s.Length - 1] == '"'))
        {
          // dies ist ein String -> in StringEntity umwandeln
          StringEntity sEntity = new StringEntity(-1, s.Substring(1, s.Length - 2));
          string var = entities.Insert(sEntity);
          tEntity.Text = var;
          s = "";
        }
        if (s.Length > 0)
        {
          // evtl. als Zahl versuchen
          try
          {
            string sz = s;
            sz = sz.Replace(".", Global.DecimalSeparator);
            sz = sz.Replace(",", Global.DecimalSeparator);

            double zahl = Convert.ToDouble(sz);
            ConstantEntity cEntity = new ConstantEntity(-1, zahl);
            string var = entities.Insert(cEntity);
            tEntity.Text = var;
            s = "";
          }
          catch (Exception)
          {
            // Exception -> keine Zahl
          }
        }
        if (s.Length > 0)
        {
          // evtl. eine Variable
          if (tEntity.IsLinks)
          {
            // Variable bei Bedarf erzeugen
            if (!CBSolver.Solver.variablen.ContainsKey(s.ToLower()))
              CBSolver.Solver.variablen.Add(s.ToLower(), "");
          }
          if (CBSolver.Solver.variablen.ContainsKey(s.ToLower()))
          {
            VariableEntity vEntity = new VariableEntity(-1, s.ToLower());
            string var = entities.Insert(vEntity);
            tEntity.Text = var;
            s = "";
          }
        }

      }
      entities.Pack();
    }
*/
}

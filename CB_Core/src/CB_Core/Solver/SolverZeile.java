package CB_Core.Solver;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class SolverZeile
{
	private String text;
	private String orgText;
	private EntityList entities;
	public String Solution = "";
	private Solver solver;

	public SolverZeile(Solver solver, String text)
	{
		this.solver = solver;
		this.text = text;
		this.orgText = text;
		entities = new EntityList(solver);
	}

	public boolean Parse()
	{
		entities.clear();
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
				entities.Insert(text);
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

		// alle Operatoren herausfinden
		searchOperators(false);
		debug = entities.ToString();

		// Functionen suchen
		searchFunctions();
		debug = entities.ToString();

		// Variablen, Konstanten und Strings
		searchVariables();
		debug = entities.ToString();

		entities.Pack();
		debug = entities.ToString();

		// Alle Entities holen, die Bestandteil eines anderen sind
		ArrayList<Entity> list = new ArrayList<Entity>();
		for (Entity ent : entities.values())
		{
			ent.GetAllEntities(list);
		}
		// diese koennen dann geloescht werden
		for (Entity ent : list)
		{
			entities.remove(ent.Id);
		}
		// es sollte nur 1 Entity uebrig bleiben. Dies beinhaltet dann die komplette Formel
		if (entities.values().size() >= 1)
		{
			Solution = entities.get(entities.firstKey()).Berechne();
			entities.get(entities.firstKey()).GetAllEntities(list);

			SortedMap<String, Integer> missingVariables = null;
			for (Entity tent : list)
			{
				// store Missing Variables in global List
				// store Missing Variables in local List too (only for this line)
				if (tent instanceof TempEntity)
				{
					TempEntity tempent = (TempEntity) tent;
					if (tempent.Text.trim().equals("")) continue;
					// store in global list
					if (solver.MissingVariables == null) solver.MissingVariables = new TreeMap<String, Integer>();
					if (!solver.MissingVariables.containsKey(tempent.Text)) solver.MissingVariables.put(tempent.Text, 0);
					// store in local list
					if (missingVariables == null) missingVariables = new TreeMap<String, Integer>();
					if (!missingVariables.containsKey(tempent.Text)) missingVariables.put(tempent.Text, 0);
				}
			}
			if ((missingVariables != null) && (missingVariables.size() > 0))
			{
				Solution = "missing: ";
				boolean first = true;
				for (String s : missingVariables.keySet())
				{
					if (!first) Solution += ", ";
					first = false;
					Solution += s;
				}
			}
		}

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
			if (!(entity instanceof TempEntity)) continue;
			TempEntity tEntity = (TempEntity) entity;
			if (tEntity.Text.equals("")) continue;
			if (tEntity.Text.substring(0, 1).equals("\"")) continue; // in String mit Anfuehrungszeichen kann kein Operator stecken!

			for (ArrayList<String> ops : solver.operatoren.values())
			{
				while (true)
				{
					int pos = -1;
					String op = "";
					for (String top : ops)
					{
						if (nurGleich && (!top.equals("="))) continue;
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
						TempEntity links = new TempEntity(solver, -1, tEntity.Text.substring(0, pos));
						entities.Insert(links);
						TempEntity rechts = new TempEntity(solver, -1, tEntity.Text.substring(pos + op.length(), tEntity.Text.length()));
						entities.Insert(rechts);
						Entity oEntity;
						if (op.equals("=")) oEntity = new ZuweisungEntity(solver, -1, links, rechts);
						else
							oEntity = new OperatorEntity(solver, -1, links, op, rechts);
						String var = entities.Insert(oEntity);
						tEntity.Text = var;
					}
					else
						break;
				}
			}
		}
		String debug = entities.ToString();
		entities.Pack();
	}

	private void searchFunctionParameters()
	{
		int ie = 0;

		for (ie = 0; ie < entities.size(); ie++)
		{
			Object[] coll = entities.values().toArray();
			Entity entity = (Entity) coll[ie];
			if (!(entity instanceof TempEntity)) continue;
			TempEntity tEntity = (TempEntity) entity;
			if (tEntity.IsLinks) continue;
			String s = tEntity.Text.trim();

			ParameterEntity pEntity = new ParameterEntity(solver, entity.Id);

			while (s.length() > 0)
			{
				s = s.trim();
				// neues Entity bis zum naechsten ";" oder bis zum Ende
				int pos = s.indexOf(';');

				if (pos < 0) pos = s.length();
				TempEntity te = new TempEntity(solver, -1, s.substring(0, pos));
				pEntity.Liste.add(te);

				if (pos == s.length()) s = "";
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

	private void searchFunctions()
	{
		// functionen heraussuchen
		int ie = 0;

		for (ie = 0; ie < entities.size(); ie++)
		{
			Object[] coll = entities.values().toArray();
			Entity entity = (Entity) coll[ie];
			if (!(entity instanceof TempEntity)) continue;
			TempEntity tEntity = (TempEntity) entity;
			if (tEntity.Text.equals("")) continue;
			if (tEntity.Text.substring(0, 1).equals("\"")) continue; // in String mit Anfuehrungszeichen kann keine Funktion stecken!
			while (true)
			{
				if (!solver.functions.InsertEntities(tEntity, entities)) break;
			}
		}
		entities.Pack();
	}

	private void searchStrings()
	{
		int ie = 0;

		for (ie = 0; ie < entities.size(); ie++)
		{
			Object[] coll = entities.values().toArray();
			Entity entity = (Entity) coll[ie];
			if (!(entity instanceof TempEntity)) continue;
			TempEntity tEntity = (TempEntity) entity;

			if (tEntity.IsLinks) continue;
			String s = tEntity.Text.trim();

			AuflistungEntity aEntity = new AuflistungEntity(solver, entity.Id);
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
					TempEntity te = new TempEntity(solver, -1, s.substring(0, pos));
					aEntity.Liste.add(te);
					s = s.substring(pos, s.length());
					pos2 -= pos;
					pos = 0;
				}
				// String abtrennen
				TempEntity te2 = new TempEntity(solver, -1, s.substring(0, pos2 + 1));
				aEntity.Liste.add(te2);
				s = s.substring(pos2 + 1, s.length());
			}
			if ((aEntity.Liste.size() > 1) || ((aEntity.Liste.size() == 1) && (s.length() > 0)))
			{
				if (s.length() > 0)
				{
					// Rest auch noch in die Auflistung aufnehmen
					TempEntity te = new TempEntity(solver, -1, s);
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

	boolean IsOperator(String s)
	{
		for (ArrayList<String> olist : solver.operatoren.values())
		{
			for (String op : olist)
			{
				if (op.equals(s)) return true;
			}
		}
		return false;
	}

	private class tmpListEntity
	{
		String text;
		boolean isFunction = false;
		boolean isOperator = false;
		boolean isVariable = false;

		tmpListEntity(String text)
		{
			this.text = text;
			if (text.length() == 0) return;
			this.isFunction = solver.functions.isFunction(text);
			this.isOperator = IsOperator(text);
			isVariable = ((text.substring(0, 1).equals("#")) && (text.substring(text.length() - 1, text.length()).equals("#")));
		}
	}

	private void searchLists()
	{
		int ie = 0;

		for (ie = 0; ie < entities.size(); ie++)
		{
			Object[] coll = entities.values().toArray();
			Entity entity = (Entity) coll[ie];
			if (!(entity instanceof TempEntity)) continue;
			TempEntity tEntity = (TempEntity) entity;
			if (tEntity.IsLinks) continue;
			String s = tEntity.Text.trim();
			if (s.equals("")) continue;
			if (s.substring(0, 1).equals("\"")) continue; // im String nichts trennen
			AuflistungEntity aEntity = new AuflistungEntity(solver, entity.Id);

			ArrayList<tmpListEntity> sList = new ArrayList<tmpListEntity>();
			String tmp = "";
			boolean isVariable = false;
			for (int ii = 0; ii < s.length(); ii++)
			{
				if ((s.substring(ii, ii + 1).equals(" ")) && (!isVariable))
				{
					if (tmp.length() > 0) sList.add(new tmpListEntity(tmp));
					tmp = "";
				}
				else if (s.substring(ii, ii + 1).equals("#"))
				{
					if (!isVariable)
					{
						if (tmp.length() > 0) sList.add(new tmpListEntity(tmp));
						tmp = "#";
						isVariable = true; // hier beginnt eine interne Variable
					}
					else
					{
						isVariable = false; // hier endet die Variable
						sList.add(new tmpListEntity(tmp + "#"));
						tmp = "";
					}
				}
				else if (IsOperator(s.substring(ii, ii + 1).toString()))
				{
					if (tmp.length() > 0) sList.add(new tmpListEntity(tmp));
					tmp = s.substring(ii, ii + 1).toString();
					sList.add(new tmpListEntity(tmp));
					tmp = "";
				}
				else
					tmp += s.substring(ii, ii + 1);
			}
			if (tmp.length() > 0) sList.add(new tmpListEntity(tmp));
			tmp = "";
			for (int li = 0; li < sList.size(); li++)
			{
				tmpListEntity tmp1 = sList.get(li);
				tmp += tmp1.text;
				if (li == sList.size() - 1)
				{
					TempEntity temp = new TempEntity(solver, -1, tmp);
					aEntity.Liste.add(temp);
					break;
				}
				tmpListEntity tmp2 = sList.get(li + 1);
				boolean trennen = true;
				// nicht trennen, wenn eine der beiden Seiten ein Operator ist
				if ((tmp1.isOperator) || (tmp2.isOperator)) trennen = false;
				// nicht trennen nach funktionsnamen
				if (tmp1.isFunction) trennen = false;

				if (trennen)
				{
					TempEntity temp = new TempEntity(solver, -1, tmp);
					aEntity.Liste.add(temp);
					tmp = "";
				}
			}
			if (aEntity.Liste.size() > 1)
			{
				// Auflistung nur erstellen, wenn mehr als 1 Eintrag!!!
				for (Entity eee : entities.values())
					eee.ReplaceTemp(entity, aEntity);
				// eee.ReplaceTemp(entities[entities.Keys[ie]], aEntity);
				entities.put(entity.Id, aEntity);

				for (Entity ent : aEntity.Liste)
					entities.Insert(ent);
			}

		}

		entities.Pack();
	}

	private void searchVariables()
	{
		int ie = 0;

		for (ie = 0; ie < entities.size(); ie++)
		{
			Object[] coll = entities.values().toArray();
			Entity entity = (Entity) coll[ie];
			if (!(entity instanceof TempEntity)) continue;
			TempEntity tEntity = (TempEntity) entity;
			String s = tEntity.Text.trim();
			if (s == null || s.length() < 1) continue;
			if (s.charAt(0) == '$')
			{
				// GC-Koordinate suchen
				CoordinateEntity cEntity = new CoordinateEntity(solver, -1, s.substring(1, s.length()));
				String var = entities.Insert(cEntity);
				tEntity.Text = var;
				s = "";
			}
			if ((s.length() >= 2) && (s.charAt(0) == '"') && (s.charAt(s.length() - 1) == '"'))
			{
				// dies ist ein String -> in StringEntity umwandeln
				StringEntity sEntity = new StringEntity(solver, -1, s.substring(1, s.length() - 1));
				String var = entities.Insert(sEntity);
				tEntity.Text = var;
				s = "";
			}
			if (s.length() > 0)
			{
				// evtl. als Zahl versuchen
				try
				{
					String sz = s;
					char sep = '.';
					sz = sz.replace('.', sep);
					sz = sz.replace(',', sep);

					double zahl = Double.valueOf(sz);
					ConstantEntity cEntity = new ConstantEntity(solver, -1, zahl);
					String var = entities.Insert(cEntity);
					tEntity.Text = var;
					s = "";
				}
				catch (Exception ex)
				{
					// Exception -> keine Zahl
				}
			}
			if (s.length() > 0)
			{
				// evtl. eine Variable
				if (tEntity.IsLinks)
				{
					// Variable bei Bedarf erzeugen
					if (!solver.Variablen.containsKey(s.toLowerCase())) solver.Variablen.put(s.toLowerCase(), "");
				}
				if (solver.Variablen.containsKey(s.toLowerCase()))
				{
					VariableEntity vEntity = new VariableEntity(solver, -1, s.toLowerCase());
					String var = entities.Insert(vEntity);
					tEntity.Text = var;
					s = "";
				}
			}
		}
		entities.Pack();
	}

	public void setText(String zeile)
	{
		text = zeile;
		orgText = zeile;
	}

	public String getText()
	{
		return text;
	}

	public String getOrgText()
	{
		return orgText;
	}
}

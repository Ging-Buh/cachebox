package de.droidcachebox.Solver;

import java.util.ArrayList;

public class OperatorEntity extends Entity {
    private Entity links;
    private Entity rechts;
    private String op;
    public OperatorEntity(int id, Entity links, String op, Entity rechts)
    {
    	super(id);
    	this.links = links;
    	this.op = op;
    	this.rechts = rechts;
    }

    @Override
    public void ReplaceTemp(Entity source, Entity dest)
    {
      if (links == source)
        links = dest;
      if (rechts == source)
        rechts = dest;
    }

    @Override
    public void GetAllEntities(ArrayList<Entity> list)
    {
      list.add(links);
      list.add(rechts);
    }

    public static String DoubleToString(double wert)
    {
    	String value = String.valueOf(wert);
    	if (Math.round(wert) == wert)
    		value = String.valueOf((int)(Math.round(wert)));    	
    	return value;
    }
    @Override
    public String Berechne()
    {
      String lLinks = links.Berechne();
      String lRechts = rechts.Berechne();
      String result = "";
      try
      {
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
      }
      catch (Exception ex)
      {
    	  // Fehler ausgeben.
    	  return ex.getMessage();
      }
      return result;
    }

    @Override
    public String ToString()
    {
      return "O" + Id + op + "(" + links.ToString() + "," + rechts.ToString() + ")";
    }

}

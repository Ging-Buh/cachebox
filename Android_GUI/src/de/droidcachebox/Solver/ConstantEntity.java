package de.droidcachebox.Solver;

import java.util.ArrayList;

public class ConstantEntity extends Entity {

	double wert;
    public ConstantEntity(int id, double wert)
    {
    	super(id);
    	this.wert = wert;
    }
  
    @Override
    public void GetAllEntities(ArrayList<Entity> list)
    {
    }

    @Override
    public void ReplaceTemp(Entity source, Entity dest)
    {
    }

    @Override
    public String Berechne()
    {
    	String value = String.valueOf(wert);
    	if (Math.round(wert) == wert)
    		value = String.valueOf((int)(Math.round(wert)));
    	return value;
    }
   
    @Override
    public String ToString()
    {
    	String value = String.valueOf(wert);
    	if (Math.round(wert) == wert)
    		value = String.valueOf((int)(Math.round(wert)));
    	return "C" + Id + ":(" + value + ")";
    }

}

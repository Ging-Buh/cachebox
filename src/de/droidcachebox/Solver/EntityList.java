package de.droidcachebox.Solver;

import java.util.TreeMap;

public class EntityList extends TreeMap<Integer, Entity> {

	private static final long serialVersionUID = 1L;

	public String Insert(String anweisung)
    {
    	int id = this.size();
    	this.put(id, new TempEntity(id, anweisung));
    	return "#" + id + "#";
    }
    public String Insert(Entity entity)
    {
    	int id = this.lastKey() + 1;
    	entity.Id = id;
    	this.put(id, entity);
    	return "#" + id + "#";
    }

    // anhand dem Text #3# das Entity nr. 3 zurueckliefern.
    // Wenn der Text nicht diesem Kriterium entspricht -> null
    private Entity getEntity(String var)
    {
    	var.trim();
    	if (var.length() < 3) return null;
    	if (!var.substring(0, 1).equals('#')) return null;
    	if (!var.substring(var.length() - 1).equals('#')) return null;
    	String sNr = var.substring(1, var.length() - 2);
    	try
    	{
    		int id = Integer.valueOf(sNr);
    		return this.get(id);
    	}
    	catch (Exception exc)
    	{
    		return null;
    	}
    }

    public void Pack()
    {
      // alle TempEntities herausloeschen, die nur einen Verweis auf ein anderes TempEntity haben
      int ie = 0;
      for (ie = 0; ie < this.size(); ie++)
      {
        Entity entity = this.get(ie);
        if (!(entity instanceof TempEntity))
        	continue;
        TempEntity tEntity = (TempEntity)entity;
        Entity inhalt = getEntity(tEntity.Text);
        if (inhalt != null)
        {
          // dieses Entity loeschen und alle Verweise auf dieses Entity umleiten auf inhalt
          for (Entity ee : this.values())
          {
            ee.ReplaceTemp(entity, inhalt);
            /*            TempEntity tee = ee as TempEntity;
                        if (tee == null) continue;
                        tee.Text = tee.Text.Replace("#" + tEntity.Id + "#", "#" + inhalt.Id + "#");*/
          }
          this.remove(entity.Id);
          ie--;
        }
      }
    }
    public String ToString()
    {
    	String result = "";
    	for (Entity entity : this.values())
    	{
    		result += entity.ToString() + "\n";
    	}
    	return result;
    }
}

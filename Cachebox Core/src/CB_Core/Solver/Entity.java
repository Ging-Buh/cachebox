package CB_Core.Solver;

import java.util.ArrayList;

public class Entity {
    protected int Id;
    protected boolean IsLinks;   // wird auf true, wenn dies links vom = ist.
    public Entity(int id)
    {
      this.Id = id;
      IsLinks = false;
    }

    // alle Vorkommen von source durch dest ersetzen, da source nur ein Verweis auf dest ist!
    public void ReplaceTemp(Entity source, Entity dest)
    {
    }

    // alle Entities herausgeben, die in diesem enthalten sind
    public void GetAllEntities(ArrayList<Entity> list)
    {
    }

    public String Berechne()
    {
    	return "";
    }

	public String ToString() {
		return "";
	}
}

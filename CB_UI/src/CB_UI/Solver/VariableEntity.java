package CB_UI.Solver;

import java.util.ArrayList;

public class VariableEntity extends Entity {
	  // Speichert einen Wert in eine Variable
	String Name;
	public VariableEntity(int id, String name) 
	{
		super(id);
		this.Name = name;
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
		if (Solver.Variablen.containsKey(Name.toLowerCase()))
		{
			return Solver.Variablen.get(Name.toLowerCase());
		}
		else
			return "Fehler";
	}

	@Override
	public String ToString()
	{
		return "V" + Id + ":(" + Name + ")";
	}
}
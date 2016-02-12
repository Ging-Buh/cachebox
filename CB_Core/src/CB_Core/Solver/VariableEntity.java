package CB_Core.Solver;

import java.util.ArrayList;

public class VariableEntity extends Entity {
	// Speichert einen Wert in eine Variable
	String Name;

	public VariableEntity(Solver solver, int id, String name) {
		super(solver, id);
		this.Name = name;
	}

	@Override
	public void GetAllEntities(ArrayList<Entity> list) {
	}

	@Override
	public void ReplaceTemp(Entity source, Entity dest) {
	}

	@Override
	public String Berechne() {
		if (solver.Variablen.containsKey(Name.toLowerCase())) {
			return solver.Variablen.get(Name.toLowerCase());
		} else
			return "Fehler";
	}

	@Override
	public String ToString() {
		return "V" + Id + ":(" + Name + ")";
	}
}

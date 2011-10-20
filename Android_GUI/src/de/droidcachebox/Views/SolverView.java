package de.droidcachebox.Views;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Events.ViewOptionsMenu;

import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SolverView extends FrameLayout implements ViewOptionsMenu
{
	Context context;
	EditText edSolver;
	EditText edResult;
	Cache aktCache;
	boolean mustLoadSolver;
	Button bSolve;

	public SolverView(Context context, LayoutInflater inflater)
	{
		super(context);
		mustLoadSolver = false;
		RelativeLayout solverLayout = (RelativeLayout) inflater.inflate(main.N ? R.layout.night_solverview : R.layout.solverview, null,
				false);
		this.addView(solverLayout);
		edSolver = (EditText) findViewById(R.id.solverText);
		edResult = (EditText) findViewById(R.id.solverResult);
		bSolve = (Button) findViewById(R.id.solverButtonSolve);
		bSolve.setText("Solve");
		bSolve.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				solve();
			}
		});
		SetSelectedCache(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());
	}

	protected void solve()
	{
		Solver solver = new Solver(edSolver.getText().toString());
		if (!solver.Solve())
		{
			Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
		}
		edResult.setText("");
		String result = "";
		for (SolverZeile zeile : solver)
		{
			result += zeile.Solution + "\n";
		}

		edResult.setText(result);

		if ((Solver.MissingVariables != null) && (Solver.MissingVariables.size() > 0))
		{
			// es sind nicht alle Variablen zugewiesen
			// Abfrage, ob die Deklarationen eingefügt werden sollen
			String message = "";
			for (String s : Solver.MissingVariables.keySet())
			{
				if (message != "") message += ", ";
				message += s;
			}

			MessageBox.Show("Insert declarations for the missing variables:\n" + message, "Missing Variables", MessageBoxButtons.YesNo,
					MessageBoxIcon.Asterisk, DialogListner);
		}

	}

	private final DialogInterface.OnClickListener DialogListner = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int button)
		{
			// Behandle das ergebniss
			switch (button)
			{
			case -1:
				/* User clicked OK so do some stuff */
				String missing = "";
				for (String s : Solver.MissingVariables.keySet())
				{
					missing += s + "=\n";
					edResult.setText("\n" + edSolver.getText().toString());
				}
				edSolver.setText(missing + edSolver.getText().toString());
				break;
			case -2:

				break;
			case -3:

				break;
			}

			dialog.dismiss();
		}

	};

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			mustLoadSolver = true;
			aktCache = cache;
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item)
	{
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu)
	{

	}

	@Override
	public void OnShow()
	{
		if (mustLoadSolver)
		{
			edSolver.setText(Database.GetSolver(aktCache));
			mustLoadSolver = false;
		}
	}

	@Override
	public void OnHide()
	{
		// Save changed Solver text
		if (aktCache != null) Database.SetSolver(aktCache, edSolver.getText().toString());
	}

	@Override
	public void OnFree()
	{

	}

	@Override
	public int GetMenuId()
	{
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{

	}

	@Override
	public int GetContextMenuId()
	{
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu)
	{

	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item)
	{
		return false;
	}

}

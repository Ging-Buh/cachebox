package de.droidcachebox.Views;

import de.droidcachebox.Database;
import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;

import de.droidcachebox.Solver.Solver;
import de.droidcachebox.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SolverView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent{
	Context context;
	EditText edSolver;
	EditText edResult;
	Cache aktCache;
	boolean mustLoadSolver;
	Button bSolve;

	public SolverView(Context context, LayoutInflater inflater) {
		super(context);
		mustLoadSolver = false;
		SelectedCacheEventList.Add(this);

		RelativeLayout solverLayout = (RelativeLayout)inflater.inflate(R.layout.solverview, null, false);
		this.addView(solverLayout);
        edSolver = (EditText) findViewById(R.id.solverText);
        edSolver.setTextColor(Color.BLACK);
        edResult = (EditText) findViewById(R.id.solverResult);
        bSolve = (Button) findViewById(R.id.solverButtonSolve);
        bSolve.setText("Solve");
        bSolve.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	solve();
            }
        });
        
	}

	protected void solve() {
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
	        	if (message != "")
	        		message += ", ";
	        	message += s;
	        }

	        //	        if (MessageBox.Show("Insert declarations for the missing variables:" + Environment.NewLine + message, "Missing variables", MessageBoxButtons.YesNo, MessageBoxIcon.Question) == DialogResult.Yes)
            AlertDialog dialog = new AlertDialog.Builder(getContext())
//            	.setIcon(R.drawable.alert_dialog_icon)
            	.setTitle("Missing Variables")
            	.setMessage("Insert declarations for the missing variables:\n" + message)
            	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int whichButton) {
            			/* User clicked OK so do some stuff */
                    	String missing = "";
                    	for (String s : Solver.MissingVariables.keySet())
                    	{
                    		missing += s + "=\n";
                    		edResult.setText("\n" + edSolver.getText().toString());
                    	}
                    	edSolver.setText(missing + edSolver.getText().toString());
            		}
            	})
            	.setNegativeButton("No", new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int whichButton) {
            			
            			/* User clicked Cancel so do some stuff */
            		}
            	})
            	.create();
	        dialog.show();
	        
	        
	        
	        
	    }

	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (aktCache != cache)
		{
			mustLoadSolver = true;
			aktCache = cache;
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnShow() {
		// TODO Auto-generated method stub
		if (mustLoadSolver)
		{
			edSolver.setText(Database.GetSolver(aktCache));
			mustLoadSolver = false;
		}
	}

	@Override
	public void OnHide() {
		// Save changed Solver text
		Database.SetSolver(aktCache,edSolver.getText().toString());		
	}

	@Override
	public void OnFree() {
		
	}
	
	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

}

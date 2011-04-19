package de.droidcachebox.Views;

import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Solver.Solver;
import de.droidcachebox.Solver.SolverZeile;
import android.content.Context;
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
        edResult.setEnabled(false);
        bSolve = (Button) findViewById(R.id.solverButtonSolve);
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
/*		
	      if ((Solver.MissingVariables != null) && (Solver.MissingVariables.Count > 0))
	      { 
	        // es sind nicht alle Variablen zugewiesen
	        // Abfrage, ob die Deklarationen eingefügt werden sollen
	        string message = "";
	        foreach (string s in Solver.MissingVariables.Keys)
	        {
	          if (message != "")
	            message += ", ";
	          message += s;
	        }
	        if (MessageBox.Show("Insert declarations for the missing variables:" + Environment.NewLine + message, "Missing variables", MessageBoxButtons.YesNo, MessageBoxIcon.Question) == DialogResult.Yes)
	        {
	          string missing = "";
	          foreach (string s in Solver.MissingVariables.Keys)
	          {
	            missing += s + "=" + Environment.NewLine;
	            tbLösung.Text = Environment.NewLine + tbLösung.Text;
	          }
	          textBox1.Text = missing + textBox1.Text;
	        }
	      }
*/
	    edResult.setText(result);
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
			edSolver.setText(aktCache.GetSolver());
			mustLoadSolver = false;
		}
	}

	@Override
	public void OnHide() {
		// Save changed Solver text
		aktCache.SetSolver(edSolver.getText().toString());		
	}

}

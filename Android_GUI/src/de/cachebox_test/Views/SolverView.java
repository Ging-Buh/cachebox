package de.cachebox_test.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Solver.Functions.Function;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.selectSolverFunction;

public class SolverView extends FrameLayout implements ViewOptionsMenu
{
	Context context;
	EditText edSolver;
	EditText edResult;
	Cache aktCache;
	boolean mustLoadSolver;
	Button bSolve;
	Button bFunct;
	Button bSelect;

	Button bLeft;
	Button bMiddle;
	Button bRight;

	LinearLayout ButtonsLayout;

	public SolverView(Context context, LayoutInflater inflater)
	{
		super(context);
		mustLoadSolver = false;
		LinearLayout solverLayout = (LinearLayout) inflater.inflate(main.N ? R.layout.night_solverview : R.layout.solverview, null, false);

		this.addView(solverLayout);

		findViewById();
		setLang();
		SetSelectedCache(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());

		bSolve.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				solve();
			}
		});

		bFunct.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				// Funktion Auswahl Dialog öffnen
				Intent intent = new Intent().setClass(bFunct.getContext(), selectSolverFunction.class);

				main.mainActivity.startActivityForResult(intent, Global.RESULT_SELECT_SOLVER_FUNCTION);

			}
		});

		bLeft.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LinearLayout.LayoutParams PO = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, .20f);
				LinearLayout.LayoutParams MO = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
				edSolver.setLayoutParams(PO);
				edResult.setLayoutParams(MO);
			}
		});

		bMiddle.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LinearLayout.LayoutParams PO = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
				LinearLayout.LayoutParams MO = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
				edSolver.setLayoutParams(PO);
				edResult.setLayoutParams(MO);
			}
		});

		bRight.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				LinearLayout.LayoutParams PO = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
				LinearLayout.LayoutParams MO = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, .20f);
				edSolver.setLayoutParams(PO);
				edResult.setLayoutParams(MO);
			}
		});

	}

	private void findViewById()
	{
		edSolver = (EditText) findViewById(R.id.solverText);
		edResult = (EditText) findViewById(R.id.solverResult);
		bSolve = (Button) findViewById(R.id.solverButtonSolve);
		bFunct = (Button) findViewById(R.id.solverButtonFunktion);
		bSelect = (Button) findViewById(R.id.solverButtonSelect);

		bLeft = (Button) findViewById(R.id.solverButtonViewLeft);
		bMiddle = (Button) findViewById(R.id.solverButtonViewMidle);
		bRight = (Button) findViewById(R.id.solverButtonViewRight);

		ButtonsLayout = (LinearLayout) findViewById(R.id.solverViewButtons);
	}

	private void setLang()
	{

		bSolve.setText(GlobalCore.Translations.Get("Solve"));
		bFunct.setText(GlobalCore.Translations.Get("Funct."));
		bSelect.setText(GlobalCore.Translations.Get("Select."));

		bLeft.setText(GlobalCore.Translations.Get("LeftWindow"));
		bMiddle.setText(GlobalCore.Translations.Get("BothWindow"));
		bRight.setText(GlobalCore.Translations.Get("RightWindow"));
	}

	protected void solve()
	{
		// Hide Keyboard when Calculating
		showVirturalKeyboard(false);
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

			MessageBox.Show(GlobalCore.Translations.Get("insertVars") + "\n" + message, GlobalCore.Translations.Get("missingVars"),
					MessageBoxButtons.YesNo, MessageBoxIcon.Asterisk, DialogListner);
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
		if (data == null) return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Function function = (Function) bundle.getSerializable("FunctionResult");
			if (function != null)
			{
				CharSequence selection = edSolver.getText().subSequence(edSolver.getSelectionStart(), edSolver.getSelectionEnd());
				// String newFunction = function.getShortcut();
				String newFunction = function.getLongLocalName();
				int newFunctionLength = newFunction.length();
				String zeichen = "";
				if (function.needsTextArgument())
				{
					zeichen = "\"";
					if ((selection.length() > 0) && (selection.charAt(0) == '"'))
					{
						// Anführungszeichen bereits vorhanden
						zeichen = "";
					}
				}
				newFunction += "(" + zeichen + selection + zeichen + ")";
				int newSelectionStart = edSolver.getSelectionStart() + newFunctionLength + 1 + zeichen.length() + selection.length();

				int start = edSolver.getSelectionStart();
				int end = edSolver.getSelectionEnd();
				edSolver.getText().replace(Math.min(start, end), Math.max(start, end), newFunction, 0, newFunction.length());
				edSolver.setSelection(newSelectionStart);
				showVirturalKeyboard(true);
			}
		}
	}

	private void showVirturalKeyboard(final boolean show)
	{
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				InputMethodManager m = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

				if (m != null)
				{
					if (show) m.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
					else
						m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}

		}, 100);
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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
		final int actualHeight = getHeight();

		if (actualHeight > proposedheight)
		{
			// Keyboard is shown set Buttons Gone
			ButtonsLayout.setVisibility(GONE);
			((main) main.mainActivity).setBottomButtonVisibility(GONE);
		}
		else
		{
			// Keyboard is hidden set Buttons Visible
			ButtonsLayout.setVisibility(VISIBLE);
			((main) main.mainActivity).setBottomButtonVisibility(VISIBLE);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}

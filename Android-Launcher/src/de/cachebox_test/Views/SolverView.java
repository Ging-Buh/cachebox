package de.cachebox_test.Views;

import CB_Core.DB.Database;
import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Solver.Functions.Function;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.SelectSolverFunction;
import CB_UI.GL_UI.Activitys.SelectSolverFunction.IFunctionResult;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.settings.CB_UI_Base_Settings;
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
import android.widget.LinearLayout;
import android.widget.Toast;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Views.Forms.MessageBox;

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
	private Solver solver = new Solver("");

	public SolverView(Context context, LayoutInflater inflater)
	{
		super(context);
		mustLoadSolver = false;
		LinearLayout solverLayout = (LinearLayout) inflater.inflate(CB_UI_Base_Settings.nightMode.getValue() ? R.layout.night_solverview
				: R.layout.solverview, null, false);

		this.addView(solverLayout);

		findViewById();
		setLang();
		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());

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
				SelectSolverFunction ssf = new SelectSolverFunction(solver, DataType.None, new IFunctionResult()
				{

					@Override
					public void selectedFunction(final Function function)
					{
						// ausgewählte Funktion verarbeiten!
						// wenn funktion==null wurde Cancel gedrückt

						if (function != null)
						{

							// muss in UI-Thread verarbeitet werden nicht im GL-Thread, wo wir gerade her kommen!

							main.mainActivity.runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									CharSequence selection = edSolver.getText().subSequence(edSolver.getSelectionStart(),
											edSolver.getSelectionEnd());
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
									int newSelectionStart = edSolver.getSelectionStart() + newFunctionLength + 1 + zeichen.length()
											+ selection.length();

									int start = edSolver.getSelectionStart();
									int end = edSolver.getSelectionEnd();
									edSolver.getText().replace(Math.min(start, end), Math.max(start, end), newFunction, 0,
											newFunction.length());
									edSolver.setSelection(newSelectionStart);
									edSolver.invalidate();

								}
							});

						}

					}
				});
				GL.that.showDialog(ssf);
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

		bSolve.setText(Translation.Get("Solve"));
		bFunct.setText(Translation.Get("Funct."));
		bSelect.setText(Translation.Get("Select."));

		bLeft.setText(Translation.Get("LeftWindow"));
		bMiddle.setText(Translation.Get("BothWindow"));
		bRight.setText(Translation.Get("RightWindow"));
	}

	protected void solve()
	{
		// Hide Keyboard when Calculating
		// showVirturalKeyboard(false);
		solver = new Solver(edSolver.getText().toString());
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

		if ((solver.MissingVariables != null) && (solver.MissingVariables.size() > 0))
		{
			// es sind nicht alle Variablen zugewiesen
			// Abfrage, ob die Deklarationen eingefügt werden sollen
			String message = "";
			for (String s : solver.MissingVariables.keySet())
			{
				if (message != "") message += ", ";
				message += s;
			}

			MessageBox.Show(Translation.Get("insertVars") + "\n" + message, Translation.Get("missingVars"), MessageBoxButtons.YesNo,
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
				for (String s : solver.MissingVariables.keySet())
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
		if (aktCache != null)
		{
			Database.SetSolver(aktCache, edSolver.getText().toString());
			// When Solve 1 changes -> Solver 2 must reload the information from DB to get the changes from Solver 1
			aktCache.setSolver1Changed(true);
		}
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

	// private void showVirturalKeyboard(final boolean show)
	// {
	// Timer timer = new Timer();
	// timer.schedule(new TimerTask()
	// {
	// @Override
	// public void run()
	// {
	// InputMethodManager m = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	//
	// if (m != null)
	// {
	// if (show) m.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
	// else
	// m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	// }
	// }
	//
	// }, 100);
	// }

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

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	// {
	//
	// final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
	// final int actualHeight = getHeight();
	//
	// if (actualHeight > proposedheight)
	// {
	// // Keyboard is shown set Buttons Gone
	// ButtonsLayout.setVisibility(GONE);
	//
	// }
	// else
	// {
	// // Keyboard is hidden set Buttons Visible
	// ButtonsLayout.setVisibility(VISIBLE);
	//
	// }
	// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	// }

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{

	}

}

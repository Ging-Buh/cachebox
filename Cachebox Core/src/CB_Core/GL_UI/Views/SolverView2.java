package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.WaypointListChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.EditWaypoint;
import CB_Core.GL_UI.Activitys.EditWaypoint.ReturnListner;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog;
import CB_Core.GL_UI.Controls.Dialogs.SolverDialog.SloverBackStringListner;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.UiSizes;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;

public class SolverView2 extends V_ListView implements SelectedCacheEvent
{
	private CustomAdapter lvAdapter;
	private Solver solver;
	private Cache cache;

	public SolverView2(CB_RectF rec, String Name)
	{
		super(rec, Name);

		Logger.LogCat("Create SolverView2 => " + rec.toString());
		/*
		 * Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel"); lblDummy.setFont(Fonts.getNormal());
		 * lblDummy.setText("Dummy SolverView"); setBackground(SpriteCache.ListBack); ^ if (GlobalCore.platform == Plattform.Desktop)
		 * this.addChild(lblDummy);
		 */

		cache = GlobalCore.getSelectedCache();
	}

	@Override
	public void onShow()
	{
		// platformConector.showView(ViewConst.SOLVER_VIEW, this.Pos.x, this.Pos.y, this.width, this.height);
		// CachListChangedEventList.Add(this);
		SelectedCacheEventList.Add(this);

		setBackground(SpriteCache.ListBack);

		cache = GlobalCore.getSelectedCache();

		intiList();
	}

	private void intiList()
	{
		if (cache == null) solver = new Solver("");
		else
		{
			this.cache = GlobalCore.getSelectedCache();
			String s = Database.GetSolver(this.cache);
			if (s == null) s = "";
			solver = new Solver(s);
			solver.Solve();
			// wenn der Solver noch leer ist oder die letzte Zeile nicht leer ist dann am Ende eine leere Zeile einfügen
			if ((solver.size() == 0) || (solver.get(solver.size() - 1).getOrgText().length() > 0))
			{
				solver.add(solver.size(), new SolverZeile(solver, ""));
			}
		}

		lvAdapter = new CustomAdapter(solver);
		this.setBaseAdapter(lvAdapter);

		int itemCount = solver.size();
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount)
		{
			this.setUndragable();
		}
		else
		{
			this.setDragable();
		}

		this.setSelection(0);
		this.setListPos(0, false);

		this.invalidate();
		GL.that.renderOnce(this.getName() + " onShow()");
	}

	private void reloadList()
	{
		this.setBaseAdapter(lvAdapter);
		int itemCount = solver.size();
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount)
		{
			this.setUndragable();
		}
		else
		{
			this.setDragable();
		}

		this.invalidate();
		GL.that.renderOnce(this.getName() + " onShow()");
	}

	@Override
	public void onHide()
	{
		// platformConector.hideView(ViewConst.SOLVER_VIEW);
		// CachListChangedEventList.Add(this);
		SelectedCacheEventList.Remove(this);
		if (GlobalCore.getSelectedCache() != null) Database.SetSolver(GlobalCore.getSelectedCache(), solver.getSolverString());
	}

	@Override
	public void Initial()
	{
		Logger.LogCat("SolverView2 => Initial()");
		this.setListPos(0, false);
		chkSlideBack();
		GL.that.renderOnce(this.getName() + " Initial()");
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();
			// GlobalCore.SelectedCache(Database.Data.Query.get(selectionIndex));
			setSelection(selectionIndex);
			// edit als default Aktion bei Click
			TabMainView.solverView2.ChangeLine();
			return true;
		}
	};

	private OnClickListener onItemLongClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();
			setSelection(selectionIndex);

			Menu cm = getContextMenu();
			cm.Show();

			return true;
		}

	};

	public Menu getContextMenu()
	{
		Menu cm = new Menu("SolverViewItemContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_CHANGE_LINE:
					TabMainView.solverView2.ChangeLine();
					return true;
				case MenuID.MI_INSERT_LINE:
					TabMainView.solverView2.InsertLine();
					return true;
				case MenuID.MI_DELETE_LINE:
					TabMainView.solverView2.DeleteLine();
					return true;
				case MenuID.MI_SET_AS_WAYPOINT:
					TabMainView.solverView2.SetAsWaypoint();
					break;
				case MenuID.MI_SET_AS_MAPCENTER:
					TabMainView.solverView2.SetAsMapCenter();
					break;
				}
				return false;
			}
		});

		cm.addItem(MenuID.MI_CHANGE_LINE, "editLine");
		cm.addItem(MenuID.MI_INSERT_LINE, "addLine");
		cm.addItem(MenuID.MI_DELETE_LINE, "delLine");
		cm.addItem(MenuID.MI_SET_AS_WAYPOINT, "addWaypoint");
		cm.addItem(MenuID.MI_SET_AS_MAPCENTER, "setMapCenter");
		return cm;
	}

	public class CustomAdapter implements Adapter
	{
		private Solver solver;

		public CustomAdapter(Solver solver)
		{
			this.solver = solver;
		}

		public int getCount()
		{
			if (solver == null) return 0;
			return solver.size();
		}

		public Object getItem(int position)
		{
			if (solver == null) return null;
			return solver.get(position);
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			if (solver == null) return null;
			SolverZeile solverZeile = solver.get(position);
			SolverViewItem v = new SolverViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, solverZeile);
			v.setClickable(true);
			v.setOnClickListener(onItemClickListner);
			v.setOnLongClickListener(onItemLongClickListner);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			if (solver == null) return 0;

			// SolverZeile solverZeile = solver.get(position);
			// if (solverZeile.Solution.length() == 0) return UiSizes.getCacheListItemRec().getHeight();
			// else
			return UiSizes.that.getCacheListItemRec().getHeight();
		}

	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		if (cache == this.cache) return; // Cache hat sich nicht geändert!
		// Solver speichern
		if (this.cache != null) Database.SetSolver(this.cache, solver.getSolverString());
		// nächsten Cache laden
		this.cache = cache;
		intiList();
	}

	public void ChangeLine()
	{
		// Show Dialog
		CB_RectF rec = GL_UISizes.UI_Left.copy();
		if (GlobalCore.isTab)
		{
			// da der Linke Tab bei einem Tablett nicht so Breit ist wie auf einem Phone,
			// verdoppeln wir hier die Breite (sieht besser aus)
			rec.setWidth(rec.getWidth() * 2);
		}

		String SolverString = solver.get(mSelectedIndex).getOrgText();

		SolverDialog solverDialog = new SolverDialog(rec, "SolverDialog", SolverString);

		neu = false;
		solverDialog.show(backListner);
	}

	boolean neu = false;

	final SloverBackStringListner backListner = new SloverBackStringListner()
	{

		@Override
		public void BackString(String backString)
		{
			SolverZeile zeile;
			if (neu)
			{
				zeile = new SolverZeile(solver, backString);
				solver.add(mSelectedIndex, zeile);
			}
			else
			{
				zeile = solver.get(mSelectedIndex);
				zeile.setText(backString);
			}

			for (int i = mSelectedIndex; i < solver.size(); i++)
			{
				SolverZeile zeile2 = solver.get(i);
				zeile2.setText(zeile2.getOrgText());
				zeile2.Parse();
			}

			if (!neu)
			{
				// wenn der letzte Eintrag geändert wurde dann soll hinter dem letzten Eintrag eine weitere neue Zeile eingefügt werden
				if (mSelectedIndex == solver.size() - 1)
				{
					solver.add(solver.size(), new SolverZeile(solver, ""));
					neu = true; // damit die Liste neu geladen wird
				}
			}

			reloadList();
		}
	};

	public void InsertLine()
	{
		CB_RectF rec = GL_UISizes.UI_Left.copy();
		if (GlobalCore.isTab)
		{
			// da der Linke Tab bei einem Tablett nicht so Breit ist wie auf einem Phone,
			// verdoppeln wir hier die Breite (sieht besser aus)
			rec.setWidth(rec.getWidth() * 2);
		}

		String SolverString = "";

		SolverDialog solverDialog = new SolverDialog(rec, "SolverDialog", SolverString);

		neu = true;
		solverDialog.show(backListner);
	}

	final OnMsgBoxClickListener deleteListener = new OnMsgBoxClickListener()
	{
		@Override
		public boolean onClick(int which, Object data)
		{
			if (which == 1)
			{
				solver.remove(mSelectedIndex);
				reloadList();
				return true;
			}
			else
				return false;
		}
	};

	public void DeleteLine()
	{
		GL_MsgBox.Show("Zeile löschen?", "Solver", MessageBoxButtons.YesNo, MessageBoxIcon.Question, deleteListener);
	}

	private Coordinate getSelectedCoordinateResult()
	{
		// Get the coordinate of the actual selected solver line
		// if one Coordinate is splitted into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
		// if the result(s) does not include any coordinate -> return null
		try
		{
			SolverZeile zeile = solver.get(mSelectedIndex);
			String text = zeile.Solution;
			// Wenn in dieser Zeile eine Zuweisung enthalten ist -> diese einfach entfernen!
			if (text.contains("="))
			{
				text = text.substring(text.indexOf("=") + 1);
			}
			Coordinate result = new Coordinate(text);
			if (!result.isValid())
			{
				// Zweizeilig versuchen
				SolverZeile zeile2 = solver.get(mSelectedIndex + 1);
				String text2 = zeile2.Solution;
				if (text2.contains("=")) text2 = text2.substring(text2.indexOf("=") + 1);
				result = new Coordinate(text + " " + text2);
			}
			if (result.isValid()) return result;
			else
				return null;
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public void SetAsWaypoint()
	{
		// Add new Waypoint with the selected Coordinates in the solver list
		// if one Coordinate is splitted into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
		Coordinate result = getSelectedCoordinateResult();
		if (result != null)
		{
			// Create New Waypoint
			Waypoint wp = new Waypoint();
			wp.CacheId = GlobalCore.getSelectedCache().Id;
			wp.setCoordinate(result);
			wp.Type = CacheTypes.Final;
			wp.Description = "Final";
			wp.IsUserWaypoint = true;
			try
			{
				wp.GcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().GcCode);
			}
			catch (Exception e)
			{
				return;
			}
			EditWaypoint EdWp = new EditWaypoint(ActivityBase.ActivityRec(), "EditWP", wp, new ReturnListner()
			{
				@Override
				public void returnedWP(Waypoint waypoint)
				{
					if (waypoint != null)
					{
						// Waypoint in der DB speichern
						GlobalCore.getSelectedCache().waypoints.add(waypoint);
						WaypointDAO waypointDAO = new WaypointDAO();
						waypointDAO.WriteToDatabase(waypoint);
						WaypointListChangedEventList.Call(GlobalCore.getSelectedCache());
						GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
					}
				}
			}, false);
			EdWp.show();
		}
	}

	public void SetAsMapCenter()
	{
		// Center Map to the actual selected Coordinates in the solver list
		// if one Coordinate is splitted into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
		Coordinate result = getSelectedCoordinateResult();
		if (result != null)
		{
			// Set Map Center
			TabMainView.mapView.setCenter(result);
			// Show MapView
			TabMainView.actionShowMap.Execute();
		}
	}
}

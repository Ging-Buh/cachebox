package CB_UI.GL_UI.Menu;

import CB_UI.GL_UI.Activitys.SearchOverNameOwnerGcCode;
import CB_UI.GL_UI.Activitys.SearchOverPosition;
import CB_UI_Base.Global;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;

public class ImportMenu
{
	private static final int GPX_IMPORT = 0;
	private static final int API_IMPORT = 1;
	private static final int API_IMPORT_OVER_POSITION = 11;
	private static final int API_IMPORT_NAME_OWNER_CODE = 12;
	private static final int CBS_IMPORT = 2;

	public static void showMainImportMenu()
	{

		Menu icm = new Menu("MainImportMenu");
		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case API_IMPORT:
					API_Import();
					return true;
				case GPX_IMPORT:
					GPX_Import();
					return true;
				case CBS_IMPORT:
					CBS_Import();
					return true;
				}
				return true;
			}
		});

		icm.addItem(GPX_IMPORT, "GPX_IMPORT");
		icm.addItem(API_IMPORT, "API_IMPORT");
		if (Global.isDevelop()) icm.addItem(CBS_IMPORT, "CBS_IMPORT");

		icm.Show();
	}

	private static void GPX_Import()
	{
		// Load GPX or import File
		Menu icm = new Menu("GPX ImportMenu");
		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// TODO Auto-generated method stub
				return false;
			}
		});

		icm.addItem(GPX_IMPORT, "GPX_IMPORT");
		icm.addItem(API_IMPORT, "API_IMPORT");

		icm.Show();

	}

	private static void API_Import()
	{
		Menu icm = new Menu("API ImportMenu");
		icm.addItemClickListner(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				switch (((MenuItem) v).getMenuItemId())
				{
				case API_IMPORT_OVER_POSITION:
					SearchOverPosition instanz = SearchOverPosition.ShowInstanz();
					instanz.setToggleBtnState(0);
					return true;
				case API_IMPORT_NAME_OWNER_CODE:
					SearchOverNameOwnerGcCode instanz2 = SearchOverNameOwnerGcCode.ShowInstanz();

					return true;
				}
				return true;
			}
		});

		icm.addItem(API_IMPORT_OVER_POSITION, "API_IMPORT_OVER_POSITION");
		icm.addItem(API_IMPORT_NAME_OWNER_CODE, "API_IMPORT_NAME_OWNER_CODE");

		icm.Show();
	}

	private static void CBS_Import()
	{

	}
}

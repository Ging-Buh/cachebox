package CB_UI.GL_UI.Main.Actions;

import CB_UI.GlobalCore;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IReadyListner;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSpoilerView extends CB_Action_ShowView
{
	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	public CB_Action_ShowSpoilerView()
	{
		super("spoiler", MenuID.AID_SHOW_SPOILER);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.spoilerView == null) && (tabMainView != null) && (tab != null)) TabMainView.spoilerView = new SpoilerView(
				tab.getContentRec(), "SpoilerView");

		if ((TabMainView.spoilerView != null) && (tab != null)) tab.ShowView(TabMainView.spoilerView);
	}

	@Override
	public boolean getEnabled()
	{
		boolean hasSpoiler = false;
		if (GlobalCore.ifCacheSelected()) hasSpoiler = GlobalCore.getSelectedCache().SpoilerExists();
		return hasSpoiler;
	}

	int spoilerState = -1;
	Sprite SpoilerIcon;

	@Override
	public Sprite getIcon()
	{
		boolean hasSpoiler = false;
		if (GlobalCore.ifCacheSelected()) hasSpoiler = GlobalCore.getSelectedCache().SpoilerExists();

		if (hasSpoiler && spoilerState != 1)
		{
			SpoilerIcon = SpriteCacheBase.Icons.get(IconName.images_18.ordinal());
			spoilerState = 1;
		}
		else if (!hasSpoiler && spoilerState != 0)
		{
			SpoilerIcon = new Sprite(SpriteCacheBase.Icons.get(IconName.images_18.ordinal()));
			SpoilerIcon.setColor(DISABLE_COLOR);
			spoilerState = 0;
		}

		return SpoilerIcon;
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.spoilerView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		Menu icm = new Menu("menu_compassView");
		icm.addItemClickListner(onItemClickListner);
		icm.addItem(MenuID.MI_RELOAD_SPOILER, "reloadSpoiler");
		icm.addItem(MenuID.MI_START_PICTUREAPP, "startPictureApp", SpriteCacheBase.getThemedSprite("image-export"));

		return icm;
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			switch (((MenuItem) v).getMenuItemId())
			{
			case MenuID.MI_RELOAD_SPOILER:
				GlobalCore.ImportSpoiler().setReadyListner(new IReadyListner()
				{
					@Override
					public void isReady()
					{
						// erst die Lokalen Images für den Cache neu laden
						if (GlobalCore.ifCacheSelected())
						{
							GlobalCore.getSelectedCache().ReloadSpoilerRessources();
							Execute();
						}

					}
				});

				return true;
			case MenuID.MI_START_PICTUREAPP:
				platformConector.StartPictureApp();
				return true;
			}
			return false;
		}
	};
}

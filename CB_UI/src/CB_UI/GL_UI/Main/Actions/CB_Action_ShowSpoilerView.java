package CB_UI.GL_UI.Main.Actions;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GlobalCore;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.SpoilerView;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IReadyListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;

public class CB_Action_ShowSpoilerView extends CB_Action_ShowView {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_Action_ShowSpoilerView.class);
	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	public CB_Action_ShowSpoilerView() {
		super("spoiler", MenuID.AID_SHOW_SPOILER);
	}

	@Override
	public void Execute() {
		if ((TabMainView.spoilerView == null) && (tabMainView != null) && (tab != null))
			TabMainView.spoilerView = new SpoilerView(tab.getContentRec(), "SpoilerView");

		if ((TabMainView.spoilerView != null) && (tab != null))
			tab.ShowView(TabMainView.spoilerView);
	}

	@Override
	public boolean getEnabled() {
		return GlobalCore.selectedCachehasSpoiler();
	}

	int spoilerState = -1;
	Sprite SpoilerIcon;

	@Override
	public Sprite getIcon() {
		boolean hasSpoiler = GlobalCore.selectedCachehasSpoiler();
		if (hasSpoiler && spoilerState != 1) {
			SpoilerIcon = SpriteCacheBase.Icons.get(IconName.images_18.ordinal());
			spoilerState = 1;
		} else if (!hasSpoiler && spoilerState != 0) {
			SpoilerIcon = new Sprite(SpriteCacheBase.Icons.get(IconName.images_18.ordinal()));
			SpoilerIcon.setColor(DISABLE_COLOR);
			spoilerState = 0;
		}
		return SpoilerIcon;
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.spoilerView;
	}

	@Override
	public boolean hasContextMenu() {
		return true;
	}

	@Override
	public Menu getContextMenu() {
		Menu icm = new Menu("menu_compassView");
		icm.addOnClickListener(onItemClickListener);
		icm.addItem(MenuID.MI_RELOAD_SPOILER, "reloadSpoiler");
		icm.addItem(MenuID.MI_START_PICTUREAPP, "startPictureApp", SpriteCacheBase.getThemedSprite("image-export"));

		return icm;
	}

	private final OnClickListener onItemClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

			switch (((MenuItem) v).getMenuItemId()) {
			case MenuID.MI_RELOAD_SPOILER:
				GlobalCore.ImportSpoiler().setReadyListener(new IReadyListener() {
					@Override
					public void isReady() {
						// erst die Lokalen Images für den Cache neu laden
						if (GlobalCore.isSetSelectedCache()) {
							GlobalCore.getSelectedCache().ReloadSpoilerRessources();
							GL.that.RunOnGL(new IRunOnGL() {

								@Override
								public void run() {
									if (TabMainView.spoilerView != null)
										TabMainView.spoilerView.ForceReload();
									Execute();
									TabMainView.spoilerView.onShow();
								}
							});

						}

					}
				});

				return true;
			case MenuID.MI_START_PICTUREAPP:
				String file = TabMainView.spoilerView.getSelectedFilePath();
				if (file == null)
					return true;
				PlatformConnector.StartPictureApp(file);
				return true;
			}
			return false;
		}
	};
}

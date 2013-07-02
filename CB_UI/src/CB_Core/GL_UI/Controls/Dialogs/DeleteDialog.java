package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.Enums.WrapType;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class DeleteDialog extends ButtonDialog
{
	private SizeF msgBoxContentSize;
	private ImageButton btDelFilter, btDelArchived, btDelFounds;
	private Label lblDelFilter, lblDelArchived, lblDelFounds;
	private CancelWaitDialog wd;

	public DeleteDialog()
	{
		super((calcMsgBoxSize("Text", true, true, false, false)).getBounds().asFloat(), "Delete-Dialog", "", Translation
				.Get("DeleteCaches"), MessageBoxButtons.Cancel, null, null);

		msgBoxContentSize = getContentSize();

		float innerWidth = msgBoxContentSize.width;

		CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UI_Size_Base.that.getButtonHeight() * 2);

		btDelFilter = new ImageButton(MTBRec, "btSetGPS");
		btDelArchived = new ImageButton(MTBRec, "btSelectWP");
		btDelFounds = new ImageButton(MTBRec, "btDeleteP");

		btDelFilter.setImage(SpriteCache.getSpriteDrawable("delete-filter"));
		btDelArchived.setImage(SpriteCache.getSpriteDrawable("delete-archived"));
		btDelFounds.setImage(SpriteCache.getSpriteDrawable("delete-founds"));

		lblDelFilter = new Label(Translation.Get("DelActFilter"), Fonts.getSmall(), null, WrapType.WRAPPED)
				.setHAlignment(HAlignment.CENTER);
		lblDelArchived = new Label(Translation.Get("DelArchived"), Fonts.getSmall(), null, WrapType.WRAPPED)
				.setHAlignment(HAlignment.CENTER);
		lblDelFounds = new Label(Translation.Get("DelFound"), Fonts.getSmall(), null, WrapType.WRAPPED).setHAlignment(HAlignment.CENTER);

		Box box = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight()), "");
		box.initRow(BOTTOMUP);
		box.addNext(lblDelFilter);
		box.addNext(lblDelArchived);
		box.addLast(lblDelFounds);
		box.addNext(btDelFilter);
		box.addNext(btDelArchived);
		box.addLast(btDelFounds);
		box.setHeight(box.getHeightFromBottom());
		this.addChild(box);

		this.setHeight(box.getHeight() + this.mFooterHeight + this.mTitleHeight + 3 * margin);

		btDelFilter.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				close();

				wd = CancelWaitDialog.ShowWait(Translation.Get("DelActFilter"), new IcancelListner()
				{

					@Override
					public void isCanceld()
					{
						// TODO Auto-generated method stub

					}
				}, new Runnable()
				{

					@Override
					public void run()
					{
						CacheListDAO dao = new CacheListDAO();
						long nun = dao.DelFilter(GlobalCore.LastFilter.getSqlWhere(Config.settings.GcLogin.getValue()),
								Config.settings.SpoilerFolder.getValue(), Config.settings.SpoilerFolderLocal.getValue(),
								Config.settings.DescriptionImageFolder.getValue(), Config.settings.DescriptionImageFolderLocal.getValue());
						cleanupLogs();
						wd.close();

						// reset Filter
						GlobalCore.LastFilter = new FilterProperties(FilterProperties.presets[0].ToString());
						EditFilterSettings.ApplyFilter(GlobalCore.LastFilter);// all Caches

						String msg = Translation.Get("DeletedCaches", String.valueOf(nun));
						GL.that.Toast(msg);
					}
				});
				return true;
			}
		});

		btDelArchived.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				close();

				wd = CancelWaitDialog.ShowWait(Translation.Get("DelArchived"), new IcancelListner()
				{

					@Override
					public void isCanceld()
					{
						// TODO Auto-generated method stub

					}
				}, new Runnable()
				{

					@Override
					public void run()
					{
						CacheListDAO dao = new CacheListDAO();
						long nun = dao.DelArchiv(Config.settings.SpoilerFolder.getValue(), Config.settings.SpoilerFolderLocal.getValue(),
								Config.settings.DescriptionImageFolder.getValue(), Config.settings.DescriptionImageFolderLocal.getValue());

						cleanupLogs();
						wd.close();

						EditFilterSettings.ApplyFilter(GlobalCore.LastFilter);

						String msg = Translation.Get("DeletedCaches", String.valueOf(nun));
						GL.that.Toast(msg);
					}
				});
				return true;
			}
		});

		btDelFounds.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				close();

				wd = CancelWaitDialog.ShowWait(Translation.Get("DelFound"), new IcancelListner()
				{

					@Override
					public void isCanceld()
					{
						// TODO Auto-generated method stub

					}
				}, new Runnable()
				{

					@Override
					public void run()
					{
						CacheListDAO dao = new CacheListDAO();
						long nun = dao.DelFound(Config.settings.SpoilerFolder.getValue(), Config.settings.SpoilerFolderLocal.getValue(),
								Config.settings.DescriptionImageFolder.getValue(), Config.settings.DescriptionImageFolderLocal.getValue());
						cleanupLogs();
						wd.close();

						EditFilterSettings.ApplyFilter(GlobalCore.LastFilter);

						String msg = Translation.Get("DeletedCaches", String.valueOf(nun));
						GL.that.Toast(msg);
					}
				});
				return true;
			}
		});

	}

	private void cleanupLogs()
	{
		LogDAO dao = new LogDAO();
		dao.ClearOrphanedLogs();

		// compact DB
		// hanging
		// Database.Data.execSQL("vacuum");

	}

	@Override
	public void dispose()
	{
		msgBoxContentSize = null;
		btDelFilter.dispose();
		btDelArchived.dispose();
		btDelFounds.dispose();
		lblDelFilter.dispose();
		lblDelArchived.dispose();
		lblDelFounds.dispose();
		super.dispose();
		btDelFilter = null;
		btDelArchived = null;
		btDelFounds = null;
		lblDelFilter = null;
		lblDelArchived = null;
		lblDelFounds = null;
	}

}

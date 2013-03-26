package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Linearlayout;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class DeleteDialog extends ButtonDialog
{

	private Linearlayout layout;

	private float TextFieldHeight;
	private SizeF msgBoxContentSize;
	private ImageButton btDelFilter, btDelArchived, btDelFounds;
	private Label lblDelFilter, lblDelArchived, lblDelFounds;
	private CancelWaitDialog wd;

	public DeleteDialog()
	{
		super((calcMsgBoxSize("Text", true, true, false)).getBounds().asFloat(), "Delete-Dialog", "", Translation.Get("DeleteCaches"),
				MessageBoxButtons.Cancel, null, null);

		msgBoxContentSize = getContentSize();
		// initial VariableField
		TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

		float innerWidth = msgBoxContentSize.width;

		layout = new Linearlayout(innerWidth, "Layout");
		layout.setX(0);
		// layout.setBackground(new ColorDrawable(Color.GREEN));

		CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UI_Size_Base.that.getButtonHeight() * 2);

		btDelFilter = new ImageButton(MTBRec, "btSetGPS");
		btDelArchived = new ImageButton(MTBRec, "btSelectWP");
		btDelFounds = new ImageButton(MTBRec, "btDeleteP");

		btDelFilter.setImage(SpriteCache.getSpriteDrawable("delete-filter"));
		btDelArchived.setImage(SpriteCache.getSpriteDrawable("delete-archived"));
		btDelFounds.setImage(SpriteCache.getSpriteDrawable("delete-founds"));

		btDelFilter.setX(0);
		btDelArchived.setX(btDelFilter.getMaxX());
		btDelFounds.setX(btDelArchived.getMaxX());

		Box box = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 2), "");

		box.addChild(btDelFilter);
		box.addChild(btDelArchived);
		box.addChild(btDelFounds);

		layout.addChild(box);

		Box box2 = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 2), "");

		lblDelFilter = new Label(btDelFilter.ScaleCenter(0.8f), "lblSetGPS");
		lblDelArchived = new Label(btDelArchived.ScaleCenter(0.8f), "lblSetGPS");
		lblDelFounds = new Label(btDelFounds.ScaleCenter(0.8f), "lblSetGPS");

		lblDelFilter.setFont(Fonts.getSmall());
		lblDelArchived.setFont(Fonts.getSmall());
		lblDelFounds.setFont(Fonts.getSmall());

		lblDelArchived.setWrappedText(Translation.Get("DelArchived"), HAlignment.CENTER);
		lblDelFilter.setWrappedText(Translation.Get("DelActFilter"), HAlignment.CENTER);
		lblDelFounds.setWrappedText(Translation.Get("DelFound"), HAlignment.CENTER);

		box2.addChild(lblDelFilter);
		box2.addChild(lblDelArchived);
		box2.addChild(lblDelFounds);

		layout.addChild(box2);

		this.addChild(layout);

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize("teste", true, true, false);
		msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight() - (TextFieldHeight / 2));
		this.setSize(msgBoxSize.asFloat());

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
						long nun = dao.DelFilter(GlobalCore.LastFilter.getSqlWhere());
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
						long nun = dao.DelArchiv();

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
						long nun = dao.DelFound();
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
		Database.Data.execSQL("vacuum");

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

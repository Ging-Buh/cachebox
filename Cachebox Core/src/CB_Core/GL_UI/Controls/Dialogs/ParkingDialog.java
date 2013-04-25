package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Linearlayout;
import CB_Core.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Locator.Locator;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class ParkingDialog extends ButtonDialog
{

	private Linearlayout layout;

	private float TextFieldHeight;
	private SizeF msgBoxContentSize;
	private ImageButton btSetGPS, btSelectWP, btDeleteP;
	private Label lblSetGPS, lblSelectWP, lblDeleteP;

	public ParkingDialog()
	{
		super((calcMsgBoxSize("Text", true, true, false, false)).getBounds().asFloat(), "Parking-Dialog", "", Translation
				.Get("My_Parking_Area_Title"), MessageBoxButtons.Cancel, null, null);

		msgBoxContentSize = getContentSize();
		// initial VariableField
		TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

		float innerWidth = msgBoxContentSize.width;

		layout = new Linearlayout(innerWidth, "Layout");
		layout.setX(0);
		// layout.setBackground(new ColorDrawable(Color.GREEN));

		CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UI_Size_Base.that.getButtonHeight() * 2);

		btSetGPS = new ImageButton(MTBRec, "btSetGPS");
		btSelectWP = new ImageButton(MTBRec, "btSelectWP");
		btDeleteP = new ImageButton(MTBRec, "btDeleteP");

		btSetGPS.setImage(SpriteCache.getSpriteDrawable("my-parking-set"));
		btSelectWP.setImage(SpriteCache.getSpriteDrawable("my-parking-wp"));
		btDeleteP.setImage(SpriteCache.getSpriteDrawable("my-parking-delete"));

		btSetGPS.setX(0);
		btSelectWP.setX(btSetGPS.getMaxX());
		btDeleteP.setX(btSelectWP.getMaxX());

		Box box = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 2), "");

		box.addChild(btSetGPS);
		box.addChild(btSelectWP);
		box.addChild(btDeleteP);

		layout.addChild(box);

		Box box2 = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 2), "");

		lblSetGPS = new Label(btSetGPS.ScaleCenter(0.8f), "lblSetGPS");
		lblSelectWP = new Label(btSelectWP.ScaleCenter(0.8f), "lblSetGPS");
		lblDeleteP = new Label(btDeleteP.ScaleCenter(0.8f), "lblSetGPS");

		lblSetGPS.setFont(Fonts.getSmall());
		lblSelectWP.setFont(Fonts.getSmall());
		lblDeleteP.setFont(Fonts.getSmall());

		lblSelectWP.setWrappedText(Translation.Get("My_Parking_Area_select"), HAlignment.CENTER);
		lblSetGPS.setWrappedText(Translation.Get("My_Parking_Area_Add"), HAlignment.CENTER);
		lblDeleteP.setWrappedText(Translation.Get("My_Parking_Area_Del"), HAlignment.CENTER);

		box2.addChild(lblSetGPS);
		box2.addChild(lblSelectWP);
		box2.addChild(lblDeleteP);

		layout.addChild(box2);

		this.addChild(layout);

		// chk disable select and delete Button
		synchronized (Database.Data.Query)
		{
			Cache cache = Database.Data.Query.GetCacheByGcCode("CBPark");
			if (cache == null)
			{
				btSelectWP.disable();
				btDeleteP.disable();
			}
		}

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize("teste", true, true, false);
		msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight() - (TextFieldHeight / 2));
		this.setSize(msgBoxSize.asFloat());

		btSetGPS.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				CB_Core.Config.settings.ParkingLatitude.setValue(Locator.getLatitude());
				CB_Core.Config.settings.ParkingLongitude.setValue(Locator.getLongitude());
				CB_Core.Config.AcceptChanges();
				CachListChangedEventList.Call();

				close();
				return true;
			}
		});

		btSelectWP.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				synchronized (Database.Data.Query)
				{
					Cache cache = Database.Data.Query.GetCacheByGcCode("CBPark");
					if (cache != null) GlobalCore.setSelectedCache(cache);
				}
				close();
				return true;
			}
		});

		btDeleteP.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				CB_Core.Config.settings.ParkingLatitude.setValue(0);
				CB_Core.Config.settings.ParkingLongitude.setValue(0);
				CB_Core.Config.AcceptChanges();
				CachListChangedEventList.Call();
				close();
				return true;
			}
		});

	}

	@Override
	protected void SkinIsChanged()
	{
	}

	@Override
	public void dispose()
	{
		msgBoxContentSize = null;
		if (btSetGPS != null) btSetGPS.dispose();
		if (btSelectWP != null) btSelectWP.dispose();
		if (btDeleteP != null) btDeleteP.dispose();
		if (lblSetGPS != null) lblSetGPS.dispose();
		if (lblSelectWP != null) lblSelectWP.dispose();
		if (lblDeleteP != null) lblDeleteP.dispose();
		super.dispose();
		btSetGPS = null;
		btSelectWP = null;
		btDeleteP = null;
		lblSetGPS = null;
		lblSelectWP = null;
		lblDeleteP = null;
	}

}

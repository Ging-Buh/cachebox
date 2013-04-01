package CB_Core.GL_UI.Activitys;

import CB_Core.Config;
import CB_Core.Enums.LogTypes;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Trackable;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;

public class TB_Details extends ActivityBase
{
	public static TB_Details that;
	private float innerHeight;
	private ScrollBox scrollBox;
	private Button btnClose, btnAction;
	private Trackable TB;
	private Image icon, image;
	private Label lblName, lblAbout, lblAboutDesc, lblGoal, lblGoalDesc;
	private Label lblTypeName, lblTbCode, lblOwner, lbllastVisit, lblHome, lblBirth, lblTravelDistance;
	private Label TypeName, TbCode, Owner, Birth, lastVisit, Home, TravelDistance; // TODO Visit,Home und Distance müssen noch angezeigt
																					// werden!
	private Box AboutThisItem, GoalThisItem, DetailThisItem;

	public TB_Details()
	{
		super(ActivityRec(), "TB_Detail_Activity");
		createControls();
		that = this;
	}

	public void Show(Trackable TB)
	{
		this.TB = TB;
		layout();
		GL.that.showActivity(this);
	}

	private void createControls()
	{
		innerHeight = 1000;

		btnClose = new Button("Close");
		btnClose.setText(Translation.Get("close"));
		btnClose.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				TB_Details.this.finish();
				return true;
			}
		});

		btnAction = new Button("Action");
		btnAction.setText(Translation.Get("TB_Log"));
		btnAction.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				showLogMenu();
				return true;
			}
		});

		scrollBox = new ScrollBox(ActivityRec(), innerHeight, "ScrollBox");
		scrollBox.setHeight(this.height - (btnClose.getHeight() - margin) * 2.5f);
		scrollBox.setBackground(SpriteCache.activityBackground);

		CB_RectF iconRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight());
		iconRec = iconRec.ScaleCenter(0.8f);

		icon = new Image(iconRec, "Icon");
		icon.setWeight(-1);
		lblName = new Label(iconRec, "Name");

		image = new Image(iconRec, "Image");
		lblAbout = new Label("About");
		lblAboutDesc = new Label("AboutDesc");
		AboutThisItem = new Box(10, 10, "AboutItemBox");
		AboutThisItem.setBackground(SpriteCache.activityBackground);

		lblGoal = new Label("Goal");
		lblGoalDesc = new Label("GoalDesc");
		GoalThisItem = new Box(10, 10, "GoalItemBox");
		GoalThisItem.setBackground(SpriteCache.activityBackground);

		lblTypeName = new Label("TypeName");
		lblTbCode = new Label("TbCode");
		lblOwner = new Label("Owner");
		lbllastVisit = new Label("LastVisit");
		lblHome = new Label("Home");
		lblBirth = new Label("Birth");
		lblTravelDistance = new Label("TravelDistance");

		TypeName = new Label("TypeName");
		TbCode = new Label("TbCode");
		Owner = new Label("Owner");
		lastVisit = new Label("LastVisit");
		Home = new Label("Home");
		Birth = new Label("Birth");
		TravelDistance = new Label("TravelDistance");

		DetailThisItem = new Box(10, 10, "DetailThisItem");
		DetailThisItem.setBackground(SpriteCache.activityBackground);
	}

	private void layout()
	{
		this.removeChilds();
		this.initRow(false);
		this.addNext(btnAction);
		this.addLast(btnClose);
		this.addLast(scrollBox);
		this.setMargins(margin * 2, 0);
		this.addNext(icon);
		icon.setImageURL(TB.getIconUrl());
		this.addLast(lblName);
		// lblName.setVAlignment(VAlignment.CENTER);
		lblName.setWrappedText(TB.getName());

		scrollBox.setWidth(width);
		scrollBox.setMargins(margin, 0);

		float minBoxHeight = Fonts.Measure("Tg").height + SpriteCache.activityBackground.getBottomHeight()
				+ SpriteCache.activityBackground.getTopHeight();

		lblAbout.setFont(Fonts.getSmall());
		AboutThisItem.setWidth(scrollBox.getWidth() - scrollBox.getLeftWidth() - scrollBox.getRightWidth());
		TextBounds bounds = lblAbout.setText(Translation.Get("AboutThisItem"), null, null, HAlignment.LEFT);
		lblAbout.setHeight(bounds.height + margin);
		lblAboutDesc.setWidth(AboutThisItem.getWidth() - AboutThisItem.getLeftWidth() - AboutThisItem.getRightWidth());
		bounds = lblAboutDesc.setWrappedText(TB.getDescription());
		lblAboutDesc.setHeight(bounds.height + margin + margin);

		String ImgUrl = TB.getImageUrl();
		float ImageHeight = 0;
		if (ImgUrl != null && ImgUrl.length() > 0)
		{
			image.setHeight(this.width / 3);
			ImageHeight = image.getHeight();
			image.setImageURL(ImgUrl);
		}

		AboutThisItem.setHeight(Math.max(minBoxHeight, (lblAboutDesc.getHeight() + (margin * 4) + ImageHeight)));
		AboutThisItem.initRow();
		AboutThisItem.setMargins(0, margin * 3);
		if (ImageHeight > 0) AboutThisItem.addLast(image);
		AboutThisItem.addLast(lblAboutDesc);

		lblGoal.setFont(Fonts.getSmall());
		GoalThisItem.setWidth(scrollBox.getWidth() - scrollBox.getLeftWidth() - scrollBox.getRightWidth());
		bounds = lblGoal.setText(Translation.Get("GoalThisItem"), null, null, HAlignment.LEFT);
		lblGoal.setHeight(bounds.height + margin);
		lblGoalDesc.setWidth(GoalThisItem.getWidth() - GoalThisItem.getLeftWidth() - GoalThisItem.getRightWidth());
		bounds = lblGoalDesc.setWrappedText(TB.getCurrentGoal());
		lblGoalDesc.setHeight(bounds.height + margin + margin);
		GoalThisItem.setHeight(Math.max(minBoxHeight, (lblGoalDesc.getHeight() + margin + margin)));
		GoalThisItem.initRow();
		GoalThisItem.addLast(lblGoalDesc);

		DetailThisItem.setWidth(scrollBox.getWidth() - scrollBox.getLeftWidth() - scrollBox.getRightWidth());

		float maxWidth = 0;

		lblTypeName.setHeight(minBoxHeight);
		maxWidth = Math.max(maxWidth, lblTypeName.setText(Translation.Get("TB_Type"), Fonts.getSmall(), Fonts.getDisableFontColor()).width);
		TypeName.setHeight(minBoxHeight);
		TypeName.setText(TB.getTypeName());

		lblTbCode.setHeight(minBoxHeight);
		maxWidth = Math.max(maxWidth, lblTbCode.setText(Translation.Get("TB_Code"), Fonts.getSmall(), Fonts.getDisableFontColor()).width);
		TbCode.setHeight(minBoxHeight);
		TbCode.setText(TB.getGcCode());

		lblOwner.setHeight(minBoxHeight);
		maxWidth = Math.max(maxWidth, lblOwner.setText(Translation.Get("TB_Owner"), Fonts.getSmall(), Fonts.getDisableFontColor()).width);
		Owner.setHeight(minBoxHeight);
		Owner.setText(TB.getOwner());

		// lbllastVisit.setHeight(minBoxHeight);
		// maxWidth = Math.max(maxWidth,
		// lbllastVisit.setText(Translation.Get("TB_LastVisit"), Fonts.getSmall(), Fonts.getDisableFontColor()).width);
		// lastVisit.setHeight(minBoxHeight);
		// lastVisit.setText(TB.getLastVisit());

		// lblHome.setHeight(minBoxHeight);
		// maxWidth = Math.max(maxWidth, lblHome.setText(Translation.Get("TB_Home"), Fonts.getSmall(), Fonts.getDisableFontColor()).width);
		// Home.setHeight(minBoxHeight);
		// Home.setText(TB.getHome());

		lblBirth.setHeight(minBoxHeight);
		maxWidth = Math.max(maxWidth, lblBirth.setText(Translation.Get("TB_Birth"), Fonts.getSmall(), Fonts.getDisableFontColor()).width);
		Birth.setHeight(minBoxHeight);
		Birth.setText(TB.getBirth());

		// lblTravelDistance.setHeight(minBoxHeight);
		// maxWidth = Math.max(maxWidth,
		// lblTravelDistance.setText(Translation.Get("TB_TravelDistance"), Fonts.getSmall(), Fonts.getDisableFontColor()).width);
		// lblTravelDistance.setHeight(minBoxHeight);
		// lblTravelDistance.setText(TB.getTravelDistance());

		lblTypeName.setWidth(maxWidth);
		lblTbCode.setWidth(maxWidth);
		lblOwner.setWidth(maxWidth);
		lbllastVisit.setWidth(maxWidth);
		lblHome.setWidth(maxWidth);
		lblBirth.setWidth(maxWidth);
		lblTravelDistance.setWidth(maxWidth);

		lblTypeName.setWeight(-1);
		lblTbCode.setWeight(-1);
		lblOwner.setWeight(-1);
		lbllastVisit.setWeight(-1);
		lblHome.setWeight(-1);
		lblBirth.setWeight(-1);
		lblTravelDistance.setWeight(-1);

		DetailThisItem.setHeight((lblTypeName.getHeight()) * 5);
		DetailThisItem.initRow();
		DetailThisItem.setMargins(margin, 0);
		DetailThisItem.addNext(lblTypeName);
		DetailThisItem.addLast(TypeName);
		DetailThisItem.addNext(lblTbCode);
		DetailThisItem.addLast(TbCode);
		DetailThisItem.addNext(lblOwner);
		DetailThisItem.addLast(Owner);
		// DetailThisItem.addNext(lbllastVisit);
		// DetailThisItem.addLast(lastVisit);
		// DetailThisItem.addNext(lblHome);
		// DetailThisItem.addLast(Home);
		DetailThisItem.addNext(lblBirth);
		DetailThisItem.addLast(Birth);
		// DetailThisItem.addNext(lblTravelDistance);
		// DetailThisItem.addLast(TravelDistance);

		scrollBox.initRow(false);
		scrollBox.setMargins(margin, 0);
		scrollBox.addLast(AboutThisItem);
		scrollBox.setMargins(margin, margin * 2);
		scrollBox.addLast(lblAbout);
		scrollBox.setMargins(margin, 0);
		scrollBox.addLast(GoalThisItem);
		scrollBox.setMargins(margin, margin * 2);
		scrollBox.addLast(lblGoal);
		scrollBox.setMargins(margin, margin);
		scrollBox.addLast(DetailThisItem);
		float ScrollBoxInnerHeight = AboutThisItem.getHeight() + lblAbout.getHeight() + GoalThisItem.getHeight() + lblGoal.getHeight()
				+ DetailThisItem.getHeight() + (scrollBox.getYmargin() * 7);
		scrollBox.setInerHeight(ScrollBoxInnerHeight);
		scrollBox.setX(0);
		AboutThisItem.setX(0);
		GoalThisItem.setX(0);
		DetailThisItem.setX(0);
		GL.that.renderOnce("TB_details Layout");
		GL.that.addRenderView(this, GL.FRAME_RATE_FAST_ACTION);
	}

	private void showLogMenu()
	{
		boolean isInventory = TB.getCurrentOwner().equalsIgnoreCase(Config.settings.GcLogin.getValue());

		final Menu cm = new Menu("TBLogContextMenu");
		cm.addItemClickListner(menuItemClickListner);

		if (!isInventory) cm.addItem(MenuID.MI_TB_DISCOVERED, "discovered", SpriteCache.Icons.get(58));
		if (isInventory) cm.addItem(MenuID.MI_TB_VISIT, "visit", SpriteCache.Icons.get(62));
		if (isInventory) cm.addItem(MenuID.MI_TB_DROPPED, "dropped", SpriteCache.Icons.get(59));
		if (!isInventory) cm.addItem(MenuID.MI_TB_GRABBED, "grabbed", SpriteCache.Icons.get(60));
		if (!isInventory) cm.addItem(MenuID.MI_TB_PICKED, "picked", SpriteCache.Icons.get(61));

		cm.Show();
	}

	private OnClickListener menuItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			if (TB_Log.that == null) new TB_Log();
			switch (((MenuItem) v).getMenuItemId())
			{

			case MenuID.MI_TB_DISCOVERED:
				TB_Log.that.Show(TB, LogTypes.discovered);
				break;

			case MenuID.MI_TB_VISIT:
				TB_Log.that.Show(TB, LogTypes.visited);
				break;

			case MenuID.MI_TB_DROPPED:
				TB_Log.that.Show(TB, LogTypes.dropped_off);
				break;

			case MenuID.MI_TB_GRABBED:
				TB_Log.that.Show(TB, LogTypes.grab_it);
				break;

			case MenuID.MI_TB_PICKED:
				TB_Log.that.Show(TB, LogTypes.retrieve);
				break;
			}
			return true;
		}
	};

}

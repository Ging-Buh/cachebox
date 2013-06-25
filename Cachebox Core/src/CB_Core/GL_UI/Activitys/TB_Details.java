package CB_Core.GL_UI.Activitys;

import CB_Core.Enums.LogTypes;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.WrapType;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Trackable;

public class TB_Details extends ActivityBase
{
	public static TB_Details that;
	private float innerHeight;
	private ScrollBox scrollBox;
	private Button btnClose, btnAction;
	private Trackable TB;
	private Image icon, image;
	private Label lblName, lblAbout, lblAboutDesc, lblGoal, lblGoalDesc;
	private Label lblTypeName, lblTbCode, lblOwner, lblBirth;
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

		scrollBox = new ScrollBox(ActivityRec());
		scrollBox.setVirtualHeight(innerHeight);
		scrollBox.setHeight(this.height - (btnClose.getHeight() - margin) * 2.5f);
		scrollBox.setBackground(SpriteCache.activityBackground);

		CB_RectF iconRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight());
		iconRec = iconRec.ScaleCenter(0.8f);

		icon = new Image(iconRec, "Icon");
		lblName = new Label(iconRec, "Name");

		image = new Image(iconRec, "Image");
		lblAbout = new Label(Translation.Get("AboutThisItem"), Fonts.getSmall(), Fonts.getFontColor(), WrapType.SINGLELINE);
		lblAboutDesc = new Label("AboutDesc");
		AboutThisItem = new Box(10, 10, "AboutItemBox");
		AboutThisItem.setBackground(SpriteCache.activityBackground);

		lblGoal = new Label(Translation.Get("GoalThisItem"), Fonts.getSmall(), Fonts.getFontColor(), WrapType.SINGLELINE);
		lblGoalDesc = new Label("GoalDesc");
		GoalThisItem = new Box(10, 10, "GoalItemBox");
		GoalThisItem.setBackground(SpriteCache.activityBackground);

		lblTypeName = new Label(Translation.Get("TB_Type"), Fonts.getSmall(), Fonts.getDisableFontColor(), WrapType.SINGLELINE);
		lblTbCode = new Label(Translation.Get("TB_Code"), Fonts.getSmall(), Fonts.getDisableFontColor(), WrapType.SINGLELINE);
		lblOwner = new Label(Translation.Get("TB_Owner"), Fonts.getSmall(), Fonts.getDisableFontColor(), WrapType.SINGLELINE);
		// lbllastVisit = new Label("LastVisit");
		// lblHome = new Label("Home");
		lblBirth = new Label(Translation.Get("TB_Birth"), Fonts.getSmall(), Fonts.getDisableFontColor(), WrapType.SINGLELINE);
		// lblTravelDistance = new Label("TravelDistance");

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
		this.initRow(BOTTOMUP);
		this.addNext(btnAction);
		this.addLast(btnClose);
		this.addLast(scrollBox);
		this.setMargins(margin * 2, 0);
		this.addNext(icon, FIXED);
		icon.setImageURL(TB.getIconUrl());
		this.addLast(lblName);
		// lblName.setVAlignment(VAlignment.CENTER);
		lblName.setWrappedText(TB.getName());

		scrollBox.setWidth(width);
		scrollBox.setMargins(margin, 0);

		float minBoxHeight = Fonts.Measure("Tg").height + SpriteCache.activityBackground.getBottomHeight()
				+ SpriteCache.activityBackground.getTopHeight();

		AboutThisItem.setWidth(scrollBox.getInnerWidth());
		lblAbout.setHeight(lblAbout.getTextHeight() + margin);
		lblAboutDesc.setWidth(AboutThisItem.getInnerWidth());
		lblAboutDesc.setWrappedText(TB.getDescription());
		lblAboutDesc.setHeight(lblAboutDesc.getTextHeight() + margin + margin);

		String ImgUrl = TB.getImageUrl();
		float ImageHeight = 0;
		if (ImgUrl != null && ImgUrl.length() > 0)
		{
			image.setHeight(this.width / 3);
			ImageHeight = image.getHeight();
			image.setImageURL(ImgUrl);
		}
		else
		{
			image.setHeight(0);
		}

		AboutThisItem.setHeight(Math.max(minBoxHeight, (lblAboutDesc.getHeight() + (margin * 4) + ImageHeight)));
		AboutThisItem.initRow();
		AboutThisItem.setMargins(0, margin * 3);
		if (ImageHeight > 0) AboutThisItem.addLast(image);
		AboutThisItem.addLast(lblAboutDesc);

		GoalThisItem.setWidth(scrollBox.getInnerWidth());
		lblGoal.setHeight(lblGoal.getTextHeight() + margin);
		lblGoalDesc.setWidth(GoalThisItem.getInnerWidth());
		lblGoalDesc.setWrappedText(TB.getCurrentGoal());
		lblGoalDesc.setHeight(lblGoalDesc.getTextHeight() + margin + margin);
		GoalThisItem.setHeight(Math.max(minBoxHeight, (lblGoalDesc.getHeight() + margin + margin)));
		GoalThisItem.initRow();
		GoalThisItem.addLast(lblGoalDesc);

		DetailThisItem.setWidth(scrollBox.getInnerWidth());

		float maxWidth = 0;

		lblTypeName.setHeight(minBoxHeight);
		maxWidth = Math.max(maxWidth, lblTypeName.getTextWidth());
		TypeName.setHeight(minBoxHeight);
		TypeName.setText(TB.getTypeName());

		lblTbCode.setHeight(minBoxHeight);
		maxWidth = Math.max(maxWidth, lblTbCode.getTextWidth());
		TbCode.setHeight(minBoxHeight);
		TbCode.setText(TB.getGcCode());

		lblOwner.setHeight(minBoxHeight);
		maxWidth = Math.max(maxWidth, lblOwner.getTextWidth());
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
		maxWidth = Math.max(maxWidth, lblBirth.getTextWidth());
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
		// lbllastVisit.setWidth(maxWidth);
		// lblHome.setWidth(maxWidth);
		lblBirth.setWidth(maxWidth);
		// lblTravelDistance.setWidth(maxWidth);

		DetailThisItem.setHeight((lblTypeName.getHeight()) * 5);
		DetailThisItem.initRow();
		DetailThisItem.setMargins(margin, 0);
		DetailThisItem.addNext(lblTypeName, FIXED);
		DetailThisItem.addLast(TypeName);
		DetailThisItem.addNext(lblTbCode, FIXED);
		DetailThisItem.addLast(TbCode);
		DetailThisItem.addNext(lblOwner, FIXED);
		DetailThisItem.addLast(Owner);
		// DetailThisItem.addNext(lbllastVisit,FIXED);
		// DetailThisItem.addLast(lastVisit);
		// DetailThisItem.addNext(lblHome);
		// DetailThisItem.addLast(Home);
		DetailThisItem.addNext(lblBirth, FIXED);
		DetailThisItem.addLast(Birth);
		// DetailThisItem.addNext(lblTravelDistance);
		// DetailThisItem.addLast(TravelDistance);

		scrollBox.initRow(BOTTOMUP);
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
		scrollBox.setVirtualHeight(scrollBox.getHeightFromBottom());
		scrollBox.setX(0);
		AboutThisItem.setX(0);
		GoalThisItem.setX(0);
		DetailThisItem.setX(0);
		GL.that.renderOnce("TB_details Layout");
		GL.that.addRenderView(this, GL.FRAME_RATE_FAST_ACTION);
	}

	private void showLogMenu()
	{

		final Menu cm = new Menu("TBLogContextMenu");
		cm.addItemClickListner(menuItemClickListner);

		cm.addItem(MenuID.MI_TB_NOTE, "note", SpriteCache.Icons.get(IconName.tbNote_63.ordinal()));

		if (TB.isLogTypePosible(LogTypes.discovered)) cm.addItem(MenuID.MI_TB_DISCOVERED, "discovered",
				SpriteCache.Icons.get(IconName.tbDiscover_58.ordinal()));

		if (TB.isLogTypePosible(LogTypes.visited)) cm.addItem(MenuID.MI_TB_VISIT, "visit",
				SpriteCache.Icons.get(IconName.tbVisit_62.ordinal()));

		if (TB.isLogTypePosible(LogTypes.dropped_off)) cm.addItem(MenuID.MI_TB_DROPPED, "dropped",
				SpriteCache.Icons.get(IconName.tbDrop_59.ordinal()));

		if (TB.isLogTypePosible(LogTypes.grab_it)) cm.addItem(MenuID.MI_TB_GRABBED, "grabbed",
				SpriteCache.Icons.get(IconName.tbGrab_60.ordinal()));

		if (TB.isLogTypePosible(LogTypes.retrieve)) cm.addItem(MenuID.MI_TB_PICKED, "picked",
				SpriteCache.Icons.get(IconName.tbPicked_61.ordinal()));

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

			case MenuID.MI_TB_NOTE:
				TB_Log.that.Show(TB, LogTypes.note);
				break;
			}
			return true;
		}
	};

}

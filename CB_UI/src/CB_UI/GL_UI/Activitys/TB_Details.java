/* 
 * Copyright (C) 2014-2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI.GL_UI.Activitys;

import CB_Core.CB_Core_Settings;
import CB_Core.LogTypes;
import CB_Core.Types.Trackable;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class TB_Details extends ActivityBase {
    public static TB_Details that;
    private float innerHeight;
    private ScrollBox scrollBox;
    private Button btnClose, btnAction;
    private Trackable TB;
    private Image icon, image;
    private Label lblAbout, lblAboutDesc, lblGoal, lblGoalDesc;
    private EditTextField lblName;
    private Label lblTypeName, lblTbCode, lblOwner, lblBirth;
    // TODO Visit,Home und Distance müssen noch angezeigt werden!
    private Label TypeName, TbCode, Owner, Birth;
    private Box AboutThisItem, GoalThisItem, DetailThisItem;

    public TB_Details() {
	super(ActivityRec(), "TB_Detail_Activity");
	createControls();
	that = this;
    }

    public void Show(Trackable TB) {
	this.TB = TB;
	layout();
	GL.that.showActivity(this);
    }

    private void createControls() {
	innerHeight = 1000;

	btnClose = new Button("Close");
	btnClose.setText(Translation.Get("close"));
	btnClose.setOnClickListener(new OnClickListener() {
	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		TB_Details.this.finish();
		return true;
	    }
	});

	btnAction = new Button("Action");
	btnAction.setText(Translation.Get("TB_Log"));
	btnAction.setOnClickListener(new OnClickListener() {
	    @Override
	    public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
		showLogMenu();
		return true;
	    }
	});

	scrollBox = new ScrollBox(ActivityRec());
	scrollBox.setVirtualHeight(innerHeight);
	scrollBox.setHeight(this.getHeight() - (btnClose.getHeight() - margin) * 2.5f);
	scrollBox.setBackground(SpriteCacheBase.activityBackground);

	CB_RectF iconRec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight());
	iconRec = iconRec.ScaleCenter(0.8f);

	icon = new Image(iconRec, "Icon", false);
	lblName = new EditTextField(iconRec, this, this.name + " lblName");

	image = new Image(iconRec, "Image", false);
	lblAbout = new Label(Translation.Get("AboutThisItem"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
	lblAboutDesc = new Label("AboutDesc");
	AboutThisItem = new Box(10, 10, "AboutItemBox");
	AboutThisItem.setBackground(SpriteCacheBase.activityBackground);

	lblGoal = new Label(Translation.Get("GoalThisItem"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
	lblGoalDesc = new Label("GoalDesc");
	GoalThisItem = new Box(10, 10, "GoalItemBox");
	GoalThisItem.setBackground(SpriteCacheBase.activityBackground);

	lblTypeName = new Label(Translation.Get("TB_Type"), Fonts.getSmall(), COLOR.getDisableFontColor(), WrapType.SINGLELINE);
	lblTbCode = new Label(Translation.Get("TB_Code"), Fonts.getSmall(), COLOR.getDisableFontColor(), WrapType.SINGLELINE);
	lblOwner = new Label(Translation.Get("TB_Owner"), Fonts.getSmall(), COLOR.getDisableFontColor(), WrapType.SINGLELINE);
	// lbllastVisit = new Label("LastVisit");
	// lblHome = new Label("Home");
	lblBirth = new Label(Translation.Get("TB_Birth"), Fonts.getSmall(), COLOR.getDisableFontColor(), WrapType.SINGLELINE);
	// lblTravelDistance = new Label("TravelDistance");

	TypeName = new Label("TypeName");
	TbCode = new Label("TbCode");
	Owner = new Label("Owner");

	Birth = new Label("Birth");

	DetailThisItem = new Box(10, 10, "DetailThisItem");
	DetailThisItem.setBackground(SpriteCacheBase.activityBackground);
    }

    private void layout() {
	this.removeChilds();
	this.initRow(BOTTOMUP);
	this.addNext(btnAction);
	this.addLast(btnClose);
	this.addLast(scrollBox);
	this.setMargins(margin * 2, 0);
	this.addNext(icon, FIXED);
	icon.setImageURL(TB.getIconUrl());
	lblName.setWrapType(WrapType.WRAPPED);
	lblName.setBackground(null, null);
	this.addLast(lblName);
	lblName.setText(TB.getName());
	lblName.setEditable(false);
	lblName.showFromLineNo(0);
	lblName.setCursorPosition(0);

	scrollBox.setWidth(getWidth());
	scrollBox.setMargins(margin, 0);

	float minBoxHeight = Fonts.Measure("Tg").height + SpriteCacheBase.activityBackground.getBottomHeight() + SpriteCacheBase.activityBackground.getTopHeight();

	AboutThisItem.setWidth(scrollBox.getInnerWidth());
	lblAbout.setHeight(lblAbout.getTextHeight() + margin);
	lblAboutDesc.setWidth(AboutThisItem.getInnerWidth());
	lblAboutDesc.setWrappedText(TB.getDescription());
	lblAboutDesc.setHeight(lblAboutDesc.getTextHeight() + margin + margin);

	String ImgUrl = TB.getImageUrl();
	float ImageHeight = 0;
	if (ImgUrl != null && ImgUrl.length() > 0) {
	    image.setHeight(this.getWidth() / 3);
	    ImageHeight = image.getHeight();
	    image.setImageURL(ImgUrl);
	} else {
	    image.setHeight(0);
	}

	AboutThisItem.setHeight(Math.max(minBoxHeight, (lblAboutDesc.getHeight() + (margin * 4) + ImageHeight)));
	AboutThisItem.initRow();
	AboutThisItem.setMargins(0, margin * 3);
	if (ImageHeight > 0)
	    AboutThisItem.addLast(image);
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
	GL.that.renderOnce();
	GL.that.addRenderView(this, GL.FRAME_RATE_FAST_ACTION);
    }

    private void showLogMenu() {

	final Menu cm = new Menu("TBLogContextMenu");
	cm.addOnClickListener(menuItemClickListener);

	cm.addItem(MenuID.MI_TB_NOTE, "note", SpriteCacheBase.Icons.get(IconName.tbNote_63.ordinal()));

	if (TB.isLogTypePosible(LogTypes.discovered, CB_Core_Settings.GcLogin.getValue()))
	    cm.addItem(MenuID.MI_TB_DISCOVERED, "discovered", SpriteCacheBase.Icons.get(IconName.tbDiscover_58.ordinal()));

	if (TB.isLogTypePosible(LogTypes.visited, CB_Core_Settings.GcLogin.getValue()))
	    cm.addItem(MenuID.MI_TB_VISIT, "visit", SpriteCacheBase.Icons.get(IconName.tbVisit_62.ordinal()));

	if (TB.isLogTypePosible(LogTypes.dropped_off, CB_Core_Settings.GcLogin.getValue()))
	    cm.addItem(MenuID.MI_TB_DROPPED, "dropped", SpriteCacheBase.Icons.get(IconName.tbDrop_59.ordinal()));

	if (TB.isLogTypePosible(LogTypes.grab_it, CB_Core_Settings.GcLogin.getValue()))
	    cm.addItem(MenuID.MI_TB_GRABBED, "grabbed", SpriteCacheBase.Icons.get(IconName.tbGrab_60.ordinal()));

	if (TB.isLogTypePosible(LogTypes.retrieve, CB_Core_Settings.GcLogin.getValue()))
	    cm.addItem(MenuID.MI_TB_PICKED, "picked", SpriteCacheBase.Icons.get(IconName.tbPicked_61.ordinal()));

	cm.Show();
    }

    private final OnClickListener menuItemClickListener = new OnClickListener() {

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
	    if (TB_Log.that == null)
		new TB_Log();
	    switch (((MenuItem) v).getMenuItemId()) {

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

    @Override
    public void dispose() {

	if (scrollBox != null)
	    scrollBox.dispose();
	scrollBox = null;
	if (btnClose != null)
	    btnClose.dispose();
	btnClose = null;
	if (btnAction != null)
	    btnAction.dispose();
	btnAction = null;
	if (icon != null)
	    icon.dispose();
	icon = null;
	if (image != null)
	    image.dispose();
	image = null;
	if (lblName != null)
	    lblName.dispose();
	lblName = null;
	if (lblAbout != null)
	    lblAbout.dispose();
	lblAbout = null;
	if (lblAboutDesc != null)
	    lblAboutDesc.dispose();
	lblAboutDesc = null;
	if (lblGoal != null)
	    lblGoal.dispose();
	lblGoal = null;
	if (lblGoalDesc != null)
	    lblGoalDesc.dispose();
	lblGoalDesc = null;
	if (lblTypeName != null)
	    lblTypeName.dispose();
	lblTypeName = null;
	if (lblTbCode != null)
	    lblTbCode.dispose();
	lblTbCode = null;
	if (lblOwner != null)
	    lblOwner.dispose();
	lblOwner = null;
	if (lblBirth != null)
	    lblBirth.dispose();
	lblBirth = null;
	if (TypeName != null)
	    TypeName.dispose();
	TypeName = null;
	if (TbCode != null)
	    TbCode.dispose();
	TbCode = null;
	if (Owner != null)
	    Owner.dispose();
	Owner = null;
	if (Birth != null)
	    Birth.dispose();
	Birth = null;
	if (AboutThisItem != null)
	    AboutThisItem.dispose();
	AboutThisItem = null;
	if (GoalThisItem != null)
	    GoalThisItem.dispose();
	GoalThisItem = null;
	if (DetailThisItem != null)
	    DetailThisItem.dispose();
	DetailThisItem = null;

	super.dispose();

	that = null;

    }

}

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
package de.droidcachebox.gdx.activities;

import de.droidcachebox.WrapType;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.database.LogType;
import de.droidcachebox.database.Trackable;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

/**
 * TODO Visit,Home und distance müssen noch angezeigt werden!
 */
public class TB_Details extends ActivityBase {
    public static TB_Details that;
    private ScrollBox scrollBox;
    private Box scrollBoxContent;
    private CB_Button btnClose, btnAction;
    private Trackable trackable;
    private Image icon, image;
    private CB_Label lblDescription, lblGoal;
    private EditTextField title, description, currentGoal;
    private CB_Label lblTypeName, lblTbCode, lblOwner, lblBirth;
    private EditTextField TypeName, TbCode, Owner, Birth;

    public TB_Details() {
        super("TB_Details");
        createControls();
        that = this;
    }

    public void show(Trackable trackable) {
        this.trackable = trackable;
        layout();
        GL.that.showActivity(this);
    }

    private void createControls() {

        btnClose = new CB_Button(Translation.get("close"));
        btnClose.setClickHandler((v, x, y, pointer, button) -> {
            TB_Details.this.finish();
            return true;
        });

        btnAction = new CB_Button(Translation.get("TB_LogButton"));
        btnAction.setClickHandler((v, x, y, pointer, button) -> {
            showLogMenu();
            return true;
        });

        CB_RectF iconRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight());
        iconRec = iconRec.scaleCenter(0.8f);
        icon = new Image(iconRec, "Icon", false);
        title = new EditTextField(iconRec, this, "title");
        // on scrollbox
        image = new Image(iconRec, "Image", false);
        lblDescription = new CB_Label(Translation.get("AboutThisItem"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
        description = new EditTextField(this, "AboutDesc");
        lblGoal = new CB_Label(Translation.get("GoalThisItem"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
        currentGoal = new EditTextField(this, "GoalDesc");

        lblTypeName = new CB_Label(Translation.get("TB_Type"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
        lblTbCode = new CB_Label(Translation.get("TB_Code"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
        lblOwner = new CB_Label(Translation.get("TB_Owner"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
        // lbllastVisit = new Label("LastVisit");
        // lblHome = new Label("Home");
        lblBirth = new CB_Label(Translation.get("TB_Birth"), Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE);
        // lblTravelDistance = new Label("TravelDistance");

        TypeName = new EditTextField(this, "TypeName");
        TbCode = new EditTextField(this, "TbCode");
        Owner = new EditTextField(this, "Owner");

        Birth = new EditTextField(this, "Birth");

        scrollBox = new ScrollBox(innerWidth, getAvailableHeight());
        scrollBox.setBackground(getBackground());

        scrollBoxContent = new Box(scrollBox.getInnerWidth(), 0);

    }

    private void layout() {
        removeChilds();
        initRow(BOTTOMUP);
        addNext(btnAction);
        addLast(btnClose);

        initRow(TOPDOWN);
        addNext(icon, FIXED);
        icon.setImageURL(trackable.getIconUrl());
        title.setWrapType(WrapType.WRAPPED);
        addLast(title);
        title.setText(trackable.getName());
        title.setEditable(false);
        title.showFromLineNo(0);

        scrollBox.setHeight(getAvailableHeight());
        scrollBox.addChild(scrollBoxContent);

        addScrollBoxContent(lblTbCode, TbCode, trackable.getTbCode());
        String ImgUrl = trackable.getImageUrl();
        if (ImgUrl != null && ImgUrl.length() > 0) {
            image.setHeight(getWidth() / 3);
            image.setImageURL(ImgUrl);
        } else {
            image.setHeight(0);
        }
        scrollBoxContent.addLast(image, FIXED);
        addScrollBoxContent(lblTypeName, TypeName, trackable.getTypeName());
        addScrollBoxContent(lblOwner, Owner, trackable.getOwner());
        addScrollBoxContent(lblBirth, Birth, trackable.getBirth());
        addScrollBoxContent(lblGoal, currentGoal, trackable.getCurrentGoal());
        addScrollBoxContent(lblDescription, description, trackable.getDescription());
        scrollBoxContent.adjustHeight();

        scrollBox.setVirtualHeight(scrollBoxContent.getHeight());
        addLast(scrollBox);

        GL.that.renderOnce();
        GL.that.addRenderView(this, GL.FRAME_RATE_FAST_ACTION);
    }

    private void addScrollBoxContent(CB_Label lbl, EditTextField edt, String text) {
        lbl.setHeight(lbl.getTextHeight());
        scrollBoxContent.addLast(lbl);
        edt.setEditable(false);
        edt.setWidth(scrollBoxContent.getInnerWidth());
        edt.setWrapType(WrapType.WRAPPED);
        edt.setText(text);
        edt.setHeight(edt.getTextHeight());
        edt.showFromLineNo(0);
        scrollBoxContent.addLast(edt);
    }

    private void showLogMenu() {
        final Menu menuLog = new Menu("TB_DetailsLogMenuTitle");
        menuLog.addMenuItem("note", Sprites.getSprite(IconName.TBNOTE.name()), () -> TB_Log.getInstance().Show(trackable, LogType.note));
        if (trackable.isLogTypePossible(LogType.discovered, CB_Core_Settings.GcLogin.getValue()))
            menuLog.addMenuItem("discovered", Sprites.getSprite(IconName.TBDISCOVER.name()), () -> TB_Log.getInstance().Show(trackable, LogType.discovered));
        if (trackable.isLogTypePossible(LogType.visited, CB_Core_Settings.GcLogin.getValue()))
            menuLog.addMenuItem("visit", Sprites.getSprite(IconName.TBVISIT.name()), () -> TB_Log.getInstance().Show(trackable, LogType.visited));
        if (trackable.isLogTypePossible(LogType.dropped_off, CB_Core_Settings.GcLogin.getValue()))
            menuLog.addMenuItem("dropped", Sprites.getSprite(IconName.TBDROP.name()), () -> TB_Log.getInstance().Show(trackable, LogType.dropped_off));
        if (trackable.isLogTypePossible(LogType.grab_it, CB_Core_Settings.GcLogin.getValue()))
            menuLog.addMenuItem("grabbed", Sprites.getSprite(IconName.TBGRAB.name()), () -> TB_Log.getInstance().Show(trackable, LogType.grab_it));
        if (trackable.isLogTypePossible(LogType.retrieve, CB_Core_Settings.GcLogin.getValue()))
            menuLog.addMenuItem("picked", Sprites.getSprite(IconName.TBPICKED.name()), () -> TB_Log.getInstance().Show(trackable, LogType.retrieve));
        menuLog.show();
    }

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
        if (title != null)
            title.dispose();
        title = null;
        if (lblDescription != null)
            lblDescription.dispose();
        lblDescription = null;
        if (description != null)
            description.dispose();
        description = null;
        if (lblGoal != null)
            lblGoal.dispose();
        lblGoal = null;
        if (currentGoal != null)
            currentGoal.dispose();
        currentGoal = null;
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

        super.dispose();

        that = null;

    }

}

package de.droidcachebox.gdx.activities;

import static de.droidcachebox.locator.map.MapViewBase.INITIAL_WP_LIST;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.KeyboardFocusChangedEventList;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.CoordinateButton;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.EditTextField.TextFieldListener;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.Spinner;
import de.droidcachebox.gdx.controls.SpinnerAdapter;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.menuBtn2.ShowWaypoints;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.translation.Translation;

public class EditWaypoint extends ActivityBase implements KeyboardFocusChangedEventList.KeyboardFocusChangedEvent {

    private float virtualHeight = 0;
    private boolean showWaypointListAfterFinish;
    private Waypoint waypoint;
    private CoordinateButton bCoord;
    private Spinner sType;
    private CB_CheckBox cbStartPoint;
    private float cbStartPointWidth;
    private CB_Button bOK;
    private CB_Label tvCacheName;
    private CB_Label tvTyp;
    private CB_Label tvStartPoint;
    private CB_Label tvTitle;
    private EditTextField etTitle;
    private CB_Label tvDescription;
    private EditTextField etDescription;
    private CB_Label tvClue;
    private EditTextField etClue;
    private boolean firstShow;
    // damit kann festgelegt werden, ob beim Start des WaypointDialogs gleich der Coordinaten-Dialog gezeigt werden soll oder nicht.
    private boolean showCoordinateDialog;
    private ScrollBox scrollBox;
    private IReturnListener mReturnListener;

    public EditWaypoint(Waypoint waypoint, IReturnListener listener, boolean showCoordinateDialog, boolean showWaypointViewAfterFinish) {
        super("EditWayPoint");
        this.showWaypointListAfterFinish = showWaypointViewAfterFinish;

        scrollBox = new ScrollBox(this);
        this.addChild(scrollBox);
        this.waypoint = waypoint;
        this.mReturnListener = listener;
        this.showCoordinateDialog = showCoordinateDialog;

        iniCacheNameLabel();
        iniCoordButton();
        iniLabelTyp();
        iniTypeSpinner();
        iniLabelTitle();
        iniTitleTextField();
        iniLabelDesc();
        iniTitleTextDesc();
        iniLabelClue();
        iniTitleTextClue();
        iniOkCancel();

        layoutTextFields();

        scrollBox.setHeight(this.getHeight() - bOK.getMaxY() - margin);
        scrollBox.setY(bOK.getMaxY() + margin);
        scrollBox.setBackground(this.getBackground());
        scrollBox.setBorders(0, 0);

        cbStartPointWidth = 0;
        firstShow = true;
    }

    private void iniCacheNameLabel() {
        tvCacheName = new CB_Label(this.name + " tvCacheName", leftBorder + margin, getHeight() - this.getTopHeight() - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvCacheName.setFont(Fonts.getBubbleNormal());
        tvCacheName.setText(GlobalCore.getSelectedCache().getGeoCacheName());
        scrollBox.addChild(tvCacheName);
    }

    private void iniCoordButton() {
        CB_RectF rec = new CB_RectF(leftBorder, tvCacheName.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        Coordinate coordinate = waypoint.getCoordinate();
        if (!coordinate.isValid() || coordinate.isZero()) {
            // coordinate = get from gps
            coordinate = Locator.getInstance().getMyPosition();
            if (!coordinate.isValid() || coordinate.isZero()) {
                // coordinate = get from cache
                coordinate = GlobalCore.getSelectedCache().getCoordinate();
            }
        }
        bCoord = new CoordinateButton(rec, "CoordButton", coordinate, null);

        bCoord.setCoordinateChangedListener(coord -> EditWaypoint.this.show());

        scrollBox.addChild(bCoord);
    }

    private void iniLabelTyp() {
        cbStartPointWidth = UiSizes.getInstance().getButtonHeight() * 1.5f;
        tvTyp = new CB_Label(this.name + " tvTyp", leftBorder + margin, bCoord.getY() - margin - MeasuredLabelHeight, innerWidth - margin - cbStartPointWidth, MeasuredLabelHeight);
        tvTyp.setFont(Fonts.getBubbleNormal());
        tvTyp.setText(Translation.get("WayPointType"));
        scrollBox.addChild(tvTyp);

        tvStartPoint = new CB_Label(this.name + " tvStartPoint", tvTyp.getMaxX() + margin, bCoord.getY() - margin - MeasuredLabelHeight, cbStartPointWidth, MeasuredLabelHeight);
        tvStartPoint.setFont(Fonts.getBubbleNormal()).setHAlignment(HAlignment.CENTER);
        tvStartPoint.setText(Translation.get("start"));
        tvStartPoint.setVisible(false);
        scrollBox.addChild(tvStartPoint);

    }

    private void iniTypeSpinner() {
        CB_RectF rec = new CB_RectF(leftBorder, tvTyp.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth - cbStartPointWidth, UiSizes.getInstance().getButtonHeight());
        sType = new Spinner(rec, "WayPointType", getSpinerAdapter(), index -> {
            EditWaypoint.this.show();
            showCbStartPoint(false);
            switch (index) {
                case 0:
                    waypoint.waypointType = GeoCacheType.ReferencePoint;
                    break;
                case 1:
                    waypoint.waypointType = GeoCacheType.MultiStage;
                    showCbStartPoint(true);
                    break;
                case 2:
                    waypoint.waypointType = GeoCacheType.MultiQuestion;
                    break;
                case 3:
                    waypoint.waypointType = GeoCacheType.Trailhead;
                    break;
                case 4:
                    waypoint.waypointType = GeoCacheType.ParkingArea;
                    break;
                case 5:
                    waypoint.waypointType = GeoCacheType.Final;
                    break;
            }

        });

        // CheckBox for the selection whether this WP is the startpoint of the cache
        rec = new CB_RectF(tvStartPoint.getX() + tvStartPoint.getHalfWidth() - (UiSizes.getInstance().getButtonHeight() / 2.0f), tvTyp.getY() - UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
        cbStartPoint = new CB_CheckBox(rec);
        cbStartPoint.setVisible(false);

        // Spinner initialisieren
        switch (waypoint.waypointType) {
            case MultiStage:
                sType.setSelection(1);
                showCbStartPoint(true);
                cbStartPoint.setChecked(waypoint.isStartWaypoint);
                break;
            case MultiQuestion:
                sType.setSelection(2);
                break;
            case Trailhead:
                sType.setSelection(3);
                break;
            case ParkingArea:
                sType.setSelection(4);
                break;
            case Final:
                sType.setSelection(5);
                break;
            default:
                sType.setSelection(0);
        }

        scrollBox.addChild(sType);

        scrollBox.addChild(cbStartPoint);
    }

    private SpinnerAdapter getSpinerAdapter() {
        final String[] names = new String[]{Translation.get("Reference"), Translation.get("StageofMulti"), Translation.get("Question2Answer"), Translation.get("Trailhead"), Translation.get("Parking"), Translation.get("Final")};

        return new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return names[position];
            }

            @Override
            public Drawable getIcon(int Position) {
                switch (Position) {
                    case 0:
                        return new SpriteDrawable(Sprites.getSprite("big" + GeoCacheType.ReferencePoint.name()));
                    case 1:
                        return new SpriteDrawable(Sprites.getSprite("big" + GeoCacheType.MultiStage.name()));
                    case 2:
                        return new SpriteDrawable(Sprites.getSprite("big" + GeoCacheType.MultiQuestion.name()));
                    case 3:
                        return new SpriteDrawable(Sprites.getSprite("big" + GeoCacheType.Trailhead.name()));
                    case 4:
                        return new SpriteDrawable(Sprites.getSprite("big" + GeoCacheType.ParkingArea.name()));
                    case 5:
                        return new SpriteDrawable(Sprites.getSprite("big" + GeoCacheType.Final.name()));

                }

                return null;
            }

            @Override
            public int getCount() {
                return names.length;
            }
        };

    }

    private void iniLabelTitle() {
        tvTitle = new CB_Label(this.name + " tvTitle", leftBorder + margin, sType.getY() - margin - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvTitle.setFont(Fonts.getBubbleNormal());
        tvTitle.setText(Translation.get("Title"));
        scrollBox.addChild(tvTitle);
    }

    private void iniTitleTextField() {
        CB_RectF rec = new CB_RectF(leftBorder, tvTitle.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        etTitle = new EditTextField(rec, this, "*" + Translation.get("Title"));

        String txt = (waypoint.getTitle() == null) ? "" : waypoint.getTitle();

        etTitle.setText(txt);
        scrollBox.addChild(etTitle);
    }

    private void iniLabelDesc() {
        tvDescription = new CB_Label(this.name + " tvDescription", leftBorder + margin, etTitle.getY() - margin - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvDescription.setFont(Fonts.getBubbleNormal());
        tvDescription.setText(Translation.get("Description"));
        scrollBox.addChild(tvDescription);
    }

    private void iniTitleTextDesc() {
        CB_RectF rec = new CB_RectF(leftBorder, tvDescription.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        etDescription = new EditTextField(rec, this, "*" + Translation.get("Description"), WrapType.WRAPPED);

        String txt = (waypoint.getDescription() == null) ? "" : waypoint.getDescription();

        etDescription.setText(txt);

        etDescription.setTextFieldListener(new TextFieldListener() {

            @Override
            public void keyTyped(EditTextField textField, char key) {

            }

            @Override
            public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {
                layoutTextFields();
            }
        });

        scrollBox.addChild(etDescription);
    }

    private void iniLabelClue() {
        tvClue = new CB_Label(this.name + " tvClue", leftBorder + margin, etDescription.getY() - margin - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvClue.setFont(Fonts.getBubbleNormal());
        tvClue.setText(Translation.get("Clue"));
        scrollBox.addChild(tvClue);
    }

    private void iniTitleTextClue() {
        CB_RectF rec = new CB_RectF(leftBorder, tvClue.getY() - UiSizes.getInstance().getButtonHeight(), innerWidth, UiSizes.getInstance().getButtonHeight());
        etClue = new EditTextField(rec, this, "*" + Translation.get("Clue"), WrapType.WRAPPED);

        String txt = (waypoint.getClue() == null) ? "" : waypoint.getClue();

        etClue.setText(txt);

        etClue.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(EditTextField textField, char key) {

            }

            @Override
            public void lineCountChanged(EditTextField textField, int lineCount, float textHeight) {
                layoutTextFields();
            }
        });

        scrollBox.addChild(etClue);
    }

    private void iniOkCancel() {
        CB_RectF btnRec = new CB_RectF(leftBorder, this.getBottomHeight(), innerWidth / 2, UiSizes.getInstance().getButtonHeight());
        bOK = new CB_Button(btnRec, "OkButton");

        btnRec.setX(bOK.getMaxX());
        CB_Button bHelp = new CB_Button(btnRec, "HelpButton");
        bHelp.setText(Translation.get("help"));

        btnRec.setX(bOK.getMaxX());
        CB_Button bCancel = new CB_Button(btnRec, "CancelButton");

        bOK.setText(Translation.get("ok"));
        bCancel.setText(Translation.get("cancel"));

        this.addChild(bOK);
        // this.addChild(bHelp);
        this.addChild(bCancel);

        bOK.setClickHandler((v, x, y, pointer, button) -> {
            if (mReturnListener != null) {
                waypoint.setCoordinate(bCoord.getCoordinate());
                waypoint.setTitle(etTitle.getText());
                waypoint.setDescription(etDescription.getText());
                waypoint.setClue(etClue.getText());
                waypoint.isStartWaypoint = cbStartPoint.isChecked();
                mReturnListener.returnedWP(waypoint);
            }

            // Änderungen auch an die MapView melden
            ShowMap.getInstance().normalMapView.setNewSettings(INITIAL_WP_LIST);

            finish();

            // Show WP View?
            if (showWaypointListAfterFinish) {
                ShowWaypoints.getInstance().execute();
            }

            return true;
        });

        bCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (mReturnListener != null)
                mReturnListener.returnedWP(null);
            finish();
            return true;
        });
        bHelp.setClickHandler((v, x, y, pointer, button) -> true);
    }

    private void showCbStartPoint(boolean visible) {
        tvStartPoint.setVisible(visible);
        cbStartPoint.setVisible(visible);
    }

    private void layoutTextFields() {
        float maxTextFieldHeight = this.getHeight() / 2.3f;
        float rand = etClue.getStyle().getBottomHeight(true) + etClue.getStyle().getTopHeight(true); //if focused
        float descriptionHeight = Math.min(maxTextFieldHeight, etDescription.getMeasuredHeight() + rand);
        float clueHeight = Math.min(maxTextFieldHeight, etClue.getMeasuredHeight() + rand);

        descriptionHeight = Math.max(descriptionHeight, UiSizes.getInstance().getButtonHeight());
        clueHeight = Math.max(clueHeight, UiSizes.getInstance().getButtonHeight());

        etDescription.setHeight(descriptionHeight);
        etClue.setHeight(clueHeight);

        virtualHeight = 0;
        virtualHeight += tvCacheName.getHeight();
        virtualHeight += bCoord.getHeight();
        virtualHeight += tvTyp.getHeight();
        virtualHeight += sType.getHeight();
        virtualHeight += tvTitle.getHeight();
        virtualHeight += etTitle.getHeight();
        virtualHeight += tvDescription.getHeight();
        virtualHeight += etDescription.getHeight();
        virtualHeight += tvClue.getHeight();
        virtualHeight += etClue.getHeight();

        virtualHeight += tvStartPoint.getHeight();
        virtualHeight += tvCacheName.getHeight();
        virtualHeight += sType.getHeight();

        virtualHeight += 15 * margin;

        scrollBox.setVirtualHeight(virtualHeight);

        tvCacheName.setY(virtualHeight - tvCacheName.getHeight() - margin);
        bCoord.setY(tvCacheName.getY() - bCoord.getHeight() - margin);
        tvTyp.setY(bCoord.getY() - tvTyp.getHeight() - margin);
        tvStartPoint.setY(tvTyp.getY());
        sType.setY(tvTyp.getY() - sType.getHeight() - margin);
        cbStartPoint.setY(sType.getY());
        tvTitle.setY(sType.getY() - tvTitle.getHeight() - margin);
        etTitle.setY(tvTitle.getY() - etTitle.getHeight() - margin);
        tvDescription.setY(etTitle.getY() - tvDescription.getHeight() - margin);
        etDescription.setY(tvDescription.getY() - etDescription.getHeight() - margin);
        tvClue.setY(etDescription.getY() - tvClue.getHeight() - margin);
        etClue.setY(tvClue.getY() - etClue.getHeight() - margin);
    }

    @Override
    public void onShow() {
        // direct switch to input of coords (editCoord), if this is the first show
        if (firstShow && showCoordinateDialog)
            bCoord.performClick();
        firstShow = false;
        KeyboardFocusChangedEventList.add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.remove(this);
    }

    @Override
    public void keyboardFocusChanged(EditTextField editTextField) {
        if (editTextField != null) {
            // scroll to top
            scrollBox.scrollTo(-(virtualHeight - editTextField.getMaxY() - MeasuredLabelHeight));
        }
    }

    public interface IReturnListener {
        void returnedWP(Waypoint wp);
    }

}

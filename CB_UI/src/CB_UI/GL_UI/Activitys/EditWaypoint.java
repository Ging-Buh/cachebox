package CB_UI.GL_UI.Activitys;

import CB_Core.CacheTypes;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI.GL_UI.Controls.CoordinateButton.ICoordinateChangedListener;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.Events.KeyboardFocusChangedEvent;
import CB_UI_Base.Events.KeyboardFocusChangedEventList;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Spinner.ISelectionChangedListener;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

public class EditWaypoint extends ActivityBase implements KeyboardFocusChangedEvent {

    private final ArrayList<EditTextField> allTextFields = new ArrayList<EditTextField>();
    float virtualHeight = 0;
    private boolean showWaypointListAfterFinish = false;
    private Waypoint waypoint;
    private CoordinateButton bCoord = null;
    private Spinner sType = null;
    private ChkBox cbStartPoint = null;
    private float cbStartPointWidth = 0;
    private Button bOK = null;
    private Button bHelp = null;
    private Button bCancel = null;
    private Label tvCacheName = null;
    private Label tvTyp = null;
    private Label tvStartPoint = null;
    private Label tvTitle = null;
    private EditTextField etTitle = null;
    private Label tvDescription = null;
    private EditTextField etDescription = null;
    private Label tvClue = null;
    private EditTextField etClue = null;
    private Boolean firstShow = true;
    // damit kann festgelegt werden, ob beim Start des WaypointDialogs gleich der Coordinaten-Dialog gezeigt werden soll oder nicht.
    private Boolean showCoordinateDialog = false;
    private ScrollBox scrollBox;
    private IReturnListener mReturnListener;

    public EditWaypoint(Waypoint waypoint, IReturnListener listener, boolean showCoordinateDialog, boolean showWaypointViewAfterFinish) {
        super(ActivityRec(), "EditWpActivity");
        this.showWaypointListAfterFinish = showWaypointViewAfterFinish;

        scrollBox = new ScrollBox(ActivityRec());
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

        iniTextfieldFocus();
        layoutTextFields();

        scrollBox.setHeight(this.getHeight() - bOK.getMaxY() - margin);
        scrollBox.setY(bOK.getMaxY() + margin);
        scrollBox.setBackground(this.getBackground());
        scrollBox.setBorders(0, 0);

    }

    private void iniCacheNameLabel() {
        tvCacheName = new Label(this.name + " tvCacheName", leftBorder + margin, getHeight() - this.getTopHeight() - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvCacheName.setFont(Fonts.getBubbleNormal());
        tvCacheName.setText(GlobalCore.getSelectedCache().getName());
        scrollBox.addChild(tvCacheName);
    }

    private void iniCoordButton() {
        CB_RectF rec = new CB_RectF(leftBorder, tvCacheName.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth, UI_Size_Base.that.getButtonHeight());
        Coordinate coordinate = waypoint.Pos;
        if (!coordinate.isValid() || coordinate.isZero()) {
            // coordinate = get from gps
            coordinate = Locator.getCoordinate();
            if (!coordinate.isValid() || coordinate.isZero()) {
                // coordinate = get from cache
                coordinate = GlobalCore.getSelectedCache().Pos;
            }
        }
        bCoord = new CoordinateButton(rec, "CoordButton", coordinate, null);

        bCoord.setCoordinateChangedListener(coord -> EditWaypoint.this.show());

        scrollBox.addChild(bCoord);
    }

    private void iniLabelTyp() {
        cbStartPointWidth = UI_Size_Base.that.getButtonHeight() * 1.5f;
        tvTyp = new Label(this.name + " tvTyp", leftBorder + margin, bCoord.getY() - margin - MeasuredLabelHeight, innerWidth - margin - cbStartPointWidth, MeasuredLabelHeight);
        tvTyp.setFont(Fonts.getBubbleNormal());
        tvTyp.setText(Translation.Get("type"));
        scrollBox.addChild(tvTyp);

        tvStartPoint = new Label(this.name + " tvStartPoint", tvTyp.getMaxX() + margin, bCoord.getY() - margin - MeasuredLabelHeight, cbStartPointWidth, MeasuredLabelHeight);
        tvStartPoint.setFont(Fonts.getBubbleNormal()).setHAlignment(HAlignment.CENTER);
        tvStartPoint.setText(Translation.Get("start"));
        tvStartPoint.setVisible(false);
        scrollBox.addChild(tvStartPoint);

    }

    private void iniTypeSpinner() {
        CB_RectF rec = new CB_RectF(leftBorder, tvTyp.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth - cbStartPointWidth, UI_Size_Base.that.getButtonHeight());
        sType = new Spinner(rec, "CoordButton", getSpinerAdapter(), new ISelectionChangedListener() {

            @Override
            public void selectionChanged(int index) {
                EditWaypoint.this.show();
                showCbStartPoint(false);
                switch (index) {
                    case 0:
                        waypoint.Type = CacheTypes.ReferencePoint;
                        break;
                    case 1:
                        waypoint.Type = CacheTypes.MultiStage;
                        showCbStartPoint(true);
                        break;
                    case 2:
                        waypoint.Type = CacheTypes.MultiQuestion;
                        break;
                    case 3:
                        waypoint.Type = CacheTypes.Trailhead;
                        break;
                    case 4:
                        waypoint.Type = CacheTypes.ParkingArea;
                        break;
                    case 5:
                        waypoint.Type = CacheTypes.Final;
                        break;
                }

            }
        });

        // CheckBox for the selection whether this WP is the startpoint of the cache
        rec = new CB_RectF(tvStartPoint.getX() + tvStartPoint.getHalfWidth() - (UI_Size_Base.that.getButtonHeight() / 2), tvTyp.getY() - UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());
        cbStartPoint = new ChkBox(rec, "CheckBoxStartPoint");
        cbStartPoint.setVisible(false);

        // Spinner initialisieren
        switch (waypoint.Type) {
            case ReferencePoint:
                sType.setSelection(0);
                break;
            case MultiStage:
                sType.setSelection(1);
                showCbStartPoint(true);
                cbStartPoint.setChecked(waypoint.IsStart);
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
        final String[] names = new String[]{Translation.Get("Reference"), Translation.Get("StageofMulti"), Translation.Get("Question2Answer"), Translation.Get("Trailhead"), Translation.Get("Parking"), Translation.Get("Final")};

        SpinnerAdapter adapter = new SpinnerAdapter() {

            @Override
            public String getText(int position) {
                return names[position];
            }

            @Override
            public Drawable getIcon(int Position) {
                switch (Position) {
                    case 0:
                        return new SpriteDrawable(Sprites.getSprite("big" + CacheTypes.ReferencePoint.name()));
                    case 1:
                        return new SpriteDrawable(Sprites.getSprite("big" + CacheTypes.MultiStage.name()));
                    case 2:
                        return new SpriteDrawable(Sprites.getSprite("big" + CacheTypes.MultiQuestion.name()));
                    case 3:
                        return new SpriteDrawable(Sprites.getSprite("big" + CacheTypes.Trailhead.name()));
                    case 4:
                        return new SpriteDrawable(Sprites.getSprite("big" + CacheTypes.ParkingArea.name()));
                    case 5:
                        return new SpriteDrawable(Sprites.getSprite("big" + CacheTypes.Final.name()));

                }

                return null;
            }

            @Override
            public int getCount() {
                return names.length;
            }
        };

        return adapter;

    }

    private void iniLabelTitle() {
        tvTitle = new Label(this.name + " tvTitle", leftBorder + margin, sType.getY() - margin - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvTitle.setFont(Fonts.getBubbleNormal());
        tvTitle.setText(Translation.Get("Title"));
        scrollBox.addChild(tvTitle);
    }

    private void iniTitleTextField() {
        CB_RectF rec = new CB_RectF(leftBorder, tvTitle.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth, UI_Size_Base.that.getButtonHeight());
        etTitle = new EditTextField(rec, this, "etTitle");

        String txt = (waypoint.getTitle() == null) ? "" : waypoint.getTitle();

        etTitle.setText(txt);
        scrollBox.addChild(etTitle);
    }

    private void iniLabelDesc() {
        tvDescription = new Label(this.name + " tvDescription", leftBorder + margin, etTitle.getY() - margin - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvDescription.setFont(Fonts.getBubbleNormal());
        tvDescription.setText(Translation.Get("Description"));
        scrollBox.addChild(tvDescription);
    }

    private void iniTitleTextDesc() {
        CB_RectF rec = new CB_RectF(leftBorder, tvDescription.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth, UI_Size_Base.that.getButtonHeight());
        etDescription = new EditTextField(rec, this, "etDescription", WrapType.WRAPPED);

        String txt = (waypoint.getDescription() == null) ? "" : waypoint.getDescription();

        etDescription.setText(txt);

        etDescription.setTextFieldListener(new TextFieldListener() {

            @Override
            public void keyTyped(EditTextFieldBase textField, char key) {

            }

            @Override
            public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight) {
                layoutTextFields();
            }
        });

        scrollBox.addChild(etDescription);
    }

    private void iniLabelClue() {
        tvClue = new Label(this.name + " tvClue", leftBorder + margin, etDescription.getY() - margin - MeasuredLabelHeight, innerWidth - margin, MeasuredLabelHeight);
        tvClue.setFont(Fonts.getBubbleNormal());
        tvClue.setText(Translation.Get("Clue"));
        scrollBox.addChild(tvClue);
    }

    private void iniTitleTextClue() {
        CB_RectF rec = new CB_RectF(leftBorder, tvClue.getY() - UI_Size_Base.that.getButtonHeight(), innerWidth, UI_Size_Base.that.getButtonHeight());
        etClue = new EditTextField(rec, this, "etClue", WrapType.WRAPPED);

        String txt = (waypoint.getClue() == null) ? "" : waypoint.getClue();

        etClue.setText(txt);

        etClue.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(EditTextFieldBase textField, char key) {

            }

            @Override
            public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight) {
                layoutTextFields();
            }
        });

        scrollBox.addChild(etClue);
    }

    private void iniOkCancel() {
        CB_RectF btnRec = new CB_RectF(leftBorder, this.getBottomHeight(), innerWidth / 2, UI_Size_Base.that.getButtonHeight());
        bOK = new Button(btnRec, "OkButton");

        btnRec.setX(bOK.getMaxX());
        bHelp = new Button(btnRec, "HelpButton");
        bHelp.setText(Translation.Get("help"));

        btnRec.setX(bOK.getMaxX());
        bCancel = new Button(btnRec, "CancelButton");

        bOK.setText(Translation.Get("ok"));
        bCancel.setText(Translation.Get("cancel"));

        this.addChild(bOK);
        // this.addChild(bHelp);
        this.addChild(bCancel);

        bOK.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mReturnListener != null) {
                    waypoint.Pos = bCoord.getCoordinate();
                    waypoint.setTitle(etTitle.getText());
                    waypoint.setDescription(etDescription.getText());
                    waypoint.setClue(etClue.getText());
                    waypoint.IsStart = cbStartPoint.isChecked();
                    mReturnListener.returnedWP(waypoint);
                }

                // Änderungen auch an die MapView melden
                if (MapView.that != null)
                    MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);

                finish();

                // Show WP View?
                if (showWaypointListAfterFinish) {
                    if (TabMainView.actionShowWaypointView != null)
                        TabMainView.actionShowWaypointView.Execute();
                }

                return true;
            }
        });

        bCancel.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mReturnListener != null)
                    mReturnListener.returnedWP(null);
                finish();
                return true;
            }
        });
        bHelp.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                return true;
            }
        });
    }

    private void iniTextfieldFocus() {
        registerTextField(etTitle);
        registerTextField(etDescription);
        registerTextField(etClue);
    }

    private void showCbStartPoint(boolean visible) {
        tvStartPoint.setVisible(visible);
        cbStartPoint.setVisible(visible);
    }

    public void registerTextField(final EditTextField textField) {
        allTextFields.add(textField);
    }

    private void scrollToY(float y, float maxY) {
        if (y < this.getHalfHeight())// wird von softKeyboard verdeckt
        {
            scrollBox.scrollTo(-(virtualHeight - maxY - MeasuredLabelHeight));
        } else {
            scrollBox.scrollTo(-(virtualHeight - maxY - MeasuredLabelHeight));
            // scrollBox.scrollTo(0);
        }
    }

    private void layoutTextFields() {
        float maxTextFieldHeight = this.getHeight() / 2.3f;
        float rand = etClue.getStyle().getBottomHeight(true) + etClue.getStyle().getTopHeight(true); //if focused
        float descriptionHeight = Math.min(maxTextFieldHeight, etDescription.getMeasuredHeight() + rand);
        float clueHeight = Math.min(maxTextFieldHeight, etClue.getMeasuredHeight() + rand);

        descriptionHeight = Math.max(descriptionHeight, UI_Size_Base.that.getButtonHeight());
        clueHeight = Math.max(clueHeight, UI_Size_Base.that.getButtonHeight());

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
        KeyboardFocusChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        KeyboardFocusChangedEventList.Remove(this);
    }

    @Override
    public void KeyboardFocusChanged(EditTextField editTextField) {
        if (editTextField != null) {
            scrollToY(editTextField.getY(), editTextField.getMaxY());
        }
    }

    public interface IReturnListener {
        void returnedWP(Waypoint wp);
    }

}

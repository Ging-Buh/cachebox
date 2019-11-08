package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Clipboard;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.activities.EditCoord;
import de.droidcachebox.gdx.activities.EditCoord.ReturnListener;
import de.droidcachebox.gdx.controls.popups.CopyPastePopUp;
import de.droidcachebox.gdx.controls.popups.ICopyPaste;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.locator.Formatter;
import de.droidcachebox.translation.Translation;

public class CoordinateButton extends CB_Button implements ICopyPaste {
    protected Coordinate mActCoord;
    protected String mwpName;
    protected CopyPastePopUp popUp;
    protected Clipboard clipboard;
    OnClickListener longCLick = (v, x, y, pointer, button) -> {
        showPopUp(x, y);
        return true;
    };
    private EditCoord edCo;
    private ICoordinateChangedListener mCoordinateChangedListener;
    OnClickListener click = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            if (edCo == null)
                initialEdCo();
            GL.that.showActivity(edCo);
            return true;
        }
    };

    public CoordinateButton(CB_RectF rec, String name, Coordinate coordinate, String wpName) {
        super(rec, name);
        if (coordinate == null)
            coordinate = new Coordinate(0, 0);
        mActCoord = coordinate;
        mwpName = wpName;
        setText();
        this.addClickHandler(click);
        this.setOnLongClickListener(longCLick);
        clipboard = PlatformUIBase.getClipboard();
    }

    public CoordinateButton(String name) {
        super(name);
        mActCoord = new CoordinateGPS(0, 0);
        this.addClickHandler(click);
        this.setOnLongClickListener(longCLick);
        clipboard = PlatformUIBase.getClipboard();
    }

    public void setCoordinateChangedListener(ICoordinateChangedListener listener) {
        mCoordinateChangedListener = listener;
    }

    private void setText() {
        if (mwpName == null)
            this.setText(mActCoord.FormatCoordinate());
        else
            this.setText(mwpName);
    }

    @Override
    protected void initialize() {
        super.initialize();
        // switch ninePatchImages
        Drawable tmp = drawableNormal;
        drawableNormal = drawablePressed;
        drawablePressed = tmp;
    }

    private void initialEdCo() {

        edCo = new EditCoord(ActivityBase.activityRec(), "EditCoord", mActCoord, new ReturnListener() {

            @Override
            public void returnCoord(Coordinate coord) {
                if (coord != null && coord.isValid()) {
                    mActCoord = coord;
                    if (mCoordinateChangedListener != null)
                        mCoordinateChangedListener.coordinateChanged(coord);
                    setText();
                }
                if (edCo != null)
                    edCo.dispose();
                edCo = null;
            }
        });
    }

    public Coordinate getCoordinate() {
        return mActCoord;
    }

    public void setCoordinate(Coordinate pos) {
        mActCoord = pos;
        if (mActCoord == null)
            mActCoord = new CoordinateGPS(0, 0);
        setText();
    }

    @Override
    public void performClick() {
        super.performClick();
    }

    protected void showPopUp(int x, int y) {

        popUp = new CopyPastePopUp("CopyPastePopUp=>" + getName(), this);

        float noseOffset = popUp.getHalfWidth() / 2;

        CB_RectF world = getWorldRec();

        // not enough place on Top?
        float windowH = UiSizes.getInstance().getWindowHeight();
        float windowW = UiSizes.getInstance().getWindowWidth();
        float worldY = world.getY();

        if (popUp.getHeight() + worldY > windowH * 0.8f) {
            popUp.flipX();
            worldY -= popUp.getHeight() + (popUp.getHeight() * 0.2f);
        }

        x += world.getX() - noseOffset;

        if (x < 0)
            x = 0;
        if (x + popUp.getWidth() > windowW)
            x = (int) (windowW - popUp.getWidth());

        y += worldY + (popUp.getHeight() * 0.2f);
        popUp.show(x, y);
    }

    @Override
    public String pasteFromClipboard() {
        if (clipboard == null)
            return null;
        String content = clipboard.getContents();
        CoordinateGPS coord = null;
        if (content != null) {
            try {
                coord = new CoordinateGPS(content);
            } catch (Exception e) {
            }

            if (coord != null) {
                if (coord != null && coord.isValid()) {
                    mActCoord = coord;
                    if (mCoordinateChangedListener != null)
                        mCoordinateChangedListener.coordinateChanged(coord);
                    setText();
                }
                return content;
            } else {
                return Translation.get("cantPaste") + GlobalCore.br + content;
            }
        } else
            return null;
    }

    @Override
    public String copyToClipboard() {
        if (clipboard == null)
            return null;
        // perhaps implement selection of Format
        // String content = this.getText();
        String content = Formatter.FormatCoordinate(this.getCoordinate(), "");
        clipboard.setContents(content);
        return content;
    }

    @Override
    public String cutToClipboard() {
        if (clipboard == null)
            return null;
        // perhaps implement selection of Format
        // String content = this.getText();
        String content = Formatter.FormatCoordinate(this.getCoordinate(), "");
        clipboard.setContents(content);
        CoordinateGPS cor = new CoordinateGPS("N 0° 0.00 / E 0° 0.00");
        cor.setValid(false);
        this.setCoordinate(cor);
        return content;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    public interface ICoordinateChangedListener {
        void coordinateChanged(Coordinate coordinate);
    }
}

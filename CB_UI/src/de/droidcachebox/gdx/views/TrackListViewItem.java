package de.droidcachebox.gdx.views;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.ColorPicker;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.map.Track;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.UnitFormatter;

public class TrackListViewItem extends ListViewItemBackground {
    private static Sprite chkOff;
    private static Sprite chkOn;

    private static CB_RectF lBounds;
    private static CB_RectF rBounds;
    private static CB_RectF rChkBounds;
    private final IRouteChangedListener mRouteChangedListener;
    public Vector2 lastItemTouchPos;
    private Track track;
    private float left;
    private CB_Label trackName;
    private CB_Label EntryLength;
    private Sprite colorReck;
    private boolean Clicked = false;

    public TrackListViewItem(CB_RectF rec, int Index, Track route, IRouteChangedListener listener) {
        super(rec, Index, route.getName());
        track = route;
        mRouteChangedListener = listener;
    }

    @Override
    protected void render(Batch batch) {

        super.render(batch);

        boolean rClick;
        boolean lClick;
        if (this.isPressed) {
            // Log.debug(log, "TrackListViewItem => is Pressed");

            lClick = lBounds.contains(this.lastItemTouchPos);
            rClick = rBounds.contains(this.lastItemTouchPos);

            if (lClick || rClick)
                Clicked = true;

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            isPressed = GL_Input.that.getIsTouchDown();
        } else {
            if (Clicked) {
                // Log.debug(log, "TrackListViewItem => is Clicked");
                Clicked = false;
                lClick = lBounds.contains(this.lastItemTouchPos);
                rClick = rBounds.contains(this.lastItemTouchPos);
                if (lClick)
                    colorClick();
                if (rClick)
                    chkClick();
            }
        }

        // initial
        left = getLeftWidth();

        drawColorRec(batch);

        // draw Name
        if (trackName == null || EntryLength == null) {
            createLabel();
        }

        drawRightChkBox(batch);

    }

    private void createLabel() {
        if (trackName == null) {

            CB_RectF rec = new CB_RectF(left, this.getHeight() / 2, this.getWidth() - left - getHeight() - 10, this.getHeight() / 2);
            trackName = new CB_Label(rec);

            trackName.setText(track.getName());

            this.addChild(trackName);
        }

        // draw Length
        if (EntryLength == null) {

            CB_RectF rec = new CB_RectF(left, 0, this.getWidth() - left - getHeight() - 10, this.getHeight() / 2);
            EntryLength = new CB_Label(this.name + " EntryLength", rec, "");
            EntryLength.setText(Translation.get("length") + ": " + UnitFormatter.DistanceString((float) track.trackLength) + " / " + UnitFormatter.DistanceString((float) track.altitudeDifference));

            this.addChild(EntryLength);
        }

        GL.that.renderOnce();
    }

    private void drawColorRec(Batch batch) {
        if (track == null)
            return;
        if (lBounds == null) {
            lBounds = new CB_RectF(0, 0, getHeight(), getHeight());
            lBounds = lBounds.scaleCenter(0.95f);
        }

        if (colorReck == null) {
            colorReck = Sprites.getSprite("text-field-back");
            colorReck.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());
            colorReck.setColor(track.getColor());
        }

        colorReck.draw(batch);

        left += lBounds.getWidth() + UiSizes.getInstance().getMargin();

    }

    private void drawRightChkBox(Batch batch) {
        if (rBounds == null || rChkBounds == null) {
            rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// = right Button bounds

            rChkBounds = rBounds.scaleCenter(0.8f);
        }

        if (chkOff == null) {
            chkOff = Sprites.getSprite("check-off");
            chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
        }

        if (chkOn == null) {
            chkOn = Sprites.getSprite("check-on");
            chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
        }

        if (track.isVisible) {
            chkOn.draw(batch);
        } else {
            chkOff.draw(batch);
        }

    }

    private void chkClick() {
        // Log.debug(log, "TrackListViewItem => Chk Clicked");

        GL.that.RunOnGL(() -> {
            track.isVisible = !track.isVisible;
            if (mRouteChangedListener != null)
                mRouteChangedListener.routeChanged(track);
        });
        GL.that.renderOnce();
    }

    private void colorClick() {
        GL.that.RunOnGL(() -> {
            ColorPicker clrPick = new ColorPicker(ActivityBase.activityRec(), track.getColor(), color -> {
                if (color == null) return;
                track.setColor(color);
                colorReck = null;
            });
            clrPick.show();
        });
        GL.that.renderOnce();
    }

    public void notifyTrackChanged(Track track) {
        this.track = track;
        if (EntryLength != null)
            EntryLength.setText(Translation.get("length") + ": " + UnitFormatter.DistanceString((float) this.track.trackLength) + " / " + UnitFormatter.DistanceString((float) this.track.altitudeDifference));
    }

    public Track getTrack() {
        return track;
    }

    public interface IRouteChangedListener {
        void routeChanged(Track track);
    }

}

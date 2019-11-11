package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.core.LiveMapQue;
import de.droidcachebox.core.LiveMapQue.QueStateChanged;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.main.menuBtn3.ShowMap;
import de.droidcachebox.locator.Coordinate;

public class LiveButton extends ImageButton implements QueStateChanged {

    private static final int Duration = 2000;
    private static final int Frames = 8;
    private boolean state = false;
    private int Animation;
    private int lastAnimation = 0;

    public LiveButton() {
        super("");
        this.name = "LiveButton";
        this.setClickable(true);
        this.setImageScale(0.9f);
    }

    public void setState(boolean newState) {
        state = newState;
        Config.LiveMapEnabeld.setValue(newState);
        Config.AcceptChanges();
        switchImage();
    }

    private void switchImage() {
        if (state) {
            if (LiveMapQue.DownloadIsActive.get()) {
                try {
                    this.setImage(Sprites.LiveBtn.get(1 + Animation));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                this.setImage(Sprites.LiveBtn.get(0));
            }
        } else {
            this.setImage(Sprites.LiveBtn.get(1));
        }
        GL.that.renderOnce();
    }

    @Override
    public void render(Batch batch) {
        if (state) {
            if (GroundspeakAPI.isDownloadLimitExceeded()) {
                GlobalCore.MsgDownloadLimit();
                setState(false);
            }
        }
        Animation = (1 + ((int) (GL.that.getStateTime() * 1000) % Duration) / (Duration / Frames));
        if (lastAnimation != Animation) {
            lastAnimation = Animation;
            switchImage();
        }
        super.render(batch);
        if (LiveMapQue.DownloadIsActive.get())
            GL.that.renderOnce(true);
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        setState(!state);
        if (state) {
            Coordinate center = ShowMap.getInstance().normalMapView.center;
            LiveMapQue.quePosition(center);
        }
        return true;
    }

    @Override
    public void stateChanged() {
        switchImage();
        if (state) {
            if (LiveMapQue.DownloadIsActive.get()) {
                GL.that.addRenderView(this, GL.FRAME_RATE_ACTION);
            } else {
                GL.that.removeRenderView(this);
            }
        }
        GL.that.renderOnce();
    }
}

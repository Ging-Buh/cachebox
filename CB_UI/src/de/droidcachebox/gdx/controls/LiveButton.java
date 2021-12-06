package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.core.LiveMapQue;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;

public class LiveButton extends ImageButton {

    private static final int Duration = 2000;
    private static final int Frames = 8;
    private boolean isActivated = false;
    private int Animation;
    private int lastAnimation = 0;

    public LiveButton() {
        super();
        name = "LiveButton";
        setClickable(true);
        setImageScale(0.9f);
    }

    public void setActivated(boolean newState) {
        isActivated = newState;
        Settings.liveMapEnabled.setValue(newState);
        Settings.getInstance().acceptChanges();
        switchImage();
    }

    private void switchImage() {
        if (isActivated) {
            if (LiveMapQue.getInstance().getDownloadIsActive()) {
                try {
                    setImage(Sprites.LiveBtn.get(1 + Animation));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                setImage(Sprites.LiveBtn.get(0));
            }
        } else {
            setImage(Sprites.LiveBtn.get(1));
        }
        GL.that.renderOnce();
    }

    @Override
    public void render(Batch batch) {
        if (isActivated) {
            if (GroundspeakAPI.isDownloadLimitExceeded()) {
                GlobalCore.MsgDownloadLimit();
                setActivated(false);
            }
        }
        Animation = (1 + ((int) (GL.that.getStateTime() * 1000) % Duration) / (Duration / Frames));
        if (lastAnimation != Animation) {
            lastAnimation = Animation;
            switchImage();
        }
        super.render(batch);
        if (LiveMapQue.getInstance().getDownloadIsActive())
            GL.that.renderOnce(true);
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        setActivated(!isActivated);
        if (isActivated) {
            LiveMapQue.getInstance().quePosition(ShowMap.getInstance().normalMapView.center);
        } else {
            LiveMapQue.getInstance().clearDescriptorStack();
        }
        return true;
    }

}

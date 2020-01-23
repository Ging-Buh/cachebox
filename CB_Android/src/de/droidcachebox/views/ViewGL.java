package de.droidcachebox.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import de.droidcachebox.R;
import de.droidcachebox.ViewOptionsMenu;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Listener_Interface;
import de.droidcachebox.utils.log.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class ViewGL extends RelativeLayout implements ViewOptionsMenu, GL_Listener_Interface {
    public final static int GLSURFACE_VIEW20 = 0;
    public final static int GLSURFACE_GLSURFACE = 3;
    private View gdxView;
    private static int currentSurfaceType = -1;
    private AtomicBoolean isContinuousRenderMode = new AtomicBoolean(true);

    public ViewGL(Context context, LayoutInflater inflater, View _gdxView) {
        super(context);
        gdxView = _gdxView;
        GL.that.setGlListener(this);
        try {
            RelativeLayout mapviewLayout = (RelativeLayout) inflater.inflate(R.layout.mapviewgl, null, false);
            addView(mapviewLayout);
            mapviewLayout.addView(_gdxView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        } catch (Exception ignored) {
        }
    }

    public ViewGL(Context context) {
        super(context);
    }

    /**
     * Setzt den OpenGl Surface Type!
     *
     * @param surfaceType </br> GLSURFACE_VIEW20=0 </br> GLSURFACE_CUPCAKE = 1 </br> GLSURFACE_DEFAULT = 2 </br> GLSURFACE_GLSURFACE = 3
     */
    public static void setSurfaceType(int surfaceType) {
        currentSurfaceType = surfaceType;
    }

    @Override
    public boolean itemSelected(MenuItem item) {
        return item.getItemId() == 0;
    }

    @Override
    public void beforeShowMenu(Menu menu) {
    }

    @Override
    public int getMenuId() {
        return 0;
    }

    @Override
    public void onShow() {
    }

    @Override
    public void onHide() {
    }

    @Override
    public void onFree() {
    }

    @Override
    public int getContextMenuId() {
        return 0;
    }

    @Override
    public boolean contextMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void requestRender() {

        // Log.debug(log, "requestRender von : " + requestName);

        switch (currentSurfaceType) {
            case GLSURFACE_VIEW20:
                ((GLSurfaceView20) gdxView).requestRender();
                break;

            case GLSURFACE_GLSURFACE:
                ((GLSurfaceView) gdxView).requestRender();
                break;
        }
    }

    @Override
    public void renderDirty() {
        // Log.debug(log, "Set: renderDirty");
        try {
            switch (currentSurfaceType) {
                case GLSURFACE_VIEW20:
                    ((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_WHEN_DIRTY);
                    break;

                case GLSURFACE_GLSURFACE:
                    ((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_WHEN_DIRTY);
                    break;
            }
            isContinuousRenderMode.set(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void renderContinous() {
        // .Log.info(log, "Set: renderContinous");
        switch (currentSurfaceType) {
            case GLSURFACE_VIEW20:
                ((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                break;

            case GLSURFACE_GLSURFACE:
                ((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                break;
        }
        isContinuousRenderMode.set(true);
    }

    @Override
    public boolean isContinous() {
        return isContinuousRenderMode.get();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mesuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mesuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        Log.info("CACHEBOX", "With/Height " + mesuredWidth + " / " + mesuredHeight);

        setMeasuredDimension(mesuredWidth, mesuredHeight);

    }

}

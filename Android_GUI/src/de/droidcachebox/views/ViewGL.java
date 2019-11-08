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
    private static final String log = "ViewGL";
    private View gdxView;
    private static int mAktSurfaceType = -1;
    private AtomicBoolean isContinousRenderMode = new AtomicBoolean(true);

    public ViewGL(Context context, LayoutInflater inflater, View gdxView) {
        super(context);
        this.gdxView = gdxView;
        GL.that.setGL_Listener_Interface(this);
        try {

            RelativeLayout mapviewLayout = (RelativeLayout) inflater.inflate(R.layout.mapviewgl, null, false);
            this.addView(mapviewLayout);

            mapviewLayout.addView(gdxView, new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        } catch (Exception ex) {
        }
    }

    public ViewGL(Context context) {
        super(context);
    }

    public static int getSurfaceType() {
        return mAktSurfaceType;
    }

    /**
     * Setzt den OpenGl Surface Type!
     *
     * @param Type </br> GLSURFACE_VIEW20=0 </br> GLSURFACE_CUPCAKE = 1 </br> GLSURFACE_DEFAULT = 2 </br> GLSURFACE_GLSURFACE = 3
     */
    public static void setSurfaceType(int Type) {
        mAktSurfaceType = Type;
    }

    @Override
    public boolean ItemSelected(MenuItem item) {
        return item.getItemId() == 0;
    }

    @Override
    public void BeforeShowMenu(Menu menu) {
    }

    @Override
    public int GetMenuId() {
        return 0;
    }

    @Override
    public void OnShow() {
    }

    @Override
    public void OnHide() {
    }

    @Override
    public void OnFree() {

    }

    @Override
    public int GetContextMenuId() {
        return 0;
    }

    @Override
    public boolean ContextMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void RequestRender() {

        // Log.debug(log, "RequestRender von : " + requestName);

        switch (mAktSurfaceType) {
            case GLSURFACE_VIEW20:
                ((GLSurfaceView20) gdxView).requestRender();
                break;

            case GLSURFACE_GLSURFACE:
                ((GLSurfaceView) gdxView).requestRender();
                break;
        }
    }

    @Override
    public void RenderDirty() {
        // Log.debug(log, "Set: RenderDirty");
        try {
            switch (mAktSurfaceType) {
                case GLSURFACE_VIEW20:
                    ((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_WHEN_DIRTY);
                    break;

                case GLSURFACE_GLSURFACE:
                    ((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_WHEN_DIRTY);
                    break;
            }
            isContinousRenderMode.set(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void RenderContinous() {
        // .Log.info(log, "Set: RenderContinous");
        switch (mAktSurfaceType) {
            case GLSURFACE_VIEW20:
                ((GLSurfaceView20) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                break;

            case GLSURFACE_GLSURFACE:
                ((GLSurfaceView) gdxView).setRenderMode(GLSurfaceView20.RENDERMODE_CONTINUOUSLY);
                break;
        }
        isContinousRenderMode.set(true);
    }

    @Override
    public boolean isContinous() {
        return isContinousRenderMode.get();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mesuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int mesuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        Log.info("CACHEBOX", "With/Height " + mesuredWidth + " / " + mesuredHeight);

        setMeasuredDimension(mesuredWidth, mesuredHeight);

    }

}

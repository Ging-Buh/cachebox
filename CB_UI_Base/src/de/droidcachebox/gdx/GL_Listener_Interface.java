package de.droidcachebox.gdx;

public interface GL_Listener_Interface {
     void requestRender();

     void renderDirty();

     void renderContinuous();

     boolean isContinuous();
}

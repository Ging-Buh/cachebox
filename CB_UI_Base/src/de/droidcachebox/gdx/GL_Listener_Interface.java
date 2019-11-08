package de.droidcachebox.gdx;

public interface GL_Listener_Interface {
     void RequestRender();

     void RenderDirty();

     void RenderContinous();

     boolean isContinous();
}

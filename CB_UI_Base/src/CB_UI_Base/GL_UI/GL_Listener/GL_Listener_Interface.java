package CB_UI_Base.GL_UI.GL_Listener;

public interface GL_Listener_Interface {
     void RequestRender();

     void RenderDirty();

     void RenderContinous();

     boolean isContinous();
}

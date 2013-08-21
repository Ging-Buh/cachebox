package CB_UI_Base.GL_UI.GL_Listener;


public interface GL_Listener_Interface
{
	public void RequestRender(String requestName);

	public void RenderDirty();

	public void RenderContinous();
}

package CB_Core.GL_UI.GL_Listener;

import CB_Core.GL_UI.GL_View_Base;

public interface GL_Listener_Interface
{
	public void RequestRender(GL_View_Base view);

	public void RenderDirty();

	public void RenderContinous();
}

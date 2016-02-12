package CB_UI_Base.GL_UI.interfaces;

public interface ICopyPaste {
	public String pasteFromClipboard();

	public String copyToClipboard();

	public String cutToClipboard();

	public boolean isEditable();
}

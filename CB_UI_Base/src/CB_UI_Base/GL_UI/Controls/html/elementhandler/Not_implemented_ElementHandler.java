package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_Utils.Exceptions.NotImplementedException;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class Not_implemented_ElementHandler implements ElementHandler {
	public static final ElementHandler INSTANCE = new Not_implemented_ElementHandler();

	@Override
	public void process(Processor x, Element element) throws IOException {
	throw new NotImplementedException("HTML Renderer element <" + element.getName() + "> is not implemented");
	}
}
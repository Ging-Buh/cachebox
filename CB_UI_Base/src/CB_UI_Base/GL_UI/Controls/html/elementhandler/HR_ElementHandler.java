package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.AbstractBlockElementHandler;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class HR_ElementHandler extends AbstractBlockElementHandler {
	public static final ElementHandler INSTANCE = new HR_ElementHandler();

	private HR_ElementHandler() {
	this(0, 0, false);
	}

	private HR_ElementHandler(int topMargin, int bottomMargin, boolean indent) {
	super(topMargin, bottomMargin, indent);
	}

	@Override
	protected void processBlockContent(Processor x, Element element) throws IOException {
	CB_HtmlProcessor cb_processor = (CB_HtmlProcessor) x;

	cb_processor.createNewSegment();
	cb_processor.createNewHrSegment();
	}

	@Override
	protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
	return new HR_ElementHandler(topMargin, bottomMargin, indent);
	}
}
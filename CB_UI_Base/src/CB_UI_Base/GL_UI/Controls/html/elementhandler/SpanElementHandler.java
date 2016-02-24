package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.AbstractBlockElementHandler;
import net.htmlparser.jericho.Renderer.ElementHandler;
import net.htmlparser.jericho.Renderer.StandardBlockElementHandler;

public final class SpanElementHandler extends AbstractBlockElementHandler {
public static final ElementHandler INSTANCE = new SpanElementHandler(0, 0, false);

public SpanElementHandler(int topMargin, int bottomMargin, boolean indent) {
	super(topMargin, bottomMargin, indent);
}

@Override
protected void processBlockContent(Processor x, Element element) throws IOException {

	CB_HtmlProcessor xp = (CB_HtmlProcessor) x;
	boolean was = xp.spanelement;
	if (!was)
	xp.spanelement = true;
	x.appendElementContent(element);
	if (!was)
	xp.spanelement = false;
}

@Override
protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
	return new StandardBlockElementHandler(topMargin, bottomMargin, indent);
}
}
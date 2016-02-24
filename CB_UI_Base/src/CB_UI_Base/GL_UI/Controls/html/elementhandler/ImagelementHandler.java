package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class ImagelementHandler implements ElementHandler {
	public static final ElementHandler INSTANCE = new ImagelementHandler();

	@Override
	public void process(Processor x, Element element) throws IOException {
		((CB_HtmlProcessor) x).createNewSegment();
		String src = element.getStartTag().getAttributeValue("src");
		if (src == null)
			return;

		//create new segment, if append not empty  
		if (!x.appendable.toString().isEmpty())
			((CB_HtmlProcessor) x).createNewSegment(true);

		x.appendText(src);
		((CB_HtmlProcessor) x).isImage = true;
		((CB_HtmlProcessor) x).createNewSegment();
	}
}
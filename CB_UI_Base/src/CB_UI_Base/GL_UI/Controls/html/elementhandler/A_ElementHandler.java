package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import CB_UI_Base.GL_UI.Controls.html.HyperLinkText;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class A_ElementHandler implements ElementHandler {
	public static final ElementHandler INSTANCE = new A_ElementHandler();

	@Override
	public void process(Processor x, Element element) throws IOException {
		CB_HtmlProcessor cb_processor = (CB_HtmlProcessor) x;

		String lastText = x.appendable.toString();

		x.appendElementContent(element);

		String text = x.appendable.toString().replace(lastText, "");
		String renderedHyperlinkURL = x.renderer.renderHyperlinkURL(element.getStartTag());

		if (text != null)
			text = text.trim();
		if (renderedHyperlinkURL != null && !text.isEmpty()) {
			renderedHyperlinkURL = renderedHyperlinkURL.trim();

			cb_processor.add(new HyperLinkText(text, renderedHyperlinkURL));
		}
	}
}
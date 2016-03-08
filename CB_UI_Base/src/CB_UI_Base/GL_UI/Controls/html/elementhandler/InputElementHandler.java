package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import CB_UI_Base.GL_UI.Controls.html.Html_Segment_Input;
import CB_UI_Base.GL_UI.Controls.html.Html_Segment_Typ;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class InputElementHandler implements ElementHandler {
	public static final ElementHandler INSTANCE = new InputElementHandler();

	@Override
	public void process(Processor x, Element element) throws IOException {

		String src = element.getAttributeValue("src");
		String val = element.getAttributeValue("value").trim();

		Html_Segment_Input seg = new Html_Segment_Input(Html_Segment_Typ.Input, CB_HtmlProcessor.AtributeStack, src, val);
		((CB_HtmlProcessor) x).segmentList.add(seg);

	}
}
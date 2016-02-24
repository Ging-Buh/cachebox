package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import CB_UI_Base.GL_UI.Controls.html.CB_Html_Renderer;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.AbstractBlockElementHandler;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class LI_ElementHandler extends AbstractBlockElementHandler {
	public static final ElementHandler INSTANCE = new LI_ElementHandler();

	private LI_ElementHandler() {
	this(0, 0, false);
	}

	private LI_ElementHandler(int topMargin, int bottomMargin, boolean indent) {
	super(topMargin, bottomMargin, indent);
	}

	@Override
	protected void processBlockContent(Processor x, Element element) throws IOException {
	if (x.listBulletNumber != CB_Html_Renderer.UNORDERED_LIST)
		x.listBulletNumber++;
	//x.bullet = true;
	((CB_HtmlProcessor) x).addListelement(element);

	}

	@Override
	protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
	return new LI_ElementHandler(topMargin, bottomMargin, indent);
	}
}
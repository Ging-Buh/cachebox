package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import CB_UI_Base.GL_UI.Controls.html.CB_Html_Renderer;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.AbstractBlockElementHandler;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class ListElementHandler extends AbstractBlockElementHandler {
	public static final ElementHandler INSTANCE_OL = new ListElementHandler(0, true);
	public static final ElementHandler INSTANCE_UL = new ListElementHandler(CB_Html_Renderer.UNORDERED_LIST, false);
	private final int initialListBulletNumber;
	private final boolean ordert;

	private ListElementHandler(int initialListBulletNumber, boolean ordertList) {
	this(initialListBulletNumber, 0, 0, false, ordertList);
	}

	private ListElementHandler(int initialListBulletNumber, int topMargin, int bottomMargin, boolean indent, boolean ordertList) {
	super(topMargin, bottomMargin, indent);
	this.initialListBulletNumber = initialListBulletNumber;
	this.ordert = ordertList;
	}

	@Override
	protected void processBlockContent(Processor x, Element element) throws IOException {
	((CB_HtmlProcessor) x).newList(element.getChildElements(), initialListBulletNumber, ordert);
	}

	@Override
	protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
	return new ListElementHandler(initialListBulletNumber, topMargin, bottomMargin, indent, false);
	}
}
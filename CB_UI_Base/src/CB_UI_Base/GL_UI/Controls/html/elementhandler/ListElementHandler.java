package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import CB_UI_Base.GL_UI.Controls.html.CB_Html_Renderer;
import CB_UI_Base.GL_UI.Controls.html.HTML_Segment_List;
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
		int oldListBulletNumber = x.listBulletNumber;
		x.listIndentLevel++;
		CB_Html_Renderer.log.debug("Create new List:");
		((CB_HtmlProcessor) x).actList = new HTML_Segment_List(CB_HtmlProcessor.AtributeStack, oldListBulletNumber, x.listIndentLevel, this.ordert);

		x.listBulletNumber = initialListBulletNumber;

		x.appendElementContent(element);
		x.listIndentLevel--;
		x.listBulletNumber = oldListBulletNumber;

		((CB_HtmlProcessor) x).addListToSegments();

	}

	@Override
	protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
		return new ListElementHandler(initialListBulletNumber, topMargin, bottomMargin, indent, false);
	}
}
package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;
import java.util.List;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.AbstractBlockElementHandler;
import net.htmlparser.jericho.Renderer.ElementHandler;

public final class TableElementHandler extends AbstractBlockElementHandler {
    public static final ElementHandler INSTANCE = new TableElementHandler(0, 0, false);

    private TableElementHandler(int topMargin, int bottomMargin, boolean indent) {
	super(topMargin, bottomMargin, indent);

    }

    @Override
    protected void processBlockContent(Processor x, Element element) throws IOException {

	List<Element> rows = element.getAllElements("tr");

	int colSize = 0;
	for (Element row : rows) {

	    List<Element> col = row.getAllElements("td");
	    colSize = Math.max(colSize, col.size());
	}

	//

	Element[][] tableElements = new Element[rows.size()][colSize];

	// fill table content elements
	int rowIdx = 0;
	for (Element row : rows) {
	    int colIdx = 0;
	    for (Element col : row.getAllElements("td")) {
		tableElements[rowIdx][colIdx++] = col;
	    }
	    rowIdx++;
	}

	float borderSize = 1;
	String borderString = element.getAttributeValue("border");
	if (borderString != null && !borderString.isEmpty()) {
	    try {
		borderSize = Float.parseFloat(borderString);
	    } catch (NumberFormatException e) {

	    }
	}

	float cellpaddingSize = 1;
	String cellpaddingString = element.getAttributeValue("cellpadding");
	if (borderString != null && !cellpaddingString.isEmpty()) {
	    try {
		cellpaddingSize = Float.parseFloat(cellpaddingString);
	    } catch (NumberFormatException e) {

	    }
	}

	float cellspacingSize = 1;
	String cellspacingString = element.getAttributeValue("cellspacing");
	if (cellspacingString != null && !cellspacingString.isEmpty()) {
	    try {
		cellspacingSize = Float.parseFloat(cellspacingString);
	    } catch (NumberFormatException e) {

	    }
	}

	((CB_HtmlProcessor) x).newTable(tableElements, borderSize, cellpaddingSize, cellspacingSize);

	System.out.println("new Table");

    }

    @Override
    protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
	return new TableElementHandler(topMargin, bottomMargin, indent);
    }
}
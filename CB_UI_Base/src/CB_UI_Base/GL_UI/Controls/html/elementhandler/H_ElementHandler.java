package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import java.io.IOException;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.AbstractBlockElementHandler;
import net.htmlparser.jericho.Renderer.ElementHandler;
import net.htmlparser.jericho.Renderer.StandardBlockElementHandler;
import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;

public class H_ElementHandler extends AbstractBlockElementHandler {
    public static final ElementHandler INSTANCE_H1 = new H_ElementHandler(1);
    public static final ElementHandler INSTANCE_H2 = new H_ElementHandler(2);
    public static final ElementHandler INSTANCE_H3 = new H_ElementHandler(3);
    public static final ElementHandler INSTANCE_H4 = new H_ElementHandler(4);
    public static final ElementHandler INSTANCE_H5 = new H_ElementHandler(5);
    public static final ElementHandler INSTANCE_H6 = new H_ElementHandler(6);

    H_ElementHandler(int type) {
	super(2, 1, false);
    }

    @Override
    protected void processBlockContent(Processor x, Element element) throws IOException {

	CB_HtmlProcessor p = ((CB_HtmlProcessor) x);
	p.appendElementContent(element);

    }

    @Override
    protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
	return new StandardBlockElementHandler(topMargin, bottomMargin, indent);
    }
}

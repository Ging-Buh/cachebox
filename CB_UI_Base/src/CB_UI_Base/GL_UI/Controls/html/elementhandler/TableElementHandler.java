package CB_UI_Base.GL_UI.Controls.html.elementhandler;

import CB_UI_Base.GL_UI.Controls.html.CB_HtmlProcessor;
import CB_UI_Base.GL_UI.Controls.html.CB_Html_Renderer;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Processor;
import net.htmlparser.jericho.Renderer.AbstractBlockElementHandler;
import net.htmlparser.jericho.Renderer.ElementHandler;

import java.io.IOException;

public final class TableElementHandler extends AbstractBlockElementHandler {
    public static final ElementHandler INSTANCE = new TableElementHandler(0, 0, false);


    private TableElementHandler(int topMargin, int bottomMargin, boolean indent) {
        super(topMargin, bottomMargin, indent);

    }

    @Override
    protected void processBlockContent(Processor x, Element element) throws IOException {

        System.out.println("new Table");


    }

    @Override
    protected AbstractBlockElementHandler newInstance(int topMargin, int bottomMargin, boolean indent) {
        return new TableElementHandler(topMargin, bottomMargin, indent);
    }
}
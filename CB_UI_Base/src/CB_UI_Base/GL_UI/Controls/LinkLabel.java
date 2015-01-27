package CB_UI_Base.GL_UI.Controls;

import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.GL_UI.Controls.html.HyperLinkText;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.Batch;

public class LinkLabel extends MultiColorLabel {

    private final AtomicBoolean dirty = new AtomicBoolean(true);;
    private final AtomicBoolean inParse = new AtomicBoolean(false);
    CB_List<HyperLinkText> hyperLinkList = new CB_List<HyperLinkText>();
    private boolean isMarkup = false;

    public LinkLabel(float X, float Y, float Width, float Height, String Text) {
	super(X, Y, Width, Height, Text);
    }

    @Override
    public void render(Batch batch) {
	if (dirty.get()) {
	    if (inParse.get())
		return;
	    parse();

	}

	if (isMarkup)
	    this.mFont.setMarkupEnabled(true);
	super.render(batch);
    }

    private void parse() {
	inParse.set(true);

	for (int i = 0, n = this.hyperLinkList.size(); i < n; i++) {

	    HyperLinkText hyper = this.hyperLinkList.get(i);

	    this.mFont.setMarkupEnabled(true);
	    this.setText(this.mText.replace(hyper.content, "[#0000ffff]" + hyper.content + "[]"));
	    isMarkup = true;

	}

	dirty.set(false);
	inParse.set(false);
    }

    public void addHyperlinks(CB_List<HyperLinkText> HyperLinkList) {
	this.hyperLinkList = HyperLinkList;
    }

    public void setMarkupEnabled(boolean IsMarkUp) {
	this.isMarkup = IsMarkUp;
    }
}

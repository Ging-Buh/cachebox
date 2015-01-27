package CB_UI_Base.GL_UI.Controls;

import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.Color;

public class MultiColorLabel extends Label {

    private class MultiColor {
	Color color;
	int start;
	int end;
    }

    private final CB_List<MultiColor> colorList = new CB_List<MultiColorLabel.MultiColor>();

    public MultiColorLabel(float X, float Y, float Width, float Height, String Text) {
	super(X, Y, Width, Height, Text);
    }

    public void resetMultiColor() {
	colorList.clear();
    }

    public void addMultiColor(Color c, int start, int end) {
	MultiColor mc = new MultiColor();
	mc.color = c;
	mc.start = start;
	mc.end = end;
	colorList.add(mc);
    }

    @Override
    protected void setText() {
	super.setText();
	setColors();
    }

    @Override
    protected void setMultiLineText() {
	super.setMultiLineText();
	setColors();
    }

    @Override
    protected void setWrappedText() {
	super.setWrappedText();
	setColors();
    }

    private void setColors() {
	if (colorList == null)
	    return;
	synchronized (colorList) {
	    for (int i = 0, n = colorList.size(); i < n; i++) {
		try {
		    MultiColor c = colorList.get(i);
		    TextObject.setColors(c.color, c.start, c.end);
		} catch (Exception e) {
		    // TODO: handle exception
		}
	    }
	}

    }

}

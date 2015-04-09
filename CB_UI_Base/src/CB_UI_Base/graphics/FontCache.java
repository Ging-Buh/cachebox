package CB_UI_Base.graphics;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontCache {

    private final static GL_Fonts NORMAL = new GL_Fonts(false);
    private final static GL_Fonts MULTICOLOR = new GL_Fonts(true);

    public static BitmapFont get(GL_FontFamily fontFamily, GL_FontStyle fontStyle, float textSize) {
	return NORMAL.get(fontFamily, fontStyle, textSize);
    }

    public static BitmapFont get(boolean markUp, GL_FontFamily fontFamily, GL_FontStyle fontStyle, float textSize) {
	if (markUp) {
	    return MULTICOLOR.get(fontFamily, fontStyle, textSize);
	} else {
	    return NORMAL.get(fontFamily, fontStyle, textSize);
	}
    }

}

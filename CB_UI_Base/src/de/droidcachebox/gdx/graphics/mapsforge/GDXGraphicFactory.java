package de.droidcachebox.gdx.graphics.mapsforge;

import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

public interface GDXGraphicFactory extends org.mapsforge.core.graphics.GraphicFactory {

    @Override
    GDXBitmap createBitmap(int width, int height);

    @Override
    Matrix createMatrix();

    @Override
    GDXPath createPath();

    @Override
    Paint createPaint();

    @Override
    GDXCanvas createCanvas();

    GDXMatrix createMatrix(GDXMatrix matrix);

    GDXPaint createPaint(GDXPaint paint);

    int setColorAlpha(int color, float paintOpacity);

}

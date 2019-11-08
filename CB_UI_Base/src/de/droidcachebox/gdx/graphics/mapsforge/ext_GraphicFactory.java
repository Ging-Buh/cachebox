package de.droidcachebox.gdx.graphics.mapsforge;

import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

public interface ext_GraphicFactory extends org.mapsforge.core.graphics.GraphicFactory {

    @Override
    ext_Bitmap createBitmap(int width, int height);

    @Override
    Matrix createMatrix();

    @Override
    ext_Path createPath();

    @Override
    Paint createPaint();

    @Override
    ext_Canvas createCanvas();

    ext_Matrix createMatrix(ext_Matrix matrix);

    ext_Paint createPaint(ext_Paint paint);

    int setColorAlpha(int color, float paintOpacity);

}

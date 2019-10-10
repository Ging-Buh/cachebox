package CB_UI_Base.graphics.extendedInterfaces;

import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

import java.util.HashMap;

public interface ext_GraphicFactory extends org.mapsforge.core.graphics.GraphicFactory {
    HashMap<Float, ext_GraphicFactory> FactoryList = new HashMap<>();

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

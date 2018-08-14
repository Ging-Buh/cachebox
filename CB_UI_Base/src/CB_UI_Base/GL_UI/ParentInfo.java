package CB_UI_Base.GL_UI;

import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * enthält die Matrix4 und die dazugehörige Vector2 für die verschiebung bei Zeichnen eines Controls auf der GL Oberfläche</br>
 * und ein rechteck, welches die letzt mögliche Zeichen Größe enthält, innerhalb dessen gezeichnet werden darf.
 *
 * @author Longri
 */
public class ParentInfo {
    private final Matrix4 matrix;
    private final Vector2 vector;
    private final CB_RectF rec;

    public ParentInfo(Matrix4 Matrix, CB_RectF Rect) {
        matrix = Matrix;
        vector = new Vector2(0, 0);
        rec = Rect;
    }

    public ParentInfo(Matrix4 Matrix, Vector2 Vector, CB_RectF Rect) {
        matrix = Matrix;
        vector = Vector;
        rec = Rect;
    }

    public ParentInfo() {
        this.matrix = new Matrix4();
        this.vector = new Vector2();
        this.rec = new CB_RectF();
    }

    public void add(Vector2 Vector) {
        add(Vector.x, Vector.y);
    }

    public void add(float x, float y) {
        matrix.translate(x, y, 0);
        vector.add(x, y);
    }

    public float x() {
        return vector.x;
    }

    public float y() {
        return vector.y;
    }

    public Matrix4 Matrix() {
        return matrix;
    }

    public Vector2 Vector() {
        return vector;
    }

    public CB_RectF drawRec() {
        return rec;
    }

    public void setWorldDrawRec(CB_RectF Rect) {
        this.rec.setRec(Rect);
    }

    public void setParentInfo(ParentInfo parentInfo) {
        this.rec.setRec(parentInfo.rec);
        this.vector.x = parentInfo.vector.x;
        this.vector.y = parentInfo.vector.y;
        this.matrix.set(parentInfo.matrix);

    }

}

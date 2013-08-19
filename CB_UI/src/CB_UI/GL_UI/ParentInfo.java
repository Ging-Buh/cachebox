package CB_UI.GL_UI;

import CB_UI.Math.CB_RectF;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * enthält die Matrix4 und die dazugehörige Vector2 für die verschiebung bei Zeichnen eines Controls auf der GL Oberfläche </br> und ein
 * rechteck, welches die letzt mögliche Zeichen Größe enthällt, inerhalb dessen gezeichnet werden darf.
 * 
 * @author Longri
 */
public class ParentInfo
{
	private Matrix4 matrix;
	private Vector2 vector;
	private CB_RectF rec;

	public ParentInfo(Matrix4 Matrix, CB_RectF Rect)
	{
		matrix = Matrix;
		vector = new Vector2(0, 0);
		rec = Rect;
	}

	public ParentInfo(Matrix4 Matrix, Vector2 Vector, CB_RectF Rect)
	{
		matrix = Matrix;
		vector = Vector;
		rec = Rect;
	}

	public void add(Vector2 Vector)
	{
		add(Vector.x, Vector.y);
	}

	public void add(float x, float y)
	{
		matrix.translate(x, y, 0);
		vector.add(x, y);
	}

	public float x()
	{
		return vector.x;
	}

	public float y()
	{
		return vector.y;
	}

	public Matrix4 Matrix()
	{
		return matrix;
	}

	public ParentInfo cpy()
	{
		return new ParentInfo(matrix.cpy(), vector.cpy(), rec.copy());
	}

	public Vector2 Vector()
	{
		return vector;
	}

	public CB_RectF drawRec()
	{
		return rec.copy();
	}

	public void setWorldDrawRec(CB_RectF Rect)
	{
		this.rec = Rect.copy();
	}

}

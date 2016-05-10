/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.model.Dimension;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_UI_Base.graphics.Images.VectorDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import CB_UI_Base.graphics.fromAndroid.RectF;

/**
 * @author Longri
 */
public class GL_Canvas implements ext_Canvas {
	ext_Matrix aktMatrix = new GL_Matrix();
	Stack<ext_Matrix> matrixStack = new Stack<ext_Matrix>();

	private VectorDrawable bitmap;

	@Override
	public void destroy() {

	}

	@Override
	public Dimension getDimension() {

		return null;
	}

	@Override
	public int getHeight() {

		return 0;
	}

	@Override
	public int getWidth() {

		return 0;
	}

	@Override
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = (VectorDrawable) bitmap;
	}

	@Override
	public void drawBitmap(Bitmap bitmap, int left, int top) {

	}

	@Override
	public void drawBitmap(Bitmap bitmap, Matrix matrix) {

	}

	@Override
	public void drawCircle(int x, int y, int radius, Paint paint) {

	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2, Paint paint) {

	}

	@Override
	public void drawPath(Path path, Paint paint) {
		// Cast
		GL_Path pat = (GL_Path) path;
		GL_Paint pai = (GL_Paint) paint;

		GL_Style style = pai.getGL_Style();

		ArrayList<float[]> pathes = pat.getVertices();

		switch (style) {
		case FILL:
			float[] Vertices = null;
			short[] Triangles = null;

			// GL_Matrix m = new GL_Matrix();
			// m.scale(100, 100);

			if (pathes.size() > 1) {
				try {
					Polygon polygon = null;
					for (float[] singlePath : pathes) {

						// m.mapPoints(singlePath);

						Polygon poly = new Polygon(singlePath);
						if (polygon == null) {
							polygon = poly;
						} else {
							polygon.addHole(poly);
						}
					}

					Poly2Tri.triangulate(polygon);
					List<DelaunayTriangle> computedTriangles = polygon.getTriangles();

					Vertices = new float[computedTriangles.size() * 6];
					Triangles = new short[computedTriangles.size() * 3];

					int index = 0;
					short TriangleIndex = 0;

					for (DelaunayTriangle tri : computedTriangles) {
						TriangulationPoint[] tp = tri.points;
						for (int i = 0; i < 3; i++) {
							Vertices[index++] = tp[i].getXf();
							Vertices[index++] = tp[i].getYf();
							Triangles[TriangleIndex] = TriangleIndex++;
						}
					}
				} catch (Exception e) {
					Vertices = pathes.get(0);
					if (Vertices.length < 6)
						return; // Nothing to Draw
					Triangles = GL_GraphicFactory.ECT.computeTriangles(Vertices).toArray();
				}

				// m.invert();
				// m.mapPoints(Vertices);
			} else {
				Vertices = pathes.get(0);
				if (Vertices.length < 6)
					return; // Nothing to Draw
				Triangles = GL_GraphicFactory.ECT.computeTriangles(Vertices).toArray();
			}

			if (Vertices != null && Triangles != null) {
				this.bitmap.addDrawable(new PolygonDrawable(Vertices, Triangles, pai, this.bitmap.getWidth(), this.bitmap.getHeight()), aktMatrix, false);
			}
			break;

		case STROKE:
			for (float[] singlePath : pathes) {
				this.bitmap.addDrawable(new PolylineDrawable(singlePath, pai, this.bitmap.getWidth(), this.bitmap.getHeight()), aktMatrix, true);
			}
			break;

		}

	}

	@Override
	public void drawText(String text, int x, int y, Paint paint) {

	}

	@Override
	public void drawTextRotated(String text, int x1, int y1, int x2, int y2, Paint paint) {

	}

	@Override
	public void fillColor(Color color) {

	}

	@Override
	public void fillColor(int color) {

	}

	@Override
	public void resetClip() {

	}

	@Override
	public void setClip(int left, int top, int width, int height) {

	}

	@Override
	public void drawText(String text, float x, float y, Paint paint) {

	}

	@Override
	public void drawRect(RectF rect, ext_Paint paint) {
		// Cast
		GL_Paint pai = (GL_Paint) paint;

		GL_Style style = pai.getGL_Style();
		switch (style) {
		case FILL:
			Quadrangle qu = new Quadrangle(rect);

			// if Fill Rect size the same like Bitmap size, delete all render commands and set Paint.colar as Background color
			// first transform Rect with matrix!
			float[] rp = qu.getVertices();
			aktMatrix.mapPoints(rp);
			if (rp[0] <= 0 && rp[1] <= 0 && rp[0] + rp[4] >= this.bitmap.getWidth() && rp[1] + rp[5] >= this.bitmap.getHeight()) {
				this.bitmap.clearDrawables();
				this.bitmap.setBackgroundColor(pai.color);
				return;
			}

			this.bitmap.addDrawable(new PolygonDrawable(qu.getVertices(), qu.getTriangles(), pai, this.bitmap.getWidth(), this.bitmap.getHeight()), aktMatrix, false);
			return;

		case STROKE:
			GL_Path p = new GL_Path();
			p.moveTo(rect.left, rect.bottom);
			p.lineTo(rect.left, rect.top);
			p.lineTo(rect.right, rect.top);
			p.lineTo(rect.right, rect.bottom);
			p.close();
			drawPath(p, paint);
			return;
		}

	}

	@Override
	public void drawRoundRect(RectF rect, float rx, float ry, ext_Paint strokePaint) {

	}

	@Override
	public void drawOval(RectF rect, ext_Paint fillPaint) {

	}

	@Override
	public void scale(float sx, float sy) {
		aktMatrix.scale(sx, sy);
	}

	@Override
	public void setMatrix(ext_Matrix matrix) {
		aktMatrix.set(matrix);
	}

	@Override
	public ext_Matrix getMatrix() {
		return aktMatrix;
	}

	@Override
	public void save() {
		matrixStack.push(new GL_Matrix(aktMatrix));
		aktMatrix = new GL_Matrix(aktMatrix);
	}

	@Override
	public void restore() {
		if (!matrixStack.empty())
			aktMatrix = matrixStack.pop();
	}

	@Override
	public void concat(ext_Matrix matrix) {
		aktMatrix.postConcat(matrix);
	}

	@Override
	public void drawTextOnPath(String text, ext_Path path, float x, float y, ext_Paint fillPaint) {

	}

	@Override
	public void clipRect(float left, float top, float right, float bottom) {

	}

	@Override
	public void clipPath(ext_Path path) {

	}

	@Override
	public void translate(float stepX, float stepY) {

	}

	@Override
	public void saveMatrix() {
		matrixStack.push(aktMatrix);
	}

	@Override
	public void setMatrix(Matrix matrix) {

	}

	@Override
	public void setClipDifference(int left, int top, int width, int height) {
		// TODO Auto-generated method stub

	}

}

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
package com.badlogic.gdx.graphics.g2d;

import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_UI_Base.graphics.GL_Fonts;
import CB_UI_Base.graphics.GL_Path;
import CB_UI_Base.graphics.Geometry.Circle;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;

/**
 * @author Longri
 */
public class TextOnPath implements Disposable
{
	private final boolean PathToClose;
	private BitmapFont font;
	private float[][] vertexData;
	private float[][] StrokeVertexData;
	// private float[][] TransVertexData;
	// private float[][] TransStrokeVertexData;

	private int[] idx;
	private int[] tmpGlyphCount;
	private float color = Color.WHITE.toFloatBits();
	private int glyphCount = 0;
	private IntArray[] glyphIndices;
	private float[] centerPoints;
	private float[] PathMapedPoints;
	private final float PathOffset;

	private final float textWidth;
	private final float[] centerpoint = new float[2];
	private boolean centerpointCalculated = false;

	private boolean isDisposed;

	public TextOnPath(String Text, GL_Path path, ext_Paint fill2, ext_Paint stroke2, boolean center)
	{

		// Convert Paint values to used GL_PaintValues
		GL_FontFamily fontFamily = fill2.getGLFontFamily();
		GL_FontStyle fontStyle = fill2.getGLFontStyle();
		float fontsize = fill2.getTextSize();

		if (fontsize < 3)
		{
			textWidth = 0;
			PathToClose = true;
			PathOffset = 0;
			return;
		}

		color = fill2.getHSV_Color().toFloatBits();
		this.font = GL_Fonts.get(fontFamily, fontStyle, fontsize);

		if (this.font == null)
		{
			textWidth = 0;
			PathToClose = true;
			PathOffset = 0;
			return;
		}

		int regionsLength = font.regions.length;
		if (regionsLength == 0) throw new IllegalArgumentException("The specified font must contain at least 1 texture page");

		this.vertexData = new float[regionsLength][];

		this.idx = new int[regionsLength];
		int vertexDataLength = vertexData.length;
		if (vertexDataLength > 1)
		{
			glyphIndices = new IntArray[vertexDataLength];
			for (int i = 0, n = glyphIndices.length; i < n; i++)
			{
				glyphIndices[i] = new IntArray();
			}

			tmpGlyphCount = new int[vertexDataLength];
		}

		requireSequence(Text, 0, Text.length());
		textWidth = addToCache(Text, 0, 0, 0, Text.length());

		if (center)
		{
			PathOffset = (path.getLength() - textWidth) / 2;
		}
		else
		{
			PathOffset = 0;
		}

		PathToClose = !mapPath(path);

		if (!PathToClose)
		{
			if (stroke2 != null && stroke2.getStrokeWidth() > 1)
			{
				createStroke(stroke2.getStrokeWidth(), stroke2.getHSV_Color());
			}

		}
		else
		{
			dispose();
		}

	}

	private void createStroke(float width, Color color)
	{

		int seg = Math.max(5, (int) width);

		Circle circ = new Circle(0, 0, width / 2, seg);
		float[] vertices = circ.getVertices();

		StrokeVertexData = new float[vertexData.length][vertexData[0].length * ((vertices.length / 2) - 1)];

		for (int j = 0, n = vertexData.length; j < n; j++)
		{
			int index = 0;
			for (int i = 2; i < vertices.length; i += 2)
			{
				float[][] tmp = createDataCopy(color, vertices[i], vertices[i + 1]);
				System.arraycopy(tmp[j], 0, StrokeVertexData[j], index, tmp[0].length);
				index += tmp[j].length;
			}
		}
		circ.dispose();
		circ = null;
		vertices = null;
	}

	private float[][] createDataCopy(Color color, float xOffset, float yOffset)
	{
		float[][] data = new float[vertexData.length][];

		for (int j = 0, n = vertexData.length; j < n; j++)
		{
			if (idx[j] >= 0)
			{ // ignore if this texture has no glyphs
				float[] vertices = vertexData[j];
				data[j] = new float[vertices.length];
				System.arraycopy(vertices, 0, data[j], 0, vertices.length);
			}
		}

		final float c = color.toFloatBits();
		for (int j = 0, length = data.length; j < length; j++)
		{
			float[] vertices = data[j];
			for (int i = 2, n = idx[j]; i < n; i += 5)
				vertices[i] = c;

			for (int i = 0, n = idx[0]; i < n; i += 20)
			{

				Matrix3 matrix3 = new Matrix3();
				matrix3.translate(xOffset, yOffset);

				float x0 = vertices[i] * matrix3.val[0] + vertices[i + 1] * matrix3.val[3] + matrix3.val[6];
				float y0 = vertices[i] * matrix3.val[1] + vertices[i + 1] * matrix3.val[4] + matrix3.val[7];

				float x1 = vertices[i + 5] * matrix3.val[0] + vertices[i + 6] * matrix3.val[3] + matrix3.val[6];
				float y1 = vertices[i + 5] * matrix3.val[1] + vertices[i + 6] * matrix3.val[4] + matrix3.val[7];

				float x2 = vertices[i + 10] * matrix3.val[0] + vertices[i + 11] * matrix3.val[3] + matrix3.val[6];
				float y2 = vertices[i + 10] * matrix3.val[1] + vertices[i + 11] * matrix3.val[4] + matrix3.val[7];

				float x3 = vertices[i + 15] * matrix3.val[0] + vertices[i + 16] * matrix3.val[3] + matrix3.val[6];
				float y3 = vertices[i + 15] * matrix3.val[1] + vertices[i + 16] * matrix3.val[4] + matrix3.val[7];

				vertices[i] = x0;
				vertices[i + 1] = y0;

				vertices[i + 5] = x1;
				vertices[i + 6] = y1;

				vertices[i + 10] = x2;
				vertices[i + 11] = y2;

				vertices[i + 15] = x3;
				vertices[i + 16] = y3;
			}

		}
		return data;
	}

	/**
	 * Returns True, if the Path to close for drawing Text
	 * 
	 * @return
	 */
	public boolean PathToClose()
	{
		return PathToClose;
	}

	Matrix3 lastTransform;
	private float[][] TransStrokeVertexData;
	private float[][] TransVertexData;

	private boolean transformChanged(Matrix3 transform)
	{
		if (lastTransform == null) return true;

		for (int i = 0; i < 9; i++)
		{
			if (transform.val[i] != lastTransform.val[i]) return true;
		}

		return false;
	}

	private void MapTransform(float[][] srcData, float[][] tarData, Matrix3 matrix3)
	{
		// Copy Data
		for (int j = 0, n = srcData.length; j < n; j++)
		{
			if (idx[j] >= 0)
			{
				float[] vertices = srcData[j];
				System.arraycopy(vertices, 0, tarData[j], 0, vertices.length);
			}
		}

		// Map Data
		for (int j = 0, length = tarData.length; j < length; j++)
		{
			float[] vertices = tarData[j];
			for (int i = 0; i < vertices.length; i += 5)
			{
				float x = vertices[i] * matrix3.val[0] + vertices[i + 1] * matrix3.val[3] + matrix3.val[6];
				float y = vertices[i] * matrix3.val[1] + vertices[i + 1] * matrix3.val[4] + matrix3.val[7];
				vertices[i] = x;
				vertices[i + 1] = y;
			}
		}
	}

	public void draw(Batch spriteBatch, Matrix3 transform)
	{

		if (vertexData == null) return;

		if (transformChanged(transform))
		{
			// FIXME Map only difference reduce copy VertexData

			lastTransform = new Matrix3(transform);
			if (StrokeVertexData != null)
			{
				if (TransStrokeVertexData == null)
				{
					TransStrokeVertexData = new float[StrokeVertexData.length][];
					for (int j = 0, n = StrokeVertexData.length; j < n; j++)
					{
						if (idx[j] >= 0)
						{
							TransStrokeVertexData[j] = new float[StrokeVertexData[j].length];
						}
					}
				}

				MapTransform(StrokeVertexData, TransStrokeVertexData, lastTransform);
			}

			if (TransVertexData == null)
			{
				TransVertexData = new float[vertexData.length][];
				for (int j = 0, n = vertexData.length; j < n; j++)
				{
					if (idx[j] >= 0)
					{
						TransVertexData[j] = new float[vertexData[j].length];
					}
				}
			}

			MapTransform(vertexData, TransVertexData, lastTransform);
		}

		if (PathToClose || isDisposed) return;
		TextureRegion[] regions = font.getRegions();

		if (StrokeVertexData != null)
		{
			drawVertexData(spriteBatch, regions, TransStrokeVertexData);
		}
		drawVertexData(spriteBatch, regions, TransVertexData);

	}

	private void drawVertexData(Batch spriteBatch, TextureRegion[] regions, float[][] data)
	{
		for (int j = 0, n = data.length; j < n; j++)
		{
			if (idx[j] >= 0)
			{ // ignore if this texture has no glyphs
				float[] vertices = data[j];
				spriteBatch.draw(regions[j].getTexture(), vertices, 0, vertices.length);

			}
		}
	}

	private void requireSequence(CharSequence seq, int start, int end)
	{
		int newGlyphCount = end - start;
		if (vertexData.length == 1)
		{
			require(0, newGlyphCount); // don't scan sequence if we just have one page
		}
		else
		{
			for (int i = 0, n = tmpGlyphCount.length; i < n; i++)
				tmpGlyphCount[i] = 0;

			// determine # of glyphs in each page
			while (start < end)
			{
				Glyph g = font.data.getGlyph(seq.charAt(start++));
				if (g == null) continue;
				tmpGlyphCount[g.page]++;
			}
			// require that many for each page
			for (int i = 0, n = tmpGlyphCount.length; i < n; i++)
				require(i, tmpGlyphCount[i]);
		}
	}

	private void require(int page, int glyphCount)
	{
		if (glyphIndices != null)
		{
			if (glyphCount > glyphIndices[page].items.length) glyphIndices[page].ensureCapacity(glyphCount
					- glyphIndices[page].items.length);
		}

		int vertexCount = idx[page] + glyphCount * 20;
		float[] vertices = vertexData[page];
		if (vertices == null)
		{
			vertexData[page] = new float[vertexCount];
		}
		else if (vertices.length < vertexCount)
		{
			float[] newVertices = new float[vertexCount];
			System.arraycopy(vertices, 0, newVertices, 0, idx[page]);
			vertexData[page] = newVertices;
		}
	}

	private float addToCache(CharSequence str, float x, float y, int start, int end)
	{
		float startX = x;
		BitmapFont font = this.font;
		Glyph lastGlyph = null;
		BitmapFontData data = font.data;
		if (data.scaleX == 1 && data.scaleY == 1)
		{
			while (start < end)
			{
				lastGlyph = data.getGlyph(str.charAt(start++));
				if (lastGlyph != null)
				{
					addGlyph(lastGlyph, x + lastGlyph.xoffset, y + lastGlyph.yoffset, lastGlyph.width, lastGlyph.height);
					x += lastGlyph.xadvance;
					break;
				}
			}
			while (start < end)
			{
				char ch = str.charAt(start++);
				Glyph g = data.getGlyph(ch);
				if (g != null)
				{
					x += lastGlyph.getKerning(ch);
					lastGlyph = g;
					addGlyph(lastGlyph, x + g.xoffset, y + g.yoffset, g.width, g.height);
					x += g.xadvance;
				}
			}
		}
		else
		{
			float scaleX = data.scaleX, scaleY = data.scaleY;
			while (start < end)
			{
				lastGlyph = data.getGlyph(str.charAt(start++));
				if (lastGlyph != null)
				{
					addGlyph(lastGlyph, //
							x + lastGlyph.xoffset * scaleX, //
							y + lastGlyph.yoffset * scaleY, //
							lastGlyph.width * scaleX, //
							lastGlyph.height * scaleY);
					x += lastGlyph.xadvance * scaleX;
					break;
				}
			}
			while (start < end)
			{
				char ch = str.charAt(start++);
				Glyph g = data.getGlyph(ch);
				if (g != null)
				{
					x += lastGlyph.getKerning(ch) * scaleX;
					lastGlyph = g;
					addGlyph(lastGlyph, //
							x + g.xoffset * scaleX, //
							y + g.yoffset * scaleY, //
							g.width * scaleX, //
							g.height * scaleY);
					x += g.xadvance * scaleX;
				}
			}
		}
		return x - startX;
	}

	private void addGlyph(Glyph glyph, float x, float y, float width, float height)
	{
		float x2 = x + width;
		float y2 = y + height;
		final float u = glyph.u;
		final float u2 = glyph.u2;
		final float v = glyph.v;
		final float v2 = glyph.v2;

		final int page = glyph.page;

		if (glyphIndices != null)
		{
			glyphIndices[page].add(glyphCount++);
		}

		final float[] vertices = vertexData[page];

		int idx = this.idx[page];
		this.idx[page] += 20;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx] = v;
	}

	/**
	 * Set the center points of all Glyphs on the given Path.<br>
	 * Returns False, if the Path to close for the Text
	 * 
	 * @param path
	 * @return
	 */
	private boolean mapPath(GL_Path path)
	{
		if (vertexData == null) return false;
		int index = 0;
		float[] vertices = vertexData[0];

		// calculate center points of anny Glyph

		centerPoints = new float[idx[0] / 10];
		PathMapedPoints = new float[idx[0] / 10];
		float[] GlyphRotation = new float[(idx[0] / 20) + 1];

		int indexCenterPoints = 0;
		int indexPathMapedPoints = 0;
		int indexGlyphRotation = 0;
		float firstCenter = (vertices[1] + vertices[11]) / 2;
		for (int i = 0, n = idx[0]; i <= n - 20; i += 20)
		{
			float centerX = (vertices[i] + vertices[i + 10]) / 2;
			float centerY = (vertices[i + 1] + vertices[i + 11]) / 2;

			centerY = firstCenter;

			centerPoints[indexCenterPoints++] = centerX;
			centerPoints[indexCenterPoints++] = centerY;

			if (i == 0)
			{
				float[] res = path.getPointOnPathAfter(PathOffset);
				if (res == null) return false;
				PathMapedPoints[indexPathMapedPoints++] = res[0];
				PathMapedPoints[indexPathMapedPoints++] = res[1];
				GlyphRotation[indexGlyphRotation++] = res[2];
				res = null;
			}
			else
			{
				float distanceFromFirst = centerPoints[indexCenterPoints - 2] - centerPoints[0];
				float[] res = path.getPointOnPathAfter(PathOffset + distanceFromFirst);
				if (res == null) return false;
				PathMapedPoints[indexPathMapedPoints++] = res[0];
				PathMapedPoints[indexPathMapedPoints++] = res[1];
				GlyphRotation[indexGlyphRotation++] = res[2];
				res = null;
			}

		}

		index = 0;
		indexGlyphRotation = 0;
		for (int i = 0, n = idx[0]; i < n; i += 20)
		{
			float GlyphCenterX = centerPoints[index];
			float GlyphCenterY = centerPoints[index + 1];

			float MapX = PathMapedPoints[index] - GlyphCenterX;
			float MapY = PathMapedPoints[index + 1] - GlyphCenterY;

			Matrix3 matrix3 = new Matrix3();
			matrix3.translate(MapX, MapY);
			matrix3.translate(GlyphCenterX, GlyphCenterY);
			matrix3.rotate(GlyphRotation[indexGlyphRotation++]);
			matrix3.translate(-GlyphCenterX, -GlyphCenterY);

			float x0 = vertices[i] * matrix3.val[0] + vertices[i + 1] * matrix3.val[3] + matrix3.val[6];
			float y0 = vertices[i] * matrix3.val[1] + vertices[i + 1] * matrix3.val[4] + matrix3.val[7];

			float x1 = vertices[i + 5] * matrix3.val[0] + vertices[i + 6] * matrix3.val[3] + matrix3.val[6];
			float y1 = vertices[i + 5] * matrix3.val[1] + vertices[i + 6] * matrix3.val[4] + matrix3.val[7];

			float x2 = vertices[i + 10] * matrix3.val[0] + vertices[i + 11] * matrix3.val[3] + matrix3.val[6];
			float y2 = vertices[i + 10] * matrix3.val[1] + vertices[i + 11] * matrix3.val[4] + matrix3.val[7];

			float x3 = vertices[i + 15] * matrix3.val[0] + vertices[i + 16] * matrix3.val[3] + matrix3.val[6];
			float y3 = vertices[i + 15] * matrix3.val[1] + vertices[i + 16] * matrix3.val[4] + matrix3.val[7];

			vertices[i] = x0;
			vertices[i + 1] = y0;

			vertices[i + 5] = x1;
			vertices[i + 6] = y1;

			vertices[i + 10] = x2;
			vertices[i + 11] = y2;

			vertices[i + 15] = x3;
			vertices[i + 16] = y3;

			index += 2;
		}
		GlyphRotation = null;
		return true;
	}

	@Override
	public void dispose()
	{
		if (isDisposed) return;
		PathMapedPoints = null;
		centerPoints = null;
		glyphIndices = null;

		if (vertexData != null)
		{
			for (int i = 0; i < vertexData.length; i++)
			{
				vertexData[i] = null;
			}
		}
		vertexData = null;

		if (StrokeVertexData != null)
		{
			for (int i = 0; i < StrokeVertexData.length; i++)
			{
				StrokeVertexData[i] = null;
			}
		}
		StrokeVertexData = null;
		idx = null;
		font = null;
		isDisposed = true;
	}

	public int getWidth()
	{
		// TODO Join all Glyph recs and return the width
		return 0;
	}

	public int getHeight()
	{
		// TODO Join all Glyph recs and return the hight
		return 0;
	}

	public float[] getCenterPoint()
	{
		if (centerpointCalculated) return centerpoint;

		if (vertexData == null)
		{
			centerpoint[0] = 0;
			centerpoint[1] = 0;
			centerpointCalculated = true;
			return centerpoint;
		}

		float x = Float.MAX_VALUE;
		float y = Float.MAX_VALUE;
		float u = Float.MIN_VALUE;
		float v = Float.MIN_VALUE;

		for (int j = 0, length = vertexData.length; j < length; j++)
		{
			float[] vertices = vertexData[j];
			for (int i = 0, n = idx[0]; i < n; i += 5)
			{
				x = Math.min(x, vertices[i]);
				y = Math.min(y, vertices[i + 1]);

				u = Math.max(u, vertices[i]);
				v = Math.max(v, vertices[i + 1]);

			}
		}

		centerpoint[0] = x + ((u - x) / 2);
		centerpoint[1] = y + ((v - y) / 2);
		centerpointCalculated = true;
		return centerpoint;

	}

}
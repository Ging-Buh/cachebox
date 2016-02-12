package de.CB.Test.Blog;

import CB_UI_Base.GL_UI.GL_Listener.GL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

import de.CB.Test.Geometry.GeometryTestCaseBase;

public class CircleBlog extends GeometryTestCaseBase {

	final static float MIN_CIRCLE_SEGMENTH_LENGTH = 10;
	final static int MIN_CIRCLE_SEGMENTH_COUNT = 18;

	protected float centerX = 80;
	protected float centerY = 80;
	protected float radius = 70;

	public CircleBlog() {
		super("Circle Blog");
	}

	@Override
	public void work() {
		// calculate segment count
		double alpha = (360 * MIN_CIRCLE_SEGMENTH_LENGTH) / (MathUtils.PI2 * radius);
		int segmente = Math.max(MIN_CIRCLE_SEGMENTH_COUNT, (int) (360 / alpha));

		segmente = 8;

		// calculate theta step
		double thetaStep = (MathUtils.PI2 / segmente);

		// initialize arrays
		vertices = new float[(segmente + 1) * 2];
		triangles = new short[(segmente) * 3];

		int index = 0;

		// first point is the center point
		vertices[index++] = centerX;
		vertices[index++] = centerY;

		int triangleIndex = 0;
		short verticeIdex = 1;
		boolean beginnTriangles = false;
		for (float i = 0; index < (segmente + 1) * 2; i += thetaStep) {
			vertices[index++] = centerX + radius * MathUtils.cos(i);
			vertices[index++] = centerY + radius * MathUtils.sin(i);

			if (!beginnTriangles) {
				if (index % 6 == 0)
					beginnTriangles = true;
			}

			if (beginnTriangles) {
				triangles[triangleIndex++] = 0;
				triangles[triangleIndex++] = verticeIdex++;
				triangles[triangleIndex++] = verticeIdex;
			}

		}

		// last triangle
		triangles[triangleIndex++] = 0;
		triangles[triangleIndex++] = verticeIdex;
		triangles[triangleIndex++] = 1;
		super.work();
	}

	@Override
	public void draw(Batch batch) {
		super.draw(batch);

		// draw lines
		batch.end();

		Matrix4 matrix = batch.getProjectionMatrix();

		matrix.scl(2);

		float lineWidth = 2;
		Gdx.gl20.glLineWidth(lineWidth / GL.camera.zoom);

		float[] polyLine = new float[vertices.length];

		int index = 2;
		while (index < vertices.length) {
			polyLine[index - 2] = vertices[index++];
			polyLine[index - 2] = vertices[index++];

		}

		polyLine[index - 2] = vertices[2];
		polyLine[index - 1] = vertices[3];

		batch.begin();
	}
}

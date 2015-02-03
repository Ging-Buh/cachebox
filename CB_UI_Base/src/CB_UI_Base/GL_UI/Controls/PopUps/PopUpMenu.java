/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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
package CB_UI_Base.GL_UI.Controls.PopUps;

import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.graphics.CircleDrawable;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.Geometry.GeometryList;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_UI_Base.graphics.Geometry.RingSegment;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

/**
 * A rounded menu PopUp with various menu entrys from 1 to 6
 * 
 * @author Longri
 */
public class PopUpMenu extends PopUp_Base
{

	private final float[] TEMPLATE_ONE_SEGMENT = new float[]
		{ 10, 50 };
	private final float[] TEMPLATE_TWO_SEGMENT = new float[]
		{ 10, 50, 90 };
	private final float[] TEMPLATE_THREE_SEGMENT = new float[]
		{ 10, 50, 90, 120 };
	private final float[] TEMPLATE_FOUR_SEGMENT = new float[]
		{ 10, 50, 90, 120, 160 };

	private final CB_List<MenuItem> items = new CB_List<MenuItem>();
	private final float outerRadius;
	private final float innerRadius;
	private final float centerX;
	private final float centerY;
	private final RingSegment ringsegment;
	private final RingSegment outersegment;
	private final RingSegment innersegment;
	private final CircleDrawable infoCircle;
	private PolygonDrawable ringDrawable;
	private PolygonDrawable borderDrawable;
	private GeometryList geomList;
	private float[] segmente;

	float lineWidth = 3;
	int SEGMENTE = 4;

	public PopUpMenu(CB_RectF rec, String Name)
	{
		super(rec, Name);

		switch (SEGMENTE)
		{
		case 1:
			segmente = TEMPLATE_ONE_SEGMENT;
		case 2:
			segmente = TEMPLATE_TWO_SEGMENT;
		case 3:
			segmente = TEMPLATE_THREE_SEGMENT;
		case 4:
			segmente = TEMPLATE_FOUR_SEGMENT;
		default:
			segmente = TEMPLATE_FOUR_SEGMENT;
		}

		rec.setPos(0, 0);

		centerX = this.getHalfWidth();
		centerY = this.getHalfHeight();

		outerRadius = Math.min(this.getHalfWidth(), this.getHalfHeight());
		innerRadius = Math.min(this.getHalfWidth() / 2, this.getHalfHeight() / 2);
		float startAngle = segmente[0];
		float endAngle = segmente[segmente.length - 1];

		ringsegment = new RingSegment(this.getHalfWidth(), this.getHalfHeight(), innerRadius, outerRadius, startAngle, endAngle);
		innersegment = new RingSegment(this.getHalfWidth(), this.getHalfHeight(), innerRadius, innerRadius + lineWidth, startAngle, endAngle);
		outersegment = new RingSegment(this.getHalfWidth(), this.getHalfHeight(), outerRadius - lineWidth, outerRadius, startAngle, endAngle);
		GL_Paint paint = new GL_Paint();
		paint.setColor(COLOR.getPopUpInfoBackColor());
		infoCircle = new CircleDrawable(rec.getCenterPosX(), rec.getCenterPosY(), innerRadius, paint, rec.getWidth(), rec.getHeight());
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	public void addMneuItem(MenuItem item)
	{
		items.add(item);
	}

	@Override
	public void render(Batch batch)
	{

		if (ringDrawable == null)
		{
			ringsegment.Compute();
			outersegment.Compute();
			GL_Paint p = new GL_Paint();
			p.setColor(COLOR.getPopUpMenuIconBackColor());

			ringDrawable = new PolygonDrawable(ringsegment.getVertices(), ringsegment.getTriangles(), p, this.getWidth(), this.getHeight());

			geomList = new GeometryList();
			geomList.add(innersegment);
			geomList.add(outersegment);

			for (float se : segmente)
			{
				addSegmentLine(se);
			}

			GL_Paint p2 = new GL_Paint();
			p2.setColor(COLOR.getPopUpMenuBorderColor());
			borderDrawable = new PolygonDrawable(geomList.getVertices(), geomList.getTriangles(), p2, this.getWidth(), this.getHeight());

		}

		// disable scissor

		ringDrawable.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
		borderDrawable.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);
		infoCircle.draw(batch, 0, 0, this.getWidth(), this.getHeight(), 0);

		// writeDebug();
		// if (DebugSprite != null)
		// {
		// batch.flush();
		// DebugSprite.draw(batch);
		//
		// }
	}

	private void addSegmentLine(float i)
	{
		float x1 = centerX + innerRadius * MathUtils.cos(i * MathUtils.degRad);
		float y1 = centerY + innerRadius * MathUtils.sin(i * MathUtils.degRad);
		float x2 = centerX + outerRadius * MathUtils.cos(i * MathUtils.degRad);
		float y2 = centerY + outerRadius * MathUtils.sin(i * MathUtils.degRad);
		Line l1 = new Line(x1, y1, x2, y2);
		Quadrangle q1 = new Quadrangle(l1, lineWidth);
		geomList.add(q1);
	}

	@Override
	protected void writeDebug()
	{
		if (DebugSprite == null)
		{
			try
			{
				GL.that.RunOnGLWithThreadCheck(new IRunOnGL()
				{

					@Override
					public void run()
					{
						// int w = getNextHighestPO2((int) getWidth());
						// int h = getNextHighestPO2((int) getHeight());

						int w = (int) getWidth();
						int h = (int) getHeight();

						debugRegPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
						debugRegPixmap.setColor(1f, 0f, 0f, 1f);
						debugRegPixmap.drawRectangle(1, 1, (int) getWidth() - 1, (int) getHeight() - 1);
						debugRegPixmap.drawLine(1, 1, (int) getWidth() - 1, (int) getHeight() - 1);
						debugRegPixmap.drawLine(1, (int) getHeight() - 1, (int) getWidth() - 1, 1);
						debugRegTexture = new Texture(debugRegPixmap, Pixmap.Format.RGBA8888, false);

						DebugSprite = new Sprite(debugRegTexture, (int) getWidth(), (int) getHeight());
					}
				});

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	// @Override
	// public void onClick()
	// {
	//
	// }

}